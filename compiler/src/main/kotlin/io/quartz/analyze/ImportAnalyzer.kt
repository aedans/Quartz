package io.quartz.analyze

import io.quartz.tree.QualifiedName
import io.quartz.tree.ast.FileT
import io.quartz.tree.ast.ImportT
import io.quartz.tree.qualifiedLocal
import kategory.Either
import org.funktionale.collections.prependTo

fun Env.import(fileT: FileT) = import(ImportT.Star(fileT.`package`).prependTo(fileT.imports))

fun Env.import(imports: List<ImportT>): Env = imports.fold(this) { a: Env, b -> b.import(a) }

fun ImportT.import(env: Env) = when (this) {
    is ImportT.Star -> import(env)
    is ImportT.Qualified -> import(env)
}

fun ImportT.Star.import(env: Env) = env
        .mapVars { name, value ->
            when (value) {
                is Either.Left -> env.getVar(QualifiedName(qualifier, name.string)).bimap({ value.a }, { it })
                else -> value
            }
        }
        .mapTypes { name, value ->
            when (value) {
                is Either.Left -> env.getType(QualifiedName(qualifier, name.string)).bimap({ value.a }, { it })
                else -> value
            }
        }

fun ImportT.Qualified.import(env: Env) = env
        .withVar(alias.qualifiedLocal) { env.getVar(qualifiedName) }
        .withType(alias.qualifiedLocal) { env.getType(qualifiedName) }
