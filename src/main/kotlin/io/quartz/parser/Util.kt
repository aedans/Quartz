package io.quartz.parser

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import io.quartz.tree.Location

/**
 * @author Aedan Smith
 */

fun TokenMatch.location(file: String) = Location(file, column, position)
