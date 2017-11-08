package io.quartz.parse

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser
import io.quartz.tree.ast.ConstraintT
import io.quartz.tree.ast.SchemeT
import io.quartz.tree.ast.TypeT
import io.quartz.tree.ast.apply
import io.quartz.tree.name

val QuartzGrammar<*>.schemeT: Parser<SchemeT> get() = zeroOrMore(constraint and skip(FAT_ARROW)) and parser { typeT } use {
    SchemeT(t1, t2)
}

val QuartzGrammar<*>.constraint: Parser<ConstraintT> get() = parser { atomicTypeT } and ID use {
    ConstraintT(t1, t2.text.name)
} or (ID use { ConstraintT(TypeT.any, text.name) })

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
    TypeT.Id(location, text.name)
}

val QuartzGrammar<*>.unitTypeT: Parser<TypeT> get() = O_PAREN and C_PAREN use { TypeT.unit }

val QuartzGrammar<*>.parenthesizedTypeT: Parser<TypeT> get() = skip(O_PAREN) and parser { typeT } and skip(C_PAREN)
