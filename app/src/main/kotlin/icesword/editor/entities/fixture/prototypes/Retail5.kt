package icesword.editor.entities.fixture.prototypes

import icesword.editor.entities.WapObjectPropsData
import kotlinx.serialization.Serializable

@Serializable
object Retail5WallCover : FixturePrototype() {
    override val localWapObjects: List<WapObjectPropsData> = listOf(
        WapObjectPropsData(
            logic = "BehindCandy",
            imageSet = "LEVEL_BRICKBACKWALLCOVER",
            x = 32,
            y = 41,
            z = 1000,
            i = -1,
        )
    )
}
