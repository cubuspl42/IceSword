package icesword

import icesword.DragOverOverlayState.DragOverState
import icesword.DragOverOverlayState.IdleState
import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.map
import icesword.frp.mapNotNull
import icesword.frp.mergeWith
import icesword.frp.reactTill
import icesword.html.createHTMLElementRaw
import icesword.html.linkChild
import icesword.html.linkProperty
import icesword.html.onEvent
import kotlinx.browser.document
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.files.File

private sealed interface DragOverOverlayState {
    val nextState: Stream<Tilled<DragOverOverlayState>>

    class IdleState(
        override val nextState: Stream<Tilled<DragOverOverlayState>>,
    ) : DragOverOverlayState

    class DragOverState(
        override val nextState: Stream<Tilled<DragOverOverlayState>>,
    ) : DragOverOverlayState
}

private class DragOverOverlay(
    private val child: HTMLElement,
    private val enableDrop: Cell<Boolean>,
    private val onFileDropped: (file: File) -> Unit,
    private val tillDetach: Till,
) {
    val dragOverPreview = createDragoverPreview()

    val dragoverPreviewWrapper = createHTMLElementRaw("div").apply {
        className = "dragoverPreviewWrapper"

        style.apply {
            display = "grid"
            setProperty("grid-template-columns", "minmax(0, 1fr)")
            setProperty("grid-template-rows", "minmax(0, 1fr)")

            setProperty("pointer-events", "none")
        }
    }

    val root = createStackLayout(
        children = listOf(
            child,
            dragoverPreviewWrapper,
        )
    )

    val onDragOver = dragOverPreview.onEvent<DragEvent>("dragover")

    fun buildIdleState(): Tilled<IdleState> = Tilled.pure(
        IdleState(
            nextState = root.onEvent<DragEvent>("dragenter")
                .mapNotNull { ev ->
                    ev.dataTransfer?.items?.get(0)?.let { firstItem ->
                        if (firstItem.kind == "file") buildDragOverState()
                        else null
                    }
                }
        )
    )

    fun buildDragOverState(): Tilled<DragOverState> =
        object : Tilled<DragOverState> {
            override fun build(till: Till): DragOverState {
                val onDragLeave = dragOverPreview.onEvent<DragEvent>("dragleave")

                val onDrop = dragOverPreview.onEvent<DragEvent>("drop")

                onDrop.reactTill(till) {
                    it.preventDefault()

                    val file = it.dataTransfer?.items?.get(0)?.getAsFile()

                    if (enableDrop.sample() && file != null) {
                        onFileDropped(file)
                    }
                }

                return DragOverState(
                    nextState = onDragLeave.mergeWith(onDrop).map { buildIdleState() }
                )
            }
        }

    val state = Stream.followTillNext<DragOverOverlayState>(
        initialValue = buildIdleState(),
        extractNext = { it.nextState },
        till = tillDetach,
    )

    val isDraggedOver = state.map { it is DragOverState }

    init {
        linkProperty(
            style = dragoverPreviewWrapper.style,
            propertyName = "pointer-events",
            property = isDraggedOver.map { if (it) null else "none" },
            till = tillDetach,
        )

        val dragoverPreviewCell = isDraggedOver.map { isDragged ->
            if (isDragged) dragOverPreview else null
        }

        linkChild(
            element = dragoverPreviewWrapper,
            child = dragoverPreviewCell,
            till = tillDetach,
        )

        onDragOver.reactTill(tillDetach) {
            it.preventDefault()
        }
    }
}

fun createDragoverOverlay(
    child: HTMLElement,
    enableDrop: Cell<Boolean>,
    onFileDropped: (file: File) -> Unit,
    tillDetach: Till,
): HTMLElement = DragOverOverlay(
    child = child,
    enableDrop = enableDrop,
    onFileDropped = onFileDropped,
    tillDetach = tillDetach,
).root

private fun createDragoverPreview(): HTMLElement {
    val root = createHTMLElementRaw("div").apply {
        className = "dragoverPreview"

        style.apply {
            display = "flex"
            justifyContent = "center"
            alignItems = "center"

            backgroundColor = "#000000ad"
        }

        appendChild(
            createHTMLElementRaw("p").apply {
                style.apply {
                    fontFamily = "sans-serif"
                    color = "white"
                    setProperty("pointer-events", "none")
                }

                appendChild(document.createTextNode("Drop the file to load the world..."))
            }
        )
    }

    return root
}
