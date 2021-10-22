package icesword

import html.createButton
import html.createHeading4
import html.createHtmlElement
import icesword.editor.Editor
import icesword.editor.ElasticPrototype
import icesword.editor.KnotBrush
import icesword.editor.KnotPrototype
import icesword.editor.KnotPrototype.OvergroundRockPrototype
import icesword.editor.KnotPrototype.UndergroundRockPrototype
import icesword.editor.LadderPrototype
import icesword.editor.LogPrototype
import icesword.editor.SpikesPrototype
import icesword.editor.TreeCrownPrototype
import icesword.editor.WapObjectPrototype
import icesword.editor.WapObjectPrototype.*
import icesword.frp.Till
import icesword.ui.createSelectButton
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

            setProperty("gap", "16px")

            width = "140px"
            padding = "8px"
            backgroundColor = "lightgrey"
        }

    }

    return root.apply {
        listOf(
            createSection(
                title = "Insert...",
                children = listOf(
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
                    createInsertElasticButton(
                        editor = editor,
                        prototype = LadderPrototype,
                        tillDetach = tillDetach,
                    ),
                    createInsertElasticButton(
                        editor = editor,
                        prototype = SpikesPrototype,
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
                    createInsertWapObject(
                        editor = editor,
                        text = "Rope",
                        insert = { editor.insertWapObject(RopePrototype) },
                        tillDetach = tillDetach,
                    ),
                    createInsertWapObject(
                        editor = editor,
                        text = "CrumblingPeg",
                        insert = { editor.insertWapObject(CrumblingPegPrototype) },
                        tillDetach = tillDetach,
                    ),
                    createInsertWapObject(
                        editor = editor,
                        text = "Coin",
                        insert = { editor.insertWapObject(CoinPrototype) },
                        tillDetach = tillDetach,
                    ),
                ),
            ),
            createSection(
                title = "Knot brush",
                children = listOf(
                    createKnotBrushButton(
                        editor = editor,
                        knotBrush = KnotBrush.Additive,
                        tillDetach = tillDetach,
                    ),
                    createKnotBrushButton(
                        editor = editor,
                        knotBrush = KnotBrush.Eraser,
                        tillDetach = tillDetach,
                    ),
                ),
            ),
        ).forEach(::appendChild)
    }
}

private fun createSection(
    title: String,
    children: List<HTMLElement>,
): HTMLElement =
    createHtmlElement("div").apply {
        className = "editorSideBarSection"

        style.apply {
            display = "flex"
            flexDirection = "column"

            setProperty("gap", "8px")
        }

        appendChild(
            createHeading4(text = title).apply {
                style.apply {
                    margin = "0"
                    fontFamily = "sans-serif"
                }
            }
        )

        appendChild(
            createHtmlElement("div").apply {
                style.apply {
                    display = "flex"
                    flexDirection = "column"

                    setProperty("gap", "4px")
                }

                children.forEach(this::appendChild)
            },
        )
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

private fun createInsertWapObject(
    editor: Editor,
    text: String,
    insert: () -> Unit,
    tillDetach: Till,
): HTMLElement =
    createButton(
        text = text,
        onPressed = {
            insert()
        },
        tillDetach = tillDetach,
    )

private fun createKnotBrushButton(
    editor: Editor,
    knotBrush: KnotBrush,
    tillDetach: Till,
): HTMLElement =
    createSelectButton(
        value = knotBrush,
        name = knotBrush.name,
        selected = editor.selectedKnotBrush,
        select = editor::selectKnotBrush,
        tillDetach = tillDetach,
    )
