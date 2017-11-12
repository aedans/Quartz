package io.quartz.interop

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.quartz.analyze.*
import io.quartz.err.Result
import io.quartz.err.resultMonad
import io.quartz.gen.asm.ProgramGenerator
import io.quartz.gen.generate
import io.quartz.parse.QuartzGrammar
import io.quartz.parse.fileT
import io.quartz.tree.QualifiedName
import io.quartz.tree.ast.DeclT
import io.quartz.tree.ast.ImportT
import io.quartz.tree.ast.Package
import io.quartz.tree.qualify
import kategory.Tuple3
import kategory.binding
import kategory.ev
import kategory.right
import org.funktionale.collections.prependTo
import java.io.File

interface SourcePath {
    fun getInterface(name: QualifiedName): Result<DeclTInfo<DeclT.Interface>>?
    fun getValue(name: QualifiedName): Result<DeclTInfo<DeclT.Value>>?
    fun getInstances(): List<DeclTInfo<DeclT.Instance>>
}

fun List<File>.sourcePath() = object : SourcePath {
    val decls = flatMap { it.decls() }

    val interfaceMap = decls.mapNotNull { (a, b, c) ->
        (c as? DeclT.Interface)?.let { it.name.qualify(a) to Tuple3(a, b, it) }
    }.toMap()

    val valueMap = decls.mapNotNull { (a, b, c) ->
        (c as? DeclT.Value)?.let { it.name.qualify(a) to Tuple3(a, b, it) }
    }.toMap()

    private val instances = decls
            .mapNotNull { (a, b, c) -> (c as? DeclT.Instance)?.let { Tuple3(a, b, it) } }

    override fun getInterface(name: QualifiedName) =
            interfaceMap[name]?.right()

    override fun getValue(name: QualifiedName) =
            valueMap[name]?.right()

    override fun getInstances() = instances
}

fun SourcePath.getType(name: QualifiedName, env: Env, pg: ProgramGenerator) = getInterface(name)?.let {
    resultMonad().binding {
        val (p, imports, decl) = it.bind()
        val localEnv = env.import(p.import.prependTo(imports))
        val schemeK = decl.schemeK(localEnv, p).bind()
        val info = TypeInfo(schemeK)
        decl.analyze(localEnv, p).map { it.generate(pg) } // Generate decl when accessed
        yields(info)
    }.ev()
}

fun SourcePath.getVar(name: QualifiedName, env: Env, pg: ProgramGenerator) = getValue(name)?.let {
    resultMonad().binding {
        val (p, imports, decl) = it.bind()
        val localEnv = env.import(p.import.prependTo(imports))
        val schemeK = decl.schemeK(localEnv).bind()
        val varLoc = VarLoc.Global(name)
        val info = VarInfo(schemeK, varLoc)
        decl.analyze(localEnv, p).map { it.generate(pg) } // Generate decl when accessed
        yields(info)
    }.ev()
}

typealias DeclTInfo<T> = Tuple3<Package, List<ImportT>, T>

fun List<File>.decls() = flatMap { it.decls() }

fun File.decls(): List<DeclTInfo<DeclT>> = if (isFile) {
    val grammar = QuartzGrammar.create(name) { fileT }
    val fileT = grammar.parseToEnd(reader())
    fileT.decls.map { Tuple3(fileT.`package`, fileT.imports, it) }
} else {
    listFiles()
            .filter { it.isDirectory || it.extension == "qz" }
            .decls()
}
