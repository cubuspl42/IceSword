package icesword.editor.elastic

import icesword.editor.ElasticGenerator
import icesword.editor.ElasticGeneratorOutput
import icesword.editor.MetaTile
import icesword.editor.WapObjectPropsData
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.tileTopLeftCorner
import icesword.utils.mapValuesNotNull


data class ElasticRectangularFragment(
    val metaTiles: List<MetaTile>,
    val wapObject: WapObjectPropsData? = null,
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

    fun get(x: Int, y: Int): MetaTile? =
        metaTiles.getOrNull(y * width + x)
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
    val centerHorizontalRepeatingWidth: Int = 0,
    val rightStaticWidth: Int = 0,

    val topStaticHeight: Int = 0,
    val centerVerticalRepeatingHeight: Int = 0,
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

    data class MetaTileOutput(
        val metaTile: MetaTile?,
        val wapObject: WapObjectPropsData?,
    )

    val leftStaticIndices: IntRange
        get() = 0 until leftStaticWidth

    val rightStaticIndices: IntRange
        get() = 0 until rightStaticWidth

    val topStaticIndices: IntRange
        get() = 0 until topStaticHeight

    val bottomStaticIndices: IntRange
        get() = 0 until bottomStaticHeight

    fun getFragment(row: Row, column: Column): ElasticRectangularFragment? = when {
        row == Row.Top && column == Column.Left -> topLeft
        row == Row.Top && column == Column.Center -> topCenter
        row == Row.Top && column == Column.Right -> topRight

        row == Row.Center && column == Column.Left -> centerLeft
        row == Row.Center && column == Column.Center -> center
        row == Row.Center && column == Column.Right -> centerRight

        row == Row.Bottom && column == Column.Left -> bottomLeft
        row == Row.Bottom && column == Column.Center -> bottomCenter
        row == Row.Bottom && column == Column.Right -> bottomRight

        else -> throw UnsupportedOperationException()
    }

    fun toElasticGenerator() = object : ElasticGenerator {
        override fun buildOutput(size: IntSize): ElasticGeneratorOutput {
            val heightOut = size.height
            val widthOut = size.width

            val metaTileOutputs: Map<IntVec2, MetaTileOutput> = (0 until heightOut).flatMap { yOut ->
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
                        else -> YCoord(row = Row.Center, y = yPatCenter % centerVerticalRepeatingHeight)
                    }

                    getFragment(yCoord.row, xCoord.column)?.let { fragment ->
                        val metaTileOrNull = fragment.get(xCoord.x, yCoord.y)

                        val pxOut = tileTopLeftCorner(IntVec2(xOut, yOut))

                        val wapObject = if (xCoord.x == 0 && yCoord.y == 0) {
                            fragment.wapObject?.let { wapObjectTemplate ->
                                wapObjectTemplate.copy(
                                    x = pxOut.x + wapObjectTemplate.x,
                                    y = pxOut.y + wapObjectTemplate.y,
                                )
                            }
                        } else null

                        IntVec2(xOut, yOut) to MetaTileOutput(
                            metaTile = metaTileOrNull,
                            wapObject = wapObject,
                        )
                    }
                }
            }.toMap()

            val metaTiles: Map<IntVec2, MetaTile> = metaTileOutputs
                .mapValuesNotNull { (_, out) -> out.metaTile }

            val wapObjects: List<WapObjectPropsData> = metaTileOutputs.values
                .mapNotNull { out -> out.wapObject }

            return ElasticGeneratorOutput(
                localMetaTiles = metaTiles,
                localWapObjects = wapObjects,
            )
        }
    }
}
