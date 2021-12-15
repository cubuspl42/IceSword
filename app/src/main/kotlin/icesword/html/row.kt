package icesword.html

import icesword.frp.Cell
import icesword.frp.Till
import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension


data class RowStyleDeclaration(
    val justifyContentHorizontally: Cell<JustifyContent>? = null,
    val alignItemsVertically: Cell<Align>? = null,
)

fun createRow(
    tagName: String = "div",
    className: String? = null,
    style: DynamicStyleDeclaration = DynamicStyleDeclaration(),
    rowStyle: RowStyleDeclaration = RowStyleDeclaration(),
    horizontalGap: LinearDimension? = null,
    children: List<HTMLWidgetB<*>>,
) = object : HTMLWidgetB<HTMLWidget> {
    override fun build(tillDetach: Till): HTMLWidget {
        val element = createStyledHtmlElement(
            tagName = tagName,
            style = style.copy(
                display = Cell.constant(Display.flex),
                flexDirection = Cell.constant(FlexDirection.row),
                gap = horizontalGap?.let(Cell.Companion::constant),
                justifyContent = rowStyle.justifyContentHorizontally,
                alignItems = rowStyle.alignItemsVertically,
            ),
            tillDetach = tillDetach,
        ).apply {
            if (className != null) {
                this.className = className
            }

            HTMLWidgetB.build(children, tillDetach).forEach {
                appendChild(HTMLWidget.resolve(it))
            }
        }

        return HTMLWidget.of(element)
    }
}
