package icesword.ui

import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.Stream
import icesword.frp.mergeWith
import icesword.frp.units
import icesword.frp.update
import icesword.geometry.IntRect
import icesword.html.DynamicStyleDeclaration
import icesword.html.FlexStyleDeclaration
import icesword.html.GridStyleDeclaration
import icesword.html.HTMLWidget
import icesword.html.HTMLWidgetB
import icesword.html.createColumnWb
import icesword.html.createGrid
import icesword.html.createLabel
import icesword.html.createNumberInput
import icesword.html.createTextButtonWb
import icesword.html.createTextInput
import icesword.html.flatMap
import icesword.html.map
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.px

fun createBasicDialog(
    content: HTMLWidgetB<*>,
    onClose: Stream<Unit> = Stream.never(),
): HTMLWidgetB<Dialog> = createTextButtonWb(
    style = DynamicStyleDeclaration(
        alignSelf = Cell.constant(Align.flexEnd),
    ),
    text = "✕",
).flatMap { closeButton ->
    createColumnWb(
        style = DynamicStyleDeclaration(
            backgroundColor = Cell.constant(Color("#d1d1d1")),
            paddingString = Cell.constant("16px"),
            fontFamily = Cell.constant("sans-serif"),
        ),
        flexStyle = FlexStyleDeclaration(
            alignItems = Cell.constant(Align.center),
        ),
        verticalGap = 8.px,
        children = listOf(
            closeButton,
            content,
        ),
    ).map { content ->
        Dialog(
            content = content,
            onClose = closeButton.onPressed.units().mergeWith(onClose),
        )
    }
}

data class InputColumnItem(
    val label: HTMLWidgetB<*>,
    val input: HTMLWidgetB<*>,
)

fun createInputColumn(
    inputs: List<InputColumnItem>,
): HTMLWidgetB<HTMLWidget> = createGrid(
    columnCount = 2,
    gridStyle = GridStyleDeclaration(
        alignContent = Cell.constant(Align.start),
        gap = Cell.constant(4.px),
    ),
    children = inputs.flatMap {
        listOf(
            it.label,
            it.input,
        )
    }
)

fun createRectangleInputs(
    labelText: String,
    property: MutCell<IntRect>,
): List<InputColumnItem> =
    listOf(
        createIntegerInput(
            labelText = "$labelText / left",
            initialValue = property.sample().left,
            onValueChanged = { left ->
                property.update {
                    IntRect.fromLtrb(
                        left = left,
                        top = it.top,
                        right = it.right,
                        bottom = it.bottom,
                    )
                }
            },
        ),
        createIntegerInput(
            labelText = "$labelText / top",
            initialValue = property.sample().top,
            onValueChanged = { top ->
                property.update {
                    IntRect.fromLtrb(
                        left = it.left,
                        top = top,
                        right = it.right,
                        bottom = it.bottom,
                    )
                }
            },
        ),
        createIntegerInput(
            labelText = "$labelText / right",
            initialValue = property.sample().right,
            onValueChanged = { right ->
                property.update {
                    IntRect.fromLtrb(
                        left = it.left,
                        top = it.top,
                        right = right,
                        bottom = it.bottom,
                    )
                }
            },
        ),
        createIntegerInput(
            labelText = "$labelText / bottom",
            initialValue = property.sample().bottom,
            onValueChanged = { bottom ->
                property.update {
                    IntRect.fromLtrb(
                        left = it.left,
                        top = it.top,
                        right = it.right,
                        bottom = bottom,
                    )
                }
            },
        ),
    )

fun createSimpleIntegerInput(
    labelText: String,
    property: MutCell<Int>,
): InputColumnItem = createIntegerInput(
    labelText = labelText,
    initialValue = property.sample(),
    onValueChanged = property::set,
)

fun createIntegerInput(
    labelText: String,
    initialValue: Int,
    onValueChanged: (newValue: Int) -> Unit,
): InputColumnItem {
    val numberInput = createNumberInput(
        initialValue = initialValue,
        onValueChanged = onValueChanged,
    )

    return InputColumnItem(
        label = createLabel(labelText),
        input = numberInput
    )
}

fun createStringInput(
    labelText: String,
    property: MutCell<String>,
): InputColumnItem {
    val numberInput = createTextInput(
        initialText = property.sample(),
        onTextChanged = property::set,
    )

    return InputColumnItem(
        label = createLabel(labelText),
        input = numberInput
    )
}
