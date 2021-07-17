package icesword.scene

import icesword.geometry.IntVec2
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.Image
import org.w3c.dom.Node as DomNode

class Texture(
    val image: Image,
)

class Tileset(
    val tileTextures: Map<Int, Texture>,
)

interface Node {
    val htmlElement: HTMLElement
}

class TileLayer(
    val tileset: Tileset,
    val tiles: Map<IntVec2, Int>,
) : Node {
    override val htmlElement: HTMLElement = run {
        fun <A> toRows(map: Map<IntVec2, A>): List<List<A?>> {
            val keys = map.keys

            val minX = keys.map { it.x }.minOrNull() ?: 0
            val maxX = keys.map { it.x }.maxOrNull() ?: -1

            val minY = keys.map { it.y }.minOrNull() ?: 0
            val maxY = keys.map { it.y }.maxOrNull() ?: -1

            return (minY..maxY).map { y ->
                (minX..maxX).map { x -> map[IntVec2(x, y)] }
            }
        }

        fun table(rows: List<DomNode>): HTMLElement =
            createHtmlElement("div").apply {
                style.display = "table"
                rows.forEach(::appendChild)
            }

        fun tr(cells: List<DomNode>): HTMLElement =
            createHtmlElement("div").apply {
                style.display = "table-row"
                cells.forEach(::appendChild)
            }

        fun td(child: DomNode): HTMLElement =
            createHtmlElement("div").apply {
                style.display = "table-cell"
                appendChild(child)
            }

        fun tileImg(tileId: Int): DomNode {
            val imageNode = tileset.tileTextures[tileId]?.image?.cloneNode() ?: Image()
            val image = imageNode as HTMLElement
            return image.apply {
                style.display = "block"
            }
        }

        table(
            toRows(tiles).map { tileIdRow ->
                tr(tileIdRow.map { tileId ->
                    td(tileImg(tileId ?: 1))
                })
            }
        ).apply {
            style.position = "absolute"
        }
    }

}

class SceneContext {
    fun createTexture(image: Image): Texture =
        Texture(image)
}

fun scene(
    builder: (SceneContext) -> Node
): HTMLElement {
    val context = SceneContext()
    val root = builder(context)
    return  root.htmlElement
}

private fun createHtmlElement(tagName: String): HTMLElement =
    document.createElement(tagName) as HTMLElement
