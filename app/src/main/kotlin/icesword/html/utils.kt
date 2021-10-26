package icesword.html

import icesword.MouseDrag
import icesword.frp.*
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.svg.SVGCircleElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGRectElement
import org.w3c.dom.svg.SVGSVGElement
import kotlin.math.roundToInt

fun createHtmlElement(tagName: String): HTMLElement =
    document.createElement(tagName) as HTMLElement

fun createStyledHtmlElement(
    tagName: String,
    style: DynamicStyleDeclaration? = null,
    tillDetach: Till,
): HTMLElement {
    val element = document.createElement(tagName) as HTMLElement

    style?.linkTo(element.style, tillDetach)

    return element
}

fun createHeading4(text: String): HTMLElement =
    createHtmlElement("h4").apply {
        appendChild(
            document.createTextNode(text),
        )
    }

fun createSvgRoot(
    tillDetach: Till,
): SVGSVGElement {
    val root = document.createElementNS("http://www.w3.org/2000/svg", "svg") as SVGSVGElement

    return root
}

fun createSvgGroup(
    svg: SVGSVGElement,
    translate: Cell<IntVec2>,
    tillDetach: Till,
): SVGElement {
    val group = document.createElementNS("http://www.w3.org/2000/svg", "g") as SVGGElement

    linkSvgTranslate(
        svg = svg,
        element = group,
        translate = translate,
        tillDetach = tillDetach,
    )

    return group
}

fun createSvgRect(
    svg: SVGSVGElement,
    size: Cell<IntSize>,
    translate: Cell<IntVec2>,
    fill: Cell<String>? = null,
    fillOpacity: Cell<Double>? = null,
    stroke: Cell<String>? = null,
    style: DynamicStyleDeclaration? = null,
    tillDetach: Till,
): SVGElement {
    val rect = document.createElementNS("http://www.w3.org/2000/svg", "rect") as SVGRectElement

    size.reactTill(tillDetach) {
        rect.width.baseVal.value = it.width.toFloat()
        rect.height.baseVal.value = it.height.toFloat()
    }

    linkSvgTranslate(
        svg = svg,
        element = rect,
        translate = translate,
        tillDetach = tillDetach,
    )

    style?.linkTo(rect.style, tillDetach)

    linkAttribute(
        element = rect,
        attributeName = "fill",
        attribute = fill,
        till = tillDetach,
    )

    linkAttribute(
        element = rect,
        attributeName = "fill-opacity",
        attribute = fillOpacity?.map { it.toString() },
        till = tillDetach,
    )

    linkAttribute(
        element = rect,
        attributeName = "stroke",
        attribute = stroke,
        till = tillDetach,
    )

    return rect
}

fun createSvgCircle(
    svg: SVGSVGElement,
    radius: Float,
    translate: Cell<IntVec2>,
    stroke: Cell<String>? = null,
    style: DynamicStyleDeclaration? = null,
    tillDetach: Till,
): SVGElement {
    val circle = document.createElementNS("http://www.w3.org/2000/svg", "circle") as SVGCircleElement

    circle.r.baseVal.value = radius

    linkSvgTranslate(
        svg = svg,
        element = circle,
        translate = translate,
        tillDetach = tillDetach,
    )

    style?.linkTo(circle.style, tillDetach)

    linkAttribute(
        element = circle,
        attributeName = "stroke",
        attribute = stroke,
        till = tillDetach,
    )

    return circle
}

private fun linkSvgTranslate(
    svg: SVGSVGElement,
    element: SVGGraphicsElement,
    translate: Cell<IntVec2>,
    tillDetach: Till,
) {

    val svgTransform = svg.createSVGTransform()
    element.transform.baseVal.initialize(svgTransform)

    translate.reactTill(tillDetach) { tv ->
        svgTransform.setTranslate(tv.x.toFloat(), tv.y.toFloat())
    }
}


fun createContainer(
    children: DynamicSet<HTMLElement>,
    tillDetach: Till,
): HTMLElement {
    val root = createHtmlElement("div")

    children.changes.reactTill(tillDetach) { change ->
        change.added.forEach { root.appendChild(it) }
        change.removed.forEach { root.removeChild(it) }
    }

    children.sample().forEach { root.appendChild(it) }

    return root
}

fun Element.onMouseDown(button: MouseButton): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mousedown")
        .filter { e: MouseEvent -> e.button == button.ordinal.toShort() }

fun Element.onMouseMove(): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mousemove")

fun Element.onMouseEnter(): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mouseenter")

fun Element.onMouseLeave(): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mouseleave")

fun Element.onMouseUp(button: MouseButton): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mouseup")
        .filter { e: MouseEvent -> e.button == button.ordinal.toShort() }

sealed interface MousePosition {
    value class Entered(val position: Cell<IntVec2>) : MousePosition
    object Left : MousePosition
}

fun Element.trackMousePosition(till: Till): Cell<MousePosition> {
    val onMove = this.onMouseMove()
    val onEnter = onMove.mergeWith(this.onMouseEnter())
    val onLeave = this.onMouseLeave()

    val mousePosition = onEnter.map { enterEvent ->
        MousePosition.Entered(
            position = onMove.map { it.clientPosition }
                .hold(enterEvent.clientPosition, till = onLeave.tillNext(till)),
        )
    }.mergeWith(
        onLeave.map { MousePosition.Left }
    ).hold(
        initialValue = MousePosition.Left,
        till = till,
    )

    return mousePosition
}

fun HTMLElement.onClick(): Stream<MouseEvent> =
    this.onEvent("click")

fun HTMLElement.onKeyDown(): Stream<KeyboardEvent> =
    this.onEvent("keydown", useCapture = true)

enum class MouseButton {
    Primary,
    Middle,
    Secondary,
}

fun Element.onMouseDrag(
    button: MouseButton,
    outer: HTMLElement? = null,
    filterTarget: Boolean = false,
    till: Till,
): Stream<MouseDrag> {
    val onMouseDown = this.onMouseDown(button = button)
    val onMouseDownFiltered =
        if (filterTarget) onMouseDown.filter { it.target === this }
        else onMouseDown
    return onMouseDownFiltered
        .until(till)
        .map { event ->
            MouseDrag.start(
                element = outer ?: this,
                initialPosition = event.clientPosition,
                button = button,
                tillAbort = till,
            )
        }
}

fun <E : Event> Element.onEvent(
    eventType: String,
    useCapture: Boolean = false,
): Stream<E> =
    Stream.source<Event>(
        subscribeToSource = { notify ->
            this.subscribeToEvent(eventType, notify, useCapture = useCapture)
        },
        tag = "onEvent",
    ).cast()

fun Element.subscribeToEvent(
    eventType: String,
    callback: ((Event) -> Unit),
    useCapture: Boolean = false,
): Subscription {
    this.addEventListener(eventType, callback, useCapture)

    return object : Subscription {
        override fun unsubscribe() {
            this@subscribeToEvent.removeEventListener(eventType, callback)
        }
    }
}

val MouseEvent.clientPosition: IntVec2
    get() = IntVec2(this.clientX, this.clientY)

fun linkNodeChildren(
    element: Node,
    children: DynamicSet<Node>,
    till: Till,
) {
    children.changes.reactTill(till) { change ->
        change.added.forEach { child ->
            element.appendChild(child)
        }

        change.removed.forEach { child ->
            element.removeChild(child)
        }
    }

    children.volatileContentView.forEach { child ->
        element.appendChild(child)
    }
}


fun linkChildren(
    element: HTMLElement,
    children: DynamicSet<HTMLElement>,
    till: Till,
) {
    linkNodeChildren(
        element = element,
        children = children,
        till = till,
    )
}

fun linkChild(
    element: HTMLElement,
    child: Cell<HTMLElement?>,
    till: Till,
) {
    child.values().reactTill(till) { c ->
        clearElement(element)
        c?.let { element.appendChild(it) }
    }

    clearElement(element)
    child.sample()?.let { element.appendChild(it) }
}

private fun clearElement(element: Element): Unit {
    element.innerHTML = ""
}

fun linkSvgChildren(
    element: SVGElement,
    children: DynamicSet<SVGElement>,
    till: Till,
) {
    linkNodeChildren(
        element = element,
        children = children,
        till = till,
    )
}

fun linkAttribute(
    element: Element,
    attributeName: String,
    attribute: Cell<String?>?,
    till: Till,
): Unit {
    attribute?.reactTill(till) {
        if (it != null) {
            element.setAttributeNS(null, attributeName, it)
        } else {
            element.removeAttributeNS(null, attributeName)
        }
    }
}

fun Element.calculateRelativePosition(clientPosition: IntVec2): IntVec2 {
    val rect = getBoundingClientRect()
    val originPosition = IntVec2(rect.x.roundToInt(), rect.y.roundToInt())
    return clientPosition - originPosition
}
