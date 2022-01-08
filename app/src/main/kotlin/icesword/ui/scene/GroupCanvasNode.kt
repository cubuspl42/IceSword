package icesword.ui.scene

import icesword.frp.Stream
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.changesUnits
import icesword.frp.dynamic_list.mergeBy
import icesword.frp.mergeWith
import icesword.geometry.IntRect
import icesword.ui.CanvasNode
import org.w3c.dom.CanvasRenderingContext2D

open class GroupCanvasNode(
    private val children: DynamicList<CanvasNode>,
) : CanvasNode {
    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        children.volatileContentView.forEach {
            ctx.save()

            it.draw(ctx = ctx, windowRect = windowRect)

            ctx.restore()
        }
    }

    override val onDirty: Stream<Unit> =
        children.changesUnits().mergeWith(children.mergeBy { it.onDirty })
}
