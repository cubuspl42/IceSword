package icesword.editor

import icesword.RezIndex
import icesword.editor.InsertionPrototype.ElasticInsertionPrototype
import icesword.editor.InsertionPrototype.KnotMeshInsertionPrototype
import icesword.editor.InsertionPrototype.WapObjectInsertionPrototype
import icesword.frp.MutableDynamicSet
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.tileAtPoint

sealed interface InsertionPrototype {
    value class ElasticInsertionPrototype(
        val elasticPrototype: ElasticPrototype,
    ) : InsertionPrototype

    value class KnotMeshInsertionPrototype(
        val knotPrototype: KnotPrototype,
    ) : InsertionPrototype

    value class WapObjectInsertionPrototype(
        val wapObjectPrototype: WapObjectPrototype,
    ) : InsertionPrototype
}

interface InsertionMode : EditorMode {
    val insertionPrototype: InsertionPrototype

    fun insert(insertionWorldPoint: IntVec2)
}

class ElasticInsertionMode(
    private val metaTileLayer: MetaTileLayer,
    override val insertionPrototype: ElasticInsertionPrototype,
) : InsertionMode {
    override fun insert(insertionWorldPoint: IntVec2) {
        val elasticPrototype = insertionPrototype.elasticPrototype

        val elastic = Elastic(
            prototype = elasticPrototype,
            initialBounds = IntRect(
                position = tileAtPoint(insertionWorldPoint),
                size = elasticPrototype.defaultSize,
            ),
        )

        metaTileLayer.insertElastic(elastic)
    }
}

class KnotMeshInsertionMode(
    private val knotMeshLayer: KnotMeshLayer,
    override val insertionPrototype: KnotMeshInsertionPrototype,
) : InsertionMode {
    override fun insert(insertionWorldPoint: IntVec2) {
        val knotMesh = KnotMesh.createSquare(
            initialTileOffset = tileAtPoint(insertionWorldPoint),
            knotPrototype = insertionPrototype.knotPrototype,
            initialSideLength = 2,
        )

        knotMeshLayer.insertKnotMesh(knotMesh)
    }
}

class WapObjectInsertionMode(
    private val rezIndex: RezIndex,
    private val wapObjects: MutableDynamicSet<WapObject>,
    override val insertionPrototype: WapObjectInsertionPrototype,
) : InsertionMode {
    override fun insert(insertionWorldPoint: IntVec2) {
        wapObjects.add(
            WapObject(
                rezIndex = rezIndex,
                wapObjectPrototype = insertionPrototype.wapObjectPrototype,
                initialPosition = insertionWorldPoint,
            )
        )
    }
}
