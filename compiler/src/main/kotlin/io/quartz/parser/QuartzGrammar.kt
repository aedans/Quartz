package io.quartz.parser

import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.Parser
import java.io.InputStream
import java.util.*

@Suppress("PropertyName")
abstract class QuartzGrammar<out T>(val uri: String) : Grammar<T>() {
    val WS by token("\\s+", ignore = true)
    val COMMENT by token("\\/\\/.+", ignore = true)
    val FAT_ARROW by token("\\=\\>")
    val F_SLASH by token("\\/")
    val ARROW by token("\\-\\>")
    val EXTENDS by token("\\:\\:")
    val EQ by token("\\=")
    val O_PAREN by token("\\(")
    val C_PAREN by token("\\)")
    val O_BRACE by token("\\[")
    val C_BRACE by token("\\]")
    val O_BRACKET by token("\\{")
    val C_BRACKET by token("\\}")
    val BACKSLASH by token("\\\\")
    val DOT by token("\\.")
    val DEF by token("def\\b")
    val IF by token("if\\b")
    val THEN by token("then\\b")
    val ELSE by token("else\\b")
    val TRUE by token("true\\b")
    val FALSE by token("false\\b")
    val IMPORT by token("import\\b")
    val PACKAGE by token("package\\b")
    val ID by token("[_a-zA-Z][_a-zA-Z0-9]*")

    fun parse(input: String) = parseToEnd(input)
    fun parse(input: InputStream) = parseToEnd(input)
    fun parse(input: Readable) = parseToEnd(input)
    fun parse(input: Scanner) = parseToEnd(input)

    companion object {
        fun <T> create(
                file: String,
                func: QuartzGrammar<*>.() -> Parser<T>
        ) = object : QuartzGrammar<T>(file) {
            override val rootParser: Parser<T> = func(this)
        }
    }
}
