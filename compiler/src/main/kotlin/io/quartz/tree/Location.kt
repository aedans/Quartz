package io.quartz.tree

interface Locatable {
    val location: Location
}

/** Class containing the source location of a token */
data class Location(
        val uri: String,
        val line: Int,
        val position: Int
) {
    override fun toString() = "$uri:$line, $position"

    companion object {
        /** Location for auto-generated code */
        val unknown = Location("???", -1, -1)
    }
}
