package io.quartz.analyzer

import io.quartz.analyzer.type.*
import io.quartz.tree.Location
import io.quartz.tree.ast.ExprT
import io.quartz.tree.ir.DeclI
import io.quartz.tree.ir.ExprI
import io.quartz.tree.ir.TypeI
import io.quartz.tree.ir.typeI
import io.quartz.tree.name
import io.quartz.tree.nil
import io.quartz.tree.qualify
import kategory.Either
import kategory.binding
import kategory.ev
import kategory.right

fun ExprT.analyze(env: Env): EitherE<ExprI> = when (this) {
    is ExprT.Unit -> analyze()
    is ExprT.Bool -> analyze()
    is ExprT.Cast -> TODO()
    is ExprT.Id -> analyze(env)
    is ExprT.Apply -> analyze(env)
    is ExprT.If -> analyze(env)
    is ExprT.Lambda -> analyze(env)
}

fun ExprT.Unit.analyze() = ExprI.InvokeStatic(
        location,
        TypeI.unit,
        TypeI.unit,
        "getInstance".name,
        nil
).right()

fun ExprT.Bool.analyze() = ExprI.Bool(location, boolean).right()

fun ExprT.Id.analyze(env: Env) = Either.monadErrorE().binding {
    val memLoc = env.getMemLoc(name).bind()
    val it = when (memLoc) {
        is MemLoc.Arg -> ExprI.Arg(location, memLoc.index)
        is MemLoc.Field -> ExprI.LocalField(
                location,
                memLoc.name,
                env.getVar(name).bind().instantiate().typeI
        )
        is MemLoc.Global -> ExprI.InvokeStatic(
                location,
                "_Get${memLoc.name.string.capitalize()}".name.qualify(memLoc.name.qualifier).typeI,
                env.getVar(name).bind().instantiate().typeI,
                "get${name.string.capitalize()}".name,
                nil
        )
    }
    yields(it)
}.ev()

fun ExprT.Apply.analyze(env: Env) = Either.monadErrorE().binding {
    val (_, _) = infer(env).bind()
    val (_, expr1TypeK) = expr1.infer(env).bind()
    val arrowK = expr1TypeK.arrow
    val expr1I = expr1.analyze(env).bind()
    val expr2I = expr2.analyze(env).bind()
    val it = ExprI.Invoke(
            location,
            expr1I,
            TypeI.function,
            arrowK.t2.typeI,
            "invoke".name,
            listOf(expr2I to expr1TypeK.typeI),
            ExprI.Invoke.Dispatch.INTERFACE
    )
    yields(it)
}.ev()

fun ExprT.If.analyze(env: Env) = Either.monadErrorE().binding {
    val conditionI = condition.analyze(env).bind()
    val expr1I = expr1.analyze(env).bind()
    val expr2I = expr2.analyze(env).bind()
    val it = ExprI.If(location, conditionI, expr1I, expr2I)
    yields(it)
}.ev()

fun ExprT.Lambda.analyze(env: Env) = Either.monadErrorE().binding {
    val (s1, typeK) = infer(env).bind()
    val argTypeK = typeK.arrow.t1
    val returnTypeK = typeK.arrow.t2
    val closures = freeVariables.map { GenericK(it.string.name, env.getVar(it).bind().instantiate()) }
    val typeSchemeK = typeK.generalize(env, s1)
    val genericsK = typeSchemeK.generics + closures.flatMap { it.type.generalize(env, s1).generics }
    val localEnv = env
            .withMemLocs(closures.map { (a, _) -> a to MemLoc.Field(a) })
            .withVar(arg, argTypeK.scheme)
            .withMemLoc(arg, MemLoc.Arg(0))
    val closuresI = closures.map { (a, b) ->
        ExprI.LocalField(Location.unknown, a, b.typeI).let { it to it.type }
    }
    val exprI = expr.analyze(localEnv).bind()
    val invokeScheme = DeclI.Method.Scheme(nil, listOf(argTypeK.typeI), returnTypeK.typeI)
    val invokeDecl = DeclI.Method("invoke".name, location, env.`package`, invokeScheme, exprI)
    val obj = DeclI.Class.Object(genericsK.map { it.genericI }, listOf(typeK.typeI), listOf(invokeDecl))
    val it = ExprI.AnonymousObject(location, env.`package`, obj, closuresI)
    yields(it)
}.ev()
