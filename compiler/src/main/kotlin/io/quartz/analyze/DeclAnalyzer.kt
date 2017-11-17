package io.quartz.analyze

import io.quartz.analyze.type.*
import io.quartz.err.Result
import io.quartz.err.flat
import io.quartz.err.resultMonad
import io.quartz.interop.varGetterName
import io.quartz.nil
import io.quartz.singletonList
import io.quartz.tree.Qualifier
import io.quartz.tree.ast.DeclT
import io.quartz.tree.ast.Package
import io.quartz.tree.ir.DeclI
import io.quartz.tree.ir.ExprI
import io.quartz.tree.ir.TypeI
import io.quartz.tree.name
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
    val constraints = schemeK.constraints.filter { it.type != TypeK.any }
    val constraintAbstractsI = constraints.map {
        val scheme = DeclI.Method.Scheme(nil, nil, it.type.apply(it.name.tVar).typeI)
        DeclI.Method("${it.name}$${it.type}".name, location, p, scheme, null)
    }
    val abstractsI = members.map { decl -> decl.analyze(localEnv, p) }.flat().bind() +
            constraintAbstractsI
    val schemeI = schemeK.schemeI
    yields(DeclI.Class(name, location, p, null, schemeI.generics, nil, abstractsI) as DeclI)
}.ev()

fun DeclT.Trait.Member.analyze(env: Env, p: Package) = resultMonad().binding {
    val localEnv = schemeT.constraints.localEnv(env)
    val scheme = schemeT.schemeK(localEnv).bind().methodScheme(nil)
    val method = DeclI.Method(name, location, p, scheme, null)
    yields(method)
}.ev()

fun DeclT.Value.analyze(env: Env, p: Package) = resultMonad().binding {
    val local = analyzeLocal(env, p).bind().copy(name = name.varGetterName())
    val it = DeclI.Class("$$name".name, location, p, null, nil, nil, local.singletonList())
    yields(it)
}.ev()

fun DeclT.Value.analyzeLocal(env: Env, p: Package) = resultMonad().binding {
    val localEnv1 = schemeT?.constraints?.localEnv(env) ?: env
    val schemeK = schemeK(localEnv1).bind()
    val constraints = schemeK.constraints.filter { it.type != TypeK.any }
    val constraintParams = constraints.map { it.type.apply(it.name.tVar).typeI }
    val scheme = schemeK.methodScheme(constraintParams)
    val exprI = expr.analyze(env, p).bind()
    val method = DeclI.Method(name, location, p, scheme, exprI)
    yields(method)
}.ev()

fun DeclT.Instance.analyze(env: Env, p: Package) = resultMonad().binding {
    val typeK = type.typeK(env).bind()
    val instanceK = instance.typeK(env).bind()
    val extensionK = typeK.apply(instanceK)
    val typeI = typeK.typeI
    val instanceI = instanceK.typeI
    val extensionI = extensionK.typeI
    val name = "${instanceI.qualifiedName.string}${typeI.qualifiedName.string}\$Instance".name
    val constructor = DeclI.Class.Constructor(nil, ExprI.Block(location, nil))
    val implsI = impls.map { it.analyzeLocal(env, p) }.flat().bind()
    val it = DeclI.Class(name, location, p, constructor, nil, extensionI.singletonList(), implsI)
    yields(it)
}.ev()

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

fun SchemeK.methodScheme(args: List<TypeI>) = run {
    val schemeI = schemeI
    DeclI.Method.Scheme(schemeI.generics, args, schemeI.type)
}
