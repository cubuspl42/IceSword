package icesword.ui.retails.retail5

import icesword.createInsertElasticButton
import icesword.createInsertKnotMeshButton
import icesword.editor.Editor
import icesword.editor.KnotPrototype
import icesword.editor.elastic.prototype.Retail5ArchLegPrototype
import icesword.editor.elastic.prototype.Retail5ArchSpanPrototype
import icesword.editor.elastic.prototype.Retail5BreakPlankPrototype
import icesword.editor.elastic.prototype.Retail5BridgeLeftPrototype
import icesword.editor.elastic.prototype.Retail5BridgeRightPrototype
import icesword.editor.elastic.prototype.Retail5HorizontalRoofPrototype
import icesword.editor.elastic.prototype.Retail5HousePrototype
import icesword.editor.elastic.prototype.Retail5LadderPrototype
import icesword.editor.elastic.prototype.Retail5MetalPlatformPrototype
import icesword.editor.elastic.prototype.Retail5SpikesPrototype
import icesword.editor.retails.Retail5
import icesword.frp.Till
import icesword.html.HTMLWidgetB
import icesword.ui.retails.RetailUiPrototype
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
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/TILES/ACTION/202.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5LadderPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/TILES/ACTION/516.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5HorizontalRoofPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/TILES/ACTION/268.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5SpikesPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/TILES/ACTION/405.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5BridgeLeftPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/TILES/ACTION/506.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5BreakPlankPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/IMAGES/BREAKPLANK/FRAME001.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5BridgeRightPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/TILES/ACTION/502.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5HousePrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/TILES/ACTION/259.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5ArchSpanPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/TILES/ACTION/526.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5ArchLegPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/TILES/ACTION/528.png",
            tillDetach = tillDetach,
        ),
        createInsertKnotMeshButton(
            editor = editor,
            knotPrototype = KnotPrototype.Retail5RockPrototype,
            imagePath = "images/CLAW/LEVEL5/TILES/ACTION/312.png",
            tillDetach = tillDetach,
        ),
    )

    override fun buildBrushesButtons(editor: Editor): List<HTMLWidgetB<*>> =
        emptyList()
}
