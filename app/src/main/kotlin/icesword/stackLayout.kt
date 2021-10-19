package icesword

import html.createHtmlElement
import org.w3c.dom.HTMLElement

fun createStackLayout(children: List<HTMLElement>): HTMLElement =
    createHtmlElement("div").apply {
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
