package io.quartz.analyze

import io.quartz.env.*
import io.quartz.err.flat
import io.quartz.tree.ast.DeclT
import io.quartz.tree.util.Context

fun List<Context<DeclT>>.analyze(env: Env) = map { it.analyze(env) }.flat()

fun Context<DeclT>.analyze(env: Env) = value
        .analyze(env.import(imports, qualifier), qualifier)
        .map { Context(qualifier, imports, it) }
