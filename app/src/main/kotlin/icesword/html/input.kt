package icesword.html

import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.hold
import icesword.frp.map
import icesword.frp.reactTill
import icesword.frp.values
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
    style: DynamicStyleDeclaration? = null,
    initialValue: Int,
    onValueChanged: ((newValue: Int) -> Unit)? = null,
): HTMLWidgetB<NumberInputWidget> = object : HTMLWidgetB<NumberInputWidget> {
    override fun build(tillDetach: Till): NumberInputWidget {
        val inputWidget = createHTMLWidget(
            tagName = "input",
            style = style,
            tillDetach = tillDetach,
        )

        val inputElement = inputWidget.resolve() as HTMLInputElement

        inputElement.apply {
            type = "number"
            value = initialValue.toString()
        }

        val root = NumberInputWidget(
            inputElement = inputElement,
            initialValue = initialValue,
            tillDetach = tillDetach,
        )

        if (onValueChanged != null) {
            root.value.values().reactTill(tillDetach) {
                onValueChanged(it)
            }
        }

        return root
    }
}

class TextInputWidget(
    inputElement: HTMLInputElement,
    initialText: String,
    tillDetach: Till,
) : HTMLShadowWidget {
    override val root: HTMLWidget =
        HTMLWidget.of(inputElement)

    val text: Cell<String> =
        inputElement.onChange().map { inputElement.value }
            .hold(initialText, tillDetach)
}

fun createTextInput(
    initialText: String,
    onTextChanged: ((newValue: String) -> Unit)? = null,
) = object : HTMLWidgetB<TextInputWidget> {
    override fun build(tillDetach: Till): TextInputWidget {
        val inputElement =
            createHTMLElementRaw(tagName = "input") as HTMLInputElement

        inputElement.apply {
            value = initialText
        }


        val root = TextInputWidget(
            inputElement = inputElement,
            initialText = initialText,
            tillDetach = tillDetach,
        )

        if (onTextChanged != null) {
            root.text.values().reactTill(tillDetach) {
                onTextChanged(it)
            }
        }

        return root
    }
}
