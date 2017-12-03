package io.quartz.gen.jvm.sym

import io.quartz.tree.util.*

sealed class JvmMemLoc {
    data class Arg(val index: Int) : JvmMemLoc()
    data class Global(val name: QualifiedName) : JvmMemLoc()
    data class LocalField(val name: Name) : JvmMemLoc()
}
