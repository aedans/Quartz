package io.quartz.analyzer

import io.quartz.analyzer.type.SchemeK
import io.quartz.tree.Name
import io.quartz.tree.QualifiedName
import io.quartz.tree.Qualifier
import io.quartz.tree.name

/**
 * @author Aedan Smith
 */

interface Env {
    val `package`: Qualifier
    fun getType(name: QualifiedName): SchemeK?
    fun getVar(name: Name): SchemeK?
    fun getMemLoc(name: Name): MemLoc?
}

fun Env.withPackage(`package`: Qualifier) = object : Env by this {
    override val `package` = `package`
}

sealed class MemLoc {
    data class Arg(val index: Int) : MemLoc()
    data class Global(val name: Name) : MemLoc()
    data class Field(val name: Name) : MemLoc()
}

fun Env.mapTypes(map: (SchemeK) -> SchemeK) = object : Env by this {
    override fun getType(name: QualifiedName) = this@mapTypes.getType(name)?.let(map)
}

fun Env.withTypes(list: List<Pair<QualifiedName, SchemeK>>) = list.fold(this) { e, (a, b) -> e.withType(a, b) }
fun Env.withType(name: QualifiedName, scheme: SchemeK) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getType(name: QualifiedName) = if (name == _name) scheme else this@withType.getType(name)
    }
}

fun Env.withVars(list: List<Pair<Name, SchemeK>>) = list.fold(this) { e, (a, b) -> e.withVar(a, b) }
fun Env.withVar(name: Name, scheme: SchemeK) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getVar(name: Name) = if (name == _name) scheme else this@withVar.getVar(name)
    }
}

fun Env.withMemLocs(list: List<Pair<Name, MemLoc>>) = list.fold(this) { e, (a, b) -> e.withMemLoc(a, b) }
fun Env.withMemLoc(name: Name, memLoc: MemLoc) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getMemLoc(name: Name) = if (name == _name) memLoc else this@withMemLoc.getMemLoc(name)
    }
}

var fresh = 0
fun fresh() = "$${fresh++}".name
