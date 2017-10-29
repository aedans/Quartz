package io.quartz.analyzer.type

import io.quartz.analyzer.*
import io.quartz.tree.ast.ExprT
import kategory.Either
import kategory.binding
import kategory.ev
import kategory.right

typealias Infer = EitherE<InferState>
typealias InferState = Pair<Subst, TypeK>

/** Returns the type of an expression and all constraints it imposes as a substitution */
fun ExprT.infer(env: Env): Infer = when (this) {
    is ExprT.Unit -> (emptySubst to TypeK.unit).right()
    is ExprT.Bool -> (emptySubst to TypeK.bool).right()
    is ExprT.Var -> env.getVar(name).map { emptySubst to it.instantiate() }
    is ExprT.Cast -> Either.monadErrorE().binding {
        val typeK = type.typeK().bind()
        val (s1, exprType) = expr.infer(env).bind()
        val s2 = unify(exprType, typeK).bind()
        yields(s2 compose s1 to apply(exprType, s2))
    }.ev()
    is ExprT.Apply -> Either.monadErrorE().binding {
        val typeVariable = TypeK.Var(fresh())
        val (s1, expr1Type) = expr1.infer(env).bind()
        val (s2, expr2Type) = expr2.infer(apply(env, s1)).bind()
        val s3 = unify(apply(expr1Type, s2), TypeK.Arrow(expr2Type, typeVariable).type).bind()
        yields((s3 compose s2 compose s1) to apply(typeVariable, s3))
    }.ev()
    is ExprT.Lambda -> Either.monadErrorE().binding {
        val argVar = TypeK.Var(fresh())
        val envP = env.withVar(arg, argVar.scheme)
        val (s1, exprType) = expr.infer(envP).bind()
        yields(s1 to TypeK.Arrow(apply(argVar, s1), exprType).type)
    }.ev()
    is ExprT.If -> Either.monadErrorE().binding {
        val (s1, conditionType) = condition.infer(env).bind()
        val s2 = unify(conditionType, TypeK.bool).bind()
        val (s3, expr1Type) = expr1.infer(apply(env, s2)).bind()
        val (s4, expr2Type) = expr2.infer(apply(env, s2)).bind()
        val s5 = unify(expr1Type, expr2Type).bind()
        val t1 = apply(expr1Type, s5)
        val t2 = apply(expr2Type, s5)
        if (t1 != t2)
            throw Exception()
        yields((s5 compose s4 compose s3 compose s2 compose s1) to t1)
    }.ev()
}
