package io.quartz.parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser
import io.quartz.nil
import io.quartz.tree.Qualifier
import io.quartz.tree.ast.FileT
import io.quartz.tree.ast.ImportT
import io.quartz.tree.name
import io.quartz.tree.qualifiedName
import io.quartz.tree.unqualified

val QuartzGrammar<*>.fileT: Parser<FileT> get() = optional(parser { packageT }) and
        zeroOrMore(parser { importT }) and
        zeroOrMore(parser { declT }) use {
    FileT(t1 ?: nil, t2, t3)
}

val QuartzGrammar<*>.packageT: Parser<Qualifier> get() = skip(PACKAGE) and qualifier

val QuartzGrammar<*>.importT: Parser<ImportT> get() = parser { importTStar } or
        parser { importTQualified }

val QuartzGrammar<*>.importTQualified: Parser<ImportT.Qualified> get() = skip(IMPORT) and
        qualifier and
        optional(skip(AS) and ID) use {
    ImportT.Qualified(
            t1.qualifiedName,
            t2?.text?.name ?: t1.qualifiedName.unqualified
    )
}

val QuartzGrammar<*>.importTStar: Parser<ImportT.Star> get() = skip(IMPORT) and skip(STAR) and qualifier use {
    ImportT.Star(this)
}

val QuartzGrammar<*>.qualifier: Parser<Qualifier> get() = zeroOrMore(ID and DOT) and ID use {
    t1.map { it.t1.text } + t2.text
}
