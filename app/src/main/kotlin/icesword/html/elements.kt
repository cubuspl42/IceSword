package icesword.html

import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import icesword.frp.sample
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import kotlinx.browser.document
import kotlinx.css.Color
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGCircleElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGGElement
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
    fill: Cell<Color>? = null,
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
        attribute = fill?.map { it.value },
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

fun createWrapper(
    child: Cell<HTMLElement?>,
    className: String? = null,
    tillDetach: Till,
): HTMLElement =
    createHtmlElement("div").apply {
        if (className != null) {
            this.className = "editorViewWrapper"
        }

        linkChild(
            element = this,
            child = child,
            till = tillDetach
        )
    }
