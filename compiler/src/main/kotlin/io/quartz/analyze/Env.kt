package io.quartz.analyze

import io.quartz.analyze.type.SchemeK
import io.quartz.analyze.type.TypeK
import io.quartz.err.Err
import io.quartz.err.err
import io.quartz.foldString
import io.quartz.tree.Name
import io.quartz.tree.QualifiedName
import io.quartz.tree.name
import kategory.right

interface Env {
    fun getType(name: QualifiedName): Err<TypeInfo>
    fun getVar(name: QualifiedName): Err<VarInfo>
    fun getInstance(type: TypeK, instance: TypeK): Err<InstanceInfo>
}

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

data class InstanceInfo(
        val varLoc: VarLoc
)

val emptyEnv = object : Env {
    override fun getType(name: QualifiedName) = err { "could not find type $name" }
    override fun getVar(name: QualifiedName) = err { "could not find variable $name" }
    override fun getInstance(type: TypeK, instance: TypeK) = err { "could not find instance of $type for $instance" }
    override fun toString() = "EmptyEnv"
}

infix fun Env.compose(env: () -> Env) = object : Env {
    override fun getType(name: QualifiedName) = env().getType(name).fold(
            { this@compose.getType(name) },
            { it.right() }
    )

    override fun getVar(name: QualifiedName) = env().getVar(name).fold(
            { this@compose.getVar(name) },
            { it.right() }
    )

    override fun getInstance(type: TypeK, instance: TypeK) = env().getInstance(type, instance).fold(
            { this@compose.getInstance(type, instance) },
            { it.right() }
    )

    override fun toString() = "${this@compose} compose $env"
}

fun Env.mapTypes(map: (QualifiedName, Err<TypeInfo>) -> Err<TypeInfo>) = object : Env by this {
    override fun getType(name: QualifiedName) = map(name, this@mapTypes.getType(name))
    override fun toString() = "${this@mapTypes} mapType"
}

fun Env.mapVars(map: (QualifiedName, Err<VarInfo>) -> Err<VarInfo>) = object : Env by this {
    override fun getVar(name: QualifiedName) = map(name, this@mapVars.getVar(name))
    override fun toString() = "${this@mapVars} mapVars"
}

fun Env.withType(name: QualifiedName, typeInfo: () -> Err<TypeInfo>) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getType(name: QualifiedName) = if (name == _name) typeInfo() else this@withType.getType(name)
        override fun toString() = "${this@withType} withType ${name to typeInfo().foldString()}"
    }
}

fun Env.withVar(name: QualifiedName, varInfo: () -> Err<VarInfo>) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getVar(name: QualifiedName) = if (name == _name) varInfo() else this@withVar.getVar(name)
        override fun toString() = "${this@withVar} withVar ${name to varInfo().foldString()}"
    }
}

private var fresh = 0
fun fresh() = "$${fresh++}".name
