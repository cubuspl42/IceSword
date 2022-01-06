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
    "cannon left" to Case(
        tileX = 173,
        tileY = 54,
        objectX = 11100,
        objectY = 3532,
    ),
    "cannon right" to Case(
        tileX = 178,
        tileY = 54,
        objectX = 11493,
        objectY = 3533,
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
