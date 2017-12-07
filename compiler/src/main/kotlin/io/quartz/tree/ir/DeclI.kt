package io.quartz.tree.ir

import io.quartz.tree.util.*

sealed class DeclI : Locatable {
    abstract val qualifier: Qualifier

    data class Trait(
            override val location: Location?,
            override val qualifier: Qualifier,
            val name: Name,
            val foralls: Set<Name>,
            val members: List<Member>
    ) : DeclI() {
        data class Member(
                val location: Location?,
                val name: Name,
                val scheme: SchemeI
        )
    }

    data class Value(
            override val location: Location?,
            override val qualifier: Qualifier,
            val name: Name,
            val scheme: SchemeI,
            val expr: ExprI
    ) : DeclI()

    data class Instance(
            override val location: Location?,
            override val qualifier: Qualifier,
            val name: Name?,
            val instance: QualifiedName,
            val scheme: SchemeI,
            val impls: List<Value>
    ) : DeclI()
}
