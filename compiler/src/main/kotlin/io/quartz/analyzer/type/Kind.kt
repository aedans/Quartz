package io.quartz.analyzer.type

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser
import io.quartz.tree.nil
import java.lang.reflect.TypeVariable

/**
 * @author Aedan Smith
 */

sealed class Kind {
    object Type : Kind() {
        override fun toString() = "*"
    }

    data class Arrow(val k1: Kind, val k2: Kind) : Kind() {
        override fun toString() = "($k1 -> $k2)"
    }
}

fun Kind.apply(kind: Kind) = when {
    this !is Kind.Arrow || k1 != kind -> throw Exception("Cannot t2 $kind to $this")
    else -> k2
}

val Class<*>.kind get() = this.typeParameters.toList().kind

val List<TypeVariable<out Class<out Any>>>.kind: Kind get() = when (this) {
    nil -> Kind.Type
    else -> Kind.Arrow(first().kind, drop(1).kind)
}

val TypeVariable<out Class<out Any>>.kind get() = getAnnotation(quartz.lang.Kind::class.java)?.value?.kind ?: Kind.Type

val String.kind get() = KindGrammar.parseToEnd(this)

object KindGrammar : Grammar<Kind>() {
    val ws by token("\\s+", ignore = true)
    val star by token("\\*")
    val arrow by token("\\-\\>")
    val oParen by token("\\(")
    val cParen by token("\\)")

    override val rootParser: Parser<Kind> = parser { kindParser }

    val kindParser: Parser<Kind> = parser { arrowKindParser } or
            parser { atomicKindParser }

    val atomicKindParser: Parser<Kind> =  parser { parenthesizedKind } or
            parser { typeParser }

    val parenthesizedKind: Parser<Kind> = skip(oParen) and parser { kindParser } and skip(cParen)

    val arrowKindParser: Parser<Kind.Arrow> = parser { atomicKindParser } and
            skip(arrow) and
            parser { kindParser } use {
        Kind.Arrow(t1, t2)
    }

    val typeParser: Parser<Kind.Type> = star use { Kind.Type }
}
