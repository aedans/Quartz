package io.quartz.analyzer

import io.quartz.tree.QualifiedName
import io.quartz.tree.ast.FileT
import io.quartz.tree.ast.ImportT
import io.quartz.tree.ir.DeclI
import io.quartz.tree.qualifiedLocal
import kategory.*

fun FileT.analyze(env: Env): EitherE<List<DeclI>> = Either.monadErrorE().binding {
    val localEnv = imports
            .fold(env.withPackage(`package`).right()) { a: EitherE<Env>, b ->
                a.flatMap { b.import(it) }
            }.bind()
    val it = decls.map { it.analyze(localEnv).bind() }
    yields(it)
}.ev()

fun ImportT.import(env: Env) = when (this) {
    is ImportT.Star -> env.mapVars { name, value ->
        value.fold(
                { err ->
                    when (err) {
                        UnknownVariable(name) -> env.getVar(QualifiedName(qualifier, name.string)).bimap({ err }, ::identity)
                        else -> err.left()
                    }
                },
                { it.right() }
        )
    }.mapTypes { name, value ->
        value.fold(
                { err ->
                    when (err) {
                        UnknownType(name) -> env.getType(QualifiedName(qualifier, name.string)).bimap({ err }, ::identity)
                        else -> err.left()
                    }
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

class UnknownPackage(qualifiedName: QualifiedName) : CompilerError("Could not find package $qualifiedName")
