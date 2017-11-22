package io.quartz.analyze

import io.quartz.analyze.type.SchemeK
import io.quartz.err.Result
import io.quartz.err.err
import io.quartz.foldString
import io.quartz.tree.Name
import io.quartz.tree.QualifiedName
import io.quartz.tree.name

interface Env {
    fun getType(name: QualifiedName): Result<TypeInfo>?
    fun getVar(name: QualifiedName): Result<VarInfo>?
}

fun Env.getTypeOrErr(name: QualifiedName) = getType(name) ?: err { "could not find type $name" }
fun Env.getVarOrErr(name: QualifiedName) = getVar(name) ?: err { "could not find var $name" }

/** ADT representing where an identifier is located */
sealed class VarLoc {
    data class Arg(val index: Int) : VarLoc()
    data class Global(val name: QualifiedName) : VarLoc()
    data class Field(val name: Name) : VarLoc()
}

data class VarInfo(
        val scheme: SchemeK,
        val varLoc: VarLoc
)

data class TypeInfo(
        val scheme: SchemeK
)

fun Env.mapTypes(map: (QualifiedName, Result<TypeInfo>?) -> Result<TypeInfo>?) = object : Env by this {
    override fun getType(name: QualifiedName) = map(name, this@mapTypes.getType(name))
    override fun toString() = "${this@mapTypes} mapType"
}

fun Env.mapVars(map: (QualifiedName, Result<VarInfo>?) -> Result<VarInfo>?) = object : Env by this {
    override fun getVar(name: QualifiedName) = map(name, this@mapVars.getVar(name))
    override fun toString() = "${this@mapVars} mapVars"
}

fun Env.withType(name: QualifiedName, typeInfo: () -> Result<TypeInfo>?) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getType(name: QualifiedName) = if (name == _name) typeInfo() else this@withType.getType(name)
        override fun toString() = "${this@withType} withType ${name to typeInfo()?.foldString()}"
    }
}

fun Env.withVar(name: QualifiedName, varInfo: () -> Result<VarInfo>?) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getVar(name: QualifiedName) = if (name == _name) varInfo() else this@withVar.getVar(name)
        override fun toString() = "${this@withVar} withVar ${name to varInfo()?.foldString()}"
    }
}

private var fresh = 0
fun fresh() = "$${fresh++}".name
