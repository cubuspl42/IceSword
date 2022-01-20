package icesword.ui

import icesword.editor.Editor
import icesword.editor.entities.elastic.prototype.ElasticPrototype
import icesword.editor.modes.InsertionPrototype
import icesword.editor.entities.KnotPrototype
import icesword.editor.entities.fixture.prototypes.FixturePrototype
import icesword.editor.retails.Retail
import icesword.editor.entities.wap_object.prototype.WapObjectPrototype
import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.Till
import icesword.frp.dynamic_list.staticListOf
import icesword.frp.map
import icesword.html.BorderStyleDeclaration
import icesword.html.DynamicStyleDeclaration
import icesword.html.HTMLWidgetB
import icesword.html.createTextWb
import icesword.html.createWrapperWb
import icesword.ui.retails.retail1.Retail1UiPrototype
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.pt
import kotlinx.css.px
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
        child = createPreviewImage(imagePath = imagePath),
        insertionPrototype = InsertionPrototype.ElasticInsertionPrototype(
            elasticPrototype = prototype,
            retail = retail,
        ),
        tillDetach = tillDetach,
    )
}

fun createInsertFixtureButton(
    editor: Editor,
    prototype: FixturePrototype,
    imagePath: String,
    tillDetach: Till,
): HTMLElement = createInsertEntityButton(
    editor = editor,
    child = createPreviewImage(imagePath = imagePath),
    insertionPrototype = InsertionPrototype.FixtureInsertionPrototype(
        fixturePrototype = prototype,
    ),
    tillDetach = tillDetach,
)

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
        child = createPreviewImage(imagePath = imagePath),
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
        child = createPreviewImage(imagePath = imagePath),
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
        child = createPreviewImage(imagePath = imagePath),
        insertionPrototype = InsertionPrototype.EnemyInsertionPrototype(
            wapObjectPrototype = wapObjectPrototype,
        ),
        tillDetach = tillDetach,
    )

fun createInsertEntityButton(
    editor: Editor,
    insertionPrototype: InsertionPrototype,
    child: HTMLWidgetB<*>,
    tillDetach: Till,
): HTMLElement =
    createCustomSelectButton(
        value = insertionPrototype,
        selected = editor.insertionMode.map { it?.insertionPrototype },
        select = { editor.enterInsertionMode(it) },
        child = child,
    ).build(tillDetach = tillDetach).element

fun createKnotPaintButton(
    editor: Editor,
    imagePath: String,
    knotPrototype: KnotPrototype,
): HTMLWidgetB<*> =
    createCustomSelectButton(
        value = knotPrototype,
        selected = editor.selectedKnotPrototype,
        select = { editor.enterKnotPaintMode(knotPrototype) },
        child = createPreviewImage(imagePath),
    )

fun createElevatorButton(
    editor: Editor,
    insertionPrototype: InsertionPrototype,
    imagePath: String,
    description: String,
    tillDetach: Till,
): HTMLElement =
    createInsertEntityButton(
        editor = editor,
        insertionPrototype = insertionPrototype,
        child = createStackWb(
            alignItems = Align.end,
            children = staticListOf(
                createPreviewImage(imagePath),
                createWrapperWb(
                    style = DynamicStyleDeclaration(
                        fontSize = constant(10.pt),
                        backgroundColor = constant(Color.gray.withAlpha(0.5)),
                        border = BorderStyleDeclaration(
                            radius = constant(4.px),
                        ),
                    ),
                    child = constant(
                        createTextWb(text = constant(description)),
                    ),
                ),
            ),
        ),
        tillDetach = tillDetach,
    )

fun createAllElevatorButtons(
    editor: Editor,
    retail: Retail,
    tillDetach: Till,
): List<HTMLElement> {
    val elevatorPrototype = retail.elevatorPrototype
    val imageSetId = elevatorPrototype.elevatorImageSetId

    val imageMetadata = editor.rezIndex.getImageMetadata(
        imageSetId = imageSetId,
        i = -1,
    ) ?: throw RuntimeException("Can't find elevator image: $imageSetId")

    val imagePath = getRezPngPath(imageMetadata)

    return listOf(
        createElevatorButton(
            editor = editor,
            imagePath = imagePath,
            description = "HORIZ.",
            insertionPrototype = InsertionPrototype.HorizontalElevatorInsertionPrototype(
                elevatorPrototype = elevatorPrototype,
            ),
            tillDetach = tillDetach,
        ),
        createElevatorButton(
            editor = editor,
            imagePath = imagePath,
            description = "VERT.",
            insertionPrototype = InsertionPrototype.VerticalElevatorInsertionPrototype(
                elevatorPrototype = elevatorPrototype,
            ),
            tillDetach = tillDetach,
        ),
        createElevatorButton(
            editor = editor,
            imagePath = imagePath,
            description = "PATH",
            insertionPrototype = InsertionPrototype.PathElevatorInsertionPrototype(
                elevatorPrototype = elevatorPrototype,
            ),
            tillDetach = tillDetach,
        ),
    )
}
