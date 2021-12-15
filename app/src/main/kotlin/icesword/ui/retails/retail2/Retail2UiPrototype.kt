package icesword.ui.retails.retail2

import icesword.createInsertElasticButton
import icesword.editor.Editor
import icesword.editor.PlatformPrototype
import icesword.editor.retails.Retail2
import icesword.frp.Till
import icesword.ui.retails.RetailUiPrototype
import icesword.ui.retails.retail3.Retail3UiPrototype
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
            retail = Retail3UiPrototype.retail,
            imagePath = "images/CLAW/LEVEL2/TILES/ACTION/029.png",
            tillDetach = tillDetach,
        ),
    )
}
