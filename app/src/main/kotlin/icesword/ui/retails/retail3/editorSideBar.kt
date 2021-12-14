package icesword.ui.retails.retail3

import icesword.createInsertElasticButton
import icesword.createInsertEnemyButton
import icesword.createInsertEntityButton
import icesword.createInsertKnotMeshButton
import icesword.createInsertWapObjectButton
import icesword.editor.CrateStackPrototype
import icesword.editor.Editor
import icesword.editor.InsertionPrototype
import icesword.editor.KnotPrototype
import icesword.editor.LadderPrototype
import icesword.editor.LogPrototype
import icesword.editor.Retail
import icesword.editor.RopePrototype
import icesword.editor.SpikesPrototype
import icesword.editor.TreeCrownPrototype
import icesword.editor.WapObjectPrototype
import icesword.frp.Till
import org.w3c.dom.HTMLElement

fun buildInsertionButtons(
    retail: Retail,
    editor: Editor,
    tillDetach: Till,
): List<HTMLElement> = listOf(
    createInsertEntityButton(
        editor = editor,
        text = "WAP32 object",
        imagePath = "images/wapObject.png",
        insertionPrototype = InsertionPrototype.WapObjectInsertionPrototype.Empty,
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
        knotPrototype = KnotPrototype.UndergroundRockPrototype,
        imagePath = "images/CLAW/LEVEL3/TILES/ACTION/621.png",
        tillDetach = tillDetach,
    ),
    createInsertKnotMeshButton(
        editor = editor,
        knotPrototype = KnotPrototype.OvergroundRockPrototype,
        imagePath = "images/CLAW/LEVEL3/TILES/ACTION/604.png",
        tillDetach = tillDetach,
    ),
    createInsertEntityButton(
        editor = editor,
        text = "Rope",
        imagePath = "images/CLAW/LEVEL3/IMAGES/ROPE/FRAME001.png",
        insertionPrototype = InsertionPrototype.RopeInsertionPrototype(
            ropePrototype = RopePrototype(retail = retail),
        ),
        tillDetach = tillDetach,
    ),
    createInsertEntityButton(
        editor = editor,
        text = "Crate stack",
        imagePath = "images/CLAW/LEVEL3/IMAGES/CRATES/FRAME001.png",
        insertionPrototype = InsertionPrototype.CrateStackInsertionPrototype(
            crateStackPrototype = CrateStackPrototype(retail = retail),
        ),
        tillDetach = tillDetach,
    ),
    createInsertWapObjectButton(
        editor = editor,
        text = "CrumblingPeg",
        imagePath = "images/CLAW/LEVEL3/IMAGES/CRUMBLINPEG1/FRAME001.png",
        wapObjectPrototype = WapObjectPrototype.CrumblingPegPrototype,
        tillDetach = tillDetach,
    ),
    createInsertEntityButton(
        editor = editor,
        text = "Elevator (horizontal)",
        imagePath = "images/CLAW/LEVEL3/IMAGES/ELEVATOR1/FRAME001.png",
        insertionPrototype = InsertionPrototype.HorizontalElevatorInsertionPrototype,
        tillDetach = tillDetach,
    ),
    createInsertEntityButton(
        editor = editor,
        text = "Elevator (vertical)",
        imagePath = "images/CLAW/LEVEL3/IMAGES/ELEVATOR1/FRAME001.png",
        insertionPrototype = InsertionPrototype.VerticalElevatorInsertionPrototype,
        tillDetach = tillDetach,
    ),
    createInsertEntityButton(
        editor = editor,
        text = "Elevator (path)",
        imagePath = "images/CLAW/LEVEL3/IMAGES/ELEVATOR1/FRAME001.png",
        insertionPrototype = InsertionPrototype.PathElevatorInsertionPrototype,
        tillDetach = tillDetach,
    ),
    createInsertEntityButton(
        editor = editor,
        text = "Floor spike row",
        imagePath = "images/CLAW/LEVEL3/IMAGES/FLOORSPIKES1/FRAME001.png",
        insertionPrototype = InsertionPrototype.FloorSpikeInsertionPrototype,
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

    createInsertEnemyButton(
        editor = editor,
        text = "Robber Thief",
        imagePath = "images/CLAW/LEVEL3/IMAGES/ROBBERTHIEF/FRAME001.png",
        wapObjectPrototype = WapObjectPrototype.RobberThiefPrototype,
        tillDetach = tillDetach,
    ),
    createInsertEnemyButton(
        editor = editor,
        text = "Cut Throat",
        imagePath = "images/CLAW/LEVEL3/IMAGES/CUTTHROAT/FRAME001.png",
        wapObjectPrototype = WapObjectPrototype.CutThroatPrototype,
        tillDetach = tillDetach,
    ),
    createInsertEnemyButton(
        editor = editor,
        text = "Rat",
        imagePath = "images/CLAW/LEVEL3/IMAGES/RAT/FRAME001.png",
        wapObjectPrototype = WapObjectPrototype.RatPrototype,
        tillDetach = tillDetach,
    ),
)
