package icesword.geometry

import icesword.frp.Cell
import icesword.frp.map

class Transform(
    // TODO: Support scaling
    private val t: IntVec2,
) {
    val reversed: Transform by lazy { Transform(-t) }

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
    val reversed: DynamicTransform by lazy {
        DynamicTransform(
            transform = transform.map { it.reversed },
        )
    }

    fun transform(point: Cell<IntVec2>): Cell<IntVec2> =
        Cell.map2(transform, point) { t, p -> t.transform(p) }

    fun transform(rect: Cell<IntRect>): Cell<IntRect> =
        Cell.map2(transform, rect) { t, r -> t.transform(r) }
}
