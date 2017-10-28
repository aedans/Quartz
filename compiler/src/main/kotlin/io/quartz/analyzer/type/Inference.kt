package io.quartz.analyzer.type

import io.quartz.analyzer.Env
import io.quartz.analyzer.fresh
import io.quartz.analyzer.withVar
import io.quartz.tree.ast.ExprT
import io.quartz.tree.ast.TypeT

/** Returns the type of an expression and all constraints it imposes as a substitution */
fun ExprT.infer(env: Env): Pair<Subst, TypeK> = when (this) {
    is ExprT.Unit -> emptySubst to TypeT.unit.typeK(env)
    is ExprT.Bool -> emptySubst to TypeT.bool.typeK(env)
    is ExprT.Var -> env.getVar(name)?.let { emptySubst to it.instantiate() }
            ?: throw NoSuchElementException(name.toString())
    is ExprT.Cast -> {
        val (s1, exprType) = expr.infer(env)
        val s2 = unify(exprType, type.typeK(env))
        s2 compose s1 to apply(exprType, s2)
    }
    is ExprT.Apply -> {
        val typeVariable = TypeK.Var(fresh())
        val (s1, expr1Type) = expr1.infer(env)
        val (s2, expr2Type) = expr2.infer(apply(env, s1))
        val s3 = unify(apply(expr1Type, s2), TypeK.Arrow(expr2Type, typeVariable).type)
        (s3 compose s2 compose s1) to apply(typeVariable, s3)
    }
    is ExprT.Lambda -> {
        val arg = TypeK.Var(fresh())
        val envP = env.withVar(this.arg, arg.scheme)
        val (s1, exprType) = expr.infer(envP)
        s1 to TypeK.Arrow(apply(arg, s1), exprType).type
    }
    is ExprT.If -> {
        val (s1, conditionType) = condition.infer(env)
        val s2 = unify(conditionType, TypeT.bool.typeK(env))
        val (s3, expr1Type) = expr1.infer(apply(env, s2))
        val (s4, expr2Type) = expr2.infer(apply(env, s2))
        val s5 = unify(expr1Type, expr2Type)
        val t1 = apply(expr1Type, s5)
        val t2 = apply(expr2Type, s5)
        if (t1 != t2)
            throw Exception()
        (s5 compose s4 compose s3 compose s2 compose s1) to t1
    }
}
