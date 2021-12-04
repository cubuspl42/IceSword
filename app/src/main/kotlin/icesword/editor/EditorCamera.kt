package icesword.editor

import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import icesword.frp.reactTillNext
import icesword.frp.switchMap
import icesword.frp.syncTill
import icesword.frp.update
import icesword.geometry.DynamicTransform
import icesword.geometry.IntVec2
import icesword.geometry.Transform

// Equation:
// viewportPoint = (worldPoint - focusPoint) * zoom
data class CameraTransform(
    val focusPoint: IntVec2,
    val zoom: Double,
) {
    companion object {
        private fun solveForFocusPoint(
            worldPoint: IntVec2,
            viewportPoint: IntVec2,
            zoom: Double,
        ): IntVec2 =
            worldPoint - viewportPoint.divRound(zoom)
    }

    val transform =
        Transform.scale(zoom) * Transform.translate(-focusPoint)

    fun zoomedAround(
        viewportPoint: IntVec2,
        newZoom: Double,
    ): CameraTransform {
        val worldPoint = transform.inversed.transform(viewportPoint)

        val newFocusPoint = solveForFocusPoint(
            worldPoint = worldPoint,
            viewportPoint = viewportPoint,
            zoom = newZoom,
        )

        return CameraTransform(
            focusPoint = newFocusPoint,
            zoom = newZoom,
        )
    }
}

class EditorCamera(
    initialFocusPoint: IntVec2,
) {
    private val _transform = MutCell(
        CameraTransform(
            focusPoint = initialFocusPoint,
            zoom = 1.0,
        ),
    )

    private val focusPoint: Cell<IntVec2>
        get() = _transform.map { it.focusPoint }

    val zoom: Cell<Double> = _transform.map { it.zoom }

    private fun setFocusPoint(
        newFocusPoint: IntVec2,
    ) {
        _transform.update {
            it.copy(focusPoint = newFocusPoint)
        }
    }

    fun zoom(
        viewportPoint: IntVec2,
        newZoom: Double,
    ) {
        _transform.update {
            it.zoomedAround(
                viewportPoint = viewportPoint,
                newZoom = newZoom,
            )
        }
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
        val initialFocusPoint = focusPoint.sample()
        val targetFocusPoint = offsetDelta.map { d -> initialFocusPoint + d }

        targetFocusPoint.reactTill(tillStop, this::setFocusPoint)
    }
}
