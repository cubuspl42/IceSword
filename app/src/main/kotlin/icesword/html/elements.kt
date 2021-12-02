package icesword.html

import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.DynamicSet
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.map
import icesword.frp.dynamic_list.staticListOf
import icesword.frp.dynamic_ordered_set.DynamicOrderedSet
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.mapTillNext
import icesword.frp.reactIndefinitely
import icesword.frp.reactTill
import icesword.frp.sample
import icesword.frp.values
import icesword.geometry.DynamicTransform
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import kotlinx.browser.document
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.LinearDimension
import kotlinx.css.em
import kotlinx.css.properties.LineHeight
import kotlinx.css.px
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Node
import org.w3c.dom.Text
import org.w3c.dom.css.CSSStyleDeclaration
import org.w3c.dom.svg.SVGCircleElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGForeignObjectElement
import org.w3c.dom.svg.SVGGElement
import org.w3c.dom.svg.SVGLineElement
import org.w3c.dom.svg.SVGPolygonElement
import org.w3c.dom.svg.SVGRectElement
import org.w3c.dom.svg.SVGSVGElement

fun createHtmlElement(tagName: String): HTMLElement =
    document.createElement(tagName) as HTMLElement

fun createStaticText(
    text: String,
): Text =
    document.createTextNode(text)

fun createText(
    text: Cell<String>,
    tillDetach: Till,
): Text {
    val textNode = document.createTextNode(text.sample())

    text.values().reactTill(tillDetach) {
        textNode.data = it
    }

    return textNode
}

fun createTextWb(
    text: Cell<String>,
) = object : HTMLWidgetB<HTMLWidget> {
    override fun build(tillDetach: Till): HTMLWidget {
        val textNode = document.createTextNode(text.sample())

        text.values().reactTill(tillDetach) {
            textNode.data = it
        }

        return HTMLWidget.of(textNode)
    }
}

fun createStyledText(
    text: Cell<String>,
    style: DynamicStyleDeclaration? = null,
    tillDetach: Till,
): HTMLElement {
    val textNode = createText(
        text = text,
        tillDetach = tillDetach,
    )

    return createStyledHtmlElement(
        tagName = "div",
        style = style,
        tillDetach = tillDetach,
    ).apply {
        appendChild(textNode)
    }
}

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

fun createHTMLWidgetB(
    tagName: String,
    children: DynamicList<HTMLWidgetB<*>>,
    style: DynamicStyleDeclaration? = null,
) = object : HTMLWidgetB<HTMLWidget> {
    override fun build(tillDetach: Till): HTMLWidget {
        val element = document.createElement(tagName) as HTMLElement

        style?.linkTo(element.style, tillDetach)

        linkNodeChildrenDl(
            element = element,
            children = HTMLWidgetB.buildDl(children, tillDetach)
                .map(HTMLWidget.Companion::resolve),
            till = tillDetach,
        )

        return HTMLWidget.of(element)
    }
}


fun createHeading4(text: String): HTMLElement =
    createHtmlElement("h4").apply {
        appendChild(
            document.createTextNode(text),
        )
    }


fun createHeading4Wb(text: Cell<String>): HTMLWidgetB<*> =
    createHTMLWidgetB(
        tagName = "h4",
        style = DynamicStyleDeclaration(
            margin = constant(0.25.em),
        ),
        children = staticListOf(
            createTextWb(text),
        ),
    )


fun createHeading5Wb(text: Cell<String>): HTMLWidgetB<*> =
    createHTMLWidgetB(
        tagName = "h5",
        style = DynamicStyleDeclaration(
            margin = constant(0.25.em),
        ),
        children = staticListOf(
            createTextWb(text),
        ),
    )

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
    strokeString: Cell<String>? = null,
    stroke: Cell<Color>? = null,
    strokeWidth: Cell<Int>? = null,
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
        attribute = strokeString,
        till = tillDetach,
    )

    linkAttribute(
        element = rect,
        attributeName = "stroke",
        attribute = stroke?.map { it.value },
        till = tillDetach,
    )

    linkAttribute(
        element = rect,
        attributeName = "stroke-width",
        attribute = strokeWidth?.map { it.toString() },
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

fun createSvgCircle(
    svg: SVGSVGElement,
    radius: Float,
    transform: DynamicTransform,
    stroke: Cell<String>? = null,
    style: DynamicStyleDeclaration? = null,
    tillDetach: Till,
): SVGElement {
    val circle = document.createElementNS("http://www.w3.org/2000/svg", "circle") as SVGCircleElement

    circle.r.baseVal.value = radius

    linkSvgTransform(
        svg = svg,
        element = circle,
        transform = transform,
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

fun createSvgLine(
    pointA: Cell<IntVec2>,
    pointB: Cell<IntVec2>,
    stroke: Cell<Color>? = null,
    tillDetach: Till,
): SVGElement {
    val line = document.createElementNS("http://www.w3.org/2000/svg", "line") as SVGLineElement

    linkSvgLength(
        length = line.x1.baseVal,
        attribute = pointA.map { it.x.toFloat() },
        till = tillDetach,
    )

    linkSvgLength(
        length = line.y1.baseVal,
        attribute = pointA.map { it.y.toFloat() },
        till = tillDetach,
    )

    linkSvgLength(
        length = line.x2.baseVal,
        attribute = pointB.map { it.x.toFloat() },
        till = tillDetach,
    )

    linkSvgLength(
        length = line.y2.baseVal,
        attribute = pointB.map { it.y.toFloat() },
        till = tillDetach,
    )

    linkAttribute(
        element = line,
        attributeName = "stroke",
        attribute = stroke?.map { it.value },
        till = tillDetach,
    )

    return line
}

fun createSvgPolygon(
    svg: SVGSVGElement,
    transform: DynamicTransform,
    points: Cell<List<IntVec2>>,
    stroke: Cell<Color>? = null,
    fill: Cell<Color>? = null,
    tillDetach: Till,
): SVGElement {
    val polygon = createSvgElement("polygon") as SVGPolygonElement

    linkSvgTransform(
        svg = svg,
        element = polygon,
        transform = transform,
        tillDetach = tillDetach,
    )

    linkAttribute(
        element = polygon,
        attributeName = "points",
        attribute = points.map { points ->
            points.joinToString(separator = " ") { "${it.x},${it.y}" }
        },
        till = tillDetach,
    )

    linkAttribute(
        element = polygon,
        attributeName = "stroke",
        attribute = stroke?.map { it.value },
        till = tillDetach,
    )

    linkAttribute(
        element = polygon,
        attributeName = "fill",
        attribute = fill?.map { it.value },
        till = tillDetach,
    )

    return polygon
}

private fun createSvgElement(name: String): Element =
    document.createElementNS("http://www.w3.org/2000/svg", name)

fun createSvgForeignObject(
    svg: SVGSVGElement,
    transform: DynamicTransform,
    width: Int,
    height: Int,
    child: HTMLElement,
    tillDetach: Till,
): SVGForeignObjectElement {
    val foreignObject = createSvgElement("foreignObject") as SVGForeignObjectElement

    foreignObject.width.baseVal.value = width.toFloat()
    foreignObject.height.baseVal.value = height.toFloat()

    linkSvgTransform(
        svg = svg,
        element = foreignObject,
        transform = transform,
        tillDetach = tillDetach,
    )

    foreignObject.appendChild(child)

    return foreignObject
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
    style: DynamicStyleDeclaration = DynamicStyleDeclaration(),
    tillDetach: Till,
): HTMLElement =
    createHtmlElement("div").apply {
        if (className != null) {
            this.className = "editorViewWrapper"
        }

//        style?.linkTo(element.style, tillDetach)

        linkChild(
            element = this,
            child = child,
            till = tillDetach
        )
    }

fun createWrapperWb(
    tagName: String = "div",
    style: DynamicStyleDeclaration? = null,
    child: Cell<HTMLWidgetB<*>?>,
): HTMLWidgetB<*> = object : HTMLWidgetB<HTMLWidget> {
    override fun build(tillDetach: Till): HTMLWidget {
        val element = document.createElement(tagName) as HTMLElement

        style?.linkTo(element.style, tillDetach)

        val childElement = HTMLWidgetB.build(child, tillDetach)
            .mapNested(HTMLWidget.Companion::resolve)

        linkChild(element, childElement, tillDetach)

        return HTMLWidget.of(element)
    }
}

fun createTableContainer(
    borderSpacing: LinearDimension,
    rows: List<List<HTMLElement>>,
): HTMLElement {
    val root = createHtmlElement("div").apply {
        style.apply {
            display = "table"
            this.borderSpacing = borderSpacing.value
        }
    }

    val children = rows.map { cells ->
        createHtmlElement("div").apply {
            style.apply {
                display = "table-row"
            }

            cells.forEach { cell ->
                appendChild(
                    createHtmlElement("div").apply {
                        style.apply {
                            display = "table-cell"
                            textAlign = "center"
                            verticalAlign = "middle"

                            appendChild(cell)
                        }
                    },
                )
            }
        }
    }

    children.forEach(root::appendChild)

    return root
}


fun createSvgSwitch(
    child: Cell<SVGElement?>,
    tillDetach: Till,
): SVGElement {
    val group = document.createElementNS("http://www.w3.org/2000/svg", "g") as SVGGElement

    linkSvgChildren(
        element = group,
        children = DynamicSet.ofSingle(child),
        till = tillDetach,
    )

    return group
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

fun createColumnWb(
    tagName: String = "div",
    style: DynamicStyleDeclaration = DynamicStyleDeclaration(),
    flexStyle: FlexStyleDeclaration = FlexStyleDeclaration(),
    verticalGap: LinearDimension? = null,
    children: List<HTMLWidgetB<*>>,
) = object : HTMLWidgetB<HTMLWidget> {
    override fun build(tillDetach: Till): HTMLWidget {
        val element = createStyledHtmlElement(
            tagName = tagName,
            style = style.copy(
                displayStyle = flexStyle.copy(
                    direction = constant(FlexDirection.column),
                    gap = flexStyle.gap ?: verticalGap?.let(::constant),
                ),
            ),
            tillDetach = tillDetach,
        ).apply {
            HTMLWidgetB.build(children, tillDetach).forEach {
                appendChild(HTMLWidget.resolve(it))
            }
        }

        return HTMLWidget.of(element)
    }
}

fun createGrid(
    tagName: String = "div",
    style: DynamicStyleDeclaration = DynamicStyleDeclaration(),
    columnCount: Int = 3,
    gap: LinearDimension? = null,
    children: List<HTMLWidgetB<*>>,
) = object : HTMLWidgetB<HTMLWidget> {
    override fun build(tillDetach: Till): HTMLWidget {
        val gridTemplateColumns = generateSequence { "1fr" }.take(columnCount)
            .joinToString(" ")

        val element = createStyledHtmlElement(
            tagName = tagName,
            style = style.copy(
                display = constant(Display.grid),
                gap = gap?.let(::constant),
            ),
            tillDetach = tillDetach,
        ).apply {
            this.style.apply {
                setProperty("grid-template-columns", gridTemplateColumns)
            }

            HTMLWidgetB.build(children, tillDetach).forEach {
                appendChild(HTMLWidget.resolve(it))
            }
        }

        return HTMLWidget.of(element)
    }
}

fun createGridDl(
    tagName: String = "div",
    style: DynamicStyleDeclaration = DynamicStyleDeclaration(),
    gap: LinearDimension? = null,
    children: DynamicList<HTMLWidgetB<*>>,
): HTMLWidgetB<HTMLWidget> {
    val widgetB = createGrid(
        tagName = tagName,
        style = style,
        gap = gap,
        children = emptyList(),
    )

    return object : HTMLWidgetB<HTMLWidget> {
        override fun build(tillDetach: Till): HTMLWidget {
            val widget = widgetB.build(tillDetach)
            val element = HTMLWidget.resolve(widget) as Element

            linkNodeChildrenDl(
                element = element,
                children = HTMLWidgetB.buildDl(children, tillDetach)
                    .map(HTMLWidget.Companion::resolve),
                till = tillDetach,
            )

            return HTMLWidget.of(element)
        }
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
            alignItems = constant(Align.center),
            gap = horizontalGap?.let(::constant),
        ),
        tillDetach = tillDetach,
    ).apply {
        children.forEach(this::appendChild)
    }

fun createRowWb(
    tagName: String = "div",
    style: DynamicStyleDeclaration = DynamicStyleDeclaration(),
    horizontalGap: LinearDimension? = null,
    children: List<HTMLWidgetB<*>>,
) = object : HTMLWidgetB<HTMLWidget> {
    override fun build(tillDetach: Till): HTMLWidget {
        val element = createStyledHtmlElement(
            tagName = tagName,
            style = style.copy(
                display = constant(Display.flex),
                flexDirection = constant(FlexDirection.row),
                gap = horizontalGap?.let(::constant),
            ),
            tillDetach = tillDetach,
        ).apply {
            HTMLWidgetB.build(children, tillDetach).forEach {
                appendChild(HTMLWidget.resolve(it))
            }
        }

        return HTMLWidget.of(element)
    }
}


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
