package icesword.scene

import icesword.*
import icesword.editor.Elastic
import icesword.editor.MetaTileCluster
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import kotlinx.css.Cursor
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement


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


fun createElasticOverlayElement(
    elastic: Elastic,
    viewTransform: Cell<IntVec2>,
    tillDetach: Till,
): HTMLElement {
    val box = createHtmlElement("div").apply {
        style.apply {
            position = "absolute"

            boxSizing = "border-box"
            width = "64px"
            height = "64px"

            borderStyle = "dashed"
            borderRadius = "4px"
            borderColor = "red"
        }
    }

    fun createHandle(
        cursor: Cursor,
    ): HTMLElement {
        val handle = createHtmlElement("div").apply {
            style.apply {
                transform = "translate(-50%,-50%)"

                boxSizing = "border-box"
                width = "16px"
                height = "16px"

                borderStyle = "solid"
                borderRadius = "50%"
                borderColor = "red"

                backgroundColor = "grey"

            }

            style.cursor = cursor.toString()
        }

        val wrapper = createHtmlElement("div").apply {

            style.apply {
                position = "absolute"
                width = "0"
                height = "0"

                appendChild(handle)
            }
        }

        return wrapper
    }

    fun buildHandles(): List<HTMLElement> {
        val offset = "-2px"

        val handles = listOf(
            createHandle(cursor = Cursor.nwseResize).apply {
                style.left = offset
                style.top = offset
            },
            createHandle(cursor = Cursor.neswResize).apply {
                style.right = offset
                style.top = offset
            },
            createHandle(cursor = Cursor.nwseResize).apply {
                style.right = offset
                style.bottom = offset
            },
            createHandle(cursor = Cursor.neswResize).apply {
                style.left = offset
                style.bottom = offset
            }
        )

        return handles
    }

    buildHandles().forEach(box::appendChild)

    linkTranslate(
        box,
        translate = Cell.map2(
            viewTransform,
            elastic.position,
        ) { vt, ep ->
            ep + vt
        },
        tillDetach,
    )

    linkSize(
        box,
        size = elastic.size.map { it * TILE_SIZE },
        tillDetach,
    )

    return box
}

private fun linkTranslate(
    element: HTMLElement,
    translate: Cell<IntVec2>,
    till: Till,
) {
    translate.reactTill(till) {
        element.style.transform = "translate(${it.x}px, ${it.y}px)"
    }
}

private fun linkSize(
    element: HTMLElement,
    size: Cell<IntSize>,
    till: Till,
) {
    size.reactTill(till) {
        element.style.apply {
            width = "${it.width}px"
            height = "${it.height}px"
        }
    }
}