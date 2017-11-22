package io.quartz.analyze

import io.quartz.analyze.type.*
import io.quartz.err.Result
import io.quartz.err.resultMonad
import io.quartz.tree.Location
import io.quartz.tree.ast.ExprT
import io.quartz.tree.ast.Package
import io.quartz.tree.ir.ExprI
import io.quartz.tree.ir.TypeI
import io.quartz.tree.name
import io.quartz.tree.qualifiedLocal
import io.quartz.tree.unqualified
import kategory.*

fun ExprT.analyze(env: Env, p: Package): Result<ExprI> = when (this) {
    is ExprT.Cast -> analyze(env, p)
    is ExprT.Var -> analyze(env)
    is ExprT.Apply -> analyze(env, p)
    is ExprT.If -> analyze(env, p)
    is ExprT.Lambda -> analyze(env, p)
}.qualify()

fun ExprT.Cast.analyze(env: Env, p: Package) = expr.analyze(env, p)

fun ExprT.Var.analyze(env: Env) = resultMonad().binding {
    val result = env.getVarOrErr(name).bind()
    yields(ExprI.Var(location, name, result.loc, result.scheme.instantiate().typeI))
}.ev()

fun ExprT.Apply.analyze(env: Env, p: Package) = resultMonad().binding {
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
            listOf(expr2I toT  expr1TypeK.typeI)
    )
    yields(it)
}.ev()

fun ExprT.If.analyze(env: Env, p: Package) = resultMonad().binding {
    val conditionI = condition.analyze(env, p).bind()
    val expr1I = expr1.analyze(env, p).bind()
    val expr2I = expr2.analyze(env, p).bind()
    val it = ExprI.If(location, conditionI, expr1I, expr2I)
    yields(it)
}.ev()

fun ExprT.Lambda.analyze(env: Env, p: Package) = resultMonad().binding {
    val (s1, typeK) = infer(env).bind()
    val arrow = typeK.arrow.bind()
    val argTypeK = arrow.t1
    val returnTypeK = arrow.t2
    val closures = freeVariables
    val closuresMap = closures.associate { it to ExprI.Var.Loc.Field(it.unqualified) }
    val constraintsI = typeK.generalize(env, s1).constraints.map { it.constraintI }
    val localEnv = env
            .mapIds { name, err ->
                err?.map { closuresMap[name]?.let { varLoc -> it.copy(loc = varLoc) } ?: it }
            }
            .withId(arg.qualifiedLocal) { IdInfo(argTypeK.scheme, ExprI.Var.Loc.Arg(0)).right() }
    val closuresI = closures.map {
        val typeI = apply(localEnv.getVarOrErr(it).bind().scheme, s1).instantiate().typeI
        Tuple2(
                ExprT.Var(Location.unknown, it).analyze(env).bind(),
                ExprI.Var(Location.unknown, it, ExprI.Var.Loc.Field(it.unqualified), typeI)
        )
    }
    val exprI = expr.analyze(localEnv, p).bind()
    val it = ExprI.Lambda(location, p, constraintsI, argTypeK.typeI, returnTypeK.typeI, exprI, closuresI)
    yields(it)
}.ev()
