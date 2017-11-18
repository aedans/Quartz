package io.quartz.parse

import io.github.aedans.parsek.dsl.*
import io.github.aedans.parsek.optional
import io.quartz.tree.ast.ExprT
import io.quartz.tree.name
import io.quartz.tree.qualifiedLocal
import io.quartz.tree.qualifiedName
import io.quartz.tup

val String.exprP: QuartzParser<ExprT> get() = parser { lambdaExprP } or
        parser { ifExprP } or
        parser { castExprP }

val String.lambdaExprP: QuartzParser<ExprT> get() = skip(TokenType.BACKSLASH) then
        TokenType.ID then
        skip(TokenType.ARROW) then
        parser { exprP } map { (t1, t2) ->
    ExprT.Lambda(t1.location(this), t1.text.name, t2)
}

val String.ifExprP: QuartzParser<ExprT> get() = TokenType.IF then
        parser { exprP } then
        skip(TokenType.THEN) then
        parser { exprP } then
        skip(TokenType.ELSE) then
        parser { exprP } map {
    val (t1, t2, t3, t4) = it.tup()
    ExprT.If(t1.location(this), t2, t3, t4)
}

val String.castExprP: QuartzParser<ExprT> get() = parser { applyExprP } then
        optional(skip(TokenType.EXTENDS) then parser { typeP }) map { (t1, t2) ->
    t2?.let { ExprT.Cast(t1.location, t1, it) } ?: t1
}

val String.applyExprP: QuartzParser<ExprT> get() = parser { atomicExprP } then
        optional(parser { applyExprP }) map { (t1, t2) ->
    t2?.let { ExprT.Apply(t1.location, t1, it) } ?: t1
}

val String.atomicExprP: QuartzParser<ExprT> get() = parser { unitExprP } or
        parser { parenthesizedExprP } or
        parser { booleanExprP } or
        parser { idExprP }

val String.unitExprP: QuartzParser<ExprT> get() =
    TokenType.O_PAREN then skip(TokenType.C_PAREN) map { ExprT.Id(it.location(this), "quartz.lang.unit".qualifiedName) }

val String.parenthesizedExprP: QuartzParser<ExprT> get() =
    skip(TokenType.O_PAREN) then parser { exprP } then skip(TokenType.C_PAREN)

val String.idExprP: QuartzParser<ExprT> get() =
    TokenType.ID map { ExprT.Id(it.location(this), it.text.name.qualifiedLocal) }

val String.booleanExprP: QuartzParser<ExprT> get() = TokenType.TRUE or TokenType.FALSE map {
    ExprT.Id(it.location(this), "quartz.lang.${it.text}".qualifiedName)
}
