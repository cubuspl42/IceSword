package icesword.ui.retails.retail14

import icesword.editor.Editor
import icesword.editor.retails.Retail4
import icesword.frp.Till
import icesword.ui.retails.RetailUiPrototype
import org.w3c.dom.HTMLElement

object Retail14UiPrototype : RetailUiPrototype {
    override val retail = Retail4

    override fun buildInsertionButtons(
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement> = emptyList()
}