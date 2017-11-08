package io.quartz.analyze

import io.quartz.analyze.type.*
import io.quartz.err.Err
import io.quartz.err.errMonad
import io.quartz.interop.varClassName
import io.quartz.interop.varGetterName
import io.quartz.nil
import io.quartz.tree.Location
import io.quartz.tree.ast.ExprT
import io.quartz.tree.ast.Package
import io.quartz.tree.ir.DeclI
import io.quartz.tree.ir.ExprI
import io.quartz.tree.ir.TypeI
import io.quartz.tree.ir.typeI
import io.quartz.tree.name
import io.quartz.tree.qualifiedLocal
import io.quartz.tree.unqualified
import kategory.binding
import kategory.ev
import kategory.right
import kategory.toT

fun ExprT.analyze(env: Env, p: Package): Err<ExprI> = when (this) {
    is ExprT.Unit -> analyze()
    is ExprT.Bool -> analyze()
    is ExprT.Cast -> analyze(env, p)
    is ExprT.Id -> analyze(env)
    is ExprT.Apply -> analyze(env, p)
    is ExprT.If -> analyze(env, p)
    is ExprT.Lambda -> analyze(env, p)
}.qualify()

fun ExprT.Unit.analyze() = ExprI.InvokeStatic(
        location,
        TypeI.unit,
        TypeI.unit,
        "getInstance".name,
        nil
).right()

fun ExprT.Bool.analyze() = ExprI.Bool(location, boolean).right()

fun ExprT.Cast.analyze(env: Env, p: Package) = expr.analyze(env, p)

fun ExprT.Id.analyze(env: Env) = errMonad().binding {
    val memLoc = env.getVar(name).bind().varLoc
    val it = when (memLoc) {
        is VarLoc.Arg -> ExprI.Arg(location, memLoc.index)
        is VarLoc.Field -> ExprI.LocalField(
                location,
                memLoc.name,
                env.getVar(name).bind().scheme.instantiate().typeI
        )
        is VarLoc.Global -> ExprI.InvokeStatic(
                location,
                memLoc.name.varClassName().typeI,
                env.getVar(name).bind().scheme.instantiate().typeI,
                name.unqualified.varGetterName(),
                nil
        )
    }
    yields(it)
}.ev()

fun ExprT.Apply.analyze(env: Env, p: Package) = errMonad().binding {
    val (_, _) = infer(env).bind()
    val (_, expr1TypeK) = expr1.infer(env).bind()
    val arrowK = expr1TypeK.arrow.bind()
    val expr1I = expr1.analyze(env, p).bind()
    val expr2I = expr2.analyze(env, p).bind()
    val it = ExprI.Invoke(
            location,
            expr1I,
            TypeI.function,
            arrowK.t2.typeI,
            "invoke".name,
            listOf(expr2I toT  expr1TypeK.typeI),
            ExprI.Invoke.Dispatch.INTERFACE
    )
    yields(it)
}.ev()

fun ExprT.If.analyze(env: Env, p: Package) = errMonad().binding {
    val conditionI = condition.analyze(env, p).bind()
    val expr1I = expr1.analyze(env, p).bind()
    val expr2I = expr2.analyze(env, p).bind()
    val it = ExprI.If(location, conditionI, expr1I, expr2I)
    yields(it)
}.ev()

fun ExprT.Lambda.analyze(env: Env, p: Package) = errMonad().binding {
    val (s1, typeK) = infer(env).bind()
    val arrow = typeK.arrow.bind()
    val argTypeK = arrow.t1
    val returnTypeK = arrow.t2
    val typeSchemeK = typeK.generalize(env, s1)
    val closures = freeVariables.map { GenericK(it.string.name, env.getVar(it).bind().scheme.instantiate()) }
    val closuresMap = closures.associate { it.name.qualifiedLocal to VarLoc.Field(it.name) }
    val genericsK = typeSchemeK.generics + closures.flatMap { it.type.generalize(env, s1).generics }
    val localEnv = env
            .mapVars { name, err ->
                err.map { closuresMap[name]?.let { varLoc -> it.copy(varLoc = varLoc) } ?: it }
            }
            .withVar(arg.qualifiedLocal, VarInfo(argTypeK.scheme, VarLoc.Arg(0)).right())
    val closuresI = closures.map { (a, b) ->
        ExprI.LocalField(Location.unknown, a, b.typeI).let { it toT it.type }
    }
    val exprI = expr.analyze(localEnv, p).bind()
    val invokeScheme = DeclI.Method.Scheme(nil, listOf(argTypeK.typeI), returnTypeK.typeI)
    val invokeDecl = DeclI.Method("invoke".name, location, p, invokeScheme, exprI)
    val obj = DeclI.Class.Object(genericsK.map { it.genericI }, listOf(typeK.typeI), listOf(invokeDecl))
    val it = ExprI.AnonymousObject(location, p, obj, closuresI)
    yields(it)
}.ev()
