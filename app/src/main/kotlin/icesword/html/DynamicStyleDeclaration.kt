package icesword.html

import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.reactTill
import icesword.geometry.IntVec2
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.Cursor
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.FontWeight
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension
import kotlinx.css.Overflow
import kotlinx.css.PointerEvents
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
    val width: Cell<LinearDimension>? = null,
    val height: Cell<LinearDimension>? = null,
    val display: Cell<Display>? = null,
    val displayStyle: FlexStyleDeclaration? = null,
    val margin: Cell<LinearDimension>? = null,
    val marginBlock: Cell<LinearDimension>? = null,
    val padding: Cell<LinearDimension>? = null,
    val paddingString: Cell<String>? = null,
    val fontFamily: Cell<String>? = null,
    val fontWeight: Cell<FontWeight?>? = null,
    val flexDirection: Cell<FlexDirection>? = null,
    val gap: Cell<LinearDimension>? = null,
    val justifyItems: Cell<Align>? = null,
    val alignItems: Cell<Align>? = null,
    val justifyContent: Cell<JustifyContent>? = null,
    val alignSelf: Cell<Align>? = null,
    val backgroundColor: Cell<Color>? = null,
    val cursor: Cell<Cursor?>? = null,
    val transform: Cell<Transform?>? = null,
    val pointerEvents: Cell<PointerEvents?>? = null,
    val overflow: Cell<Overflow?>? = null,
) {
    fun linkTo(
        style: CSSStyleDeclaration,
        tillDetach: Till,
    ) {
        linkProperty(
            style = style,
            propertyName = "width",
            property = width?.map { it.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "height",
            property = height?.map { it.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "display",
            property = display?.map { it.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "margin",
            property = margin?.map { it.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "margin-block",
            property = marginBlock?.map { it.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "padding",
            property = padding?.map { it.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "padding",
            property = paddingString,
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "font-family",
            property = fontFamily,
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "font-weight",
            property = fontWeight?.map { it?.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "flex-direction",
            property = flexDirection?.map { it.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "gap",
            property = gap?.map { it.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "justify-items",
            property = justifyItems?.map { it.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "align-items",
            property = alignItems?.map { it.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "justify-content",
            property = justifyContent?.map { it.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "align-self",
            property = alignSelf?.map { it.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "background-color",
            property = backgroundColor?.map { it.value },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "cursor",
            property = cursor?.map { it?.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "transform",
            property = transform?.map { it?.toStyleString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "pointer-events",
            property = pointerEvents?.map { it?.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "overflow",
            property = overflow?.map { it?.toString() },
            till = tillDetach,
        )

        displayStyle?.linkTo(
            style = style,
            tillDetach = tillDetach,
        )
    }
}


data class FlexStyleDeclaration(
    val direction: Cell<FlexDirection>? = null,
    val gap: Cell<LinearDimension>? = null,
    // Alignment along the main axis
    val justifyContent: Cell<JustifyContent>? = null,
    // Alignment along the cross axis
    val alignItems: Cell<Align>? = null,
) {
    fun linkTo(
        style: CSSStyleDeclaration,
        tillDetach: Till,
    ) {
        style.display = Display.flex.toString()

        linkProperty(
            style = style,
            propertyName = "flex-direction",
            property = direction?.map { it.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "gap",
            property = gap?.map { it.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "justify-content",
            property = justifyContent?.map { it.toString() },
            till = tillDetach,
        )

        linkProperty(
            style = style,
            propertyName = "align-items",
            property = alignItems?.map { it.toString() },
            till = tillDetach,
        )
    }
}

fun linkProperty(
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

