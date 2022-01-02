package icesword.ui

import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.mapTillRemoved
import icesword.html.DynamicStyleDeclaration
import icesword.html.HTMLWidget
import icesword.html.HTMLWidgetB
import icesword.html.createHTMLElementRaw
import icesword.html.createHTMLWidgetB
import icesword.html.resolve
import kotlinx.css.Align
import kotlinx.css.Display
import org.w3c.dom.HTMLElement

fun createStackLayout(children: List<HTMLElement>): HTMLElement =
    createHTMLElementRaw("div").apply {
        className = "stack"

        style.apply {
            display = "grid"
            setProperty("grid-template-columns", "minmax(0, 1fr)")
            setProperty("grid-template-rows", "minmax(0, 1fr)")
        }

        children.forEachIndexed { index, child ->
            child.style.apply {
                setProperty("grid-column", "1")
                setProperty("grid-row", "1")

                display = "grid"
                setProperty("grid-template-columns", "minmax(0, 1fr)")
                setProperty("grid-template-rows", "minmax(0, 1fr)")

                zIndex = index.toString()
            }

            appendChild(child)
        }
    }

fun createStackWb(
    children: DynamicList<HTMLWidgetB<*>>,
): HTMLWidgetB<HTMLWidget> = object : HTMLWidgetB<HTMLWidget> {
    override fun build(tillDetach: Till): HTMLWidget = createHTMLWidgetB(
        tagName = "div",
        style = DynamicStyleDeclaration(
            display = Cell.constant(Display.grid),
            alignItems = Cell.constant(Align.stretch),
            justifyItems = Cell.constant(Align.stretch),
        ),
        children = HTMLWidgetB.buildDl(children, tillDetach).mapTillRemoved(tillDetach) { child, _ ->
            child.apply {
                (resolve() as HTMLElement).style.apply {
                    setProperty("grid-column", "1")
                    setProperty("grid-row", "1")
                }
            }
        },
    ).build(tillDetach)
}
