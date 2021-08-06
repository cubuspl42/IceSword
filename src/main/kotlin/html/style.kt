package html

enum class FontWeight {
    bold;

    fun toCssString(): String = this.name
}

data class CSSStyle(
    val fontWeight: FontWeight?,
)
