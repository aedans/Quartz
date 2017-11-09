package io.quartz.analyze.type

import io.quartz.analyze.*
import io.quartz.err.CompilerError
import io.quartz.err.qualify
import io.quartz.err.resultMonad
import io.quartz.tree.ast.ExprT
import io.quartz.tree.qualifiedLocal
import kategory.*

typealias Infer = Either<List<CompilerError>, InferState>
typealias InferState = Tuple2<Subst, TypeK>

/** Returns the type of an expression and all constraints it imposes as a substitution */
fun ExprT.infer(env: Env): Infer = when (this) {
    is ExprT.Unit -> (emptySubst toT TypeK.unit).right()
    is ExprT.Bool -> (emptySubst toT TypeK.bool).right()
    is ExprT.Id -> env.getVar(name).map { emptySubst toT it.scheme.instantiate() }.qualify()
    is ExprT.Cast -> resultMonad().binding {
        val typeK = type.typeK(env).bind()
        val (s1, exprType) = expr.infer(env).bind()
        val s2 = unify(exprType, typeK).bind()
        yields(s2 compose s1 toT apply(exprType, s2))
    }.ev()
    is ExprT.Apply -> resultMonad().binding {
        val typeVariable = TypeK.Var(fresh())
        val (s1, expr1Type) = expr1.infer(env).bind()
        val (s2, expr2Type) = expr2.infer(apply(env, s1)).bind()
        val s3 = unify(apply(expr1Type, s2), TypeK.Arrow(expr2Type, typeVariable).type).bind()
        yields((s3 compose s2 compose s1) toT apply(typeVariable, s3))
    }.ev()
    is ExprT.Lambda -> resultMonad().binding {
        val argVar = TypeK.Var(fresh())
        val envP = env
                .withVar(arg.qualifiedLocal) { VarInfo(argVar.scheme, VarLoc.Arg(0)).right() }
                .withType(argVar.name.qualifiedLocal) { TypeInfo(argVar.scheme).right() }
        val (s1, exprType) = expr.infer(envP).bind()
        yields(s1 toT TypeK.Arrow(apply(argVar, s1), exprType).type)
    }.ev()
    is ExprT.If -> resultMonad().binding {
        val (s1, conditionType) = condition.infer(env).bind()
        val s2 = unify(conditionType, TypeK.bool).qualify(condition).bind()
        val (s3, expr1Type) = expr1.infer(apply(env, s2)).bind()
        val (s4, expr2Type) = expr2.infer(apply(env, s2)).bind()
        val s5 = unify(expr1Type, expr2Type).bind()
        val t1 = apply(expr1Type, s5)
        val t2 = apply(expr2Type, s5)
        if (t1 != t2) throw Exception()
        yields((s5 compose s4 compose s3 compose s2 compose s1) toT t1)
    }.ev()
}.qualify()
