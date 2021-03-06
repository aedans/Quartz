package io.quartz.analyze.type

import io.quartz.analyze.typeI
import io.quartz.env.*
import io.quartz.err.*
import io.quartz.nil
import io.quartz.tree.ast.ExprT
import io.quartz.tree.ir.*
import io.quartz.tree.util.qualifiedLocal
import kategory.*

fun ExprT.infer(env: Env): Result<Tuple2<Subst, TypeI>> = when (this) {
    is ExprT.Var -> env.getValueOrErr(name).map { emptySubst toT it.scheme.instantiate() }.qualify()
    is ExprT.Cast -> resultMonad().binding {
        val typeK = type.typeI(env).bind()
        val (s1, exprType) = expr.infer(env).bind()
        val s2 = unify(exprType, typeK).bind()
        yields(s2 compose s1 toT apply(exprType, s2))
    }.ev()
    is ExprT.Apply -> resultMonad().binding {
        val tVarName = fresh()
        val tVar = TypeI.Var(tVarName)
        val (s1, expr1Type) = expr1.infer(env).bind()
        val (s2, expr2Type) = expr2.infer(apply(env, s1)).bind()
        val s3 = unify(apply(expr1Type, s2), TypeI.Arrow(expr2Type, tVar).type).bind()
        yields((s3 compose s2 compose s1) toT apply(tVar, s3))
    }.ev()
    is ExprT.Lambda -> resultMonad().binding {
        val argName = fresh()
        val argVar = TypeI.Var(argName)
        val envP = env
                .withValue(this@infer.argName.qualifiedLocal) {
                    DeclI.Value(
                            location,
                            this@infer.argName.qualifiedLocal,
                            argVar.scheme,
                            null
                    ).right()
                }
                .withType(argVar.name.qualifiedLocal) { argVar.right() }
        val (s1, exprType) = expr.infer(envP).bind()
        yields(s1 toT TypeI.Arrow(apply(argVar, s1), exprType).type)
    }.ev()
    is ExprT.If -> resultMonad().binding {
        val (s1, conditionType) = condition.infer(env).bind()
        val s2 = unify(conditionType, TypeI.bool).qualify(condition).bind()
        val (s3, expr1Type) = expr1.infer(apply(env, s2)).bind()
        val (s4, expr2Type) = expr2.infer(apply(env, s2)).bind()
        val s5 = unify(expr1Type, expr2Type).bind()
        val t1 = apply(expr1Type, s5)
        val t2 = apply(expr2Type, s5)
        if (t1 != t2) throw Exception()
        yields((s5 compose s4 compose s3 compose s2 compose s1) toT t1)
    }.ev()
}.qualify()
