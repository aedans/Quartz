package io.quartz.analyzer

import io.quartz.tree.QualifiedName
import kategory.Either
import kategory.monadError

/** Class representing all expected compiler errors */
open class CompilerError(message: String) : Throwable(message) {
    override fun equals(other: Any?) = other is CompilerError && other.message == message
    override fun hashCode() = message!!.hashCode()
    override fun toString() = "CompilerError(\"$message\")"
}

class UnknownVar(name: QualifiedName) : CompilerError("Could not find var $name")

class UnknownType(name: QualifiedName) : CompilerError("Could not find type $name")

class UnableToType(name: QualifiedName) : CompilerError("Could not infer type for $name")

typealias EitherE<T> = Either<CompilerError, T>

@Suppress("unused")
fun Either.Companion.monadErrorE() = Either.monadError<CompilerError>()
