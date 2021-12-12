package icesword

import icesword.html.createHeading4
import icesword.html.createHTMLElementRaw
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
import icesword.editor.WapObjectPrototype.GeckoTreasurePrototype
import icesword.editor.WapObjectPrototype.RatPrototype
import icesword.editor.WapObjectPrototype.RingTreasurePrototype
import icesword.editor.WapObjectPrototype.RobberThiefPrototype
import icesword.editor.WapObjectPrototype.ScepterTreasurePrototype
import icesword.editor.WapObjectPrototype.SkullTreasurePrototype
import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.Till
import icesword.frp.map
import icesword.html.DynamicStyleDeclaration
import icesword.html.HTMLWidget
import icesword.html.HTMLWidgetB
import icesword.html.createGrid
import icesword.html.resolve
import icesword.ui.createImageSelectButton
import icesword.ui.createSelectButton
import kotlinx.css.Align
import kotlinx.css.px
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node


fun editorSideBar(
    editor: Editor,
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
    tillDetach: Till,
): HTMLElement {
    val children = listOf(
        createInsertEntityButton(
            editor = editor,
            text = "WAP32 object",
            imagePath = "images/wapObject.png",
            insertionPrototype = WapObjectInsertionPrototype.Empty,
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = LogPrototype,
            imagePath = "images/CLAW/LEVEL3/TILES/ACTION/657.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = TreeCrownPrototype,
            imagePath = "images/CLAW/LEVEL3/TILES/ACTION/645.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = LadderPrototype,
            imagePath = "images/CLAW/LEVEL3/TILES/ACTION/669.png",
            tillDetach = tillDetach,
        ),
        createInsertElasticButton(
            editor = editor,
            prototype = SpikesPrototype,
            imagePath = "images/CLAW/LEVEL3/TILES/ACTION/686.png",
            tillDetach = tillDetach,
        ),
        createInsertKnotMeshButton(
            editor = editor,
            knotPrototype = UndergroundRockPrototype,
            imagePath = "images/CLAW/LEVEL3/TILES/ACTION/621.png",
            tillDetach = tillDetach,
        ),
        createInsertKnotMeshButton(
            editor = editor,
            knotPrototype = OvergroundRockPrototype,
            imagePath = "images/CLAW/LEVEL3/TILES/ACTION/604.png",
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Rope",
            imagePath = "images/CLAW/LEVEL3/IMAGES/ROPE/FRAME001.png",
            insertionPrototype = RopeInsertionPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Crate stack",
            imagePath = "images/CLAW/LEVEL3/IMAGES/CRATES/FRAME001.png",
            insertionPrototype = CrateStackInsertionPrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "CrumblingPeg",
            imagePath = "images/CLAW/LEVEL3/IMAGES/CRUMBLINPEG1/FRAME001.png",
            wapObjectPrototype = CrumblingPegPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Elevator (horizontal)",
            imagePath = "images/CLAW/LEVEL3/IMAGES/ELEVATOR1/FRAME001.png",
            insertionPrototype = HorizontalElevatorInsertionPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Elevator (vertical)",
            imagePath = "images/CLAW/LEVEL3/IMAGES/ELEVATOR1/FRAME001.png",
            insertionPrototype = VerticalElevatorInsertionPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Elevator (path)",
            imagePath = "images/CLAW/LEVEL3/IMAGES/ELEVATOR1/FRAME001.png",
            insertionPrototype = PathElevatorInsertionPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            text = "Floor spike row",
            imagePath = "images/CLAW/LEVEL3/IMAGES/FLOORSPIKES1/FRAME001.png",
            insertionPrototype = FloorSpikeInsertionPrototype,
            tillDetach = tillDetach,
        ),

        createInsertWapObjectButton(
            editor = editor,
            text = "Coin",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/COINS/FRAME001.png",
            wapObjectPrototype = CoinPrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Cross (treasure)",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/CROSSES/GREEN/CROSS3.png",
            wapObjectPrototype = CrossTreasurePrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Scepter (treasure)",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/SCEPTERS/GREEN/SCEPTR3.png",
            wapObjectPrototype = ScepterTreasurePrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Crown (treasure)",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/CROWNS/GREEN/CROWN3.png",
            wapObjectPrototype = CrownTreasurePrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Chalice (treasure)",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/CHALICES/GREEN/CHALICE3.png",
            wapObjectPrototype = ChaliceTreasurePrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Ring (treasure)",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/RINGS/GREEN/RING3.png",
            wapObjectPrototype = RingTreasurePrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Gecko (treasure)",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/GECKOS/GREEN/GECKO3.png",
            wapObjectPrototype = GeckoTreasurePrototype,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Skull (treasure)",
            imagePath = "images/CLAW/GAME/IMAGES/TREASURE/JEWELEDSKULL/GREEN/SKULL3.png",
            wapObjectPrototype = SkullTreasurePrototype,
            tillDetach = tillDetach,
        ),

        createInsertEnemyButton(
            editor = editor,
            text = "Robber Thief",
            imagePath = "images/CLAW/LEVEL3/IMAGES/ROBBERTHIEF/FRAME001.png",
            wapObjectPrototype = RobberThiefPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEnemyButton(
            editor = editor,
            text = "Cut Throat",
            imagePath = "images/CLAW/LEVEL3/IMAGES/CUTTHROAT/FRAME001.png",
            wapObjectPrototype = CutThroatPrototype,
            tillDetach = tillDetach,
        ),
        createInsertEnemyButton(
            editor = editor,
            text = "Rat",
            imagePath = "images/CLAW/LEVEL3/IMAGES/RAT/FRAME001.png",
            wapObjectPrototype = RatPrototype,
            tillDetach = tillDetach,
        ),
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

private fun createInsertElasticButton(
    editor: Editor,
    prototype: ElasticPrototype,
    imagePath: String,
    tillDetach: Till,
): HTMLElement {
    val className = prototype::class.simpleName ?: "???"
    val text = className.replace("Prototype", "")

    return createInsertEntityButton(
        editor = editor,
        text = text,
        imagePath = imagePath,
        insertionPrototype = ElasticInsertionPrototype(
            elasticPrototype = prototype,
        ),
        tillDetach = tillDetach,
    )
}

private fun createInsertKnotMeshButton(
    editor: Editor,
    knotPrototype: KnotPrototype,
    imagePath: String,
    tillDetach: Till,
): HTMLElement {
    val className = knotPrototype::class.simpleName ?: "???"
    val text = className.replace("Prototype", "")

    return createInsertEntityButton(
        editor = editor,
        text = text,
        imagePath = imagePath,
        insertionPrototype = KnotMeshInsertionPrototype(
            knotPrototype = knotPrototype,
        ),
        tillDetach = tillDetach,
    )
}

private fun createInsertWapObjectButton(
    editor: Editor,
    text: String,
    imagePath: String,
    wapObjectPrototype: WapObjectPrototype,
    tillDetach: Till,
): HTMLElement =
    createInsertEntityButton(
        editor = editor,
        text = text,
        imagePath = imagePath,
        insertionPrototype = WapObjectInsertionPrototype(
            wapObjectPrototype = wapObjectPrototype,
        ),
        tillDetach = tillDetach,
    )

private fun createInsertEnemyButton(
    editor: Editor,
    text: String,
    imagePath: String,
    wapObjectPrototype: WapObjectPrototype,
    tillDetach: Till,
): HTMLElement =
    createInsertEntityButton(
        editor = editor,
        text = "$text [enemy]",
        imagePath = imagePath,
        insertionPrototype = EnemyInsertionPrototype(
            wapObjectPrototype = wapObjectPrototype,
        ),
        tillDetach = tillDetach,
    )

private fun createInsertEntityButton(
    editor: Editor,
    text: String,
    imagePath: String,
    insertionPrototype: InsertionPrototype,
    tillDetach: Till,
): HTMLElement =
    createImageSelectButton(
        value = insertionPrototype,
        imagePath = imagePath,
        selected = editor.insertionMode.map { it?.insertionPrototype },
        select = { editor.enterInsertionMode(it) },
    ).build(tillDetach = tillDetach).element

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
