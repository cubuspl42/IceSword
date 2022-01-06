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
