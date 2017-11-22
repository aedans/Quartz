package io.quartz.analyze

import io.quartz.analyze.type.*
import io.quartz.err.Result
import io.quartz.err.flat
import io.quartz.err.resultMonad
import io.quartz.tree.Qualifier
import io.quartz.tree.ast.DeclT
import io.quartz.tree.ast.Package
import io.quartz.tree.ir.DeclI
import io.quartz.tree.qualify
import kategory.binding
import kategory.ev

fun DeclT.analyze(env: Env, p: Package): Result<DeclI> = when (this) {
    is DeclT.Trait -> analyze(env, p)
    is DeclT.Value -> analyze(env, p)
    is DeclT.Instance -> analyze(env, p)
}

fun DeclT.Trait.analyze(env: Env, p: Package) = resultMonad().binding {
    val localEnv = constraints.localEnv(env)
    val schemeK = schemeK(localEnv, p).bind()
    val constraints = schemeK.constraints.map { it.constraintI }
    val members = members.map { it.analyze(localEnv) }.flat().bind()
    val it = DeclI.Trait(location, name, p, constraints, members)
    yields(it as DeclI)
}.ev()

fun DeclT.Trait.Member.analyze(env: Env) = resultMonad().binding {
    val it = DeclI.Trait.Member(location, name, schemeT.schemeK(env).bind().schemeI)
    yields(it)
}.ev()

fun DeclT.Value.analyze(env: Env, p: Package) = resultMonad().binding {
    val localEnv = schemeT?.constraints?.localEnv(env) ?: env
    val schemeI = schemeK(localEnv).bind().schemeI
    val exprI = expr.analyze(localEnv, p).bind()
    val it = DeclI.Value(location, name, p, schemeI, exprI)
    yields(it)
}.ev()

fun DeclT.Instance.analyze(env: Env, p: Package): Nothing = TODO()

fun DeclT.Trait.schemeK(env: Env, qualifier: Qualifier) = resultMonad().binding {
    val it = SchemeK(
            constraints.map { it.constraintK(env).bind() },
            TypeK.Const(name.qualify(qualifier))
    )
    yields(it)
}.ev()

fun DeclT.Value.schemeK(env: Env) = resultMonad().binding {
    val schemeK = schemeT?.schemeK(env)?.bind()
    val (s1, exprType) = expr.infer(env).bind()
    val s2 = unify(exprType, schemeK?.instantiate() ?: TypeK.Var(fresh())).bind()
    yields(schemeK ?: apply(exprType, s2 compose s1).generalize(env, s2))
}.ev()
