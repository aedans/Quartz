package io.quartz.analyzer

import io.quartz.tree.ast.FileT
import kategory.Either
import kategory.binding
import kategory.ev

fun FileT.analyze(env: Env) = Either.monadErrorE().binding {
    val localEnv = env.withPackage(`package`)
    val it = decls.map { it.analyze(localEnv).bind() }
    yields(it)
}.ev()
