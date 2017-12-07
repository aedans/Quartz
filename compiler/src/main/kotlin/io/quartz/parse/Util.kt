package io.quartz.parse

import io.github.aedans.parsek.*
import io.github.aedans.parsek.tokenizer.Token
import io.quartz.err.Result
import io.quartz.tree.util.Location
import kategory.*

fun Token<*>.location(string: String) = Location(string, row, col)

infix fun <A, B> Parser<A, B>.mapErr(map: (ParseResult.Failure<A>) -> ParseResult<A, B>) =
        io.github.aedans.parsek.mapErrParser(this) { map(it) }

fun <A, B> ParseResult<A, B>.toResult(): Result<Tuple2<Sequence<A>, B>> = when (this) {
    is ParseResult.Success -> (rest toT result).right()
    is ParseResult.Failure -> io.quartz.err.err { err }
}
