package icesword.html

import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.hold
import icesword.frp.map
import icesword.html.HTMLWidget.HTMLShadowWidget
import org.w3c.dom.HTMLInputElement

class NumberInputWidget(
    inputElement: HTMLInputElement,
    initialValue: Int,
    tillDetach: Till,
) : HTMLShadowWidget {
    override val root: HTMLWidget =
        HTMLWidget.of(inputElement)

    val value: Cell<Int> =
        inputElement.onChange().map { inputElement.value.toInt() }
            .hold(initialValue, tillDetach)
}

fun createNumberInput(
    initialValue: Int,
): HTMLWidgetB<NumberInputWidget> =
    object : HTMLWidgetB<NumberInputWidget> {
        override fun build(tillDetach: Till): NumberInputWidget {
            val inputElement =
                createHTMLElementRaw(tagName = "input") as HTMLInputElement

            inputElement.apply {
                type = "number"
                value = initialValue.toString()
            }

            return NumberInputWidget(
                inputElement = inputElement,
                initialValue = initialValue,
                tillDetach = tillDetach,
            )
        }
    }
