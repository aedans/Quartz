package io.quartz.parse

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser
import io.quartz.tree.ast.DeclT
import io.quartz.tree.name

val QuartzGrammar<*>.declT: Parser<DeclT> get() = parser { classDeclT } or
        parser { valueDeclT } or
        parser { abstractDeclT }

val QuartzGrammar<*>.classDeclT: Parser<DeclT.Class> get() = skip(DEF) and
        ID and
        parser { constraints } and
        skip(O_BRACKET) and
        zeroOrMore(parser { declT }) and
        skip(C_BRACKET) use {
    DeclT.Class(t1.text.name, t1.location, t2, t3)
}

val QuartzGrammar<*>.valueDeclT: Parser<DeclT.Value> get() = skip(DEF) and
        ID and
        optional(skip(EXTENDS) and parser { schemeT }) and
        skip(EQ) and
        parser { exprT } use {
    DeclT.Value(t1.text.name, t1.location, t2, t3)
}

val QuartzGrammar<*>.abstractDeclT: Parser<DeclT.Abstract> get() = skip(DEF) and
        ID and
        skip(EXTENDS) and
        parser { schemeT } use {
    DeclT.Abstract(t1.text.name, t1.location, t2)
}
