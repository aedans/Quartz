package io.quartz.analyzer

import io.quartz.tree.ast.DeclT
import io.quartz.tree.ast.FileT
import io.quartz.tree.ir.DeclI

/**
 * @author Aedan Smith
 */

fun List<DeclT>.analyze(env: Env): List<DeclI> = map { it.analyze(env) }
