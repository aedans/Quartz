package io.quartz.tree

/**
 * @author Aedan Smith
 */

interface Locatable {
    val location: Location
}

data class Location(
        val uri: String,
        val line: Int,
        val position: Int
) {
    override fun toString() = "$uri:$line, $position"

    companion object {
        val unknown = Location("???", -1, -1)
    }
}
