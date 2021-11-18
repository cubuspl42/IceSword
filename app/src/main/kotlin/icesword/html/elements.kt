package icesword.html

import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.DynamicSet
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_ordered_set.DynamicOrderedSet
import icesword.frp.map
import icesword.frp.reactIndefinitely
import icesword.frp.reactTill
import icesword.frp.sample
import icesword.geometry.DynamicTransform
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import kotlinx.browser.document
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.LinearDimension
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Node
import org.w3c.dom.Text
import org.w3c.dom.css.CSSStyleDeclaration
import org.w3c.dom.svg.SVGCircleElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGRectElement
import org.w3c.dom.svg.SVGSVGElement

fun createHtmlElement(tagName: String): HTMLElement =
    document.createElement(tagName) as HTMLElement

fun createText(
    text: String,
): Text =
    document.createTextNode(text)

fun createStyledHtmlElement(
    tagName: String,
    staticStyle: (CSSStyleDeclaration.() -> Unit)? = null,
    style: DynamicStyleDeclaration? = null,
    tillDetach: Till,
): HTMLElement {
    val element = document.createElement(tagName) as HTMLElement

    if (staticStyle != null) {
        staticStyle(element.style)
    }

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

fun createSvgGroupDt(
    svg: SVGSVGElement,
    transform: DynamicTransform,
    tillDetach: Till,
): SVGElement {
    val group = document.createElementNS("http://www.w3.org/2000/svg", "g") as SVGGElement

    linkSvgTransform(
        svg = svg,
        element = group,
        transform = transform,
        tillDetach = tillDetach,
    )

    return group
}

fun createSvgGroup(
    svg: SVGSVGElement,
    transform: DynamicTransform? = null,
    children: DynamicSet<SVGElement>,
    tillDetach: Till,
): SVGElement {
    val group = document.createElementNS("http://www.w3.org/2000/svg", "g") as SVGGElement

    if (transform != null) {
        linkSvgTransform(
            svg = svg,
            element = group,
            transform = transform,
            tillDetach = tillDetach,
        )
    }

    linkSvgChildren(
        element = group,
        children = children,
        till = tillDetach,
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


fun createContainer(
    children: DynamicOrderedSet<HTMLElement>,
    tillDetach: Till,
): HTMLElement {
    val root = createHtmlElement("div")

    children.changes.reactTill(tillDetach) { change ->
        change.removed?.let {
            root.removeChild(it)
        }

        change.inserted?.let {
            val node = it.before
            val child = it.value

            if (node != null) {
                root.insertBefore(node, child)
            } else {
                root.appendChild(child)
            }
        }
    }

    children.volatileContentView.forEach { root.appendChild(it) }

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

fun createColumn(
    tagName: String = "div",
    style: DynamicStyleDeclaration = DynamicStyleDeclaration(),
    verticalGap: LinearDimension? = null,
    children: List<Node>,
    tillDetach: Till,
): HTMLElement {
    return createStyledHtmlElement(
        tagName = tagName,
        style = style.copy(
            display = constant(Display.flex),
            flexDirection = constant(FlexDirection.column),
            gap = verticalGap?.let(::constant),
        ),
        tillDetach = tillDetach,
    ).apply {
        children.forEach(this::appendChild)
    }
}

fun createColumnDl(
    tagName: String = "div",
    style: DynamicStyleDeclaration = DynamicStyleDeclaration(),
    verticalGap: LinearDimension? = null,
    children: DynamicList<Node>,
    tillDetach: Till,
): HTMLElement =
    createColumn(
        tagName = tagName,
        style = style,
        verticalGap = verticalGap,
        children = emptyList(),
        tillDetach = tillDetach,
    ).apply {
        linkNodeChildrenDl(
            element = this,
            children = children,
            till = tillDetach,
        )
    }

fun createRow(
    tagName: String = "div",
    staticStyle: (CSSStyleDeclaration.() -> Unit)? = null,
    style: DynamicStyleDeclaration = DynamicStyleDeclaration(),
    horizontalGap: LinearDimension? = null,
    children: List<Node>,
    tillDetach: Till,
): HTMLElement =
    createStyledHtmlElement(
        tagName = tagName,
        staticStyle = staticStyle,
        style = style.copy(
            display = constant(Display.flex),
            flexDirection = constant(FlexDirection.row),
            gap = horizontalGap?.let(::constant),
        ),
        tillDetach = tillDetach,
    ).apply {
        children.forEach(this::appendChild)
    }

fun createRowDl(
    tagName: String = "div",
    style: DynamicStyleDeclaration = DynamicStyleDeclaration(),
    horizontalGap: LinearDimension? = null,
    children: DynamicList<Node>,
    tillDetach: Till,
): HTMLElement = TODO()

fun createNumberInput(
    staticStyle: (CSSStyleDeclaration.() -> Unit)? = null,
    initialValue: Int,
    onValueChanged: (Int) -> Unit,
): HTMLInputElement {
    val input = (createStyledHtmlElement(
        tagName = "input",
        staticStyle = staticStyle,
        tillDetach = Till.never,
    ) as HTMLInputElement)
        .apply {
            type = "number"
            value = initialValue.toString()
        }

    input.onChange().reactIndefinitely {
        onValueChanged(input.value.toInt())
    }

    return input
}
