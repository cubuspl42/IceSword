package icesword.ui.retails.retail2

import icesword.editor.Editor
import icesword.editor.retails.Retail2
import icesword.frp.Till
import icesword.ui.retails.RetailUiPrototype
import org.w3c.dom.HTMLElement

object Retail2UiPrototype : RetailUiPrototype {
    override val retail = Retail2

    override fun buildInsertionButtons(
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement> = emptyList()
}
