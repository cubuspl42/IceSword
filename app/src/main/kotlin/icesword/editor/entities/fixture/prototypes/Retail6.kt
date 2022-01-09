package icesword.editor.entities.fixture.prototypes

import icesword.editor.MetaTile
import icesword.editor.retails.Retail6.MetaTiles.ShutterWindow
import icesword.geometry.IntVec2
import kotlinx.serialization.Serializable

@Serializable
object Retail6ShutterWindow : FixturePrototype() {
    override val localMetaTiles: Map<IntVec2, MetaTile> = mapOf(
        IntVec2(0, 0) to ShutterWindow.leftShutter,
        IntVec2(1, 0) to ShutterWindow.core,
        IntVec2(2, 0) to ShutterWindow.rightShutter,
    )
}
