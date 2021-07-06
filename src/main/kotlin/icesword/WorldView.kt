package icesword

import icesword.geometry.IntVec2
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Image

fun worldView(world: World): Element {
    fun <A> toRows(map: Map<IntVec2, A>): List<List<A?>> {
        val keys = map.keys

        val minX = keys.map { it.x }.minOrNull() ?: 0
        val maxX = keys.map { it.x }.maxOrNull() ?: 0

        val minY = keys.map { it.y }.minOrNull() ?: 0
        val maxY = keys.map { it.y }.maxOrNull() ?: 0

        return (minY..maxY).map { y ->
            (minX..maxX).map { x -> map[IntVec2(x, y)] }
        }
    }

    fun table(rows: List<Element>): Element =
        document.createElement("table").apply {
            rows.forEach(::appendChild)
        }

    fun tr(cells: List<Element>): Element =
        document.createElement("tr").apply {
            cells.forEach(::appendChild)
        }

    fun td(child: Element): Element =
        document.createElement("td").apply {
            appendChild(child)
        }

    fun tileImg(tileId: Int): Element {
        val tileIdStr = tileId.toString().padStart(3, '0')
        return Image().apply {
            src = "images/CLAW/LEVEL2/TILES/ACTION/$tileIdStr.png"
        }
    }

    return table(
        toRows(world.tiles).map { tileIdRow ->
            tr(tileIdRow.map { tileId ->
                td(tileImg(tileId ?: 1))
            })
        }
    )
}
