package io.quartz.tree.ir

import io.quartz.tree.Locatable
import io.quartz.tree.Location
import io.quartz.tree.Name
import io.quartz.tree.Qualifier

/** Sealed class representing all IR declarations */
sealed class DeclI : Locatable {
    abstract val name: Name

    data class Class(
            override val name: Name,
            override val location: Location,
            val qualifier: Qualifier,
            val constructor: Constructor?,
            val obj: Object
    ) : DeclI() {
        data class Object(
                val generics: List<GenericI>,
                val superTypes: List<TypeI>,
                val decls: List<DeclI>
        )

        data class Constructor(
                val args: List<TypeI>,
                val expr: ExprI
        )
    }

    data class Method(
            override val name: Name,
            override val location: Location,
            val qualifier: Qualifier,
            val scheme: Scheme,
            val expr: ExprI
    ) : DeclI() {
        data class Scheme(
                val generics: List<GenericI>,
                val args: List<TypeI>,
                val ret: TypeI
        )
    }

    data class Field(
            override val name: Name,
            override val location: Location,
            val type: TypeI
    ) : DeclI()
}
