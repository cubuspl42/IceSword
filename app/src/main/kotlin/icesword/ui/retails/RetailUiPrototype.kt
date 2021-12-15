package icesword.ui.retails

import icesword.editor.Editor
import icesword.editor.Retail
import icesword.frp.Till
import icesword.ui.retails.retail1.Retail1UiPrototype
import icesword.ui.retails.retail3.Retail3UiPrototype
import org.w3c.dom.HTMLElement

interface RetailUiPrototype {
    companion object {
        fun forRetail(retail: Retail): RetailUiPrototype = when (retail) {
            Retail.Retail1 -> Retail1UiPrototype
            Retail.Retail2 -> TODO()
            Retail.Retail3 -> Retail3UiPrototype
            Retail.Retail4 -> TODO()
            Retail.Retail5 -> TODO()
            Retail.Retail6 -> TODO()
            Retail.Retail7 -> TODO()
            Retail.Retail8 -> TODO()
            Retail.Retail9 -> TODO()
            Retail.Retail10 -> TODO()
            Retail.Retail11 -> TODO()
            Retail.Retail12 -> TODO()
            Retail.Retail13 -> TODO()
            Retail.Retail14 -> TODO()
        }
    }

    val retail: Retail

    fun buildInsertionButtons(
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement>
}
