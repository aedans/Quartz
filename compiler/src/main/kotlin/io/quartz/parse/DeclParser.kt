package io.quartz.parse

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser
import io.quartz.tree.ast.DeclT
import io.quartz.tree.name

val QuartzGrammar<*>.declT: Parser<DeclT> get() = parser { interfaceDeclT } or
        parser { valueDeclT }

val QuartzGrammar<*>.interfaceDeclT: Parser<DeclT.Interface> get() = skip(INTERFACE) and
        ID and
        zeroOrMore(parser { constraintT }) and
        skip(O_BRACKET) and
        zeroOrMore(parser { abstractT }) and
        skip(C_BRACKET) use {
    DeclT.Interface(t1.location, t1.text.name, t2, t3)
}

val QuartzGrammar<*>.abstractT: Parser<DeclT.Interface.Abstract> get() = skip(DEF) and
        ID and
        skip(EXTENDS) and
        parser { schemeT } use {
    DeclT.Interface.Abstract(t1.text.name, t1.location, t2)
}

val QuartzGrammar<*>.valueDeclT: Parser<DeclT.Value> get() = skip(DEF) and
        ID and
        optional(skip(EXTENDS) and parser { schemeT }) and
        skip(EQ) and
        parser { exprT } use {
    DeclT.Value(t1.location, t1.text.name, t2, t3)
}
