package icesword.ui.retails.retail3

import icesword.createInsertElasticButton
import icesword.createInsertEnemyButton
import icesword.createInsertEntityButton
import icesword.createInsertKnotMeshButton
import icesword.createInsertWapObjectButton
import icesword.createKnotPaintButton
import icesword.editor.CrateStackPrototype
import icesword.editor.Editor
import icesword.editor.InsertionPrototype
import icesword.editor.KnotPrototype
import icesword.editor.LadderPrototype
import icesword.editor.LogPrototype
import icesword.editor.RopePrototype
import icesword.editor.SpikesPrototype
import icesword.editor.TreeCrownPrototype
import icesword.editor.WapObjectPrototype
import icesword.editor.retails.Retail3
import icesword.frp.Till
import icesword.html.HTMLWidgetB
import icesword.ui.retails.RetailUiPrototype
import org.w3c.dom.HTMLElement

object Retail3UiPrototype : RetailUiPrototype {
    override val retail = Retail3

    override fun buildInsertionButtons(
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement> = listOf(
        createInsertElasticButton(
            editor = editor,
            prototype = LogPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL3/TILES/ACTION/657.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = TreeCrownPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL3/TILES/ACTION/645.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = LadderPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL3/TILES/ACTION/669.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = SpikesPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL3/TILES/ACTION/686.png",
            tillDetach = tillDetach,
        ),
        createInsertKnotMeshButton(
            editor = editor,
            knotPrototype = KnotPrototype.Level3UndergroundRockPrototype,
            imagePath = "images/CLAW/LEVEL3/TILES/ACTION/621.png",
            tillDetach = tillDetach,
        ),
        createInsertKnotMeshButton(
            editor = editor,
            knotPrototype = KnotPrototype.Level3OvergroundRockPrototype,
            imagePath = "images/CLAW/LEVEL3/TILES/ACTION/604.png",
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Rope",
            imagePath = "images/CLAW/LEVEL3/IMAGES/ROPE/FRAME001.png",
            insertionPrototype = InsertionPrototype.RopeInsertionPrototype(
                ropePrototype = RopePrototype(retail = retail),
            ),
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Crate stack",
            imagePath = "images/CLAW/LEVEL3/IMAGES/CRATES/FRAME001.png",
            insertionPrototype = InsertionPrototype.CrateStackInsertionPrototype(
                crateStackPrototype = CrateStackPrototype(retail = retail),
            ),
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "CrumblingPeg",
            imagePath = "images/CLAW/LEVEL3/IMAGES/CRUMBLINPEG1/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.Level3CrumblingPegPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Elevator (horizontal)",
            imagePath = "images/CLAW/LEVEL3/IMAGES/ELEVATOR1/FRAME001.png",
            insertionPrototype = InsertionPrototype.HorizontalElevatorInsertionPrototype(
                elevatorPrototype = retail.elevatorPrototype,
            ),
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Elevator (vertical)",
            imagePath = "images/CLAW/LEVEL3/IMAGES/ELEVATOR1/FRAME001.png",
            insertionPrototype = InsertionPrototype.VerticalElevatorInsertionPrototype(
                elevatorPrototype = retail.elevatorPrototype,
            ),
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Elevator (path)",
            imagePath = "images/CLAW/LEVEL3/IMAGES/ELEVATOR1/FRAME001.png",
            insertionPrototype = InsertionPrototype.PathElevatorInsertionPrototype(
                elevatorPrototype = retail.elevatorPrototype,
            ),
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Floor spike row",
            imagePath = "images/CLAW/LEVEL3/IMAGES/FLOORSPIKES1/FRAME001.png",
            insertionPrototype = InsertionPrototype.FloorSpikeInsertionPrototype,
            tillDetach = tillDetach,
        ),

        createInsertEnemyButton(
            editor = editor,
            text = "Robber Thief",
            imagePath = "images/CLAW/LEVEL3/IMAGES/ROBBERTHIEF/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.RobberThiefPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEnemyButton(
            editor = editor,
            text = "Cut Throat",
            imagePath = "images/CLAW/LEVEL3/IMAGES/CUTTHROAT/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.CutThroatPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEnemyButton(
            editor = editor,
            text = "Rat",
            imagePath = "images/CLAW/LEVEL3/IMAGES/RAT/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.Level3RatPrototype,
            tillDetach = tillDetach,
        ),
    )

    override fun buildBrushesButtons(editor: Editor): List<HTMLWidgetB<*>> =
        listOf(
            createKnotPaintButton(
                editor = editor,
                imagePath = "images/CLAW/LEVEL3/TILES/ACTION/621.png",
                knotPrototype = KnotPrototype.Level3UndergroundRockPrototype,
            ),
            createKnotPaintButton(
                editor = editor,
                imagePath = "images/CLAW/LEVEL3/TILES/ACTION/604.png",
                knotPrototype = KnotPrototype.Level3OvergroundRockPrototype,
            ),
        )
}
