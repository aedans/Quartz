package io.quartz.analyzer

import io.quartz.analyzer.type.*
import io.quartz.foldMap
import io.quartz.interop.varGetterName
import io.quartz.tree.ast.DeclT
import io.quartz.tree.ast.Package
import io.quartz.tree.ir.DeclI
import io.quartz.nil
import io.quartz.tree.qualify
import kategory.*

fun DeclT.analyze(env: Env, p: Package): Tuple2<Env, EitherE<DeclI>> = when (this) {
    is DeclT.Class -> analyze(env, p)
    is DeclT.Value -> analyze(env, p)
}

fun DeclT.Class.analyze(env: Env, p: Package) =  run {
    val (localEnv, declsI) = decls.foldMap(env) { env, decl -> decl.analyze(env, p) }
    val schemeK = schemeK(p)
    val typeInfo = TypeInfo(schemeK)
    val nEnv = env.withType(name.qualify(p), typeInfo.right())
    nEnv toT Either.monadErrorE().binding {
        val obj = DeclI.Class.Object(nil, nil, declsI.map { it.bind() })
        yields(DeclI.Class(name, location, p, null, obj))
    }.ev()
}

fun DeclT.Value.analyze(env: Env, p: Package) = run {
    val schemeKE = schemeK(env)
    val varLoc = VarLoc.Global(name.qualify(p))
    val varInfo = schemeKE.map { schemeK -> VarInfo(schemeK, varLoc) }
    val nEnv = env.withVar(name.qualify(p), varInfo)
    nEnv toT Either.monadErrorE().binding {
        val name = name.varGetterName()
        val schemeK = schemeKE.bind()
        val schemeI = schemeK.schemeI
        val scheme = DeclI.Method.Scheme(schemeI.generics, nil, schemeI.type)
        val exprI = expr.analyze(nEnv, p).bind()
        yields(DeclI.Method(name, location, p, scheme, exprI))
    }.ev()
}

fun DeclT.Value.schemeK(env: Env) = Either.monadErrorE().binding {
    val (s1, exprType) = expr.infer(env).bind()
    val schemeK = schemeT?.schemeK(env)?.bind()
    val s2 = unify(exprType, schemeK?.instantiate() ?: TypeK.Var(fresh())).bind()
    yields(schemeK ?: apply(exprType, s2 compose s1).generalize(env, s2))
}.ev()
