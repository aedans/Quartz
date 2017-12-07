package io.quartz.analyze

import io.quartz.analyze.tree.*
import io.quartz.analyze.type.*
import io.quartz.env.*
import io.quartz.err.*
import io.quartz.tree.ast.DeclT
import io.quartz.tree.ir.DeclI
import io.quartz.tree.util.*
import kategory.*

fun DeclT.analyze(env: Env, qualifier: Qualifier): Result<DeclI> = when (this) {
    is DeclT.Trait -> analyze(env, qualifier)
    is DeclT.Value -> analyze(env, qualifier)
    is DeclT.Instance -> analyze(env, qualifier)
}

fun DeclT.Trait.analyze(env: Env, qualifier: Qualifier) = resultMonad().binding {
    val localEnv = foralls.localEnv(env)
    val schemeK = schemeK(localEnv, qualifier).bind()
    val members = members.map { it.analyze(localEnv) }.flat().bind()
    val it = DeclI.Trait(location, qualifier, name, schemeK.foralls, members)
    yields(it as DeclI)
}.ev()

fun DeclT.Trait.Member.analyze(env: Env) = resultMonad().binding {
    val it = DeclI.Trait.Member(location, name, schemeT.schemeK(env).bind().schemeI)
    yields(it)
}.ev()

fun DeclT.Value.analyze(env: Env, qualifier: Qualifier) = resultMonad().binding {
    val localEnv = scheme?.foralls?.localEnv(env) ?: env
    val schemeI = schemeK(localEnv, qualifier).bind().schemeI
    val exprI = expr.analyze(localEnv, qualifier).bind()
    val it = DeclI.Value(location, qualifier, name, schemeI, exprI)
    yields(it)
}.ev()

fun DeclT.Instance.analyze(env: Env, qualifier: Qualifier) = resultMonad().binding {
    val schemeK = scheme.schemeK(env).bind()
    val schemeI = schemeK.schemeI
    val implsI = impls.map { it.analyze(env, qualifier) }.flat().bind()
    val it = DeclI.Instance(location, qualifier, name, instance, schemeI, implsI)
    yields(it)
}.ev()

fun DeclT.schemeK(env: Env, qualifier: Qualifier) = when (this) {
    is DeclT.Trait -> schemeK(env, qualifier)
    is DeclT.Value -> schemeK(env, qualifier)
    is DeclT.Instance -> TODO()
}

fun DeclT.Trait.schemeK(env: Env, qualifier: Qualifier) = resultMonad().binding {
    val it = SchemeK(
            foralls,
            constraints.map { it.constraintK(env).bind() },
            TypeK.Const(name.qualify(qualifier))
    )
    yields(it)
}.ev()

fun DeclT.Value.schemeK(env: Env, qualifier: Qualifier) = resultMonad().binding {
    val schemeK = scheme?.schemeK(env)?.bind()
    val (s1, exprType) = expr.infer(env).bind()
    val s2 = unify(exprType, schemeK?.instantiate() ?: TypeK.Var(fresh())).qualify().bind()
    yields(schemeK ?: apply(exprType, s2 compose s1).generalize())
}.ev()
