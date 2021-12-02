package icesword.html

import icesword.frp.*
import icesword.frp.dynamic_list.DynamicList
import icesword.geometry.DynamicTransform
import icesword.geometry.IntVec2
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGLength
import org.w3c.dom.svg.SVGSVGElement
import kotlin.math.roundToInt

fun linkSvgTranslate(
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

fun linkSvgTransform(
    svg: SVGSVGElement,
    element: SVGGraphicsElement,
    transform: DynamicTransform,
    tillDetach: Till,
) {
    val svgTransform = svg.createSVGTransform().apply {
        setMatrix(transform.transform.sample().toSVGMatrix(svg))
    }

    element.transform.baseVal.initialize(svgTransform)

    transform.transform.values().reactTill(tillDetach) { t ->
        svgTransform.setMatrix(
            matrix = t.toSVGMatrix(svg),
        )
    }
}

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
            try {
                element.removeChild(child)
            } catch (e: Throwable) {
                console.error("Failed to remove DOM node", e, element, child)
            }
        }
    }

    children.volatileContentView.forEach { child ->
        element.appendChild(child)
    }
}

fun linkNodeChildrenDl(
    element: Element,
    children: DynamicList<Node>,
    till: Till,
) {
    children.content.values().reactTill(till) { childrenNow ->
        clearElement(element)

        childrenNow.forEach(element::appendChild)
    }

    children.volatileContentView.forEach(element::appendChild)
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

fun linkHtmlChildrenDl(
    element: HTMLElement,
    children: DynamicList<HTMLElement>,
    till: Till,
) {
    linkNodeChildrenDl(
        element = element,
        children = children,
        till = till,
    )
}

fun linkChild(
    element: HTMLElement,
    child: Cell<Node?>,
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

fun linkSvgLength(
    length: SVGLength,
    attribute: Cell<Float>,
    till: Till,
) {
    attribute.reactTill(till) {
        length.value = it
    }
}

fun Element.calculateRelativePosition(clientPosition: IntVec2): IntVec2 {
    val rect = getBoundingClientRect()
    val originPosition = IntVec2(rect.x.roundToInt(), rect.y.roundToInt())
    return clientPosition - originPosition
}
