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
import io.quartz.tree.name
import io.quartz.tree.qualifiedLocal
import io.quartz.tree.qualify
import kategory.*

fun DeclT.analyze(env: Env, p: Package, local: Boolean): Tuple2<Env, Errs<DeclI>> = when (this) {
    is DeclT.Class -> analyze(env, p, local)
    is DeclT.Value -> analyze(env, p, local)
}.let { (a, b) -> a toT b.qualifyAll() }

fun DeclT.Class.analyze(env: Env, p: Package, local: Boolean) = run {
    val (_, declsIE) = decls.foldMap(env) { env, decl -> decl.analyze(env, p, true) }
    val typeInfo = TypeInfo(schemeK(p))
    val qualifiedName = if (local) name.qualifiedLocal else name.qualify(p)
    val envP = env.withType(qualifiedName, typeInfo.right())
    envP toT errsMonad().binding {
        val declsI = declsIE.flat().bind()
        val obj = DeclI.Class.Object(nil, nil, declsI)
        yields(DeclI.Class(name, location, p, null, obj) as DeclI)
    }.ev()
}

fun DeclT.Value.analyze(env: Env, p: Package, local: Boolean) = run {
    val localEnv = schemeT?.generics?.localEnv(env) ?: env
    val qualifiedName = if (local) name.qualifiedLocal else name.qualify(p)
    val schemeKE = schemeK(localEnv)
    val varInfo = schemeKE.map { schemeK -> VarInfo(schemeK, VarLoc.Global(qualifiedName)) }
    val nEnv = localEnv.withVar(qualifiedName, varInfo)
    nEnv toT errsMonad().binding {
        val name = name.varGetterName()
        val schemeK = schemeKE.errs().bind()
        val schemeI = schemeK.schemeI
        val scheme = DeclI.Method.Scheme(schemeI.generics, nil, schemeI.type)
        val exprI = expr.analyze(nEnv, p).errs().bind()
        val method = DeclI.Method(name, location, p, scheme, exprI)
        val it = if (local) method else
            DeclI.Class("$$name".name, location, p, null, DeclI.Class.Object(nil, nil, method.singletonList()))
        yields(it)
    }.ev()
}

fun DeclT.Class.schemeK(qualifier: Qualifier) = SchemeK(nil, TypeK.Const(name.qualify(qualifier)))

fun DeclT.Value.schemeK(env: Env) = errMonad().binding {
    val schemeK = schemeT?.schemeK(env)?.bind()
    val (s1, exprType) = expr.infer(env).bind()
    val s2 = unify(exprType, schemeK?.instantiate() ?: TypeK.Var(fresh())).bind()
    yields(schemeK ?: apply(exprType, s2 compose s1).generalize(env, s2))
}.ev()
