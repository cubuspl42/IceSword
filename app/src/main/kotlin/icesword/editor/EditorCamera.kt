package icesword.editor

import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.switchMap
import icesword.frp.syncTill
import icesword.geometry.DynamicTransform
import icesword.geometry.IntVec2

class EditorCamera(
    initialFocusPoint: IntVec2,
) {
    private val _focusPoint = MutCell(initialFocusPoint)

    private val focusPoint: Cell<IntVec2>
        get() = _focusPoint

    private val _zoom = MutCell(1.0)

    val zoom: Cell<Double> = _zoom

    fun setZoom(newZoom: Double) {
        _zoom.set(newZoom)
    }

    private val focusTransform = DynamicTransform.translate(focusPoint.map { -it })

    private val zoomTransform = DynamicTransform.scale(zoom)

    val transform = zoomTransform * focusTransform

    fun transformToWorld(cameraPoint: IntVec2): Cell<IntVec2> =
        transform.inversed.transform(cameraPoint)

    fun transformToWorld(cameraPoint: Cell<IntVec2>): Cell<IntVec2> =
        cameraPoint.switchMap(this::transformToWorld)

    fun drag(
        offsetDelta: Cell<IntVec2>,
        tillStop: Till,
    ) {
        val initialFocusPoint = _focusPoint.sample()
        val targetFocusPoint = offsetDelta.map { d -> initialFocusPoint + d }

        targetFocusPoint.syncTill(_focusPoint, till = tillStop)
    }
}
