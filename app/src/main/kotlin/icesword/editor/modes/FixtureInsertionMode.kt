package icesword.editor.modes

import icesword.RezIndex
import icesword.editor.MetaTileLayerProduct
import icesword.editor.World
import icesword.editor.entities.Fixture
import icesword.editor.entities.FixtureProduct
import icesword.editor.modes.InsertionPrototype.FixtureInsertionPrototype
import icesword.editor.retails.Retail
import icesword.frp.map
import icesword.frp.staticSetOf
import icesword.frp.unionWith
import icesword.geometry.IntVec2
import icesword.tileAtPoint

class FixtureInsertionMode(
    private val rezIndex: RezIndex,
    private val retail: Retail,
    private val world: World,
    override val insertionPrototype: FixtureInsertionPrototype,
) : StampInsertionMode<FixtureInsertionMode.FixturePreview>(
    world = world,
) {
    interface FixturePreview {
        val fixtureProduct: FixtureProduct
        val metaTileLayerProduct: MetaTileLayerProduct
    }

    private val fixturePrototype = insertionPrototype.fixturePrototype

    override fun buildPreview(inputMode: StampOverInputMode): FixturePreview = object : FixturePreview {
        override val fixtureProduct: FixtureProduct = FixtureProduct(
            rezIndex = rezIndex,
            retail = retail,
            prototype = fixturePrototype,
            tileOffset = inputMode.stampWorldPosition.map(::tileAtPoint),
        )

        private val metaTileCluster = fixtureProduct.metaTileCluster

        private val metaTileClusters = world.metaTileLayer.metaTileClusters.unionWith(
            staticSetOf(metaTileCluster),
        )

        override val metaTileLayerProduct = MetaTileLayerProduct(
            tileGenerator = retail.tileGenerator,
            metaTileClusters = metaTileClusters,
            globalTileCoords = metaTileCluster.globalTileCoords,
        )
    }

    override fun buildEntity(insertionWorldPoint: IntVec2) = Fixture(
        rezIndex = rezIndex,
        retail = retail,
        prototype = fixturePrototype,
        initialTileOffset = tileAtPoint(insertionWorldPoint),
    )
}
