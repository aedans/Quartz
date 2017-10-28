package io.quartz.interop

import io.quartz.analyzer.*
import io.quartz.analyzer.type.schemeK
import io.quartz.tree.*
import io.quartz.tree.ast.DeclT
import kategory.identity
import kategory.left
import kategory.right

data class GlobalEnv(
        private val cp: ClassPath,
        private val sp: SourcePath,
        override val `package`: Qualifier
) : Env {
    override fun getType(name: QualifiedName) = spGetType(name)?.right()
            ?: cpGetType(name)?.right()
            ?: UnknownVariable(name).left()

    private fun spGetType(name: QualifiedName) =
            (sp.getDecl(name) as? DeclT.Class)?.schemeK(this)

    private fun cpGetType(name: QualifiedName) = cp.getClass(name)?.schemeK

    override fun getVar(name: Name) = spGetVar(name)
            ?: cpGetVar(name)?.right()
            ?: UnknownVariable(name).left()

    private fun spGetVar(name: Name) = (sp.getDecl(name.qualify(`package`)) as? DeclT.Value)
            ?.schemeK(this)
            ?.bimap(
                    { UnknownTypeOf(name) },
                    ::identity
            )

    private fun cpGetVar(name: Name) = cp.getClass("\$Get${name.capitalize()}".name.qualify(`package`))
            ?.getMethod("get${name.capitalize()}")
            ?.returnType
            ?.schemeK

    override fun getMemLoc(name: Name) = spGetMemLoc(name)?.right()
            ?: cpGetMemLoc(name)?.right()
            ?: UnknownVariable(name).left()

    private fun spGetMemLoc(name: Name) = (sp.getDecl(name.qualify(`package`)) as? DeclT.Value)
            ?.let { MemLoc.Global(name) }

    private fun cpGetMemLoc(name: Name) = cp.getClass("\$Get${name.capitalize()}".name.qualify(`package`))
            ?.let { MemLoc.Global(name) }
}
