package io.quartz.interop

import io.quartz.analyzer.Env
import io.quartz.analyzer.MemLoc
import io.quartz.analyzer.schemeK
import io.quartz.analyzer.type.schemeK
import io.quartz.tree.ast.DeclT

/**
 * @author Aedan Smith
 */

data class GlobalEnv(
        private val cp: ClassPath,
        private val sp: SourcePath
) : Env {
    override fun getType(name: String) = spGetType(name) ?: cpGetType(name)

    private fun spGetType(name: String) = (sp.getDecl(name) as? DeclT.Class)?.schemeK
    private fun cpGetType(name: String) = cp.getClass(name)?.schemeK

    override fun getVar(name: String) = spGetVar(name) ?: cpGetVar(name)

    private fun spGetVar(name: String) = (sp.getDecl(name) as? DeclT.Value)?.schemeK(this)
    private fun cpGetVar(name: String) = cp.getClass("\$Get${name.capitalize()}")
            ?.getMethod("get${name.capitalize()}")
            ?.returnType
            ?.schemeK

    override fun getMemLoc(name: String) = spGetMemLoc(name) ?: cpGetMemLoc(name)

    private fun spGetMemLoc(name: String) = (sp.getDecl(name) as? DeclT.Value)
            ?.let { MemLoc.Global(name) }

    private fun cpGetMemLoc(name: String) = cp.getClass("\$Get${name.capitalize()}")
            ?.let { MemLoc.Global(name) }
}
