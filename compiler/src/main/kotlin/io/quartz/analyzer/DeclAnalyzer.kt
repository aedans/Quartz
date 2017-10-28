package io.quartz.analyzer

import io.quartz.analyzer.type.*
import io.quartz.tree.ast.DeclT
import io.quartz.tree.ir.DeclI
import io.quartz.tree.name
import io.quartz.tree.nil

/**
 * @author Aedan Smith
 */

fun DeclT.analyze(env: Env): DeclI = when (this) {
    is DeclT.Class -> analyze(env)
    is DeclT.Value -> analyze(env)
}

fun DeclT.Class.analyze(env: Env) = run {
    val declsI = decls.map { it.analyze(env) }
    val obj = DeclI.Class.Object(nil, nil, declsI)
    DeclI.Class(name, location, env.`package`, null, obj)
}

fun DeclT.Value.analyze(env: Env) = run {
    val name = "get${name.capitalize()}".name
    val schemeK = schemeK(env)
    val schemeI = schemeK.schemeI
    val scheme = DeclI.Method.Scheme(schemeI.generics, nil, schemeI.type)
    val exprI = expr.analyze(env)
    DeclI.Method(name, location, env.`package`, scheme, exprI)
}

fun DeclT.Value.schemeK(env: Env) = run {
    val (s1, exprType) = expr.infer(env)
    val schemeK = type?.typeK(env)?.generalize(env, s1)
    val s2 = unify(exprType, schemeK?.instantiate() ?: TypeK.Var(fresh()))
    schemeK ?: apply(s2 compose s1, exprType).generalize(env, s2)
}
