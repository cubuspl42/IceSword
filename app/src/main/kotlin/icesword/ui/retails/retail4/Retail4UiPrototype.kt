package icesword.ui.retails.retail4

import icesword.createInsertElasticButton
import icesword.editor.Editor
import icesword.editor.elastic.prototype.Retail4TreeLogPrototype
import icesword.editor.elastic.prototype.Retail4WoodenPlatformPrototype
import icesword.editor.retails.Retail4
import icesword.frp.Till
import icesword.html.HTMLWidgetB
import icesword.ui.retails.RetailUiPrototype
import org.w3c.dom.HTMLElement

object Retail4UiPrototype : RetailUiPrototype {
    override val retail = Retail4

    override fun buildInsertionButtons(
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement> = listOf(
        createInsertElasticButton(
            editor = editor,
            prototype = Retail4TreeLogPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL4/TILES/ACTION/184.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail4WoodenPlatformPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL4/TILES/ACTION/159.png",
            tillDetach = tillDetach,
        ),
    )

    override fun buildBrushesButtons(editor: Editor): List<HTMLWidgetB<*>> =
        emptyList()
}
