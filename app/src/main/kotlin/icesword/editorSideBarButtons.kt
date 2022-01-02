package icesword

import icesword.editor.Editor
import icesword.editor.entities.elastic.prototype.ElasticPrototype
import icesword.editor.InsertionPrototype
import icesword.editor.entities.KnotPrototype
import icesword.editor.retails.Retail
import icesword.editor.entities.wap_object.prototype.WapObjectPrototype
import icesword.frp.Till
import icesword.frp.map
import icesword.html.HTMLWidgetB
import icesword.ui.createImageSelectButton
import org.w3c.dom.HTMLElement

fun createInsertElasticButton(
    editor: Editor,
    prototype: ElasticPrototype,
    retail: Retail,
    imagePath: String,
    tillDetach: Till,
): HTMLElement {
    val className = prototype::class.simpleName ?: "???"
    val text = className.replace("Prototype", "")

    return createInsertEntityButton(
        editor = editor,
        text = text,
        imagePath = imagePath,
        insertionPrototype = InsertionPrototype.ElasticInsertionPrototype(
            elasticPrototype = prototype,
            retail = retail,
        ),
        tillDetach = tillDetach,
    )
}

fun createInsertKnotMeshButton(
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
        insertionPrototype = InsertionPrototype.KnotMeshInsertionPrototype(
            knotPrototype = knotPrototype,
        ),
        tillDetach = tillDetach,
    )
}

fun createInsertWapObjectButton(
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
        insertionPrototype = InsertionPrototype.WapObjectInsertionPrototype(
            wapObjectPrototype = wapObjectPrototype,
        ),
        tillDetach = tillDetach,
    )

fun createInsertEnemyButton(
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
        insertionPrototype = InsertionPrototype.EnemyInsertionPrototype(
            wapObjectPrototype = wapObjectPrototype,
        ),
        tillDetach = tillDetach,
    )

fun createInsertEntityButton(
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

fun createKnotPaintButton(
    editor: Editor,
    imagePath: String,
    knotPrototype: KnotPrototype,
): HTMLWidgetB<*> =
    createImageSelectButton(
        value = knotPrototype,
        imagePath = imagePath,
        selected = editor.selectedKnotPrototype,
        select = { editor.enterKnotPaintMode(knotPrototype) },
    )
