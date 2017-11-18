package io.quartz.tree.ir

import io.quartz.tree.Locatable
import io.quartz.tree.Location
import io.quartz.tree.Name
import io.quartz.tree.ast.Package

/** Sealed class representing all IR declarations */
sealed class DeclI : Locatable {
    data class Trait(
            override val location: Location,
            val name: Name,
            val p: Package,
            val constraints: List<ConstraintI>,
            val members: List<Member>
    ) : DeclI() {
        data class Member(
                val location: Location,
                val name: Name,
                val scheme: SchemeI
        )
    }

    data class Value(
            override val location: Location,
            val name: Name,
            val p: Package,
            val scheme: SchemeI,
            val expr: ExprI
    ) : DeclI()

    data class Instance(
            override val location: Location,
            val p: Package,
            val constraints: List<ConstraintI>,
            val type: TypeI,
            val instance: TypeI,
            val impls: List<Value>
    ) : DeclI()
}
