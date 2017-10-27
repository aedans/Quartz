package io.quartz.analyzer

import io.quartz.analyzer.type.*
import io.quartz.tree.Location
import io.quartz.tree.ast.ExprT
import io.quartz.tree.ir.DeclI
import io.quartz.tree.ir.ExprI
import io.quartz.tree.ir.TypeI
import io.quartz.tree.ir.typeI
import io.quartz.tree.nil

/**
 * @author Aedan Smith
 */

fun ExprT.analyze(env: Env): ExprI = when (this) {
    is ExprT.Unit -> analyze()
    is ExprT.Bool -> analyze()
    is ExprT.Cast -> analyze(env)
    is ExprT.Var -> analyze(env)
    is ExprT.Apply -> analyze(env)
    is ExprT.If -> analyze(env)
    is ExprT.Lambda -> analyze(env)
}

fun ExprT.Unit.analyze() = ExprI.InvokeStatic(
        location,
        TypeI.unit,
        TypeI.unit,
        "getInstance",
        nil
)

fun ExprT.Bool.analyze() = ExprI.Bool(location, boolean)

fun ExprT.Cast.analyze(env: Env): Nothing = TODO()

fun ExprT.Var.analyze(env: Env) = run {
    val memLoc = env.getMemLoc(name)!!
    when (memLoc) {
        is MemLoc.Arg -> ExprI.Arg(location, memLoc.index)
        is MemLoc.Field -> ExprI.LocalField(
                location,
                memLoc.name,
                env.getVar(name)!!.instantiate().typeI
        )
        is MemLoc.Global -> ExprI.InvokeStatic(
                location,
                "\$Get${name.capitalize()}".typeI,
                env.getVar(name)!!.instantiate().typeI,
                "get${name.capitalize()}",
                nil
        )
    }
}

fun ExprT.Apply.analyze(env: Env) = run {
    val (_, typeK) = infer(env)
    val (_, expr1TypeK) = expr1.infer(env)
    val arrowK = expr1TypeK.arrow
    val expr1I = expr1.analyze(env)
    val expr2I = expr2.analyze(env)
    ExprI.Invoke(
            location,
            expr1I,
            TypeI.function,
            arrowK.t2.typeI,
            "invoke",
            listOf(expr2I to expr1TypeK.typeI),
            ExprI.Invoke.Dispatch.INTERFACE
    )
}

fun ExprT.If.analyze(env: Env) = run {
    val conditionI = condition.analyze(env)
    val expr1I = expr1.analyze(env)
    val expr2I = expr2.analyze(env)
    ExprI.If(location, conditionI, expr1I, expr2I)
}

fun ExprT.Lambda.analyze(env: Env) = run {
    val (s1, typeK) = infer(env)
    val argTypeK = typeK.arrow.t1
    val returnTypeK = typeK.arrow.t2
    val closures = freeVariables.map { GenericK(it, env.getVar(it)!!.instantiate()) }
    val typeSchemeK = typeK.generalize(env, s1)
    val genericsK = typeSchemeK.generics + closures.flatMap { it.type.generalize(env, s1).generics }
    val localEnv = env
            .withMemLocs(closures.map { (a, _) -> a to MemLoc.Field(a) })
            .withVar(arg, argTypeK.scheme)
            .withMemLoc(arg, MemLoc.Arg(0))
    val closuresI = closures.map { (a, b) ->
        ExprI.LocalField(Location.unknown, a, b.typeI).let { it to it.type }
    }
    val exprI = expr.analyze(localEnv)
    val invokeScheme = DeclI.Method.Scheme(nil, listOf(argTypeK.typeI), returnTypeK.typeI)
    val invokeDecl = DeclI.Method("invoke", location, invokeScheme, exprI)
    val obj = DeclI.Class.Object(genericsK.map { it.genericI }, listOf(typeK.typeI), listOf(invokeDecl))
    ExprI.AnonymousObject(location, obj, closuresI)
}
