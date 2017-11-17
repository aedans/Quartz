package io.quartz.tree.ir

import io.quartz.tree.Locatable
import io.quartz.tree.Location
import io.quartz.tree.Name
import io.quartz.tree.ast.Package

/** Sealed class representing all IR declarations */
sealed class DeclI : Locatable {
    data class Class(
            override val location: Location,
            val name: Name,
            val p: Package,
            val constructor: Constructor?,
            val generics: List<GenericI>,
            val superTypes: List<TypeI>,
            val decls: List<DeclI>
    ) : DeclI() {
        data class Constructor(
                val args: List<TypeI>,
                val expr: ExprI
        )
    }

    data class Method(
            override val location: Location,
            val name: Name,
            val p: Package,
            val scheme: Scheme,
            val expr: ExprI?
    ) : DeclI() {
        data class Scheme(
                val generics: List<GenericI>,
                val args: List<TypeI>,
                val ret: TypeI
        )
    }
}
