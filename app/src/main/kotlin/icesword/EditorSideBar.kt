package icesword

import html.createHeading4
import html.createHtmlElement
import icesword.editor.Editor
import icesword.editor.ElasticPrototype
import icesword.editor.InsertionPrototype
import icesword.editor.InsertionPrototype.*
import icesword.editor.KnotBrush
import icesword.editor.KnotPrototype
import icesword.editor.KnotPrototype.OvergroundRockPrototype
import icesword.editor.KnotPrototype.UndergroundRockPrototype
import icesword.editor.LadderPrototype
import icesword.editor.LogPrototype
import icesword.editor.SpikesPrototype
import icesword.editor.TreeCrownPrototype
import icesword.editor.WapObjectPrototype
import icesword.editor.WapObjectPrototype.ChaliceTreasurePrototype
import icesword.editor.WapObjectPrototype.CoinPrototype
import icesword.editor.WapObjectPrototype.CrossTreasurePrototype
import icesword.editor.WapObjectPrototype.CrownTreasurePrototype
import icesword.editor.WapObjectPrototype.CrumblingPegPrototype
import icesword.editor.WapObjectPrototype.CutThroatPrototype
import icesword.editor.WapObjectPrototype.ElevatorPrototype
import icesword.editor.WapObjectPrototype.GeckoTreasurePrototype
import icesword.editor.WapObjectPrototype.RatPrototype
import icesword.editor.WapObjectPrototype.RingTreasurePrototype
import icesword.editor.WapObjectPrototype.RobberThiefPrototype
import icesword.editor.WapObjectPrototype.RopePrototype
import icesword.editor.WapObjectPrototype.ScepterTreasurePrototype
import icesword.editor.WapObjectPrototype.SkullTreasurePrototype
import icesword.frp.Till
import icesword.frp.map
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
                    createInsertWapObjectButton(
                        editor = editor,
                        text = "Rope",
                        wapObjectPrototype = RopePrototype,
                        tillDetach = tillDetach,
                    ),
                    createInsertWapObjectButton(
                        editor = editor,
                        text = "CrumblingPeg",
                        wapObjectPrototype = CrumblingPegPrototype,
                        tillDetach = tillDetach,
                    ),
                    createInsertElevatorButton(
                        editor = editor,
                        text = "Elevator",
                        tillDetach = tillDetach,
                    ),

                    createInsertWapObjectButton(
                        editor = editor,
                        text = "Coin",
                        wapObjectPrototype = CoinPrototype,
                        tillDetach = tillDetach,
                    ),
                    createInsertWapObjectButton(
                        editor = editor,
                        text = "Cross (treasure)",
                        wapObjectPrototype = CrossTreasurePrototype,
                        tillDetach = tillDetach,
                    ),
                    createInsertWapObjectButton(
                        editor = editor,
                        text = "Scepter (treasure)",
                        wapObjectPrototype = ScepterTreasurePrototype,
                        tillDetach = tillDetach,
                    ),
                    createInsertWapObjectButton(
                        editor = editor,
                        text = "Crown (treasure)",
                        wapObjectPrototype = CrownTreasurePrototype,
                        tillDetach = tillDetach,
                    ),
                    createInsertWapObjectButton(
                        editor = editor,
                        text = "Chalice (treasure)",
                        wapObjectPrototype = ChaliceTreasurePrototype,
                        tillDetach = tillDetach,
                    ),
                    createInsertWapObjectButton(
                        editor = editor,
                        text = "Ring (treasure)",
                        wapObjectPrototype = RingTreasurePrototype,
                        tillDetach = tillDetach,
                    ),
                    createInsertWapObjectButton(
                        editor = editor,
                        text = "Gecko (treasure)",
                        wapObjectPrototype = GeckoTreasurePrototype,
                        tillDetach = tillDetach,
                    ),
                    createInsertWapObjectButton(
                        editor = editor,
                        text = "Skull (treasure)",
                        wapObjectPrototype = SkullTreasurePrototype,
                        tillDetach = tillDetach,
                    ),

                    createInsertWapObjectButton(
                        editor = editor,
                        text = "Robber Thief",
                        wapObjectPrototype = RobberThiefPrototype,
                        tillDetach = tillDetach,
                    ),
                    createInsertWapObjectButton(
                        editor = editor,
                        text = "Cut Throat",
                        wapObjectPrototype = CutThroatPrototype,
                        tillDetach = tillDetach,
                    ),
                    createInsertWapObjectButton(
                        editor = editor,
                        text = "Rat",
                        wapObjectPrototype = RatPrototype,
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

    return createInsertEntityButton(
        editor = editor,
        text = text,
        insertionPrototype = ElasticInsertionPrototype(
            elasticPrototype = prototype,
        ),
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

    return createInsertEntityButton(
        editor = editor,
        text = text,
        insertionPrototype = KnotMeshInsertionPrototype(
            knotPrototype = knotPrototype,
        ),
        tillDetach = tillDetach,
    )
}

private fun createInsertWapObjectButton(
    editor: Editor,
    text: String,
    wapObjectPrototype: WapObjectPrototype,
    tillDetach: Till,
): HTMLElement =
    createInsertEntityButton(
        editor = editor,
        text = text,
        insertionPrototype = WapObjectInsertionPrototype(
            wapObjectPrototype = wapObjectPrototype,
        ),
        tillDetach = tillDetach,
    )

private fun createInsertElevatorButton(
    editor: Editor,
    text: String,
    tillDetach: Till,
): HTMLElement =
    createInsertEntityButton(
        editor = editor,
        text = text,
        insertionPrototype = ElevatorInsertionPrototype,
        tillDetach = tillDetach,
    )

private fun createInsertEntityButton(
    editor: Editor,
    text: String,
    insertionPrototype: InsertionPrototype,
    tillDetach: Till,
): HTMLElement =
    createSelectButton<InsertionPrototype?, InsertionPrototype>(
        value = insertionPrototype,
        name = text,
        selected = editor.insertionMode.map { it?.insertionPrototype },
        select = { it: InsertionPrototype -> editor.enterInsertionMode(it) },
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
