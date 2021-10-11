package icesword.scene

import html.DynamicStyleDeclaration
import html.createHtmlElement
import html.createStyledHtmlElement
import html.onMouseDrag
import icesword.*
import icesword.editor.Editor
import icesword.editor.Elastic
import icesword.editor.MetaTileCluster
import icesword.editor.Tool
import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import kotlinx.css.Cursor
import kotlinx.css.button
import kotlinx.css.style
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement


class ElasticUi(
    private val viewTransform: Cell<IntVec2>,
    private val elastic: Elastic,
) : Node {
    private val metaTileCluster: MetaTileCluster
        get() = elastic.metaTileCluster

    private val localTileCoords = metaTileCluster.localMetaTilesDynamic.getKeys()

    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {

        val viewTransform = this.viewTransform.sample()
        val tileOffset = elastic.tileOffset.sample()
        val size = elastic.size.sample()
        val isSelected = elastic.isSelected.sample()

        val localTileCoords = localTileCoords.volatileContentView

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
            .mergeWith(localTileCoords.changes().units())
}


fun createElasticOverlayElement(
    editor: Editor,
    elastic: Elastic,
    viewport: HTMLElement,
    viewTransform: Cell<IntVec2>,
    tillDetach: Till,
): HTMLElement {
    val boxMoveController = Cell.map2(
        editor.selectedEntity,
        editor.selectedTool,
    ) { selectedEntity, selectedTool ->
        if (selectedEntity == elastic && selectedTool == Tool.MOVE) {
            BoxMoveController(editor)
        } else null
    }

    val boxCursor = boxMoveController.map {
        it?.let { Cursor.move }
    }

    boxCursor.reactTill(tillDetach) {
        println("Box cursor: $it")
    }

    val box = createStyledHtmlElement(
        tagName = "div",
        style = DynamicStyleDeclaration(
            cursor = boxCursor,
        ),
        tillDetach = tillDetach,
    ).apply {
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

    box.onMouseDrag(
        button = 0,
        outer = viewport,
        filterTarget = true,
        till = tillDetach,
    ).reactDynamicNotNullTill(
        dynamicHandler = boxMoveController.mapNotNull { it::handleDrag },
        till = tillDetach,
    )

    fun createHandle(
        cursor: Cursor,
        resize: (tileCoord: Cell<IntVec2>, till: Till) -> Unit,
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

        handle.onMouseDrag(
            button = 0,
            outer = viewport,
            till = tillDetach,
        ).reactTill(tillDetach) { mouseDrag ->
            val initialPosition = mouseDrag.position.sample()

            val deltaTileCoord = mouseDrag.position.map {
                val deltaPosition = it - initialPosition
                deltaPosition.divRound(TILE_SIZE)
            }

            resize(
                deltaTileCoord,
                mouseDrag.tillEnd,
            )
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
            createHandle(
                cursor = Cursor.nwseResize,
                resize = elastic::resizeTopLeft,
            ).apply {
                style.left = offset
                style.top = offset
            },
            createHandle(
                cursor = Cursor.neswResize,
                resize = elastic::resizeTopRight,
            ).apply {
                style.right = offset
                style.top = offset
            },
            createHandle(
                cursor = Cursor.nwseResize,
                resize = elastic::resizeBottomRight,
            ).apply {
                style.right = offset
                style.bottom = offset
            },
            createHandle(
                cursor = Cursor.neswResize,
                resize = elastic::resizeBottomLeft,
            ).apply {
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

class BoxMoveController(
    private val editor: Editor,
) {
    fun handleDrag(mouseDrag: MouseDrag) {
        val world = editor.world

        editor.selectedEntity.sample()?.let { selectedEntity ->
            val worldPosition = world.transformToWorld(mouseDrag.position)
            val initialWorldPosition = worldPosition.sample()
            val tileOffsetDelta = worldPosition.map {
                (it - initialWorldPosition).divRound(TILE_SIZE)
            }

            selectedEntity.move(
                tileOffsetDelta = tileOffsetDelta,
                tillStop = mouseDrag.tillEnd,
            )
        }
    }
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