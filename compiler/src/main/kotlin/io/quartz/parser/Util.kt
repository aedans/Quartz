package io.quartz.parser

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import io.quartz.tree.Location

fun TokenMatch.location(grammar: QuartzGrammar<*>) = Location(grammar.uri, column, position)
