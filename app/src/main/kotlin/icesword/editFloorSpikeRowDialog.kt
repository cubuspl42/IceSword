package icesword

import icesword.editor.FloorSpikeRow
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.dynamic_list.mapTillRemoved
import icesword.html.DynamicStyleDeclaration
import icesword.html.HTMLWidget
import icesword.html.HTMLWidgetB
import icesword.html.createButton
import icesword.html.createColumn
import icesword.html.createColumnDl
import icesword.html.createNumberInputElement
import icesword.html.createRowElement
import icesword.html.createStaticText
import kotlinx.css.Align
import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.px
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node

fun createEditFloorSpikeRowDialogWb(
    floorSpikeRow: FloorSpikeRow,
) = object : HTMLWidgetB<Dialog> {
    override fun build(tillDetach: Till): Dialog {
        val onClose = StreamSink<Unit>()

        val content = createEditFloorSpikeRowDialog(
            floorSpikeRow = floorSpikeRow,
            onClosePressed = { onClose.send(Unit) },
            tillDetach = tillDetach,
        )

        return Dialog(
            content = HTMLWidget.of(content),
            onClose = onClose,
        )
    }
}

private fun createEditFloorSpikeRowDialog(
    floorSpikeRow: FloorSpikeRow,
    onClosePressed: () -> Unit,
    tillDetach: Till,
): HTMLElement {
    val closeButton = createButton(
        style = DynamicStyleDeclaration(
            alignSelf = Cell.constant(Align.flexEnd),
        ),
        text = "✕",
        onPressed = onClosePressed,
        tillDetach = tillDetach,
    )

    fun createTimeInput(
        labelText: String,
        propertyMillis: MutCell<Int>,
    ): HTMLElement = createRowElement(
        staticStyle = {
            borderStyle = BorderStyle.dashed.toString()
            borderWidth = 1.px.toString()
            padding = 4.px.toString()
        },
        style = DynamicStyleDeclaration(
            alignItems = Cell.constant(Align.center),
        ),
        horizontalGap = 4.px,
        children = listOf(
            createStaticText("$labelText:"),
            createNumberInputElement(
                staticStyle = {
                    width = 64.px.toString()
                },
                initialValue = propertyMillis.sample(),
                onValueChanged = {
                    propertyMillis.set(it)
                },
            ),
            createStaticText("ms"),
        ),
        tillDetach = tillDetach,
    )

    fun createEditSpikeRow(
        config: FloorSpikeRow.FloorSpikeConfig,
        tillDetach: Till,
    ): Node = createRowElement(
        style = DynamicStyleDeclaration(
            alignItems = Cell.constant(Align.center),
        ),
        horizontalGap = 8.px,
        children = listOf(
            createStaticText("Spike"),
            createTimeInput(
                labelText = "Start delay",
                propertyMillis = config.startDelayMillis,
            ),
            createTimeInput(
                labelText = "Time off",
                propertyMillis = config.timeOffMillis,
            ),
            createTimeInput(
                labelText = "Time on",
                propertyMillis = config.timeOnMillis,
            ),
            createButton(
                text = "Remove",
                onPressed = {
                    floorSpikeRow.removeSpike(
                        floorSpikeConfig = config,
                    )
                },
                tillDetach = tillDetach,
            ),
        ),
        tillDetach = tillDetach,
    )

    return createColumn(
        style = DynamicStyleDeclaration(
            backgroundColor = Cell.constant(Color("#d1d1d1")),
            paddingString = Cell.constant("16px"),
            fontFamily = Cell.constant("sans-serif"),
        ),
        verticalGap = 8.px,
        children = listOf(
            closeButton,
            createColumnDl(
                children = floorSpikeRow.spikeConfigs
                    .mapTillRemoved(tillAbort = tillDetach) { config, tillRemoved ->
                        createEditSpikeRow(
                            config = config,
                            tillDetach = tillRemoved,
                        )
                    },
                verticalGap = 8.px,
                tillDetach = tillDetach,
            ),
            createButton(
                style = DynamicStyleDeclaration(
                    alignSelf = Cell.constant(Align.flexStart),
                ),
                text = "Add",
                onPressed = {
                    floorSpikeRow.addSpike()
                },
                tillDetach = tillDetach,
            ),
        ),
        tillDetach = tillDetach,
    )
}