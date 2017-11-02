package io.quartz.interop

import io.quartz.analyzer.*
import io.quartz.analyzer.type.scheme
import io.quartz.analyzer.type.typeK
import io.quartz.tree.QualifiedName
import io.quartz.tree.unqualified
import kategory.binding
import kategory.ev

data class ClassPathEnv(private val cp: ClassPath) : Env {
    override fun getType(name: QualifiedName) = cp.getType(name)
    override fun getVar(name: QualifiedName) = cp.getVar(name)
}

fun ClassPath.getType(name: QualifiedName) = monadErrorE().binding {
    val scheme = getTypeScheme(name).bind()
    yields(TypeInfo(scheme))
}.ev()

fun ClassPath.getTypeScheme(name: QualifiedName) = getClass(name).map { it.typeK.scheme }

fun ClassPath.getVar(name: QualifiedName) = monadErrorE().binding {
    val scheme = getVarScheme(name).bind()
    val varLoc = getVarLoc(name).bind()
    yields(VarInfo(scheme, varLoc))
}.ev()

fun ClassPath.getVarScheme(name: QualifiedName) =
        getClass(name.varClassName()).map {
            it.getMethod(name.unqualified.varGetterName().string)
                    .returnType
                    .typeK
                    .scheme
        }

fun ClassPath.getVarLoc(name: QualifiedName) = getClass(name.varClassName())
        .map { VarLoc.Global(name) }
