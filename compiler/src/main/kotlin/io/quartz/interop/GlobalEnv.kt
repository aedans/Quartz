package io.quartz.interop

import io.quartz.analyzer.*
import io.quartz.analyzer.type.scheme
import io.quartz.analyzer.type.schemeK
import io.quartz.analyzer.type.typeK
import io.quartz.tree.QualifiedName
import io.quartz.tree.Qualifier
import io.quartz.tree.ast.DeclT
import io.quartz.tree.name
import io.quartz.tree.qualify
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

    private fun spGetType(name: QualifiedName) = (sp.getDecl(name) as? DeclT.Class)
            ?.schemeK(name.qualifier)

    private fun cpGetType(name: QualifiedName) = cp.getClass(name)?.typeK?.scheme

    override fun getVar(name: QualifiedName) = spGetVar(name)
            ?: cpGetVar(name)?.right()
            ?: UnknownVariable(name).left()

    private fun spGetVar(name: QualifiedName) = (sp.getDecl(name) as? DeclT.Value)
            ?.schemeK(this)
            ?.bimap(
                    { UnknownType(name) },
                    ::identity
            )

    private fun cpGetVar(name: QualifiedName) = cp.getClass("\$Get${name.string.capitalize()}".name.qualify(name.qualifier))
            ?.getMethod("get${name.string.capitalize()}")
            ?.returnType
            ?.typeK
            ?.scheme

    override fun getMemLoc(name: QualifiedName) = spGetMemLoc(name)?.right()
            ?: cpGetMemLoc(name)?.right()
            ?: UnknownVariable(name).left()

    private fun spGetMemLoc(name: QualifiedName) = (sp.getDecl(name) as? DeclT.Value)
            ?.let { MemLoc.Global(name) }

    private fun cpGetMemLoc(name: QualifiedName) = cp.getClass("\$Get${name.string.capitalize()}".name.qualify(name.qualifier))
            ?.let { MemLoc.Global(name) }
}
