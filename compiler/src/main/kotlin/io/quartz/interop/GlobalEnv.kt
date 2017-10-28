package io.quartz.interop

import io.quartz.analyzer.Env
import io.quartz.analyzer.MemLoc
import io.quartz.analyzer.schemeK
import io.quartz.analyzer.type.schemeK
import io.quartz.tree.*
import io.quartz.tree.ast.DeclT

data class GlobalEnv(
        private val cp: ClassPath,
        private val sp: SourcePath,
        override val `package`: Qualifier
) : Env {
    override fun getType(name: QualifiedName) = spGetType(name) ?: cpGetType(name)

    private fun spGetType(name: QualifiedName) = (sp.getDecl(name) as? DeclT.Class)
            ?.schemeK(this)

    private fun cpGetType(name: QualifiedName) = cp.getClass(name)?.schemeK

    override fun getVar(name: Name) = spGetVar(name) ?: cpGetVar(name)

    private fun spGetVar(name: Name) = (sp.getDecl(name.qualify(`package`)) as? DeclT.Value)
            ?.schemeK(this)

    private fun cpGetVar(name: Name) = cp.getClass("\$Get${name.capitalize()}".name.qualify(`package`))
            ?.getMethod("get${name.capitalize()}")
            ?.returnType
            ?.schemeK

    override fun getMemLoc(name: Name) = spGetMemLoc(name) ?: cpGetMemLoc(name)

    private fun spGetMemLoc(name: Name) = (sp.getDecl(name.qualify(`package`)) as? DeclT.Value)
            ?.let { MemLoc.Global(name) }

    private fun cpGetMemLoc(name: Name) = cp.getClass("\$Get${name.capitalize()}".name.qualify(`package`))
            ?.let { MemLoc.Global(name) }
}
