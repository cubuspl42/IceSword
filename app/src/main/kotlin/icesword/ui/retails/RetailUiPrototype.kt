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
import icesword.html.HTMLWidget
import icesword.html.HTMLWidgetB
import icesword.ui.retails.retail1.Retail1UiPrototype
import icesword.ui.retails.retail10.Retail10UiPrototype
import icesword.ui.retails.retail11.Retail11UiPrototype
import icesword.ui.retails.retail12.Retail12UiPrototype
import icesword.ui.retails.retail13.Retail13UiPrototype
import icesword.ui.retails.retail14.Retail14UiPrototype
import icesword.ui.retails.retail2.Retail2UiPrototype
import icesword.ui.retails.retail3.Retail3UiPrototype
import icesword.ui.retails.retail4.Retail4UiPrototype
import icesword.ui.retails.retail5.Retail5UiPrototype
import icesword.ui.retails.retail6.Retail6UiPrototype
import icesword.ui.retails.retail7.Retail7UiPrototype
import icesword.ui.retails.retail8.Retail8UiPrototype
import icesword.ui.retails.retail9.Retail9UiPrototype
import org.w3c.dom.HTMLElement

interface RetailUiPrototype {
    companion object {
        fun forRetail(retail: Retail): RetailUiPrototype = when (retail) {
            Retail1 -> Retail1UiPrototype
            Retail2 -> Retail2UiPrototype
            Retail3 -> Retail3UiPrototype
            Retail4 -> Retail4UiPrototype
            Retail5 -> Retail5UiPrototype
            Retail6 -> Retail6UiPrototype
            Retail7 -> Retail7UiPrototype
            Retail8 -> Retail8UiPrototype
            Retail9 -> Retail9UiPrototype
            Retail10 -> Retail10UiPrototype
            Retail11 -> Retail11UiPrototype
            Retail12 -> Retail12UiPrototype
            Retail13 -> Retail13UiPrototype
            Retail14 -> Retail14UiPrototype
        }
    }

    val retail: Retail

    fun buildInsertionButtons(
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement>

    fun buildBrushesButtons(
        editor: Editor,
    ): List<HTMLWidgetB<*>>
}
