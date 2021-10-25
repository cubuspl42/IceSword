package icesword.geometry

import icesword.frp.Cell
import kotlinx.css.p

class Transform(
    // TODO: Support scaling
    private val t: IntVec2,
) {
    fun transform(point: IntVec2): IntVec2 =
        point + t

    fun transform(rect: IntRect): IntRect =
        rect.translate(t)

    fun transform(lineSeg: IntLineSeg): IntLineSeg =
        lineSeg.translate(t)
}

class DynamicTransform(
    // TODO: Support scaling
    private val transform: Cell<Transform>,
) {
    fun transform(point: Cell<IntVec2>): Cell<IntVec2> =
        Cell.map2(transform, point) { t, p -> t.transform(p) }

    fun transform(rect: Cell<IntRect>): Cell<IntRect> =
        Cell.map2(transform, rect) { t, r -> t.transform(r) }
}
