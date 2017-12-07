package io.quartz.tree.ir

import io.quartz.tree.util.*

sealed class DeclI : Locatable {
    data class Trait(
            override val location: Location?,
            val name: QualifiedName,
            val scheme: SchemeI,
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
            val name: QualifiedName,
            val scheme: SchemeI,
            val expr: ExprI?
    ) : DeclI()

    data class Instance(
            override val location: Location?,
            val name: QualifiedName?,
            val instance: QualifiedName,
            val scheme: SchemeI,
            val impls: List<Value>
    ) : DeclI()
}
