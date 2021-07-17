package icesword

import icesword.geometry.IntVec2
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Image

fun worldView(world: World): HTMLElement {
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

    fun table(rows: List<HTMLElement>): HTMLElement =
        createHtmlElement("div").apply {
            style.display = "table"
            rows.forEach(::appendChild)
        }

    fun tr(cells: List<HTMLElement>): HTMLElement =
        createHtmlElement("div").apply {
            style.display = "table-row"
            cells.forEach(::appendChild)
        }

    fun td(child: HTMLElement): HTMLElement =
        createHtmlElement("div").apply {
            style.display = "table-cell"
            appendChild(child)
        }

    fun tileImg(tileId: Int): HTMLElement {
        val tileIdStr = tileId.toString().padStart(3, '0')
        return Image().apply {
            src = "images/CLAW/LEVEL3/TILES/ACTION/$tileIdStr.png"
            style.display = "block"
        }
    }

    return table(
        toRows(world.tiles).map { tileIdRow ->
            tr(tileIdRow.map { tileId ->
                td(tileImg(tileId ?: 1))
            })
        }
    ).apply {
        style.position = "absolute"
    }
}

private fun createHtmlElement(tagName: String): HTMLElement =
    document.createElement(tagName) as HTMLElement
