package io.quartz.gen.jvm.tree

import io.quartz.tree.util.Name

data class JvmScheme(val foralls: Set<Name>, val constraints: List<JvmConstraint>, val type: JvmType)
