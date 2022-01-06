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
    "candle" to Case(
        tileX = 8,
        tileY = 71,
        objectX = 543,
        objectY = 4587,
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
