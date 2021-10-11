package html

import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import icesword.geometry.IntVec2
import kotlinx.css.Cursor
import kotlinx.css.properties.Transforms
import kotlinx.css.properties.translate
import kotlinx.css.px
import org.w3c.dom.css.CSSStyleDeclaration

interface Transform {
    fun toStyleString(): String

    companion object {
        private fun translate(tx: Double, ty: Double): Transform =
            object : Transform {
                override fun toStyleString(): String =
                    Transforms().apply { translate(tx.px, ty.px) }.toString()
            }

        fun translate(tv: IntVec2): Transform =
            translate(tv.x.toDouble(), tv.y.toDouble())
    }
}

data class DynamicStyleDeclaration(
    val cursor: Cell<Cursor?>? = null,
    val transform: Cell<Transform?>? = null,
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

        linkProperty(
            style = style,
            propertyName = "transform",
            property = transform?.map { it?.toStyleString() },
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
