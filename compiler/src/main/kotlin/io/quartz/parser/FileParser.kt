package io.quartz.parser

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser
import io.quartz.tree.ast.FileT

/**
 * @author Aedan Smith
 */

val QuartzGrammar<*>.fileT: Parser<FileT> get() = zeroOrMore(parser { import }) and zeroOrMore(parser { declT }) use {
    FileT(t1, t2)
}
