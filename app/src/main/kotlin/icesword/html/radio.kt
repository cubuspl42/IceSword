package icesword.html

import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.reactTill
import icesword.frp.values
import icesword.html.HTMLWidget.HTMLShadowWidget
import org.w3c.dom.HTMLInputElement

class RadioGroup<A>(
    initialSelectedValue: A,
    private val dump: (A) -> String,
) {
    companion object {
        private var nextGroupId = 0

        fun allocateGroupId() = nextGroupId++
    }

    private val groupName = "group-${allocateGroupId()}"

    private val _selectedValue = MutCell(initialSelectedValue)

    val selectedValue: Cell<A> = _selectedValue

    fun createRadioInput(
        style: DynamicStyleDeclaration? = null,
        value: A,
    ): HTMLWidgetB<*> = object : HTMLWidgetB<HTMLWidget> {
        override fun build(tillDetach: Till): HTMLWidget {
            val inputWidget = createHTMLWidget(
                tagName = "input",
                style = style,
                tillDetach = tillDetach,
            )

            val inputElement = inputWidget.resolve() as HTMLInputElement

            inputElement.apply {
                this.type = "radio"
                this.name = groupName
                this.value = dump(value)
                this.checked = selectedValue.sample() == value
            }

            inputElement.onChange().reactTill(tillDetach) {
                _selectedValue.set(value)
            }

            return HTMLWidget.of(inputElement)
        }
    }
}
