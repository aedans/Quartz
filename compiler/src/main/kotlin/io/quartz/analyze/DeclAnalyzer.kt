package io.quartz.analyze

import io.quartz.analyze.type.*
import io.quartz.err.*
import io.quartz.foldMap
import io.quartz.interop.varGetterName
import io.quartz.nil
import io.quartz.singletonList
import io.quartz.tree.Qualifier
import io.quartz.tree.ast.DeclT
import io.quartz.tree.ast.Package
import io.quartz.tree.ir.DeclI
import io.quartz.tree.ir.TypeI
import io.quartz.tree.name
import io.quartz.tree.qualifiedLocal
import io.quartz.tree.qualify
import kategory.Tuple2
import kategory.binding
import kategory.ev
import kategory.toT

fun DeclT.analyze(env: Env, p: Package): Tuple2<Env, Errs<DeclI>> = when (this) {
    is DeclT.Interface -> analyze(env, p)
    is DeclT.Value -> analyze(env, p)
}.let { (a, b) -> a toT b.qualifyAll() }

fun DeclT.Interface.analyze(env: Env, p: Package) = run {
    val localEnv = constraints.localEnv(env)
    val (_, abstractsIE) = abstracts
            .foldMap(localEnv) { env, decl -> decl.analyze(env, p) }
    val schemeKE = schemeK(localEnv, p)
    val typeInfo = schemeKE.map { scheme -> TypeInfo(scheme) }
    val qualifiedName = name.qualify(p)
    val envP = env.withType(qualifiedName, typeInfo)
    envP toT errsMonad().binding {
        val schemeK = schemeKE.errs().bind()
        val schemeI = schemeK.schemeI
        val abstractsI = abstractsIE.flat().bind()
        val obj = DeclI.Class.Object(schemeI.generics, nil, abstractsI)
        yields(DeclI.Class(name, location, p, null, obj) as DeclI)
    }.ev()
}

fun DeclT.Interface.Abstract.analyze(env: Env, p: Package) = run {
    val localEnv = schemeT.constraints.localEnv(env)
    val qualifiedName = name.qualifiedLocal
    val schemeKE = schemeT.schemeK(localEnv)
    val varInfo = schemeKE.map { schemeK -> VarInfo(schemeK, VarLoc.Global(qualifiedName)) }
    val envP = localEnv.withVar(qualifiedName, varInfo)
    envP toT errsMonad().binding {
        val name = name.varGetterName()
        val scheme = schemeKE.errs().bind().methodScheme(nil)
        val method = DeclI.Method(name, location, p, scheme, null)
        yields(method)
    }.ev()
}

fun DeclT.Value.analyze(env: Env, p: Package) = run {
    val localEnv1 = schemeT?.constraints?.localEnv(env) ?: env
    val qualifiedName = name.qualify(p)
    val schemeKE = schemeK(localEnv1)
    val varInfo = schemeKE.map { schemeK -> VarInfo(schemeK, VarLoc.Global(qualifiedName)) }
    val envP = env.withVar(qualifiedName, varInfo)
    envP toT errsMonad().binding {
        val name = name.varGetterName()
        val schemeK = schemeKE.errs().bind()
        val scheme = schemeK.methodScheme(nil)
        val exprI = expr.analyze(envP, p).errs().bind()
        val method = DeclI.Method(name, location, p, scheme, exprI)
        val obj = DeclI.Class.Object(nil, nil, method.singletonList())
        val it = DeclI.Class("$$name".name, location, p, null, obj)
        yields(it)
    }.ev()
}

fun DeclT.Interface.schemeK(env: Env, qualifier: Qualifier) = errMonad().binding {
    val it = SchemeK(
            constraints.map { it.constraintK(env).bind() },
            TypeK.Const(name.qualify(qualifier))
    )
    yields(it)
}.ev()

fun DeclT.Value.schemeK(env: Env) = errMonad().binding {
    val schemeK = schemeT?.schemeK(env)?.bind()
    val (s1, exprType) = expr.infer(env).bind()
    val s2 = unify(exprType, schemeK?.instantiate() ?: TypeK.Var(fresh())).bind()
    yields(schemeK ?: apply(exprType, s2 compose s1).generalize(env, s2))
}.ev()

fun SchemeK.methodScheme(args: List<TypeI>) = run {
    val schemeI = schemeI
    DeclI.Method.Scheme(schemeI.generics, args, schemeI.type)
}
