package io.quartz.env

import io.quartz.tree.util.*

val Qualifier.import get() = Import.Star(this)

fun Env.import(imports: List<Import>, qualifier: Qualifier) = imports
        .fold(this) { a: Env, b -> b.import(a) }
        .let { qualifier.import.import(it) }

fun Import.import(env: Env) = when (this) {
    is Import.Star -> import(env)
    is Import.Qualified -> import(env)
}

fun Import.Star.import(env: Env) = env
        .mapValues { name, value -> value ?: env.getValue(QualifiedName(qualifier, name.string)) }
        .mapTypes { name, value -> value ?: env.getType(QualifiedName(qualifier, name.string)) }

fun Import.Qualified.import(env: Env) = env
        .withValue(alias.qualifiedLocal) { env.getValue(name) }
        .withType(alias.qualifiedLocal) { env.getType(name) }
