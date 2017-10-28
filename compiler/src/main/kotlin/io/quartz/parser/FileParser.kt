package io.quartz.parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser
import io.quartz.tree.Qualifier
import io.quartz.tree.ast.FileT
import io.quartz.tree.nil

val QuartzGrammar<*>.fileT: Parser<FileT> get() = optional(parser { packageT }) and
        zeroOrMore(parser { importT }) and
        zeroOrMore(parser { declT }) use {
    FileT(t1 ?: nil, t2, t3)
}

val QuartzGrammar<*>.packageT: Parser<Qualifier> get() = skip(PACKAGE) and qualifier

val QuartzGrammar<*>.importT: Parser<Qualifier> get() = skip(IMPORT) and qualifier

val QuartzGrammar<*>.qualifier: Parser<Qualifier> get() = zeroOrMore(VAR and DOT) and (VAR or CONST) use {
    t1.map { it.t1.text } + t2.text
}
