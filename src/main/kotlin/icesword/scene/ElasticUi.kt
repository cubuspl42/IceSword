package icesword.scene

import icesword.TILE_SIZE
import icesword.editor.Elastic
import icesword.editor.MetaTileCluster
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.tileAtPoint
import icesword.tileRect
import icesword.tileTopLeftCorner
import org.w3c.dom.CanvasRenderingContext2D


class ElasticUi(
    private val viewTransform: Cell<IntVec2>,
    private val elastic: Elastic,
) : Node {
    private val metaTileCluster: MetaTileCluster
        get() = elastic.metaTileCluster

    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {

        val viewTransform = this.viewTransform.sample()
        val tileOffset = elastic.tileOffset.sample()
        val size = elastic.size.sample()
        val isSelected = elastic.isSelected.sample()

        val localTileCoords = metaTileCluster.localMetaTilesDynamic.keys.volatileContentView

        ctx.strokeStyle = if (isSelected) "red" else "rgba(103, 103, 131, 0.3)"

        localTileCoords.forEach { localTileCoord ->
            val globalTileCoord = tileOffset + localTileCoord
            val rect = tileRect(globalTileCoord).translate(viewTransform)

            ctx.lineWidth = 1.0

            ctx.strokeRect(
                x = rect.xMin.toDouble(),
                y = rect.yMin.toDouble(),
                w = rect.width.toDouble(),
                h = rect.height.toDouble(),
            )
        }

        val sizeRect = IntRect(
            tileTopLeftCorner(tileOffset),
            size * TILE_SIZE,
        ).translate(viewTransform)

        ctx.lineWidth = 4.0

        ctx.strokeRect(
            x = sizeRect.xMin.toDouble(),
            y = sizeRect.yMin.toDouble(),
            w = sizeRect.width.toDouble(),
            h = sizeRect.height.toDouble(),
        )
    }

    override val onDirty: Stream<Unit> =
        viewTransform.values().units()
            .mergeWith(metaTileCluster.localMetaTilesDynamic.changesUnits())
            .mergeWith(elastic.tileOffset.values().units())
            .mergeWith(elastic.size.values().units())
            .mergeWith(elastic.isSelected.values().units())
}
