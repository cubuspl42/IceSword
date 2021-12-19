package icesword.ui.retails.retail5

import icesword.createInsertElasticButton
import icesword.editor.Editor
import icesword.editor.elastic.prototype.Retail5MetalPlatformPrototype
import icesword.editor.retails.Retail4
import icesword.editor.retails.Retail5
import icesword.frp.Till
import icesword.html.HTMLWidgetB
import icesword.ui.retails.RetailUiPrototype
import icesword.ui.retails.retail4.Retail4UiPrototype
import org.w3c.dom.HTMLElement

object Retail5UiPrototype : RetailUiPrototype {
    override val retail = Retail5

    override fun buildInsertionButtons(
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement> = listOf(
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5MetalPlatformPrototype,
            retail = Retail4UiPrototype.retail,
            imagePath = "images/CLAW/LEVEL5/TILES/ACTION/202.png",
            tillDetach = tillDetach,
        ),
    )

    override fun buildBrushesButtons(editor: Editor): List<HTMLWidgetB<*>> =
        emptyList()
}
