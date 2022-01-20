package icesword.ui

import icesword.editor.Editor
import icesword.editor.entities.Warp
import icesword.editor.entities.wap_object.prototype.WapObjectPrototype
import icesword.editor.modes.InsertionPrototype
import icesword.editor.modes.InsertionPrototype.WarpInsertionPrototype
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
import kotlinx.css.Overflow
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
            overflowY = Overflow.scroll.toString()

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
            createBrushesSection(
                editor = editor,
                retailUiPrototype = retailUiPrototype,
                tillDetach = tillDetach,
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
            child = createPreviewImage("images/wapObject.png"),
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


        createInsertWapObjectButton(
            editor = editor,
            text = "HealthPotion1",
            imagePath = "images/CLAW/GAME/IMAGES/HEALTH/POTION1/HEALTH1.png",
            wapObjectPrototype = WapObjectPrototype.HealthPotion1,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "HealthPotion2",
            imagePath = "images/CLAW/GAME/IMAGES/HEALTH/POTION2/HEALTH2.png",
            wapObjectPrototype = WapObjectPrototype.HealthPotion2,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "HealthPotion3",
            imagePath = "images/CLAW/GAME/IMAGES/HEALTH/POTION3/HEALTH3.png",
            wapObjectPrototype = WapObjectPrototype.HealthPotion3,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "AmmoShot",
            imagePath = "images/CLAW/GAME/IMAGES/AMMO/SHOT/AMMO1.png",
            wapObjectPrototype = WapObjectPrototype.AmmoShot,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "AmmoShotbag",
            imagePath = "images/CLAW/GAME/IMAGES/AMMO/SHOTBAG/AMMO2.png",
            wapObjectPrototype = WapObjectPrototype.AmmoShotbag,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "AmmoDeathbag",
            imagePath = "images/CLAW/GAME/IMAGES/AMMO/DEATHBAG/AMMO3.png",
            wapObjectPrototype = WapObjectPrototype.AmmoDeathbag,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Dynamite",
            imagePath = "images/CLAW/GAME/IMAGES/DYNAMITE/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.Dynamite,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "MagicGlow",
            imagePath = "images/CLAW/GAME/IMAGES/MAGIC/GLOW/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.MagicGlow,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "MagicStarGlow",
            imagePath = "images/CLAW/GAME/IMAGES/MAGIC/STARGLOW/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.MagicStarGlow,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "MagicClaw",
            imagePath = "images/CLAW/GAME/IMAGES/MAGICCLAW/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.MagicClaw,
            tillDetach = tillDetach,
        ),

        createInsertWapObjectButton(
            editor = editor,
            text = "ExtraLife",
            imagePath = "images/CLAW/GAME/IMAGES/POWERUPS/EXTRALIFE/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.ExtraLife,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "FireSword",
            imagePath = "images/CLAW/GAME/IMAGES/POWERUPS/FIRESWORD/FRAME01.png",
            wapObjectPrototype = WapObjectPrototype.FireSword,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Ghost",
            imagePath = "images/CLAW/GAME/IMAGES/POWERUPS/GHOST/FRAME1.png",
            wapObjectPrototype = WapObjectPrototype.Ghost,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "IceSword",
            imagePath = "images/CLAW/GAME/IMAGES/POWERUPS/ICESWORD/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.IceSword,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Invlulerable",
            imagePath = "images/CLAW/GAME/IMAGES/POWERUPS/INVULNERABLE/FRAME01.png",
            wapObjectPrototype = WapObjectPrototype.Invulnerable,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "LightningSword",
            imagePath = "images/CLAW/GAME/IMAGES/POWERUPS/LIGHTNINGSWORD/FRAME001.png",
            wapObjectPrototype = WapObjectPrototype.LightningSword,
            tillDetach = tillDetach,
        ),

        createInsertWapObjectButton(
            editor = editor,
            text = "Catnip1",
            imagePath = "images/CLAW/GAME/IMAGES/CATNIPS/NIP1/CATNIP1.png",
            wapObjectPrototype = WapObjectPrototype.Catnip1,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Catnip2",
            imagePath = "images/CLAW/GAME/IMAGES/CATNIPS/NIP2/CATNIP2.png",
            wapObjectPrototype = WapObjectPrototype.Catnip2,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "Checkpoint",
            imagePath = "images/CLAW/GAME/IMAGES/CHECKPOINTFLAG/01.png",
            wapObjectPrototype = WapObjectPrototype.Checkpoint,
            tillDetach = tillDetach,
        ),
        createInsertWapObjectButton(
            editor = editor,
            text = "FirstSuperCheckpoint",
            imagePath = "images/CLAW/GAME/IMAGES/SUPERCHECKPOINT/FRAME01.png",
            wapObjectPrototype = WapObjectPrototype.FirstSuperCheckpoint,
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            child = createPreviewImage("images/CLAW/GAME/IMAGES/WARP/FRAME001.png"),
            insertionPrototype = WarpInsertionPrototype(
                warpPrototype = Warp.HorizontalWarpPrototype,
            ),
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            child = createPreviewImage("images/CLAW/GAME/IMAGES/VERTWARP/FRAME001.png"),
            insertionPrototype = WarpInsertionPrototype(
                warpPrototype = Warp.VerticalWarpPrototype,
            ),
            tillDetach = tillDetach,
        ),
        createInsertEntityButton(
            editor = editor,
            child = createPreviewImage("images/CLAW/GAME/IMAGES/BOSSWARP/BOSSWARP1.png"),
            insertionPrototype = WarpInsertionPrototype(
                warpPrototype = Warp.BossWarpPrototype,
            ),
            tillDetach = tillDetach,
        ),
    )

    val elevatorButtons = createAllElevatorButtons(
        editor = editor,
        retail = retailUiPrototype.retail,
        tillDetach = tillDetach,
    )

    val children = staticInsertionButtons +
            elevatorButtons +
            retailUiPrototype.buildInsertionButtons(
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

private fun createBrushesSection(
    editor: Editor,
    retailUiPrototype: RetailUiPrototype,
    tillDetach: Till,
): HTMLElement {
    val children = retailUiPrototype.buildBrushesButtons(
        editor = editor,
    )

    return createTitledSection(
        title = "Brushes",
        child = createGrid(
            style = DynamicStyleDeclaration(
                justifyItems = constant(Align.center),
            ),
            columnCount = 2,
            gap = 4.px,
            children = children,
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

