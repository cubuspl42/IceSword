package icesword.editor.entities.fixture.prototypes

import icesword.editor.MetaTile
import icesword.editor.entities.WapObjectPropsData
import icesword.geometry.IntVec2
import kotlinx.serialization.Serializable

@Serializable
sealed class FixturePrototype {
    open val localWapObjects: List<WapObjectPropsData> = emptyList()

    abstract val localMetaTiles: Map<IntVec2, MetaTile>
}
