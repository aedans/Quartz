package io.quartz.analyzer

import io.quartz.analyzer.type.*
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
import kategory.*

fun ExprT.analyze(env: Env, p: Package): Err<ExprI> = when (this) {
    is ExprT.Unit -> analyze()
    is ExprT.Bool -> analyze()
    is ExprT.Cast -> TODO()
    is ExprT.Id -> analyze(env)
    is ExprT.Apply -> analyze(env, p)
    is ExprT.If -> analyze(env, p)
    is ExprT.Lambda -> analyze(env, p)
    is ExprT.Dot -> TODO()
}

fun ExprT.Unit.analyze() = ExprI.InvokeStatic(
        location,
        TypeI.unit,
        TypeI.unit,
        "getInstance".name,
        nil
).right()

fun ExprT.Bool.analyze() = ExprI.Bool(location, boolean).right()

fun ExprT.Id.analyze(env: Env) = monadErrorE().binding {
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

fun ExprT.Apply.analyze(env: Env, p: Package) = monadErrorE().binding {
    val (_, _) = infer(env).bind()
    val (_, expr1TypeK) = expr1.infer(env).bind()
    val arrowK = expr1TypeK.arrow
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

fun ExprT.If.analyze(env: Env, p: Package) = monadErrorE().binding {
    val conditionI = condition.analyze(env, p).bind()
    val expr1I = expr1.analyze(env, p).bind()
    val expr2I = expr2.analyze(env, p).bind()
    val it = ExprI.If(location, conditionI, expr1I, expr2I)
    yields(it)
}.ev()

fun ExprT.Lambda.analyze(env: Env, p: Package) = monadErrorE().binding {
    val (s1, typeK) = infer(env).bind()
    val argTypeK = typeK.arrow.t1
    val returnTypeK = typeK.arrow.t2
    val closures = freeVariables.map { GenericK(it.string.name, env.getVar(it).bind().scheme.instantiate()) }
    val typeSchemeK = typeK.generalize(env, s1)
    val genericsK = typeSchemeK.generics + closures.flatMap { it.type.generalize(env, s1).generics }
    val localEnv = closures.map { (a, _) -> a.qualifiedLocal to VarLoc.Field(a) }
            .fold(env) { e, (a, b) -> e.withVarLoc(a, b) }
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
