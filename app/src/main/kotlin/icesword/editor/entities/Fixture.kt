@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor.entities

import icesword.RezIndex
import icesword.editor.IntVec2Serializer
import icesword.editor.MetaTileCluster
import icesword.editor.entities.fixture.prototypes.FixturePrototype
import icesword.editor.retails.Retail
import icesword.frp.Cell
import icesword.frp.DynamicMap
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.concatWith
import icesword.frp.dynamic_list.fuseBy
import icesword.frp.dynamic_list.sampleContent
import icesword.frp.dynamic_list.withAppended
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.tileTopLeftCorner
import icesword.wwd.Wwd
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class FixtureProduct(
    rezIndex: RezIndex,
    retail: Retail,
    prototype: FixturePrototype,
    tileOffset: Cell<IntVec2>,
) {
    val pixelOffset = tileOffset.map(::tileTopLeftCorner)

    val metaTileCluster = MetaTileCluster(
        tileOffset = tileOffset,
        localMetaTiles = DynamicMap.of(prototype.localMetaTiles),
    )

    val wapObjectSprites = produceWapObjectSprites(
        rezIndex = rezIndex,
        retail = retail,
        localWapObjects = DynamicList.of(prototype.localWapObjects),
        pixelOffset = pixelOffset,
    )

    val boundingBox: Cell<IntRect> = IntRect.enclosing(
        wapObjectSprites.fuseBy { it.boundingBox }
            .concatWith(DynamicList.ofSingle(metaTileCluster.boundingBox))
    )
}

class Fixture(
    rezIndex: RezIndex,
    retail: Retail,
    private val prototype: FixturePrototype,
    initialTileOffset: IntVec2,
) : Entity(),
    WapObjectExportable {

    companion object {
        fun load(
            rezIndex: RezIndex,
            retail: Retail,
            data: FixtureData,
        ): Fixture = Fixture(
            rezIndex = rezIndex,
            retail = retail,
            prototype = data.prototype,
            initialTileOffset = data.tileOffset,
        )
    }

    override val entityPosition = SimpleEntityTilePosition(
        initialTileOffset = initialTileOffset,
    )

    override val zOrder: Cell<Int> = Cell.constant(0)

    private val tileOffset = entityPosition.tileOffset

    val product = FixtureProduct(
        rezIndex = rezIndex,
        retail = retail,
        prototype = prototype,
        tileOffset = tileOffset,
    )

    override fun isSelectableIn(area: IntRect): Boolean =
        product.boundingBox.sample().overlaps(area)

    override fun exportWapObjects(): List<Wwd.Object_> {
        val pixelOffset = product.pixelOffset.sample()

        return prototype.localWapObjects.map {
            it.copy(
                x = it.x + pixelOffset.x,
                y = it.y + pixelOffset.y,
            ).toWwdObject()
        }
    }

    override fun toEntityData(): FixtureData = FixtureData(
        prototype = prototype,
        tileOffset = tileOffset.sample(),
    )

    override fun toString(): String = "Fixture()"
}

@Serializable
data class FixtureData(
    val prototype: FixturePrototype,
    val tileOffset: IntVec2,
) : EntityData()
