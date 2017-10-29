package io.quartz.analyzer

import io.quartz.tree.*
import kategory.*

/** Class representing all expected errors */
open class CompilerError(message: String) : Throwable(message)

class UnknownVariable(name: QualifiedName) : CompilerError("Could not find variable $name") {
    constructor(name: LocatableName) : this(name.qualifiedName)
    constructor(name: Name) : this(name.qualifiedLocal)
}

class UnknownTypeOf(name: QualifiedName) : CompilerError("Could not infer type of $name") {
    constructor(name: LocatableName) : this(name.qualifiedName)
    constructor(name: Name) : this(name.qualifiedLocal)
}

typealias EitherE<T> = Either<CompilerError, T>
typealias ValidatedE<T> = ValidatedNel<CompilerError, T>

@Suppress("unused")
fun Either.Companion.monadErrorE() = Either.monadError<CompilerError>()

fun <A, B> List<A>.validIfEmpty(b: B): ValidatedNel<A, B> = if (isNotEmpty())
    Validated.Invalid(NonEmptyList.fromListUnsafe(this))
else
    b.validNel()

fun <A, B> List<Either<A, B>>.validated(): ValidatedNel<A, List<B>> =
        fold(emptyList<A>() to emptyList<B>()) { (a, b), e ->
            e.fold(
                    { (a + it) to b },
                    { a to (b + it) }
            )
        }.let { (a, b) -> a.validIfEmpty(b) }

fun <A, B> List<ValidatedNel<A, B>>.flatten(): ValidatedNel<A, List<B>> =
        this.fold(emptyList<A>() to emptyList<B>()) { (a, b), it ->
            it.fold(
                    { (a + it.all) to b },
                    { a to (b + it) }
            )
        }.let { (a, b) -> a.validIfEmpty(b) }
