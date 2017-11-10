package io.quartz.analyze

import io.quartz.tree.QualifiedName
import io.quartz.tree.ast.ImportT
import io.quartz.tree.ast.Package
import io.quartz.tree.qualifiedLocal

val Package.import get() = ImportT.Star(this)

fun Env.import(imports: List<ImportT>) = imports.fold(this) { a: Env, b -> b.import(a) }

fun ImportT.import(env: Env) = when (this) {
    is ImportT.Star -> import(env)
    is ImportT.Qualified -> import(env)
}

fun ImportT.Star.import(env: Env) = env
        .mapVars { name, value -> value ?: env.getVar(QualifiedName(qualifier, name.string)) }
        .mapTypes { name, value -> value ?: env.getType(QualifiedName(qualifier, name.string)) }

fun ImportT.Qualified.import(env: Env) = env
        .withVar(alias.qualifiedLocal) { env.getVar(qualifiedName) }
        .withType(alias.qualifiedLocal) { env.getType(qualifiedName) }
