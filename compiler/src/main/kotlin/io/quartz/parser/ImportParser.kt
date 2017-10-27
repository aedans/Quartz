package io.quartz.parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.parser

/**
 * @author Aedan Smith
 */

val QuartzGrammar<*>.import get() = skip(IMPORT) and parser { `package` } use { this }

val QuartzGrammar<*>.`package` get() = zeroOrMore(VAR and skip(DOT)) and
        (VAR or CONST) use {
    t1.map { it.text } + t2.text
}
