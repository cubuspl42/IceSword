package icesword.ui.retails.retail1

import icesword.createInsertElasticButton
import icesword.createInsertEnemyButton
import icesword.createInsertEntityButton
import icesword.createInsertKnotMeshButton
import icesword.createInsertWapObjectButton
import icesword.createKnotPaintButton
import icesword.editor.elastic.prototype.Retail1ColumnPrototype
import icesword.editor.CrateStackPrototype
import icesword.editor.Editor
import icesword.editor.InsertionPrototype
import icesword.editor.InsertionPrototype.CrumblingPegInsertionPrototype
import icesword.editor.KnotPrototype
import icesword.editor.elastic.prototype.LadderPrototype
import icesword.editor.elastic.prototype.Retail1PlatformPrototype
import icesword.editor.elastic.prototype.Retail1SpikesPrototype
import icesword.editor.retails.Retail1
import icesword.editor.wap_object.prototype.Level1CrumblingPegPrototype
import icesword.editor.wap_object.prototype.Level1RatPrototype
import icesword.editor.wap_object.prototype.OfficerPrototype
import icesword.editor.wap_object.prototype.Retail1Health
import icesword.editor.wap_object.prototype.Retail1PowderKeg
import icesword.editor.wap_object.prototype.SoldierPrototype
import icesword.editor.wap_object.prototype.WapObjectPrototype
import icesword.frp.Till
import icesword.html.HTMLWidgetB
import icesword.ui.retails.RetailUiPrototype
import org.w3c.dom.HTMLElement

object Retail1UiPrototype : RetailUiPrototype {
    override val retail = Retail1

    override fun buildInsertionButtons(
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement> = listOf(
        createInsertWapObjectButton(
            editor = editor,
            text = "Health",
            imagePath = "images/CLAW/LEVEL1/IMAGES/HEALTH/BREADWATER1.png",
            wapObjectPrototype = Retail1Health,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "PowderKeg",
            imagePath = "images/CLAW/LEVEL1/IMAGES/POWDERKEG/FRAME001.png",
            wapObjectPrototype = Retail1PowderKeg,
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = LadderPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL1/TILES/ACTION/311.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail1ColumnPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL1/TILES/ACTION/933.png",
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
        createInsertEntityButton(
            editor = editor,
            text = "",
            imagePath = "images/CLAW/LEVEL1/IMAGES/CRUMBLINGPEG/FRAME001.png",
            insertionPrototype = CrumblingPegInsertionPrototype(
                crumblingPegPrototype = Retail1.crumblingPegPrototype,
            ),
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "",
            imagePath = "images/CLAW/LEVEL1/IMAGES/PEG/FRAME001.png",
            insertionPrototype = InsertionPrototype.TogglePegInsertionPrototype(
                togglePegPrototype = Retail1.togglePegPrototype,
            ),
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
        createInsertElasticButton(
            editor = editor,
            prototype = Retail1PlatformPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL1/TILES/ACTION/331.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail1SpikesPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL1/TILES/ACTION/323.png",
            tillDetach = tillDetach,
        ),

        createInsertEnemyButton(
            editor = editor,
            text = "Officer",
            imagePath = "images/CLAW/LEVEL1/IMAGES/OFFICER/FRAME001.png",
            wapObjectPrototype = OfficerPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEnemyButton(
            editor = editor,
            text = "Soldier",
            imagePath = "images/CLAW/LEVEL1/IMAGES/SOLDIER/FRAME001.png",
            wapObjectPrototype = SoldierPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEnemyButton(
            editor = editor,
            text = "Rat",
            imagePath = "images/CLAW/LEVEL1/IMAGES/RAT/FRAME001.png",
            wapObjectPrototype = Level1RatPrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "MapPiece",
            imagePath = "images/CLAW/GAME/IMAGES/MAPPIECE/MAPFRAG1.png",
            wapObjectPrototype = WapObjectPrototype.MapPiece,
            tillDetach = tillDetach,
        ),
    )

    override fun buildBrushesButtons(editor: Editor): List<HTMLWidgetB<*>> =
        listOf(
            createKnotPaintButton(
                editor = editor,
                knotPrototype = KnotPrototype.Level1Foundation,
                imagePath = "images/CLAW/LEVEL1/TILES/ACTION/303.png",
            ),
        )
}
