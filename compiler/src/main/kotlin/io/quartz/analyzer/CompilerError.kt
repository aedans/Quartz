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

class UnknownVar(name: QualifiedName) : CompilerError("could not find var $name")

class UnknownType(name: QualifiedName) : CompilerError("could not find type $name")

class UnknownClass(name: QualifiedName) : CompilerError("could not find class $name")

infix fun CompilerError.and(e: CompilerError) = CompilerError("$message and ${e.message}")

typealias Err<T> = Either<CompilerError, T>

fun monadErrorE() = Either.monadError<CompilerError>()
