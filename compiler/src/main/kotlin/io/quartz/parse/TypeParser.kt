package io.quartz.parse

import io.github.aedans.parsek.dsl.*
import io.github.aedans.parsek.optional
import io.quartz.nil
import io.quartz.tree.ast.ConstraintT
import io.quartz.tree.ast.SchemeT
import io.quartz.tree.ast.TypeT
import io.quartz.tree.ast.apply
import io.quartz.tree.name

val String.schemeP: QuartzParser<SchemeT> get() =
    optional(list(parser { constraintP }) then skip(TokenType.FAT_ARROW)) then
        parser { typeP } map {
    SchemeT(it.first ?: nil, it.second)
}

val String.constraintP: QuartzParser<ConstraintT> get() = skip(TokenType.O_PAREN) then
        parser { atomicTypeP } then
        TokenType.ID then
        skip(TokenType.C_PAREN) map {
    ConstraintT(it.first, it.second.text.name)
} or (TokenType.ID map { ConstraintT(null, it.text.name) })

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
    TypeT.Id(it.location(this), it.text.name)
}

val String.unitTypeP: QuartzParser<TypeT> get() = TokenType.O_PAREN then skip(TokenType.C_PAREN) map {
    TypeT.unit.copy(location = it.location(this))
}

val String.parenthesizedTypeP: QuartzParser<TypeT> get() =
    skip(TokenType.O_PAREN) then parser { typeP } then skip(TokenType.C_PAREN)
