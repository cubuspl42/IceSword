package icesword.scene

import icesword.frp.Stream
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.changesUnits
import icesword.frp.dynamic_list.mergeBy
import icesword.frp.mergeWith
import icesword.frp.units
import icesword.frp.values
import icesword.geometry.IntRect
import org.w3c.dom.CanvasRenderingContext2D

class GroupCanvasNode(
    private val children: DynamicList<CanvasNode>,
) : CanvasNode {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        ctx.save()

        children.volatileContentView.forEach {
            it.draw(ctx = ctx, windowRect = windowRect)
        }

        ctx.restore()
    }

    override val onDirty: Stream<Unit> =
        children.changesUnits().mergeWith(children.mergeBy { it.onDirty })
}
