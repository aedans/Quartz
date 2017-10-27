package io.quartz.parser

import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser
import io.quartz.tree.ast.ProgramT

/**
 * @author Aedan Smith
 */

val QuartzGrammar<*>.programT: Parser<ProgramT> get() = zeroOrMore(parser { declT })
