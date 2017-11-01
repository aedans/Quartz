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
import kategory.*

data class GlobalEnv(
        private val cp: ClassPath,
        private val sp: SourcePath,
        override val `package`: Qualifier
) : Env {
    override fun getType(name: QualifiedName) = Either.monadErrorE().binding {
        val scheme = getTypeScheme(name).bind()
        yields(TypeInfo(scheme))
    }.ev()

    private fun getTypeScheme(name: QualifiedName) = spGetTypeScheme(name)?.right() ?:
            cpGetTypeScheme(name)?.right() ?:
            UnknownType(name).left()

    private fun spGetTypeScheme(name: QualifiedName) = (sp.getDecl(name) as? DeclT.Class)
            ?.schemeK(name.qualifier)

    private fun cpGetTypeScheme(name: QualifiedName) = cp.getClass(name)?.typeK?.scheme

    override fun getVar(name: QualifiedName) = Either.monadErrorE().binding {
        val scheme = getVarType(name).bind()
        val varLoc = getVarLoc(name).bind()
        yields(VarInfo(scheme, varLoc))
    }.ev()

    private fun getVarType(name: QualifiedName) = spGetVarType(name)
            ?: cpGetVarType(name)?.right()
            ?: UnknownVariable(name).left()

    private fun spGetVarType(name: QualifiedName) = (sp.getDecl(name) as? DeclT.Value)
            ?.schemeK(this)
            ?.bimap(
                    { UnableToType(name) },
                    ::identity
            )

    private fun cpGetVarType(name: QualifiedName) = cp.getClass("\$Get${name.string.capitalize()}".name.qualify(name.qualifier))
            ?.getMethod("get${name.string.capitalize()}")
            ?.returnType
            ?.typeK
            ?.scheme

    private fun getVarLoc(name: QualifiedName) = spGetMemLoc(name)?.right()
            ?: cpGetMemLoc(name)?.right()
            ?: UnknownVariable(name).left()

    private fun spGetMemLoc(name: QualifiedName) = (sp.getDecl(name) as? DeclT.Value)
            ?.let { VarLoc.Global(name) }

    private fun cpGetMemLoc(name: QualifiedName) = cp.getClass("\$Get${name.string.capitalize()}".name.qualify(name.qualifier))
            ?.let { VarLoc.Global(name) }
}
