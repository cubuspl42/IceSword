package icesword.ui.retails.retail10

import icesword.editor.Editor
import icesword.editor.retails.Retail4
import icesword.frp.Till
import icesword.html.HTMLWidgetB
import icesword.ui.retails.RetailUiPrototype
import org.w3c.dom.HTMLElement

object Retail10UiPrototype : RetailUiPrototype {
    override val retail = Retail4

    override fun buildInsertionButtons(
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement> = emptyList()

    override fun buildBrushesButtons(editor: Editor): List<HTMLWidgetB<*>> =
        emptyList()
}
