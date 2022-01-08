package icesword.html

import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.reactTill
import icesword.geometry.IntSize
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.MouseEvent

fun createCanvas(
    size: Cell<IntSize>,
): HTMLWidgetB<HTMLCanvas> = object : HTMLWidgetB<HTMLCanvas> {
    override fun build(tillDetach: Till): HTMLCanvas {
        val element = createHTMLElement(
            tagName = "canvas",
            tillDetach = tillDetach
        ) as HTMLCanvasElement

        size.reactTill(tillDetach) { size ->
            element.width = size.width
            element.height = size.height
        }

        return HTMLCanvas(
            element = element,
        )
    }
}

class HTMLCanvas(
    override val element: HTMLCanvasElement,
) : HTMLWidget.HTMLElementWidget {
    fun getContext2D(): CanvasRenderingContext2D =
        element.getContext("2d").unsafeCast<CanvasRenderingContext2D>()
}
