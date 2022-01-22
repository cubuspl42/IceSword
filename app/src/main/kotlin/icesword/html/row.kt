package icesword.html

import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension


data class RowStyleDeclaration(
    val horizontalGap: Cell<LinearDimension>? = null,
    val justifyHorizontally: Cell<JustifyContent>? = null,
    val alignVertically: Cell<Align>? = null,
) {
    fun toFlexStyle() = FlexStyleDeclaration(
        gap = horizontalGap,
        justifyContent = justifyHorizontally,
        alignItems = alignVertically,
    )
}

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
                displayStyle = rowStyle.copy(
                    horizontalGap = rowStyle.horizontalGap ?: horizontalGap?.let(Cell.Companion::constant),
                ).toFlexStyle(),
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
