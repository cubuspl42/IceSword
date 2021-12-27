package icesword.ui.retails.retail6

import icesword.createInsertElasticButton
import icesword.createInsertKnotMeshButton
import icesword.editor.Editor
import icesword.editor.KnotPrototype
import icesword.editor.elastic.prototype.Retail6BrownHousePrototype
import icesword.editor.elastic.prototype.Retail6FencePrototype
import icesword.editor.elastic.prototype.Retail6HorizontalRoofPrototype
import icesword.editor.elastic.prototype.Retail6LadderPrototype
import icesword.editor.elastic.prototype.Retail6PavementPrototype
import icesword.editor.elastic.prototype.Retail6WhiteHousePrototype
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
            prototype = Retail6PavementPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL6/TILES/ACTION/101.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail6FencePrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL6/TILES/ACTION/022.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail6HorizontalRoofPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL6/TILES/ACTION/039.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail6LadderPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL6/TILES/ACTION/016.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail6WhiteHousePrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL6/TILES/ACTION/032.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail6BrownHousePrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL6/TILES/ACTION/069.png",
            tillDetach = tillDetach,
        ),
        createInsertKnotMeshButton(
            editor = editor,
            knotPrototype = KnotPrototype.Retail6BricksPrototype,
            imagePath = "images/CLAW/LEVEL6/TILES/ACTION/102.png",
            tillDetach = tillDetach,
        ),
    )

    override fun buildBrushesButtons(editor: Editor): List<HTMLWidgetB<*>> =
        emptyList()
}
