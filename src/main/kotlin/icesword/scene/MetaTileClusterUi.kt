package icesword.scene

import icesword.editor.MetaTileCluster
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.tileRect
import org.w3c.dom.CanvasRenderingContext2D


class MetaTileClusterUi(
    private val viewTransform: Cell<IntVec2>,
    private val metaTileCluster: MetaTileCluster,
) : Node {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        val viewTransform = this.viewTransform.sample()
        val tileOffset = metaTileCluster.tileOffset.sample()
        val isSelected = metaTileCluster.isSelected.sample()

        val localTileCoords = metaTileCluster.localMetaTiles.keys

        localTileCoords.forEach { localTileCoord ->
            val globalTileCoord = tileOffset + localTileCoord
            val rect = tileRect(globalTileCoord).translate(viewTransform)

            ctx.strokeStyle = if (isSelected) "red" else "rgba(103, 103, 131, 0.3)"
            ctx.lineWidth = 2.0

            ctx.strokeRect(
                x = rect.xMin.toDouble(),
                y = rect.yMin.toDouble(),
                w = rect.width.toDouble(),
                h = rect.height.toDouble(),
            )
        }
    }

    override val onDirty: Stream<Unit> =
        viewTransform.values().units()
            .mergeWith(metaTileCluster.tileOffset.values().units())
            .mergeWith(metaTileCluster.isSelected.values().units())
}
