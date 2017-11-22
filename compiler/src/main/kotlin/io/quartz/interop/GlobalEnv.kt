package io.quartz.interop

import io.quartz.analyze.Env
import io.quartz.gen.asm.ProgramGenerator
import io.quartz.tree.QualifiedName

class GlobalEnv(
        private val cp: ClassPath,
        private val sp: SourcePath,
        private val pg: ProgramGenerator
) : Env {
    override fun getType(name: QualifiedName) = sp.getType(name, this, pg)
            ?: cp.getType(name)

    override fun getVar(name: QualifiedName) = sp.getVar(name, this, pg)
            ?: cp.getVar(name)
}
