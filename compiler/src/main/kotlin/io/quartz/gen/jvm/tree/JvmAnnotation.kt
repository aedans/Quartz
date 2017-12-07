package io.quartz.gen.jvm.tree

import io.quartz.nil
import io.quartz.tree.util.*

data class JvmAnnotation(
        val qualifiedName: QualifiedName,
        val args: List<Arg> = nil
) {
    data class Arg(val name: Name, val arg: Any)
}
