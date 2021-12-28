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
    "left" to Case(
        tileX = 281,
        tileY = 63,
        objectX = 18000,
        objectY = 4160,
    ),
    "center" to Case(
        tileX = 282,
        tileY = 63,
        objectX = 18080,
        objectY = 4160,
    ),
    "right" to Case(
        tileX = 293,
        tileY = 63,
        objectX = 18800,
        objectY = 4160,
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
