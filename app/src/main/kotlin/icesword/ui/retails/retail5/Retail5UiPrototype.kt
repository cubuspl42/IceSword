package icesword.ui.retails.retail5

import icesword.ui.side_bar.createInsertElasticButton
import icesword.ui.side_bar.createInsertEntityButton
import icesword.ui.side_bar.createKnotPaintButton
import icesword.editor.Editor
import icesword.editor.modes.InsertionPrototype.TogglePegInsertionPrototype
import icesword.editor.entities.KnotPrototype
import icesword.editor.entities.elastic.prototype.Retail5ArchLegPrototype
import icesword.editor.entities.elastic.prototype.Retail5ArchSpanPrototype
import icesword.editor.entities.elastic.prototype.Retail5BreakPlankPrototype
import icesword.editor.entities.elastic.prototype.Retail5BridgeLeftPrototype
import icesword.editor.entities.elastic.prototype.Retail5BridgeRightPrototype
import icesword.editor.entities.elastic.prototype.Retail5CleanWallPrototype
import icesword.editor.entities.elastic.prototype.Retail5DirtyWallPrototype
import icesword.editor.entities.elastic.prototype.Retail5HorizontalRoofPrototype
import icesword.editor.entities.elastic.prototype.Retail5HousePrototype
import icesword.editor.entities.elastic.prototype.Retail5LadderPrototype
import icesword.editor.entities.elastic.prototype.Retail5MetalPlatformPrototype
import icesword.editor.entities.elastic.prototype.Retail5Post1PaddedPrototype
import icesword.editor.entities.elastic.prototype.Retail5Post1Prototype
import icesword.editor.entities.elastic.prototype.Retail5Post2PaddedPrototype
import icesword.editor.entities.elastic.prototype.Retail5Post2Prototype
import icesword.editor.entities.elastic.prototype.Retail5SpikesPrototype
import icesword.editor.entities.fixture.prototypes.Retail5WallCover
import icesword.editor.retails.Retail5
import icesword.frp.Till
import icesword.html.HTMLWidgetB
import icesword.ui.side_bar.createInsertFixtureButton
import icesword.ui.createPreviewImage
import icesword.ui.retails.RetailUiPrototype
import org.w3c.dom.HTMLElement

object Retail5UiPrototype : RetailUiPrototype {
    override val retail = Retail5

    override fun buildInsertionButtons(
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement> = listOf(
        createInsertEntityButton(
            editor = editor,
            child = createPreviewImage(imagePath = "images/CLAW/LEVEL5/IMAGES/SLIDAWAYPLANK/FRAME001.png"),
            insertionPrototype = TogglePegInsertionPrototype(
                togglePegPrototype = Retail5.togglePegPrototype,
            ),
            tillDetach = tillDetach,
        ),
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
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5DirtyWallPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/TILES/ACTION/233.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5CleanWallPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/TILES/ACTION/217.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5Post1Prototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/IMAGES/POSTTOP1/001.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5Post1PaddedPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/IMAGES/POSTTOP1/001.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5Post2Prototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/IMAGES/POSTTOP2/001.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail5Post2PaddedPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL5/IMAGES/POSTTOP2/001.png",
            tillDetach = tillDetach,
        ),
        createInsertFixtureButton(
            editor = editor,
            prototype = Retail5WallCover,
            imagePath = "images/CLAW/LEVEL5/IMAGES/BRICKBACKWALLCOVER/001.png",
            tillDetach = tillDetach,
        ),
    )

    override fun buildBrushesButtons(editor: Editor): List<HTMLWidgetB<*>> =
        listOf(
            createKnotPaintButton(
                editor = editor,
                knotPrototype = KnotPrototype.Retail5RockPrototype,
                imagePath = "images/CLAW/LEVEL5/TILES/ACTION/312.png",
            ),
        )
}
