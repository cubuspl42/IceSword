package icesword.ui

import icesword.editor.NewProjectContext
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail1
import icesword.editor.retails.Retail10
import icesword.editor.retails.Retail11
import icesword.editor.retails.Retail12
import icesword.editor.retails.Retail13
import icesword.editor.retails.Retail14
import icesword.editor.retails.Retail2
import icesword.editor.retails.Retail3
import icesword.editor.retails.Retail4
import icesword.editor.retails.Retail5
import icesword.editor.retails.Retail6
import icesword.editor.retails.Retail7
import icesword.editor.retails.Retail8
import icesword.editor.retails.Retail9
import icesword.frp.Cell.Companion.constant
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.html.HTMLWidgetB
import icesword.html.createColumnWb
import icesword.html.createHeading4Wb
import icesword.html.createTextButtonWb
import kotlinx.css.px

fun createConfigureNewProjectDialog(
    newProjectContext: NewProjectContext,
): HTMLWidgetB<Dialog> = object : HTMLWidgetB<Dialog> {
    override fun build(tillDetach: Till): Dialog {
        val onClose = StreamSink<Unit>()

        fun onRetailButtonPressed(retail: Retail): () -> Unit = {
            newProjectContext.createWithBase(retail)
            onClose.send(Unit)
        }

        return createBasicDialog(
            content = createColumnWb(
                verticalGap = 8.px,
                children = listOf(
                    createHeading4Wb(
                        text = constant("Create new project"),
                    ),
                    createColumnWb(
                        verticalGap = 8.px,
                        children = listOf(
                            createTextButtonWb(
                                text = "Retail 1 (La Roca)",
                                onPressed = onRetailButtonPressed(Retail1),
                            ),
                            createTextButtonWb(
                                text = "Retail 2",
                                onPressed = onRetailButtonPressed(Retail2),
                            ),
                            createTextButtonWb(
                                text = "Retail 3",
                                onPressed = onRetailButtonPressed(Retail3),
                            ),
                            createTextButtonWb(
                                text = "Retail 4",
                                onPressed = onRetailButtonPressed(Retail4),
                            ),
                            createTextButtonWb(
                                text = "Retail 5",
                                onPressed = onRetailButtonPressed(Retail5),
                            ),
                            createTextButtonWb(
                                text = "Retail 6",
                                onPressed = onRetailButtonPressed(Retail6),
                            ),
                            createTextButtonWb(
                                text = "Retail 7",
                                onPressed = onRetailButtonPressed(Retail7),
                            ),
                            createTextButtonWb(
                                text = "Retail 8",
                                onPressed = onRetailButtonPressed(Retail8),
                            ),
                            createTextButtonWb(
                                text = "Retail 9",
                                onPressed = onRetailButtonPressed(Retail9),
                            ),
                            createTextButtonWb(
                                text = "Retail 10",
                                onPressed = onRetailButtonPressed(Retail10),
                            ),
                            createTextButtonWb(
                                text = "Retail 11",
                                onPressed = onRetailButtonPressed(Retail11),
                            ),
                            createTextButtonWb(
                                text = "Retail 12",
                                onPressed = onRetailButtonPressed(Retail12),
                            ),
                            createTextButtonWb(
                                text = "Retail 13",
                                onPressed = onRetailButtonPressed(Retail13),
                            ),
                            createTextButtonWb(
                                text = "Retail 14",
                                onPressed = onRetailButtonPressed(Retail14),
                            ),
                        )
                    ),
                ),
            ),
            onClose = onClose,
        ).build(tillDetach)
    }
}
