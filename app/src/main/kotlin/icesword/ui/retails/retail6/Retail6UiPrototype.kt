package icesword.ui.retails.retail6

import icesword.createInsertElasticButton
import icesword.createInsertEntityButton
import icesword.createInsertKnotMeshButton
import icesword.createKnotPaintButton
import icesword.editor.Editor
import icesword.editor.InsertionPrototype
import icesword.editor.KnotPrototype
import icesword.editor.elastic.prototype.Retail6BrownHousePrototype
import icesword.editor.elastic.prototype.Retail6FencePrototype
import icesword.editor.elastic.prototype.Retail6GooPrototype
import icesword.editor.elastic.prototype.Retail6HorizontalRoofPrototype
import icesword.editor.elastic.prototype.Retail6LadderPrototype
import icesword.editor.elastic.prototype.Retail6PavementPrototype
import icesword.editor.elastic.prototype.Retail6PlatePrototype
import icesword.editor.elastic.prototype.Retail6SewerPrototype
import icesword.editor.elastic.prototype.Retail6TunnelBricksFloorPrototype
import icesword.editor.elastic.prototype.Retail6TunnelPlateFloorPrototype
import icesword.editor.elastic.prototype.Retail6TunnelTubeCoverGapPrototype
import icesword.editor.elastic.prototype.Retail6TunnelTubeCoverPrototype
import icesword.editor.elastic.prototype.Retail6TunnelTubePrototype
import icesword.editor.elastic.prototype.Retail6WhiteHousePrototype
import icesword.editor.retails.Retail4
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
        createInsertEntityButton(
            editor = editor,
            text = "",
            imagePath = "images/CLAW/LEVEL6/IMAGES/BREAKINGLEDGE/FRAME001.png",
            insertionPrototype = InsertionPrototype.CrumblingPegInsertionPrototype(
                crumblingPegPrototype = Retail6.crumblingPegPrototype,
            ),
            tillDetach = tillDetach,
        ),
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
        createInsertElasticButton(
            editor = editor,
            prototype = Retail6TunnelTubePrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL6/TILES/ACTION/142.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail6TunnelTubeCoverPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL6/IMAGES/HORIZONTALTUBEALL/001.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail6TunnelTubeCoverGapPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL6/IMAGES/HORIZONTALTUBEALL/001.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail6TunnelBricksFloorPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL6/TILES/ACTION/133.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail6TunnelPlateFloorPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL6/TILES/ACTION/137.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail6GooPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL6/TILES/ACTION/148.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail6SewerPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL6/TILES/ACTION/197.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail6PlatePrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL6/TILES/ACTION/128.png",
            tillDetach = tillDetach,
        ),
    )

    override fun buildBrushesButtons(editor: Editor): List<HTMLWidgetB<*>> =
        listOf(
            createKnotPaintButton(
                editor = editor,
                knotPrototype = KnotPrototype.Retail6BricksPrototype,
                imagePath = "images/CLAW/LEVEL6/TILES/ACTION/102.png",
            ),
        )
}
