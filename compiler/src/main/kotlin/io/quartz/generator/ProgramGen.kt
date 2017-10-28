package io.quartz.generator

import io.quartz.generator.asm.ProgramGenerator
import io.quartz.tree.ir.DeclI

fun List<DeclI>.generate(pg: ProgramGenerator) {
    forEach { it.generate(pg) }
}
