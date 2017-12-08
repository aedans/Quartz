package io.quartz.parse

import io.github.aedans.parsek.dsl.*
import io.github.aedans.parsek.optional
import io.quartz.nil
import io.quartz.tree.ast.*
import io.quartz.tree.util.*

val String.schemeP: QuartzParser<SchemeT> get() = list(parser { forallP }) then
        optional(list(parser { constraintP }) then skip(TokenType.FAT_ARROW)) then
        parser { typeP } map { (it, type) ->
    val (foralls, constraints) = it
    SchemeT(foralls.toSet(), constraints ?: nil, type)
}

val String.forallP: QuartzParser<Name> get() = TokenType.ID then skip(TokenType.DOT) map { it.text.name }

val String.constraintP: QuartzParser<ConstraintT> get() = skip(TokenType.O_PAREN) then
        parser { TokenType.ID } then
        TokenType.ID then
        skip(TokenType.C_PAREN) map {
    ConstraintT(it.first.text.name, it.second.text.name)
}

val String.typeP: QuartzParser<TypeT> get() = parser { functionTypeP }

val String.functionTypeP: QuartzParser<TypeT> get() = parser { applyTypeP } then
        optional(skip(TokenType.ARROW) then parser { functionTypeP }) map { (t1, t2) ->
    t2?.let { TypeT.function(t1, it) } ?: t1
}

val String.applyTypeP: QuartzParser<TypeT> get() = parser { atomicTypeP } then
        optional(parser { applyTypeP }) map { (t1, t2) ->
    t2?.let { t1.apply(it) } ?: t1
}

val String.atomicTypeP: QuartzParser<TypeT> get() = parser { idTypeP } or
        parser { unitTypeP } or
        parser { parenthesizedTypeP }

val String.idTypeP: QuartzParser<TypeT> get() = TokenType.ID map {
    TypeT.Id(it.location(this), it.text.name.qualifiedLocal)
}

val String.unitTypeP: QuartzParser<TypeT> get() = TokenType.O_PAREN then skip(TokenType.C_PAREN) map {
    TypeT.unit.copy(location = it.location(this))
}

val String.parenthesizedTypeP: QuartzParser<TypeT> get() =
    skip(TokenType.O_PAREN) then parser { typeP } then skip(TokenType.C_PAREN)
