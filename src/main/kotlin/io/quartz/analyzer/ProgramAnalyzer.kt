package io.quartz.analyzer

import io.quartz.tree.ast.ProgramT
import io.quartz.tree.ir.ProgramI

/**
 * @author Aedan Smith
 */

fun ProgramT.analyze(env: Env): ProgramI = map { it.analyze(env) }
