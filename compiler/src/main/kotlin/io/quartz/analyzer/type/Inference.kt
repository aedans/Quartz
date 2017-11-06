package io.quartz.analyzer.type

import io.quartz.analyzer.*
import io.quartz.tree.ast.ExprT
import io.quartz.tree.qualifiedLocal
import kategory.*

typealias Infer = Err<InferState>
typealias InferState = Tuple2<Subst, TypeK>

/** Returns the type of an expression and all constraints it imposes as a substitution */
fun ExprT.infer(env: Env): Infer = when (this) {
    is ExprT.Unit -> (emptySubst toT TypeK.unit).right()
    is ExprT.Bool -> (emptySubst toT TypeK.bool).right()
    is ExprT.Id -> env.getVar(name).map { emptySubst toT it.scheme.instantiate() }
    is ExprT.Cast -> errMonad().binding {
        val typeK = type.typeK(env).bind()
        val (s1, exprType) = expr.infer(env).bind()
        val s2 = unify(exprType, typeK).bind()
        yields(s2 compose s1 toT apply(exprType, s2))
    }.ev()
    is ExprT.Apply -> errMonad().binding {
        val typeVariable = TypeK.Var(fresh())
        val (s1, expr1Type) = expr1.infer(env).bind()
        val (s2, expr2Type) = expr2.infer(apply(env, s1)).bind()
        val s3 = unify(apply(expr1Type, s2), TypeK.Arrow(expr2Type, typeVariable).type).bind()
        yields((s3 compose s2 compose s1) toT apply(typeVariable, s3))
    }.ev()
    is ExprT.Lambda -> errMonad().binding {
        val argVar = TypeK.Var(fresh())
        val envP = env
                .withVar(arg.qualifiedLocal, VarInfo(argVar.scheme, VarLoc.Arg(0)).right())
                .withType(argVar.name.qualifiedLocal, TypeInfo(argVar.scheme).right())
        val (s1, exprType) = expr.infer(envP).bind()
        yields(s1 toT TypeK.Arrow(apply(argVar, s1), exprType).type)
    }.ev()
    is ExprT.If -> errMonad().binding {
        val (s1, conditionType) = condition.infer(env).bind()
        val s2 = unify(conditionType, TypeK.bool).bind()
        val (s3, expr1Type) = expr1.infer(apply(env, s2)).bind()
        val (s4, expr2Type) = expr2.infer(apply(env, s2)).bind()
        val s5 = unify(expr1Type, expr2Type).bind()
        val t1 = apply(expr1Type, s5)
        val t2 = apply(expr2Type, s5)
        if (t1 != t2)
            throw Exception()
        yields((s5 compose s4 compose s3 compose s2 compose s1) toT t1)
    }.ev()
    is ExprT.Dot -> errMonad().binding {
        val (s1, exprType) = expr.infer(env).bind()
        val it = when (exprType) {
            is TypeK.Const -> exprType.env.getVar(name.qualifiedLocal)
                    .map { it.scheme.instantiate() }
            is TypeK.Var -> TODO()
            is TypeK.Apply -> TODO()
        }.bind()
        yields(s1 toT it)
    }.ev()
}
