package icesword.editor.elastic

import icesword.editor.ElasticGenerator
import icesword.editor.MetaTile
import icesword.geometry.IntSize
import icesword.geometry.IntVec2


data class ElasticRectangularFragment(
    val metaTiles: List<MetaTile>,
    val width: Int,
    val height: Int,
) {
    companion object {
        fun empty() = ElasticRectangularFragment(
            metaTiles = emptyList(),
            width = 0,
            height = 0,
        )

        fun ofSingle(metaTile: MetaTile) = ElasticRectangularFragment(
            metaTiles = listOf(metaTile),
            width = 1,
            height = 1,
        )
    }

    init {
        if (metaTiles.size != width * height) throw IllegalArgumentException()
    }

    fun get(coord: IntVec2): MetaTile? =
        metaTiles.getOrNull(coord.y * width + coord.x)
}

data class ElasticRectangularPattern(
    val topLeft: ElasticRectangularFragment? = null,
    val topCenter: ElasticRectangularFragment? = null,
    val topRight: ElasticRectangularFragment? = null,
    val centerLeft: ElasticRectangularFragment? = null,
    val center: ElasticRectangularFragment? = null,
    val centerRight: ElasticRectangularFragment? = null,
    val bottomLeft: ElasticRectangularFragment? = null,
    val bottomCenter: ElasticRectangularFragment? = null,
    val bottomRight: ElasticRectangularFragment? = null,

    val leftStaticWidth: Int = 0,
    val centerHorizontalRepeatingWidth: Int,
    val rightStaticWidth: Int = 0,

    val topStaticHeight: Int = 0,
    val centerVerticalRepeatingHeight: Int,
    val bottomStaticHeight: Int = 0,
) {
    enum class Column {
        Left,
        Center,
        Right,
    }

    enum class Row {
        Top,
        Center,
        Bottom,
    }

    data class XCoord(
        val column: Column,
        val x: Int,
    )

    data class YCoord(
        val row: Row,
        val y: Int,
    )

    val leftStaticIndices: IntRange
        get() = 0 until leftStaticWidth

    val rightStaticIndices: IntRange
        get() = 0 until rightStaticWidth

    val topStaticIndices: IntRange
        get() = 0 until topStaticHeight

    val bottomStaticIndices: IntRange
        get() = 0 until bottomStaticHeight

    fun get(x: XCoord, y: YCoord): MetaTile? = when {
        y.row == Row.Top && x.column == Column.Left -> topLeft?.get(IntVec2(x.x, y.y))
        y.row == Row.Top && x.column == Column.Center -> topCenter?.get(IntVec2(x.x, y.y))
        y.row == Row.Top && x.column == Column.Right -> topRight?.get(IntVec2(x.x, y.y))

        y.row == Row.Center && x.column == Column.Left -> centerLeft?.get(IntVec2(x.x, y.y))
        y.row == Row.Center && x.column == Column.Center -> center?.get(IntVec2(x.x, y.y))
        y.row == Row.Center && x.column == Column.Right -> centerRight?.get(IntVec2(x.x, y.y))

        y.row == Row.Bottom && x.column == Column.Left -> bottomLeft?.get(IntVec2(x.x, y.y))
        y.row == Row.Bottom && x.column == Column.Center -> bottomCenter?.get(IntVec2(x.x, y.y))
        y.row == Row.Bottom && x.column == Column.Right -> bottomRight?.get(IntVec2(x.x, y.y))

        else -> throw UnsupportedOperationException()
    }

    fun toElasticGenerator(): ElasticGenerator = object : ElasticGenerator {
        override fun buildMetaTiles(size: IntSize): Map<IntVec2, MetaTile> {
            val heightOut = size.height
            val widthOut = size.width

            return (0 until heightOut).flatMap { yOut ->
                (0 until widthOut).mapNotNull { xOut ->
                    val xPatLeft = xOut
                    val xPatCenter = xOut - leftStaticWidth
                    val xPatRight = xOut + rightStaticWidth - widthOut

                    val xCoord = when {
                        xPatLeft in leftStaticIndices -> XCoord(column = Column.Left, x = xPatLeft)
                        xPatRight in rightStaticIndices -> XCoord(column = Column.Right, x = xPatRight)
                        else -> XCoord(column = Column.Center, x = xPatCenter % centerHorizontalRepeatingWidth)
                    }

                    val yPatTop = yOut
                    val yPatCenter = yOut - topStaticHeight
                    val yPatBottom = yOut + bottomStaticHeight - heightOut

                    val yCoord = when {
                        yPatTop in topStaticIndices -> YCoord(row = Row.Top, y = yPatTop)
                        yPatBottom in bottomStaticIndices -> YCoord(row = Row.Bottom, y = yPatBottom)
                        else -> YCoord(row = Row.Center, y = yPatCenter % centerHorizontalRepeatingWidth)
                    }

                    get(xCoord, yCoord)?.let { metaTile ->
                        IntVec2(xOut, yOut) to metaTile
                    }
                }
            }.toMap()
        }
    }
}
