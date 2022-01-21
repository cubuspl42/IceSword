package icesword.html

import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.map
import icesword.frp.map
import kotlinx.css.Align
import kotlinx.css.FlexDirection
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension

data class ColumnStyleDeclaration(
    val reverseDirection: Cell<Boolean>? = null,
    val verticalGap: Cell<LinearDimension>? = null,
    val justifyVertically: Cell<JustifyContent>? = null,
    val alignHorizontally: Cell<Align>? = null,
) {
    fun toFlexStyle() = FlexStyleDeclaration(
        direction = reverseDirection?.map {
            if (it) FlexDirection.columnReverse else FlexDirection.column
        } ?: Cell.constant(FlexDirection.column),
        gap = verticalGap,
        justifyContent = justifyVertically,
        alignItems = alignHorizontally,
    )
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
                    direction = Cell.constant(FlexDirection.column),
                    gap = flexStyle.gap ?: verticalGap?.let(Cell.Companion::constant),
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

fun createColumnWbDl(
    tagName: String = "div",
    style: DynamicStyleDeclaration = DynamicStyleDeclaration(),
    columnStyle: ColumnStyleDeclaration = ColumnStyleDeclaration(),
    children: DynamicList<HTMLWidgetB<*>>,
) = object : HTMLWidgetB<HTMLWidget> {
    override fun build(tillDetach: Till): HTMLWidget {
        val element = createStyledHtmlElement(
            tagName = tagName,
            style = style.copy(
                displayStyle = columnStyle.toFlexStyle(),
            ),
            tillDetach = tillDetach,
        )

        linkNodeChildrenDl(
            element = element,
            children = HTMLWidgetB.buildDl(children, tillDetach).map { it.resolve() },
            till = tillDetach,
        )

        return HTMLWidget.of(element)
    }
}
