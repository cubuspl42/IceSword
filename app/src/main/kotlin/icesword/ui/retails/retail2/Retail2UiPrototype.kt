package icesword.ui.retails.retail2

import icesword.ui.side_bar.createInsertElasticButton
import icesword.ui.side_bar.createInsertEnemyButton
import icesword.ui.side_bar.createInsertEntityButton
import icesword.ui.side_bar.createInsertWapObjectButton
import icesword.editor.Editor
import icesword.editor.modes.InsertionPrototype
import icesword.editor.entities.elastic.prototype.Retail2DoublePilePrototype
import icesword.editor.entities.elastic.prototype.Retail2GooPrototype
import icesword.editor.entities.elastic.prototype.Retail2PlatformPrototype
import icesword.editor.entities.elastic.prototype.Retail2SinglePilePrototype
import icesword.editor.entities.elastic.prototype.Retail2TowerPrototype
import icesword.editor.entities.elastic.prototype.Retail2TowerTopPrototype
import icesword.editor.entities.fixture.prototypes.Retail2TowerCannonLeft
import icesword.editor.entities.fixture.prototypes.Retail2TowerCannonRight
import icesword.editor.entities.fixture.prototypes.Retail2TowerWindow
import icesword.editor.retails.Retail2
import icesword.editor.entities.wap_object.prototype.LaRauxPrototype
import icesword.editor.entities.wap_object.prototype.PunkRatCannon
import icesword.editor.entities.wap_object.prototype.Retail2Health
import icesword.editor.entities.wap_object.prototype.Retail2OfficerPrototype
import icesword.editor.entities.wap_object.prototype.Retail2PowderKeg
import icesword.editor.entities.wap_object.prototype.Retail2SoldierPrototype
import icesword.frp.Till
import icesword.html.HTMLWidgetB
import icesword.ui.side_bar.createInsertFixtureButton
import icesword.ui.createPreviewImage
import icesword.ui.retails.RetailUiPrototype
import org.w3c.dom.HTMLElement

object Retail2UiPrototype : RetailUiPrototype {
    override val retail = Retail2

    override fun buildInsertionButtons(
        editor: Editor,
        tillDetach: Till,
    ): List<HTMLElement> = listOf(
        createInsertWapObjectButton(
            editor = editor,
            text = "Health",
            imagePath = "images/CLAW/LEVEL2/IMAGES/HEALTH/BREADWATER1.png",
            wapObjectPrototype = Retail2Health,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "PowderKeg",
            imagePath = "images/CLAW/LEVEL2/IMAGES/POWDERKEG/FRAME001.png",
            wapObjectPrototype = Retail2PowderKeg,
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            child = createPreviewImage(imagePath =  "images/CLAW/LEVEL2/IMAGES/PEGSLIDER/FRAME001.png"),
            insertionPrototype = InsertionPrototype.TogglePegInsertionPrototype(
                togglePegPrototype = Retail2.togglePegPrototype,
            ),
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail2PlatformPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL2/TILES/ACTION/029.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail2DoublePilePrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL2/TILES/ACTION/113.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail2SinglePilePrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL2/TILES/ACTION/018.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail2TowerTopPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL2/TILES/ACTION/071.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail2TowerPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL2/TILES/ACTION/080.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = Retail2GooPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL2/TILES/ACTION/313.png",
            tillDetach = tillDetach,
        ),
        createInsertFixtureButton(
            editor = editor,
            prototype = Retail2TowerCannonLeft,
            imagePath = "images/CLAW/LEVEL2/IMAGES/TOWERCANNONLEFT/FRAME001.png",
            tillDetach = tillDetach,
        ),
        createInsertFixtureButton(
            editor = editor,
            prototype = Retail2TowerCannonRight,
            imagePath = "images/CLAW/LEVEL2/IMAGES/TOWERCANNONRIGHT/FRAME001.png",
            tillDetach = tillDetach,
        ),
        createInsertFixtureButton(
            editor = editor,
            prototype = Retail2TowerWindow,
            imagePath = "images/CLAW/LEVEL2/TILES/ACTION/085.png",
            tillDetach = tillDetach,
        ),
        createInsertEnemyButton(
            editor = editor,
            text = "Officer",
            imagePath = "images/CLAW/LEVEL2/IMAGES/OFFICER/FRAME001.png",
            wapObjectPrototype = Retail2OfficerPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEnemyButton(
            editor = editor,
            text = "Soldier",
            imagePath = "images/CLAW/LEVEL2/IMAGES/SOLDIER/FRAME001.png",
            wapObjectPrototype = Retail2SoldierPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEnemyButton(
            editor = editor,
            text = "Punk Rat Cannon",
            imagePath = "images/CLAW/LEVEL2/IMAGES/CANNON/FRAME001.png",
            wapObjectPrototype = PunkRatCannon,
            tillDetach = tillDetach,
        ),
        createInsertEnemyButton(
            editor = editor,
            text = "La Raux",
            imagePath = "images/CLAW/LEVEL2/IMAGES/RAUX/FRAME001.png",
            wapObjectPrototype = LaRauxPrototype,
            tillDetach = tillDetach,
        ),
    )

    override fun buildBrushesButtons(editor: Editor): List<HTMLWidgetB<*>> =
        emptyList()
}
