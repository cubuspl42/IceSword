package icesword.editor

import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import icesword.frp.switchMap
import icesword.frp.update
import icesword.geometry.DynamicTransform
import icesword.geometry.IntVec2
import icesword.geometry.Transform
import kotlin.math.pow

// Equation:
// viewportPoint = (worldPoint - focusPoint) * zoom
data class CameraTransform(
    val focusPoint: IntVec2,
    val zoomExponent: Int,
) {
    companion object {
        const val minZoomExponent = -4
        const val maxZoomExponent = 4

        private fun solveForFocusPoint(
            worldPoint: IntVec2,
            viewportPoint: IntVec2,
            zoom: Double,
        ): IntVec2 =
            worldPoint - viewportPoint.divRound(zoom)

        private fun calculateZoom(
            zoomExponent: Int,
        ): Double =
            2.0.pow(zoomExponent)
    }

    val zoom =
        calculateZoom(zoomExponent)

    val transform =
        Transform.scale(zoom) * Transform.translate(-focusPoint)

    fun zoomedAround(
        viewportPoint: IntVec2,
        zoomExponentDelta: Int,
    ): CameraTransform {
        val newZoomExponent = (zoomExponent + zoomExponentDelta).coerceIn(
            minZoomExponent,
            maxZoomExponent,
        )

        val worldPoint = transform.inversed.transform(viewportPoint)

        val newZoom = calculateZoom(newZoomExponent)

        val newFocusPoint = solveForFocusPoint(
            worldPoint = worldPoint,
            viewportPoint = viewportPoint,
            zoom = newZoom,
        )

        return CameraTransform(
            focusPoint = newFocusPoint,
            zoomExponent = newZoomExponent,
        )
    }
}

class EditorCamera(
    initialFocusPoint: IntVec2,
) {
    private val _transform = MutCell(
        CameraTransform(
            focusPoint = initialFocusPoint,
            zoomExponent = 0,
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

    private fun zoom(
        viewportPoint: IntVec2,
        zoomExponentDelta: Int,
    ) {
        _transform.update {
            it.zoomedAround(
                viewportPoint = viewportPoint,
                zoomExponentDelta = zoomExponentDelta,
            )
        }
    }

    fun zoomIn(
        viewportPoint: IntVec2,
    ) {
        zoom(
            viewportPoint,
            zoomExponentDelta = 1,
        )
    }

    fun zoomOut(
        viewportPoint: IntVec2,
    ) {
        zoom(
            viewportPoint,
            zoomExponentDelta = -1,
        )
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
