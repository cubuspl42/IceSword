package icesword

import html.createButton
import html.createHtmlElement
import icesword.editor.*
import icesword.frp.Till
import org.w3c.dom.HTMLElement


fun editorSideBar(
    editor: Editor,
    tillDetach: Till,
): HTMLElement {
    val root = createHtmlElement("div").apply {
        className = "editorSideBar"

        style.apply {
            display = "flex"
            flexDirection = "column"

            setProperty("gap", "4px")

            width = "140px"
            padding = "8px"
            backgroundColor = "lightgrey"
        }

    }

    return root.apply {
        listOf(
            createInsertElasticButton(
                editor = editor,
                prototype = LogPrototype,
                tillDetach = tillDetach,
            ),
            createInsertElasticButton(
                editor = editor,
                prototype = TreeCrownPrototype,
                tillDetach = tillDetach,
            ),
            createInsertKnotMeshButton(
                editor = editor,
                knotPrototype = UndergroundRockPrototype,
                tillDetach = tillDetach,
            ),
            createInsertKnotMeshButton(
                editor = editor,
                knotPrototype = OvergroundRockPrototype,
                tillDetach = tillDetach,
            ),
        ).forEach(::appendChild)
    }
}

private fun createInsertElasticButton(
    editor: Editor,
    prototype: ElasticPrototype,
    tillDetach: Till,
): HTMLElement {
    val className = prototype::class.simpleName ?: "???"
    val text = className.replace("Prototype", "")

    return createButton(
        text = text,
        onPressed = {
            editor.insertElastic(prototype)
        },
        tillDetach = tillDetach,
    )
}

private fun createInsertKnotMeshButton(
    editor: Editor,
    knotPrototype: KnotPrototype,
    tillDetach: Till,
): HTMLElement {
    val className = knotPrototype::class.simpleName ?: "???"
    val text = className.replace("Prototype", "")

    return createButton(
        text = text,
        onPressed = {
            editor.insertKnotMesh(knotPrototype)
        },
        tillDetach = tillDetach,
    )
}

