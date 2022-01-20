package icesword.editor.entities

import icesword.RezIndex
import icesword.TILE_SIZE
import icesword.editor.DynamicWapSprite
import icesword.editor.MetaTileCluster
import icesword.editor.retails.Retail
import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.DynamicMap
import icesword.frp.diffMap
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.map
import icesword.frp.map
import icesword.geometry.IntRect
import icesword.geometry.IntVec2

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

    val wapObjectSprites = produceWapObjectSprites(
        rezIndex = rezIndex,
        retail = retail,
        localWapObjects = this.localWapObjects,
        pixelOffset = pixelBoundsTopLeft,
    )
}

fun produceWapObjectSprites(
    rezIndex: RezIndex,
    retail: Retail,
    localWapObjects: DynamicList<WapObjectPropsData>,
    pixelOffset: Cell<IntVec2>,
): DynamicList<DynamicWapSprite> = localWapObjects.map { localWapObject ->
    DynamicWapSprite.fromImageSet(
        rezIndex = rezIndex,
        imageSetId = expandImageSetId(
            retail = retail,
            shortImageSetId = localWapObject.imageSet,
        ),
        position = pixelOffset.map { it + localWapObject.position },
        i = localWapObject.i,
        z = constant(localWapObject.z),
    )
}
