const val tileSize = 64

data class Case(
    val tileX: Int,
    val tileY: Int,
    val objectX: Int,
    val objectY: Int,
) {
    val offsetX: Int
        get() = objectX - tileX * tileSize

    val offsetY: Int
        get() = objectY - tileY * tileSize
}

val cases = listOf(
    "wall cover" to Case(
        tileX = 298,
        tileY = 79,
        objectX = 19104,
        objectY = 5079,
    ),
    "wall cover 2" to Case(
        tileX = 367,
        tileY = 85,
        objectX = 23520,
        objectY = 5481,
    ),
)

fun main() {
    cases.forEach { (name, case) ->
        val sourceFragment = """
            // $name
            x = ${case.offsetX},
            y = ${case.offsetY},
        """.trimIndent()

        println(sourceFragment)
    }
}
