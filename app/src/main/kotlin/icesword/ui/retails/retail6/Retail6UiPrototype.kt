package icesword.ui.retails.retail6

import icesword.createInsertElasticButton
import icesword.editor.Editor
import icesword.editor.elastic.prototype.Retail5SpikesPrototype
import icesword.editor.elastic.prototype.Retail6FencePrototype
import icesword.editor.retails.Retail6
import icesword.frp.Till
import icesword.html.HTMLWidgetB
import icesword.ui.retails.RetailUiPrototype
import org.w3c.dom.HTMLElement

object Retail6UiPrototype : RetailUiPrototype {
    override val retail = Retail6

    override fun buildInsertionButtons(
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement> = listOf(
        createInsertElasticButton(
            editor = editor,
            prototype = Retail6FencePrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL6/TILES/ACTION/022.png",
            tillDetach = tillDetach,
        ),
    )

    override fun buildBrushesButtons(editor: Editor): List<HTMLWidgetB<*>> =
        emptyList()
}
