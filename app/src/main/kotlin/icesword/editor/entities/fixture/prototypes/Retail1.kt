package icesword.editor.entities.fixture.prototypes

import icesword.editor.MetaTile
import icesword.editor.entities.WapObjectPropsData
import icesword.geometry.IntVec2

object Retail1NicheTorch : FixturePrototype() {
    override val localWapObjects: List<WapObjectPropsData> = listOf(
        WapObjectPropsData(
            logic = "BehindAniCandy",
            imageSet = "LEVEL_TORCH",
            x = 31,
            y = 43,
            z = 5000,
            i = -1,
        )
    )

    override val localMetaTiles: Map<IntVec2, MetaTile> = mapOf(
        IntVec2.ZERO to MetaTile(926),
    )
}

object Retail1DoorRight : FixturePrototype() {
    override val localMetaTiles: Map<IntVec2, MetaTile> = mapOf(
        IntVec2(0, 0) to MetaTile(401), IntVec2(1, 0) to MetaTile(402),
        IntVec2(0, 1) to MetaTile(403), IntVec2(1, 1) to MetaTile(404),
    )
}
