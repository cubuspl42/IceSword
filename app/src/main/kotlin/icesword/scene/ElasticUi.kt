package icesword.scene

import icesword.RezTextureBank
import icesword.RezIndex
import icesword.TILE_SIZE
import icesword.editor.Editor
import icesword.editor.Elastic
import icesword.editor.Entity
import icesword.editor.MetaTileCluster
import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.changesUnits
import icesword.frp.dynamic_list.map
import icesword.frp.getKeys
import icesword.frp.map
import icesword.frp.mergeWith
import icesword.frp.reactTill
import icesword.frp.units
import icesword.frp.values
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.html.DynamicStyleDeclaration
import icesword.html.MouseButton
import icesword.html.createSvgCircle
import icesword.html.createSvgGroup
import icesword.html.createSvgRect
import icesword.html.onMouseDrag
import icesword.tileRect
import kotlinx.css.Cursor
import kotlinx.css.PointerEvents
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement


private class ElasticUi private constructor(
    private val viewTransform: DynamicTransform,
    private val elastic: Elastic,
    private val isSelected: Cell<Boolean>,
) : CanvasNode {
    constructor(
        editor: Editor,
        viewTransform: DynamicTransform,
        elastic: Elastic,
    ) : this(
        viewTransform = viewTransform,
        elastic = elastic,
        isSelected = editor.isEntitySelected(elastic)
    )

    private val metaTileCluster: MetaTileCluster
        get() = elastic.metaTileCluster

    private val localTileCoords = metaTileCluster.localMetaTiles.getKeys()

    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {

        val viewTransform = this.viewTransform.transform.sample()
        val tileBounds = elastic.tileBounds.sample()
        val pixelBounds = tileBounds * TILE_SIZE

        val isSelected = isSelected.sample()

        ctx.strokeStyle = if (isSelected) "red" else "rgba(103, 103, 131, 0.3)"

        tileBounds.points().forEach { globalTileCoord ->
            val viewTileRect = viewTransform.transform(tileRect(globalTileCoord))

            ctx.lineWidth = 1.0

            ctx.strokeRect(
                x = viewTileRect.xMin.toDouble(),
                y = viewTileRect.yMin.toDouble(),
                w = viewTileRect.width.toDouble(),
                h = viewTileRect.height.toDouble(),
            )
        }

        val viewBounds = viewTransform.transform(pixelBounds)

        ctx.lineWidth = 4.0

        ctx.strokeRect(
            x = viewBounds.xMin.toDouble(),
            y = viewBounds.yMin.toDouble(),
            w = viewBounds.width.toDouble(),
            h = viewBounds.height.toDouble(),
        )
    }

    override val onDirty: Stream<Unit> =
        viewTransform.transform.values().units()
            .mergeWith(metaTileCluster.localMetaTiles.changesUnits())
            .mergeWith(elastic.tileBounds.values().units())
            .mergeWith(isSelected.values().units())
            .mergeWith(localTileCoords.changes.units())
}

class ElasticNode(
    private val rezIndex: RezIndex,
    private val editor: Editor,
    private val elastic: Elastic,
    private val viewTransform: DynamicTransform,
) : HybridNode() {
    override fun buildCanvasNode(
        textureBank: RezTextureBank,
    ): CanvasNode = GroupCanvasNode(
        children = elastic.wapObjectSprites.map {
            WapSpriteNode(
                editorTextureBank = editor.editorTextureBank,
                textureBank = textureBank,
                wapSprite = it,
            )
        }
    )

    override fun buildOverlayElement(
        context: HybridNode.OverlayBuildContext,
    ): SVGElement = context.run {
        createElasticOverlayElement(
            editor = editor,
            svg = svg,
            viewport = viewport,
            viewTransform = viewTransform,
            elastic = elastic,
            tillDetach = tillDetach,
        )
    }
}


private fun createElasticOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: DynamicTransform,
    elastic: Elastic,
    tillDetach: Till,
): SVGElement {
    val relativeViewBounds = viewTransform.transform(elastic.pixelBounds)
        .map { it.copy(position = IntVec2.ZERO) }

    val rootTranslate = viewTransform.transform(elastic.position)

    val isSelected = editor.isEntitySelected(elastic)

    val pointerEvents = isSelected.map {
        if (it) null else PointerEvents.none
    }

    val box = createEntityFrameElement(
        editor = editor,
        svg = svg,
        outer = viewport,
        entity = elastic,
        boundingBox = relativeViewBounds,
        tillDetach = tillDetach,
    )

    val handleStroke = isSelected.map {
        if (it) "red" else "none"
    }

    fun createHandle(
        cursor: Cursor,
        resize: (tileCoord: Cell<IntVec2>, till: Till) -> Unit,
        corner: (IntRect) -> IntVec2,
    ): SVGElement {
        val handle = createSvgCircle(
            svg = svg,
            translate = relativeViewBounds.map(corner),
            radius = 8.0f,
            stroke = handleStroke,
            style = DynamicStyleDeclaration(
                pointerEvents = pointerEvents,
                cursor = Cell.constant(cursor),
            ),
            tillDetach = tillDetach,
        ).apply {
            setAttributeNS(null, "fill", "grey")
            setAttributeNS(null, "fill-opacity", "0.5")
            setAttributeNS(null, "stroke-width", "3")

            style.apply {
                boxSizing = "border-box"
                backgroundColor = "grey"
            }
        }

        handle.onMouseDrag(
            button = MouseButton.Primary,
            outer = viewport,
            till = tillDetach,
        ).reactTill(tillDetach) { mouseDrag ->
            val worldPosition = viewTransform.inversed.transform(mouseDrag.position)

            val initialPosition = worldPosition.sample()

            // TODO: Support dragging handle and camera at the same time
            val deltaTileCoord = worldPosition.map {
                val deltaPosition = it - initialPosition
                deltaPosition.divRound(TILE_SIZE)
            }

            resize(
                deltaTileCoord,
                mouseDrag.tillEnd,
            )
        }

        return handle
    }

    fun buildHandles(): List<SVGElement> {
        val handles = listOf(
            createHandle(
                cursor = Cursor.nwseResize,
                resize = elastic::resizeTopLeft,
                corner = { it.topLeft },
            ),
            createHandle(
                cursor = Cursor.neswResize,
                resize = elastic::resizeTopRight,
                corner = { it.topRight },
            ),
            createHandle(
                cursor = Cursor.nwseResize,
                resize = elastic::resizeBottomRight,
                corner = { it.bottomRight },
            ),
            createHandle(
                cursor = Cursor.neswResize,
                resize = elastic::resizeBottomLeft,
                corner = { it.bottomLeft },
            )
        )

        return handles
    }

    val group = createSvgGroup(
        svg = svg,
        translate = rootTranslate,
        tillDetach = tillDetach,
    )

    group.appendChild(box)
    buildHandles().forEach(group::appendChild)

    return group
}

fun createEntityFrameElement(
    editor: Editor,
    svg: SVGSVGElement,
    outer: HTMLElement,
    entity: Entity,
    boundingBox: Cell<IntRect>,
    tillDetach: Till,
): SVGElement =
    createDraggableOverlayElement(
        editor = editor,
        entity = entity,
        outer = outer,
        till = tillDetach,
    ) { context ->
        val isAreaSelectionCovered = editor.isAreaSelectionCovered(entity)

        val isSelected = editor.isEntitySelected(entity)

        val stroke = Cell.map2(
            isSelected,
            isAreaSelectionCovered,
        ) { isSel, isCovered ->
            when {
                isCovered -> "orange"
                isSel -> "red"
                else -> "none"
            }
        }

        val box = createSvgRect(
            svg = svg,
            size = boundingBox.map { it.size },
            translate = boundingBox.map { it.position },
            strokeString = stroke,
            style = DynamicStyleDeclaration(
                pointerEvents = context.map { it.pointerEvents },
                cursor = context.map { it.cursor },
            ),
            tillDetach = tillDetach,
        ).apply {
            setAttributeNS(null, "fill-opacity", "0")
        }

        box
    }
