package icesword.ui.retails.retail4

import icesword.ui.side_bar.createInsertElasticButton
import icesword.ui.side_bar.createInsertEntityButton
import icesword.editor.Editor
import icesword.editor.modes.InsertionPrototype.CrumblingPegInsertionPrototype
import icesword.editor.entities.elastic.prototype.Retail4CavePrototype
import icesword.editor.entities.elastic.prototype.Retail4GooPrototype
import icesword.editor.entities.elastic.prototype.Retail4LadderPrototype
import icesword.editor.entities.elastic.prototype.Retail4NarrowNaturalPlatformPrototype
import icesword.editor.entities.elastic.prototype.Retail4TreeLogPrototype
import icesword.editor.entities.elastic.prototype.Retail4TreePrototype
import icesword.editor.entities.elastic.prototype.Retail4WideNaturalPlatformPrototype
import icesword.editor.entities.elastic.prototype.Retail4WoodenPlatformPrototype
import icesword.editor.retails.Retail4
import icesword.frp.Till
import icesword.html.HTMLWidgetB
import icesword.ui.createPreviewImage
import icesword.ui.retails.RetailUiPrototype
import org.w3c.dom.HTMLElement

object Retail4UiPrototype : RetailUiPrototype {
    override val retail = Retail4

    override fun buildInsertionButtons(
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement> = listOf(
        createInsertEntityButton(
            editor = editor,
            child = createPreviewImage(imagePath = "images/CLAW/LEVEL4/IMAGES/CRUMBLINGBUSH/FRAME001.png"),
            insertionPrototype = CrumblingPegInsertionPrototype(
                crumblingPegPrototype = Retail4.crumblingPegPrototype,
            ),
            tillDetach = tillDetach,
        ),
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
        createInsertElasticButton(
            editor = editor,
            prototype = Retail4LadderPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL4/TILES/ACTION/182.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail4NarrowNaturalPlatformPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL4/TILES/ACTION/133.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail4WideNaturalPlatformPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL4/TILES/ACTION/101.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail4TreePrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL4/TILES/ACTION/184.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail4GooPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL4/TILES/ACTION/228.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail4CavePrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL4/TILES/ACTION/114.png",
            tillDetach = tillDetach,
        ),
    )

    override fun buildBrushesButtons(editor: Editor): List<HTMLWidgetB<*>> =
        emptyList()
}
