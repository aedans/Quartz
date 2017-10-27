package io.quartz.parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser
import io.quartz.tree.ast.*

/**
 * @author Aedan Smith
 */

val QuartzGrammar<*>.exprT: Parser<ExprT> get() = parser { lambdaExprT } or
        parser { ifExprT } or
        parser { applyExprT }

val QuartzGrammar<*>.ifExprT: Parser<ExprT> get() = IF and
        parser { exprT } and
        THEN and
        parser { exprT } and
        ELSE and
        parser { exprT } use {
    ExprT.If(t1.location(file), t2, t4, t6)
}

val QuartzGrammar<*>.lambdaExprT: Parser<ExprT.Lambda> get() = skip(BACKSLASH) and
        VAR and
        skip(ARROW) and
        exprT use {
    ExprT.Lambda(t1.location(file), t1.text, t2)
}

val QuartzGrammar<*>.applyExprT: Parser<ExprT> get() = parser { atomicExprT } and oneOrMore(parser { atomicExprT }) use {
    t2.fold(t1) { a, b -> ExprT.Apply(b.location, a, b) }
} or parser { atomicExprT }

val QuartzGrammar<*>.atomicExprT: Parser<ExprT> get() = parser { unitExprT } or
        parser { parenthesizedExprT } or
        parser { booleanExprT } or
        parser { varExprT }

val QuartzGrammar<*>.unitExprT: Parser<ExprT> get() = O_PAREN and skip(C_PAREN) use {
    ExprT.Unit(location(file))
}

val QuartzGrammar<*>.parenthesizedExprT: Parser<ExprT> get() = skip(O_PAREN) and
        parser { exprT } and
        skip(C_PAREN)

val QuartzGrammar<*>.varExprT: Parser<ExprT> get() = parser { VAR } use {
    ExprT.Var(location(file), text)
}

val QuartzGrammar<*>.booleanExprT: Parser<ExprT> get() = TRUE or FALSE use {
    ExprT.Bool(location(file), text.toBoolean())
}
