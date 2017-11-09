package io.quartz.interop

import io.quartz.analyze.*
import io.quartz.analyze.type.ConstraintK
import io.quartz.analyze.type.SchemeK
import io.quartz.analyze.type.TypeK
import io.quartz.err.errMonad
import io.quartz.tree.QualifiedName
import io.quartz.tree.name
import io.quartz.tree.qualifiedName
import io.quartz.tree.unqualified
import kategory.binding
import kategory.ev

data class ClassPathEnv(private val cp: ClassPath) : Env {
    override fun getType(name: QualifiedName) = cp.getType(name)
    override fun getVar(name: QualifiedName) = cp.getVar(name)
    override fun getInstance(type: TypeK, instance: TypeK) = emptyEnv.getInstance(type, instance) // TODO
    override fun toString() = "ClassPathEnv($cp)"
}

fun ClassPath.getType(name: QualifiedName) = errMonad().binding {
    val scheme = getTypeScheme(name).bind()
    yields(TypeInfo(scheme))
}.ev()

fun ClassPath.getTypeScheme(name: QualifiedName) = getClass(name).map { it.schemeK }

fun ClassPath.getVar(name: QualifiedName) = errMonad().binding {
    val scheme = getVarScheme(name).bind()
    val varLoc = getVarLoc(name).bind()
    yields(VarInfo(scheme, varLoc))
}.ev()

fun ClassPath.getVarScheme(name: QualifiedName) =
        getClass(name.varClassName()).map {
            it.getMethod(name.unqualified.varGetterName().string)
                    .returnType
                    .schemeK
        }

fun ClassPath.getVarLoc(name: QualifiedName) = getClass(name.varClassName())
        .map { VarLoc.Global(name) }

val Class<*>.schemeK: SchemeK get() = run {
    val generics = typeParameters.map { ConstraintK(null, it.name.name) }
    val typeK = TypeK.Const(qualifiedName)
    SchemeK(generics, typeK)
}
