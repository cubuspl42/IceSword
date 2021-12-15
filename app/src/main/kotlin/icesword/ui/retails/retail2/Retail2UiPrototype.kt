package icesword.ui.retails.retail2

import icesword.createInsertElasticButton
import icesword.editor.DoublePilePrototype
import icesword.editor.Editor
import icesword.editor.PlatformPrototype
import icesword.editor.retails.Retail2
import icesword.frp.Till
import icesword.ui.retails.RetailUiPrototype
import org.w3c.dom.HTMLElement

object Retail2UiPrototype : RetailUiPrototype {
    override val retail = Retail2

    override fun buildInsertionButtons(
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement> = listOf(
        createInsertElasticButton(
            editor = editor,
            prototype = PlatformPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL2/TILES/ACTION/029.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = DoublePilePrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL2/TILES/ACTION/113.png",
            tillDetach = tillDetach,
        ),
    )
}
