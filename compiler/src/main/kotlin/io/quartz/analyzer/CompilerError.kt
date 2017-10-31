package io.quartz.analyzer

import io.quartz.tree.QualifiedName
import kategory.Either
import kategory.monadError

/** Class representing all expected errors */
open class CompilerError(message: String) : Throwable(message)

class UnknownVariable(name: QualifiedName) : CompilerError("Could not find variable $name")

class UnknownType(name: QualifiedName) : CompilerError("Could not find type of $name")

class InvalidMemoryLocation(name: QualifiedName) : CompilerError("Invalid memory location of $name")

typealias EitherE<T> = Either<CompilerError, T>

@Suppress("unused")
fun Either.Companion.monadErrorE() = Either.monadError<CompilerError>()
