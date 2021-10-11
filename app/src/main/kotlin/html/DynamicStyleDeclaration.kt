package html

import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import kotlinx.css.Cursor
import org.w3c.dom.css.CSSStyleDeclaration

data class DynamicStyleDeclaration(
    val cursor: Cell<Cursor?>? = null,
) {
    fun linkTo(
        style: CSSStyleDeclaration,
        tillDetach: Till,
    ) {
        linkProperty(
            style = style,
            propertyName = "cursor",
            property = cursor?.map { it?.toString() },
            till = tillDetach
        )
    }
}

private fun linkProperty(
    style: CSSStyleDeclaration,
    propertyName: String,
    property: Cell<String?>?,
    till: Till,
): Unit {
    property?.reactTill(till) {
        if (it != null) {
            style.setProperty(propertyName, it)
        } else {
            style.removeProperty(propertyName)
        }
    }
}
