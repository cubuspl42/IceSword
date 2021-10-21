package html

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

fun Element.onMouseDown(button: Short): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mousedown")
        .filter { e: MouseEvent -> e.button == button }

fun Element.onMouseMove(): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mousemove")

fun Element.onMouseUp(button: Short): Stream<MouseEvent> =
    this.onEvent<MouseEvent>("mouseup")
        .filter { e: MouseEvent -> e.button == button }

fun HTMLElement.onClick(): Stream<MouseEvent> =
    this.onEvent("click")

fun HTMLElement.onKeyDown(): Stream<KeyboardEvent> =
    this.onEvent("keydown", useCapture = true)

fun Element.onMouseDrag(
    button: Short,
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
