package io.quartz.analyzer

import io.quartz.tree.*
import kategory.Either
import kategory.ValidatedNel
import kategory.monadError

/** Class representing all expected errors */
open class CompilerError(message: String) : Throwable(message)

class UnknownVariable(name: QualifiedName) : CompilerError("Could not find variable $name") {
    constructor(name: LocatableName) : this(name.qualifiedName)
    constructor(name: Name) : this(name.qualifiedLocal)
}

typealias EitherE<T> = Either<CompilerError, T>
typealias ValidatedE<T> = ValidatedNel<CompilerError, T>

fun Either.Companion.monadErrorE() = Either.monadError<CompilerError>()
