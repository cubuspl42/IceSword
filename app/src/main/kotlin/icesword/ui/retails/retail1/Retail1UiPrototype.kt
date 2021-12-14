package icesword.ui.retails.retail1

import icesword.createInsertElasticButton
import icesword.createInsertEnemyButton
import icesword.createInsertEntityButton
import icesword.createInsertKnotMeshButton
import icesword.createInsertWapObjectButton
import icesword.editor.CrateStackPrototype
import icesword.editor.Editor
import icesword.editor.InsertionPrototype
import icesword.editor.KnotPrototype
import icesword.editor.LadderPrototype
import icesword.editor.Retail
import icesword.editor.SpikesPrototype
import icesword.editor.WapObjectPrototype
import icesword.frp.Till
import icesword.ui.retails.RetailUiPrototype
import org.w3c.dom.HTMLElement

object Retail1UiPrototype : RetailUiPrototype {
    override fun buildInsertionButtons(
        retail: Retail,
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement> = listOf(
        createInsertElasticButton(
            editor = editor,
            prototype = LadderPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL1/TILES/ACTION/311.png",
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Crate stack",
            imagePath = "images/CLAW/LEVEL1/IMAGES/CRATES/FRAME001.png",
            insertionPrototype = InsertionPrototype.CrateStackInsertionPrototype(
                crateStackPrototype = CrateStackPrototype(retail = retail),
            ),
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "CrumblingPeg",
            imagePath = "images/CLAW/LEVEL1/IMAGES/CRUMBLINGPEG/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.Level1CrumblingPegPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Elevator (horizontal)",
            imagePath = "images/CLAW/LEVEL1/IMAGES/ELEVATORS/1.png",
            insertionPrototype = InsertionPrototype.HorizontalElevatorInsertionPrototype(
                elevatorPrototype = retail.elevatorPrototype,
            ),
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Elevator (vertical)",
            imagePath = "images/CLAW/LEVEL1/IMAGES/ELEVATORS/1.png",
            insertionPrototype = InsertionPrototype.VerticalElevatorInsertionPrototype(
                elevatorPrototype = retail.elevatorPrototype,
            ),
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Elevator (path)",
            imagePath = "images/CLAW/LEVEL1/IMAGES/ELEVATORS/1.png",
            insertionPrototype = InsertionPrototype.PathElevatorInsertionPrototype(
                elevatorPrototype = retail.elevatorPrototype,
            ),
            tillDetach = tillDetach,
        ),

        createInsertEnemyButton(
            editor = editor,
            text = "Officer",
            imagePath = "images/CLAW/LEVEL1/IMAGES/OFFICER/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.OfficerPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEnemyButton(
            editor = editor,
            text = "Soldier",
            imagePath = "images/CLAW/LEVEL1/IMAGES/SOLDIER/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.SoldierPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEnemyButton(
            editor = editor,
            text = "Rat",
            imagePath = "images/CLAW/LEVEL1/IMAGES/RAT/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.Level1RatPrototype,
            tillDetach = tillDetach,
        ),
    )
}
