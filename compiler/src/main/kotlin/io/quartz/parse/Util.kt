package io.quartz.parse

import io.github.aedans.parsek.*
import io.github.aedans.parsek.tokenizer.Token
import io.quartz.tree.util.Location

fun Token<*>.location(string: String) = Location(string, row, col)

infix fun <A, B> Parser<A, B>.mapErr(map: (ParseResult.Failure<A>) -> ParseResult<A, B>) =
        io.github.aedans.parsek.mapErrParser(this) { map(it) }
