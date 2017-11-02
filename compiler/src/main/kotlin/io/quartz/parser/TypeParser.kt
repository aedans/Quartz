package io.quartz.parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser
import io.quartz.tree.ast.GenericT
import io.quartz.tree.ast.SchemeT
import io.quartz.tree.ast.TypeT
import io.quartz.tree.ast.apply
import io.quartz.tree.name
import io.quartz.nil

val QuartzGrammar<*>.schemeT: Parser<SchemeT> get() = optional(oneOrMore(ID) and skip(FAT_ARROW)) and parser { typeT } use {
    SchemeT(t1?.map { GenericT(it.text.name, TypeT.any) } ?: nil, t2)
}

val QuartzGrammar<*>.typeT: Parser<TypeT> get() = parser { functionTypeT }

val QuartzGrammar<*>.functionTypeT: Parser<TypeT> get() = parser { applyTypeT } and
        optional(skip(ARROW) and parser { functionTypeT }) use {
    t2?.let { TypeT.function(t1, it) } ?: t1
}

val QuartzGrammar<*>.applyTypeT: Parser<TypeT> get() = parser { atomicTypeT } and
        zeroOrMore(parser { atomicTypeT }) use {
    t2.fold(t1) { a, b -> a.apply(b) }
}

val QuartzGrammar<*>.atomicTypeT: Parser<TypeT> get() = parser { idType } or
        parser { unitTypeT } or
        parser { parenthesizedTypeT }

val QuartzGrammar<*>.idType: Parser<TypeT> get() = ID use {
    TypeT.Id(text.name)
}

val QuartzGrammar<*>.unitTypeT: Parser<TypeT> get() = O_PAREN and C_PAREN use { TypeT.unit }

val QuartzGrammar<*>.parenthesizedTypeT: Parser<TypeT> get() = skip(O_PAREN) and parser { typeT } and skip(C_PAREN)
