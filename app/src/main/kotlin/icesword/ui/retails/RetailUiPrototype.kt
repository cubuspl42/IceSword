package icesword.ui.retails

import icesword.editor.Editor
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail1
import icesword.editor.retails.Retail10
import icesword.editor.retails.Retail11
import icesword.editor.retails.Retail12
import icesword.editor.retails.Retail13
import icesword.editor.retails.Retail14
import icesword.editor.retails.Retail2
import icesword.editor.retails.Retail3
import icesword.editor.retails.Retail4
import icesword.editor.retails.Retail5
import icesword.editor.retails.Retail6
import icesword.editor.retails.Retail7
import icesword.editor.retails.Retail8
import icesword.editor.retails.Retail9
import icesword.frp.Till
import icesword.ui.retails.retail1.Retail1UiPrototype
import icesword.ui.retails.retail3.Retail3UiPrototype
import org.w3c.dom.HTMLElement

interface RetailUiPrototype {
    companion object {
        fun forRetail(retail: Retail): RetailUiPrototype = when (retail) {
            Retail1 -> Retail1UiPrototype
            Retail2 -> TODO()
            Retail3 -> Retail3UiPrototype
            Retail4 -> TODO()
            Retail5 -> TODO()
            Retail6 -> TODO()
            Retail7 -> TODO()
            Retail8 -> TODO()
            Retail9 -> TODO()
            Retail10 -> TODO()
            Retail11 -> TODO()
            Retail12 -> TODO()
            Retail13 -> TODO()
            Retail14 -> TODO()
        }
    }

    val retail: Retail

    fun buildInsertionButtons(
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement>
}
