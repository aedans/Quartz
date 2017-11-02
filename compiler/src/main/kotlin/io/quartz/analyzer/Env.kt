package io.quartz.analyzer

import io.quartz.analyzer.type.SchemeK
import io.quartz.tree.Name
import io.quartz.tree.QualifiedName
import io.quartz.tree.name
import kategory.flatMap
import kategory.left

interface Env {
    fun getType(name: QualifiedName): EitherE<TypeInfo>
    fun getVar(name: QualifiedName): EitherE<VarInfo>
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

val emptyEnv = object : Env {
    override fun getType(name: QualifiedName) = UnknownType(name).left()
    override fun getVar(name: QualifiedName) = UnknownVar(name).left()
}

infix fun Env.compose(env: Env) = object : Env {
    override fun getType(name: QualifiedName) = this@compose.getType(name).flatMap { env.getType(name) }
    override fun getVar(name: QualifiedName) = this@compose.getVar(name).flatMap { env.getVar(name) }
}

fun Env.mapTypes(map: (QualifiedName, EitherE<TypeInfo>) -> EitherE<TypeInfo>) = object : Env by this {
    override fun getType(name: QualifiedName) = map(name, this@mapTypes.getType(name))
}

fun Env.mapVars(map: (QualifiedName, EitherE<VarInfo>) -> EitherE<VarInfo>) = object : Env by this {
    override fun getVar(name: QualifiedName) = map(name, this@mapVars.getVar(name))
}

fun Env.withType(name: QualifiedName, scheme: EitherE<TypeInfo>) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getType(name: QualifiedName) = if (name == _name) scheme else this@withType.getType(name)
    }
}

fun Env.withVar(name: QualifiedName, varInfo: EitherE<VarInfo>) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getVar(name: QualifiedName) = if (name == _name) varInfo else this@withVar.getVar(name)
    }
}

fun Env.withVarLoc(name: QualifiedName, varLoc: VarLoc) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getVar(name: QualifiedName) = if (name == _name)
            this@withVarLoc.getVar(name).map { it.copy(varLoc = varLoc) }
        else
            this@withVarLoc.getVar(name)
    }
}

private var fresh = 0
fun fresh() = "$${fresh++}".name
