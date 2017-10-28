package io.quartz.analyzer

import io.quartz.tree.ast.FileT
import io.quartz.tree.ir.DeclI

fun FileT.analyze(env: Env): ValidatedE<List<DeclI>> = run {
    val localEnv = env.withPackage(`package`)
    decls.map { it.analyze(localEnv) }.validated()
}
