package io.quartz.analyzer

import io.quartz.tree.QualifiedName
import io.quartz.tree.ast.ImportT
import io.quartz.tree.qualifiedLocal
import kategory.*

class UnknownPackage(qualifiedName: QualifiedName) : CompilerError("Could not find package $qualifiedName")

fun Env.import(imports: List<ImportT>) = imports
        .fold(right()) { a: EitherE<Env>, b ->
            a.flatMap { b.import(it) }
        }

fun ImportT.import(env: Env) = when (this) {
    is ImportT.Star -> env.mapVars { name, value ->
        value.fold(
                { err ->
                    env.getVar(QualifiedName(qualifier, name.string)).bimap({ err }, ::identity)
                },
                { it.right() }
        )
    }.mapTypes { name, value ->
        value.fold(
                { err ->
                    env.getType(QualifiedName(qualifier, name.string)).bimap({ err }, ::identity)
                },
                { it.right() }
        )
    }.right()
    is ImportT.Qualified -> import(env)
}

fun ImportT.Qualified.import(env: Env) = Ior.fromOptions(
        env.getVar(qualifiedName).toOption(),
        env.getType(qualifiedName).toOption()
).toEither { UnknownPackage(qualifiedName) }.map {
    it.bimap(
            { { env: Env -> env.withVar(alias.qualifiedLocal, it.right()) } },
            { { env: Env -> env.withType(alias.qualifiedLocal, it.right()) } }
    ).fold(::identity, ::identity, { a, b -> { a(b(it)) } })(env)
}
