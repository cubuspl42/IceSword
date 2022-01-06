package icesword.editor.entities.fixture.prototypes

import icesword.editor.MetaTile
import icesword.editor.entities.WapObjectPropsData
import icesword.geometry.IntVec2
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed class FixturePrototype {
    @Transient
    open val localWapObjects: List<WapObjectPropsData> = emptyList()

    @Transient
    open val localMetaTiles: Map<IntVec2, MetaTile> = emptyMap()
}
