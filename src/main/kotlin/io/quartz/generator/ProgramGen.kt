package io.quartz.generator

import io.quartz.generator.asm.ProgramGenerator
import io.quartz.tree.ir.ProgramI

/**
 * @author Aedan Smith
 */

fun ProgramI.generate(pg: ProgramGenerator) {
    forEach { it.generate(pg) }
}
