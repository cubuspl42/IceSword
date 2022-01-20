package icesword.ui

import icesword.frp.Cell
import icesword.frp.DynamicView
import icesword.frp.MutCell
import icesword.frp.Stream
import icesword.frp.StreamSink
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.hold
import icesword.frp.mapTo
import icesword.frp.mergeWith
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.html.HTMLWidgetB
import icesword.html.alsoTillDetach
import icesword.html.createCanvas
import icesword.ui.world_view.scene.GroupCanvasNode
import icesword.ui.world_view.scene.base.HybridNode
import icesword.ui.world_view.scene.setTransformT
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.DOMRect
import kotlin.math.roundToInt

fun createCanvasView(root: CanvasNode): HTMLWidgetB<*> {
    val canvasSize = MutCell(IntSize(1024, 1024))

    return createCanvas(size = canvasSize).alsoTillDetach { canvas, tillDetach ->
        val context2D = canvas.getContext2D()

        val onDrawn = StreamSink<Unit>()

        val isDirty = root.onDirty.mapTo(true)
            .mergeWith(onDrawn.mapTo(false))
            .hold(false, till = tillDetach)

        fun resizeCanvasIfNeeded() {
            val rectSize = canvas.element.getBoundingClientRect().size

            if (canvasSize.sample() != rectSize) {
                canvasSize.set(rectSize)
            }
        }

        fun drawScene() {
            val viewportRect = canvasSize.sample().toRect()

            context2D.resetTransform()

            context2D.clearRect(
                x = viewportRect.xMin.toDouble(),
                y = viewportRect.yMin.toDouble(),
                w = viewportRect.width.toDouble(),
                h = viewportRect.height.toDouble(),
            )

            root.draw(
                ctx = context2D,
                windowRect = viewportRect,
            )
        }

        fun handleAnimationFrame() {
            resizeCanvasIfNeeded()

            if (isDirty.sample()) {
                drawScene()

                onDrawn.send(Unit)
            }
        }

        fun requestAnimationFrames() {
            window.requestAnimationFrame {
                if (!tillDetach.wasReached()) {
                    handleAnimationFrame()
                    requestAnimationFrames()
                }
            }
        }

        drawScene()

        requestAnimationFrames()
    }
}

interface CanvasNode {
    companion object {
        fun ofView(drawerView: DynamicView<CanvasDrawer>): CanvasNode = object : CanvasNode {
            override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
                drawerView.view.draw(
                    ctx = ctx,
                    windowRect = windowRect,
                )
            }

            override val onDirty: Stream<Unit> = drawerView.updates
        }

        fun switch(node: Cell<CanvasNode?>): CanvasNode = GroupCanvasNode(
            children = DynamicList.ofSingle(node),
        )

        fun group(
            children: DynamicList<CanvasNode>,
        ): CanvasNode = GroupCanvasNode(
            children = children,
        )

        fun transform(
            node: CanvasNode,
            viewTransform: DynamicTransform,
        ): CanvasNode = object : CanvasNode {
            override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
                val transform = viewTransform.transform.sample()

                val inverseTransform = transform.inversed

                val newWindowRect = inverseTransform.transform(windowRect)

                ctx.setTransformT(transform)

                node.draw(ctx = ctx, windowRect = newWindowRect)
            }

            override val onDirty: Stream<Unit> = node.onDirty
        }

        fun transform(
            children: DynamicList<CanvasNode>,
            viewTransform: DynamicTransform,
        ): CanvasNode = transform(
            node = CanvasNode.group(
                children = children,
            ),
            viewTransform = viewTransform,
        )
    }

    /**
     * @param windowRect viewport rectangle transformed to the source coordinate
     * system, i.e. the system that the coordinates passed to rendering context
     * methods are expressed in.
     *
     * Can be used for optimizing out some rendering calls if it's obvious that
     * an element has no chance of being even partially included in the
     * viewport.
     *
     * All [CanvasNode] implementations that have children and apply
     * transformations to the rendering context need to make this invariant
     * hold.
     */
    fun draw(
        ctx: CanvasRenderingContext2D,
        windowRect: IntRect,
    )

    val onDirty: Stream<Unit>
}

fun CanvasNode.asHybridNode() = object : HybridNode() {
    override fun buildCanvasNode(context: CanvasNodeBuildContext): CanvasNode =
        this@asHybridNode
}

interface CanvasDrawer {
    fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect)
}

private val DOMRect.size: IntSize
    get() = IntSize(
        this.width.roundToInt(),
        this.height.roundToInt(),
    )
