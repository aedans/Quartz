package io.quartz.analyze

import io.quartz.analyze.type.*
import io.quartz.env.*
import io.quartz.err.*
import io.quartz.tree.ast.DeclT
import io.quartz.tree.ir.*
import io.quartz.tree.util.*
import kategory.*

fun DeclT.analyze(env: Env, qualifier: Qualifier): Result<DeclI> = when (this) {
    is DeclT.Trait -> analyze(env, qualifier)
    is DeclT.Value -> analyze(env, qualifier)
    is DeclT.Instance -> analyze(env, qualifier)
}

fun DeclT.Trait.analyze(env: Env, qualifier: Qualifier) = resultMonad().binding {
    val localEnv = foralls.localEnv(env)
    val schemeI = SchemeI(
            foralls,
            constraints.map { it.constraintI(localEnv) }.flat().bind(),
            TypeI.Const(name.qualify(qualifier))
    )
    val members = members.map { it.analyze(localEnv) }.flat().bind()
    val it = DeclI.Trait(location, name.qualify(qualifier), schemeI, members)
    yields(it)
}.ev()

fun DeclT.Trait.Member.analyze(env: Env) = resultMonad().binding {
    val it = DeclI.Trait.Member(location, name, schemeT.schemeI(env).bind())
    yields(it)
}.ev()

fun DeclT.Value.analyze(env: Env, qualifier: Qualifier) = resultMonad().binding {
    val localEnv = scheme?.foralls?.localEnv(env) ?: env
    val schemeIO = scheme?.schemeI(localEnv)?.bind()
    val (s1, exprType) = expr.infer(localEnv).bind()
    val s2 = unify(exprType, schemeIO?.instantiate() ?: TypeI.Var(fresh())).qualify().bind()
    val schemeI = schemeIO ?: apply(exprType, s2 compose s1).generalize()
    val exprI = expr.analyze(localEnv, qualifier).bind()
    val it = DeclI.Value(location, name.qualify(qualifier), schemeI, exprI)
    yields(it)
}.ev()

fun DeclT.Instance.analyze(env: Env, qualifier: Qualifier) = resultMonad().binding {
    val schemeI = scheme.schemeI(env).bind()
    val traitI = env.getTraitOrErr(instance.qualifiedLocal).bind()
    val implsI = impls.map {
        it.analyze(env, qualifier).flatMap { impl ->
            val superValue = traitI.members.find { it.name == impl.name.unqualified }
            val s1 = unify(superValue?.scheme?.instantiate() ?: TypeI.Var(fresh()), impl.scheme.instantiate())
            s1.map { impl.copy(scheme = apply(impl.scheme, it)) }
        }
    }.flat().bind()
    val it = DeclI.Instance(location, name?.qualify(qualifier), traitI.name, schemeI, implsI)
    yields(it)
}.ev()

