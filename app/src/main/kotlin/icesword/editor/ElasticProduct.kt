package icesword.editor

import icesword.RezIndex
import icesword.TILE_SIZE
import icesword.editor.retails.Retail
import icesword.frp.Cell
import icesword.frp.DynamicMap
import icesword.frp.diffMap
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.map
import icesword.frp.map
import icesword.geometry.IntRect

open class ElasticProduct(
    rezIndex: RezIndex,
    retail: Retail,
    private val generator: ElasticGenerator,
    val tileBounds: Cell<IntRect>,
) {
    val pixelBounds = tileBounds.map { it * TILE_SIZE }

    val pixelBoundsTopLeft = pixelBounds.map { it.topLeft }

    val boundsTopLeft = tileBounds.map { it.topLeft }

    val size = tileBounds.map { it.size }

    private val generatorOutput = size.map {
        generator.buildOutput(size = it)
    }

    val localWapObjects: DynamicList<WapObjectPropsData> =
        generatorOutput.diffMap { it.localWapObjects }

    val metaTileCluster = MetaTileCluster(
        tileOffset = boundsTopLeft,
        localMetaTiles = DynamicMap.diff(
            generatorOutput.map { it.localMetaTiles },
            tag = "metaTileCluster.localMetaTilesDynamic",
        )
    )

    val wapObjectSprites = this.localWapObjects.map { localWapObject ->
        DynamicWapSprite.fromImageSet(
            rezIndex = rezIndex,
            imageSetId = expandImageSetId(
                retail = retail,
                shortImageSetId = localWapObject.imageSet,
            ),
            position = pixelBoundsTopLeft.map { it + localWapObject.position },
            i = localWapObject.i,
        )
    }
}
