package icesword.editor

enum class Retail {
    Retail1,
    Retail2,
    Retail3,
    Retail4,
    Retail5,
    Retail6,
    Retail7,
    Retail8,
    Retail9,
    Retail10,
    Retail11,
    Retail12,
    Retail13,
    Retail14;

    companion object {
        // TODO: Nuke
        val theRetail = Retail3
    }

    val naturalIndex: Int
        get() = ordinal + 1
}
