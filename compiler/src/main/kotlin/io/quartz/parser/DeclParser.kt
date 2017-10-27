package io.quartz.parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser
import io.quartz.tree.ast.DeclT

/**
 * @author Aedan Smith
 */

val QuartzGrammar<*>.declT: Parser<DeclT> get() = parser { classDeclT } or
        parser { valueDeclT }

val QuartzGrammar<*>.classDeclT: Parser<DeclT.Class> get() = skip(DEF) and
        CONST and
        skip(O_BRACKET) and
        zeroOrMore(parser { declT }) and
        skip(C_BRACKET) use {
    DeclT.Class(t1.text, t1.location(file), t2)
}

val QuartzGrammar<*>.valueDeclT: Parser<DeclT.Value> get() = skip(DEF) and
        VAR and
        optional(skip(EXTENDS) and parser { typeT }) and
        skip(EQ) and
        parser { exprT } use {
    DeclT.Value(t1.text, t1.location(file), t2, t3)
}
