package icesword.editor.entities.fixture.prototypes

import icesword.editor.MetaTile
import icesword.editor.entities.WapObjectPropsData
import icesword.editor.retails.Retail2
import icesword.editor.retails.Retail2.MetaTiles.Tower
import icesword.geometry.IntVec2

object Retail2TowerCannonLeft : FixturePrototype() {
    override val localWapObjects: List<WapObjectPropsData> = listOf(
        WapObjectPropsData(
            logic = "TowerCannonLeft",
            imageSet = "LEVEL_TOWERCANNONLEFT",
            x = 28,
            y = 76,
            z = 0,
            i = -1,
        )
    )

    override val localMetaTiles: Map<IntVec2, MetaTile> = mapOf(
        IntVec2(0, 0) to Tower.CannonLeft.topLeft, IntVec2(1, 0) to Tower.CannonLeft.topRight,
        IntVec2(0, 1) to Tower.CannonLeft.centerLeft, IntVec2(1, 1) to Tower.CannonLeft.centerRight,
        IntVec2(0, 2) to Tower.CannonLeft.bottomLeft, IntVec2(1, 2) to Tower.CannonLeft.bottomRight,
    )
}

object Retail2TowerCannonRight : FixturePrototype() {
    override val localWapObjects: List<WapObjectPropsData> = listOf(
        WapObjectPropsData(
            logic = "TowerCannonLeft",
            imageSet = "LEVEL_TOWERCANNONRIGHT",
            x = 101,
            y = 77,
            z = 0,
            i = -1,
        )
    )

    override val localMetaTiles: Map<IntVec2, MetaTile> = mapOf(
        IntVec2(0, 0) to Tower.CannonRight.topLeft, IntVec2(1, 0) to Tower.CannonRight.topRight,
        IntVec2(0, 1) to Tower.CannonRight.centerLeft, IntVec2(1, 1) to Tower.CannonRight.centerRight,
        IntVec2(0, 2) to Tower.CannonRight.bottomLeft, IntVec2(1, 2) to Tower.CannonRight.bottomRight,
    )
}

object Retail2TowerWindow : FixturePrototype() {
    override val localMetaTiles: Map<IntVec2, MetaTile> = mapOf(
        IntVec2(0, 0) to Tower.window
    )
}
