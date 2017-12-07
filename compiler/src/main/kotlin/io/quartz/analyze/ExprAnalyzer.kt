package io.quartz.analyze

import io.quartz.analyze.tree.*
import io.quartz.analyze.type.*
import io.quartz.env.*
import io.quartz.err.*
import io.quartz.tree.ast.ExprT
import io.quartz.tree.ir.ExprI
import io.quartz.tree.util.*
import kategory.*

fun ExprT.analyze(env: Env, qualifier: Qualifier): Result<ExprI> = when (this) {
    is ExprT.Cast -> analyze(env, qualifier)
    is ExprT.Var -> analyze(env)
    is ExprT.Apply -> analyze(env, qualifier)
    is ExprT.If -> analyze(env, qualifier)
    is ExprT.Lambda -> analyze(env, qualifier)
}.qualify()

fun ExprT.Cast.analyze(env: Env, qualifier: Qualifier) = expr.analyze(env, qualifier)

fun ExprT.Var.analyze(env: Env) = resultMonad().binding {
    val result = env.getVarOrErr(name).bind()
    yields(ExprI.Var(location, name, result.instantiate().typeI))
}.ev()

fun ExprT.Apply.analyze(env: Env, qualifier: Qualifier) = resultMonad().binding {
    val (_, _) = infer(env).bind()
    val (_, expr1TypeK) = expr1.infer(env).bind()
    val arrowK = expr1TypeK.arrow.bind()
    val expr1I = expr1.analyze(env, qualifier).bind()
    val expr2I = expr2.analyze(env, qualifier).bind()
    val it = ExprI.Invoke(
            location,
            expr1I,
            expr2I,
            arrowK.arrowI
    )
    yields(it)
}.ev()

fun ExprT.If.analyze(env: Env, qualifier: Qualifier) = resultMonad().binding {
    val conditionI = condition.analyze(env, qualifier).bind()
    val expr1I = expr1.analyze(env, qualifier).bind()
    val expr2I = expr2.analyze(env, qualifier).bind()
    val it = ExprI.If(location, conditionI, expr1I, expr2I)
    yields(it)
}.ev()

fun ExprT.Lambda.analyze(env: Env, qualifier: Qualifier) = resultMonad().binding {
    val (s1, typeK) = infer(env).bind()
    val arrow = typeK.arrow.bind()
    val argTypeK = arrow.t1
    val returnTypeK = arrow.t2
    val closures = freeVariables
    val localEnv = env
            .withVar(argName.qualifiedLocal) { argTypeK.scheme.right() }
    val closuresK = closures.map {
        val qualifiedName = it.qualifiedLocal
        val typeI = apply(localEnv.getVarOrErr(qualifiedName).bind(), s1).instantiate()
        Tuple3(it, ExprT.Var(null, qualifiedName).analyze(env).bind(), typeI)
    }
    val foralls = typeK.generalize().foralls + closuresK.flatMap { it.c.generalize().foralls }
    val exprI = expr.analyze(localEnv, qualifier).bind()
    val closuresI = closuresK.map { Tuple3(it.a, it.b, it.c.typeI) }
    val it = ExprI.Lambda(location, qualifier, foralls, argName, argTypeK.typeI, returnTypeK.typeI, exprI, closuresI)
    yields(it)
}.ev()
