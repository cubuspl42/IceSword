package icesword.ui.retails.retail2

import icesword.createInsertElasticButton
import icesword.createInsertEnemyButton
import icesword.createInsertWapObjectButton
import icesword.editor.elastic.prototype.Retail2DoublePilePrototype
import icesword.editor.Editor
import icesword.editor.elastic.prototype.Retail2PlatformPrototype
import icesword.editor.elastic.prototype.Retail2TowerTopPrototype
import icesword.editor.retails.Retail2
import icesword.editor.wap_object.prototype.Retail2Health
import icesword.editor.wap_object.prototype.LaRauxPrototype
import icesword.editor.wap_object.prototype.PunkRatCannon
import icesword.editor.wap_object.prototype.Retail2OfficerPrototype
import icesword.editor.wap_object.prototype.Retail2PowderKeg
import icesword.editor.wap_object.prototype.Retail2SoldierPrototype
import icesword.frp.Till
import icesword.html.HTMLWidgetB
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
            prototype = Retail2TowerTopPrototype,
            retail = retail,
            imagePath = "images/CLAW/LEVEL2/TILES/ACTION/071.png",
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
