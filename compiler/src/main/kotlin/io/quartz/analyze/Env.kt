package io.quartz.analyze

import io.quartz.analyze.type.SchemeK
import io.quartz.err.Result
import io.quartz.err.err
import io.quartz.foldString
import io.quartz.tree.QualifiedName
import io.quartz.tree.ir.ExprI
import io.quartz.tree.name

interface Env {
    fun getType(name: QualifiedName): Result<TypeInfo>?
    fun getId(name: QualifiedName): Result<IdInfo>?
}

fun Env.getTypeOrErr(name: QualifiedName) = getType(name) ?: err { "could not find type $name" }
fun Env.getVarOrErr(name: QualifiedName) = getId(name) ?: err { "could not find var $name" }

data class IdInfo(val scheme: SchemeK, val loc: ExprI.Var.Loc)

data class TypeInfo(val scheme: SchemeK)

fun Env.mapTypes(map: (QualifiedName, Result<TypeInfo>?) -> Result<TypeInfo>?) = object : Env by this {
    override fun getType(name: QualifiedName) = map(name, this@mapTypes.getType(name))
    override fun toString() = "${this@mapTypes} mapType"
}

fun Env.mapIds(map: (QualifiedName, Result<IdInfo>?) -> Result<IdInfo>?) = object : Env by this {
    override fun getId(name: QualifiedName) = map(name, this@mapIds.getId(name))
    override fun toString() = "${this@mapIds} mapIds"
}

fun Env.withType(name: QualifiedName, typeInfo: () -> Result<TypeInfo>?) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getType(name: QualifiedName) = if (name == _name) typeInfo() else this@withType.getType(name)
        override fun toString() = "${this@withType} withType ${name to typeInfo()?.foldString()}"
    }
}

fun Env.withId(name: QualifiedName, idInfo: () -> Result<IdInfo>?) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getId(name: QualifiedName) = if (name == _name) idInfo() else this@withId.getId(name)
        override fun toString() = "${this@withId} withId ${name to idInfo()?.foldString()}"
    }
}

private var fresh = 0
fun fresh() = "$${fresh++}".name
