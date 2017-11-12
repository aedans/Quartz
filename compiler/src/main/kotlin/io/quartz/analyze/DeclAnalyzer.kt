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
import io.quartz.tree.ir.TypeI
import io.quartz.tree.name
import io.quartz.tree.qualify
import kategory.binding
import kategory.ev

fun DeclT.analyze(env: Env, p: Package): Result<DeclI> = when (this) {
    is DeclT.Interface -> analyze(env, p)
    is DeclT.Value -> analyze(env, p)
}

fun DeclT.Interface.analyze(env: Env, p: Package) = resultMonad().binding {
    val localEnv = constraints.localEnv(env)
    val schemeK = schemeK(localEnv, p).bind()
    val constraints = schemeK.constraints.filter { it.type != TypeK.any }
    val constraintAbstractsI = constraints.map {
        val scheme = DeclI.Method.Scheme(nil, nil, it.type.apply(it.name.tVar).typeI)
        DeclI.Method("${it.name}$${it.type}".name, location, p, scheme, null)
    }
    val abstractsI = abstracts.map { decl -> decl.analyze(localEnv, p) }.flat().bind() +
            constraintAbstractsI
    val schemeI = schemeK.schemeI
    val obj = DeclI.Class.Object(schemeI.generics, nil, abstractsI)
    yields(DeclI.Class(name, location, p, null, obj) as DeclI)
}.ev()

fun DeclT.Interface.Abstract.analyze(env: Env, p: Package) = resultMonad().binding {
    val localEnv = schemeT.constraints.localEnv(env)
    val scheme = schemeT.schemeK(localEnv).bind().methodScheme(nil)
    val method = DeclI.Method(name, location, p, scheme, null)
    yields(method)
}.ev()

fun DeclT.Value.analyze(env: Env, p: Package) = resultMonad().binding {
    val localEnv1 = schemeT?.constraints?.localEnv(env) ?: env
    val schemeK = schemeK(localEnv1).bind()
    val scheme = schemeK.methodScheme(nil)
    val exprI = expr.analyze(env, p).bind()
    val method = DeclI.Method(name.varGetterName(), location, p, scheme, exprI)
    val obj = DeclI.Class.Object(nil, nil, method.singletonList())
    val it = DeclI.Class("$$name".name, location, p, null, obj)
    yields(it)
}.ev()

fun DeclT.Interface.schemeK(env: Env, qualifier: Qualifier) = resultMonad().binding {
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
