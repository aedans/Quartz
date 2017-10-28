package io.quartz.analyzer

import io.quartz.analyzer.type.*
import io.quartz.tree.ast.DeclT
import io.quartz.tree.ir.DeclI
import io.quartz.tree.name
import io.quartz.tree.nil
import kategory.Either
import kategory.binding
import kategory.ev

fun DeclT.analyze(env: Env): EitherE<DeclI> = when (this) {
    is DeclT.Class -> analyze(env)
    is DeclT.Value -> analyze(env)
}

fun DeclT.Class.analyze(env: Env) = Either.monadErrorE().binding {
    val declsI = decls.map { it.analyze(env).bind() }
    val obj = DeclI.Class.Object(nil, nil, declsI)
    yields(DeclI.Class(name, location, env.`package`, null, obj))
}.ev()

fun DeclT.Value.analyze(env: Env) = Either.monadErrorE().binding {
    val name = "get${name.capitalize()}".name
    val schemeK = schemeK(env).bind()
    val schemeI = schemeK.schemeI
    val scheme = DeclI.Method.Scheme(schemeI.generics, nil, schemeI.type)
    val exprI = expr.analyze(env).bind()
    yields(DeclI.Method(name, location, env.`package`, scheme, exprI))
}.ev()

fun DeclT.Value.schemeK(env: Env) = Either.monadErrorE().binding {
    val (s1, exprType) = expr.infer(env).bind()
    val schemeK = type?.typeK(env)?.bind()?.generalize(env, s1)
    val s2 = unify(exprType, schemeK?.instantiate() ?: TypeK.Var(fresh())).bind()
    yields(schemeK ?: apply(exprType, s2 compose s1).generalize(env, s2))
}.ev()
