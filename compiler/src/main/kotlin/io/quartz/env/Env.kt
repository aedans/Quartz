package io.quartz.env

import io.quartz.err.*
import io.quartz.foldString
import io.quartz.tree.util.*

interface Env {
    fun getType(name: QualifiedName): Result<TypeInfo>?
    fun getVar(name: QualifiedName): Result<VarInfo>?
    fun getInstances(name: QualifiedName): Sequence<Result<InstanceInfo>>
}

fun Env.getTypeOrErr(name: QualifiedName) = getType(name) ?: err { "could not find instance $name" }
fun Env.getVarOrErr(name: QualifiedName) = getVar(name) ?: err { "could not find var $name" }

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

private var acc = 0
fun fresh() = "_${acc++}".name
