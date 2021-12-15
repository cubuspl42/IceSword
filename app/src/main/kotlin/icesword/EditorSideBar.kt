package icesword

import icesword.editor.Editor
import icesword.editor.InsertionPrototype
import icesword.editor.KnotBrush
import icesword.editor.retails.Retail
import icesword.editor.WapObjectPrototype
import icesword.frp.Cell.Companion.constant
import icesword.frp.Till
import icesword.html.DynamicStyleDeclaration
import icesword.html.HTMLWidget
import icesword.html.createGrid
import icesword.html.createHTMLElementRaw
import icesword.html.createHeading4
import icesword.html.resolve
import icesword.ui.retails.RetailUiPrototype
import kotlinx.css.Align
import kotlinx.css.px
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node


fun editorSideBar(
    editor: Editor,
    retailUiPrototype: RetailUiPrototype,
    tillDetach: Till,
): HTMLElement {
    val root = createHTMLElementRaw("div").apply {
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
            createInsertSection(
                editor = editor,
                retailUiPrototype = retailUiPrototype,
                tillDetach = tillDetach,
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

private fun createInsertSection(
    editor: Editor,
    retailUiPrototype: RetailUiPrototype,
    tillDetach: Till,
): HTMLElement {
    val staticInsertionButtons = listOf(
        createInsertEntityButton(
            editor = editor,
            text = "WAP32 object",
            imagePath = "images/wapObject.png",
            insertionPrototype = InsertionPrototype.WapObjectInsertionPrototype.Empty,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Coin",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/COINS/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.CoinPrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Cross (treasure)",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/CROSSES/GREEN/CROSS3.png",
            wapObjectPrototype = WapObjectPrototype.CrossTreasurePrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Scepter (treasure)",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/SCEPTERS/GREEN/SCEPTR3.png",
            wapObjectPrototype = WapObjectPrototype.ScepterTreasurePrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Crown (treasure)",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/CROWNS/GREEN/CROWN3.png",
            wapObjectPrototype = WapObjectPrototype.CrownTreasurePrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Chalice (treasure)",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/CHALICES/GREEN/CHALICE3.png",
            wapObjectPrototype = WapObjectPrototype.ChaliceTreasurePrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Ring (treasure)",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/RINGS/GREEN/RING3.png",
            wapObjectPrototype = WapObjectPrototype.RingTreasurePrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Gecko (treasure)",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/GECKOS/GREEN/GECKO3.png",
            wapObjectPrototype = WapObjectPrototype.GeckoTreasurePrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Skull (treasure)",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/JEWELEDSKULL/GREEN/SKULL3.png",
            wapObjectPrototype = WapObjectPrototype.SkullTreasurePrototype,
            tillDetach = tillDetach,
        ),
    )

    val children = staticInsertionButtons + retailUiPrototype.buildInsertionButtons(
        editor = editor,
        tillDetach = tillDetach,
    )

    return createTitledSection(
        title = "Insert...",
        child = createGrid(
            style = DynamicStyleDeclaration(
                justifyItems = constant(Align.center),
            ),
            columnCount = 2,
            gap = 4.px,
            children = children.map { HTMLWidget.of(it) },
        ).build(tillDetach).resolve()
    )
}

private fun createSection(
    title: String,
    alignItems: Align = Align.center,
    children: List<HTMLElement>,
): HTMLElement = createTitledSection(
    title = title,
    child = createHTMLElementRaw("div").apply {
        style.apply {
            display = "flex"
            flexDirection = "column"
            this.alignItems = alignItems.toString()

            setProperty("gap", "4px")
        }

        children.forEach(this::appendChild)
    },
)

private fun createTitledSection(
    title: String,
    child: Node,
): HTMLElement =
    createHTMLElementRaw("div").apply {
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

        appendChild(child)
    }

