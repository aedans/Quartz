package io.quartz.analyzer

import io.quartz.tree.ast.FileT
import io.quartz.tree.ir.DeclI

/**
 * @author Aedan Smith
 */

fun FileT.analyze(env: Env): List<DeclI> = run {
    val localEnv = env.withPackage(`package`)
    decls.map { it.analyze(localEnv) }
}
