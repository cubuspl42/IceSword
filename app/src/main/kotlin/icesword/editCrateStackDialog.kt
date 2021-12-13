package icesword

import TextureBank
import icesword.editor.CrateStack
import icesword.editor.PickupKind
import icesword.frp.Cell
import icesword.frp.Cell.Companion.constant
import icesword.frp.CellLoop
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.divertMap
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.firstNotNullOrNull
import icesword.frp.dynamic_list.firstOrNullDynamic
import icesword.frp.dynamic_list.fuseBy
import icesword.frp.dynamic_list.mapIndexedDynamic
import icesword.frp.dynamic_list.mergeBy
import icesword.frp.dynamic_list.sampleContent
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.mapNotNull
import icesword.frp.mapTillNext
import icesword.frp.reactTill
import icesword.frp.switchMap
import icesword.html.BorderStyleDeclaration
import icesword.html.DragGesture
import icesword.html.DraggableState
import icesword.html.DropTargetState
import icesword.html.DynamicStyleDeclaration
import icesword.html.FlexStyleDeclaration
import icesword.html.HTMLElementAttrs
import icesword.html.HTMLWidget
import icesword.html.HTMLWidgetB
import icesword.html.alsoTillDetach
import icesword.html.createColumnWb
import icesword.html.createColumnWbDl
import icesword.html.createDropTarget
import icesword.html.createGrid
import icesword.html.createHeading4Wb
import icesword.html.createRow
import icesword.html.createTextButtonWb
import icesword.html.createWrapperWb
import icesword.html.flatMapTillDetach
import icesword.html.map
import icesword.html.onDragGestureStart
import icesword.html.onDragStart
import icesword.html.trackDraggingState
import icesword.ui.createPickupImage
import kotlinx.css.Align
import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.JustifyContent
import kotlinx.css.Overflow
import kotlinx.css.px
import org.w3c.dom.DataTransfer
import org.w3c.dom.get

private fun pickupKindToMimeType(pickupKind: PickupKind): String =
    "application/x-pickupkind.${pickupKind.name}"

private fun pickupKindFromMimeType(mimeType: String): PickupKind? {
    return if (mimeType.startsWith("application/x-pickupkind.")) {
        val name = mimeType.split(".")[1]
        return PickupKind.values().first { it.name.lowercase() == name }
    } else null
}

private class PickupDraggableWidget(
    val pickupKind: PickupKind,
    val isDragged: Cell<Boolean>,
    override val root: HTMLWidget,
) : HTMLWidget.HTMLShadowWidget

sealed interface PickupDropTargetState {
    object Idle : PickupDropTargetState

    value class PickupDraggedOver(
        val pickupKind: PickupKind,
    ) : PickupDropTargetState
}

private class PickupDropTarget(
    val crateIndex: Int,
    val onDragGestureStarted: Stream<DragGesture>,
    val state: Cell<PickupDropTargetState>,
    val onDropped: Stream<PickupKind>,
    override val root: HTMLWidget,
) : HTMLWidget.HTMLShadowWidget {
    val draggedOverPickup = state.map {
        (it as? PickupDropTargetState.PickupDraggedOver)?.pickupKind
    }
}

data class DropTargetPickupDrag(
    val dropTargetIndex: Int,
    val draggedOverPickupKind: PickupKind,
)

private class PickupGalleryWidget(
    val draggedPickup: Cell<PickupKind?>,
    override val root: HTMLWidget,
) : HTMLWidget.HTMLShadowWidget

data class CratePickupDragGesture(
    val crateIndex: Int,
    val onEnd: Stream<Unit>,
)

private sealed interface CrateStackSectionState {
    val pickupsPreview: DynamicList<PickupKind>

    // Reacting to changes from the model
    class Idle(
        override val pickupsPreview: DynamicList<PickupKind>,
    ) : CrateStackSectionState

    // Handling inserting a pickup from the gallery
    class Inserting(
        override val pickupsPreview: DynamicList<PickupKind>,
    ) : CrateStackSectionState

    // Internally reordering pickups inside the crate stack
    class Reordering(
        override val pickupsPreview: DynamicList<PickupKind>,
    ) : CrateStackSectionState
}

fun createEditCrateStackDialog(
    rezIndex: RezIndex,
    textureBank: TextureBank,
    crateStack: CrateStack,
): HTMLWidgetB<Dialog> = EditCrateStackDialogObject(
    crateStack = crateStack,
    rezIndex = rezIndex,
    textureBank = textureBank,
).root

class EditCrateStackDialogObject(
    private val crateStack: CrateStack,
    private val rezIndex: RezIndex,
    private val textureBank: TextureBank,
) {
    private fun createPickupDraggable(pickupKind: PickupKind) =
        createWrapperWb(
            attrs = HTMLElementAttrs(
                draggable = constant(true),
            ),
            child = constant(createPickupImage(
                rezIndex = rezIndex,
                textureBank = textureBank,
                pickupKind = pickupKind,
            )),
        ).flatMapTillDetach { draggable, tillDetach ->
            val onDragStart = draggable.onDragStart()

            val draggingState = draggable.trackDraggingState(tillDetach)

            onDragStart.reactTill(tillDetach) { ev ->
                ev.dataTransfer?.apply {
                    effectAllowed = "move"
                    setData(pickupKindToMimeType(pickupKind), "")
                }
            }

            createWrapperWb(
                style = DynamicStyleDeclaration(
                    width = constant(42.px),
                    height = constant(42.px),
                    displayStyle = FlexStyleDeclaration(
                        justifyContent = constant(JustifyContent.center),
                        alignItems = constant(Align.center),
                    ),
                    border = BorderStyleDeclaration(
                        style = constant(BorderStyle.solid),
                        color = constant(Color.darkGray),
                        width = constant(2.px),
                    ),
                    overflow = constant(Overflow.hidden),
                ),
                child = constant(draggable),
            ).map { root ->
                PickupDraggableWidget(
                    pickupKind = pickupKind,
                    isDragged = draggingState.map { it == DraggableState.Dragged },
                    root = root,
                )
            }
        }

    private fun test(dataTransfer: DataTransfer): Boolean =
        dataTransfer.items[0]?.type?.let(::pickupKindFromMimeType) != null

    private fun createPickupDropTarget(
        pickupIndex: Int,
        pickupKind: Cell<PickupKind>,
    ): HTMLWidgetB<PickupDropTarget> =
        object : HTMLWidgetB<PickupDropTarget> {
            override fun build(tillDetach: Till): PickupDropTarget {
                val pickupDraggable = HTMLWidgetB.build(
                    widget = pickupKind.map {
                        createPickupDraggable(
                            pickupKind = it,
                        )
                    },
                    tillDetach = tillDetach,
                )

                val dropTargetStateLoop = CellLoop<DropTargetState>(
                    placeholderValue = DropTargetState.Idle,
                )

                val dropTargetState = dropTargetStateLoop.asCell

                val dropTarget = createDropTarget(
                    child = createWrapperWb(
                        style = DynamicStyleDeclaration(
                            border = BorderStyleDeclaration(
                                style = constant(BorderStyle.solid),
                                color = dropTargetState.map {
                                    if (it is DropTargetState.DraggedOver) Color.red else Color.transparent
                                },
                                width = constant(2.px),
                            ),
                        ),
                        child = pickupDraggable,
                    ),
                    test = { test(it) },
                ).build(tillDetach = tillDetach)

                dropTargetStateLoop.close(dropTarget.state)

                fun extractPickupKind(dataTransfer: DataTransfer): PickupKind? =
                    dataTransfer.items[0]?.let { item ->
                        pickupKindFromMimeType(item.type)
                    }

                val state: Cell<PickupDropTargetState> = dropTarget.state.map { dropTargetState ->
                    when (dropTargetState) {
                        DropTargetState.Idle -> PickupDropTargetState.Idle
                        is DropTargetState.DraggedOver -> extractPickupKind(dropTargetState.dataTransfer)?.let {
                            PickupDropTargetState.PickupDraggedOver(pickupKind = it)
                        } ?: PickupDropTargetState.Idle
                    }
                }

                return PickupDropTarget(
                    crateIndex = pickupIndex,
                    onDragGestureStarted = pickupDraggable.divertMap { it.onDragGestureStart() },
                    state = state,
                    onDropped = dropTarget.onDrop.mapNotNull(::extractPickupKind),
                    root = dropTarget,
                )
            }
        }

    @Suppress("IfThenToElvis")
    private fun createCrateStackSection(
        handleInsertion: Cell<Boolean>,
    ) = object : HTMLWidgetB<HTMLWidget> {
        val pickupsPreviewLoop = CellLoop(crateStack.pickups)

        val pickupsPreview: DynamicList<PickupKind> =
            DynamicList.diff(pickupsPreviewLoop.asCell)

        override fun build(tillDetach: Till): HTMLWidget = object {
            val pickupWrappers: DynamicList<PickupDropTarget> = HTMLWidgetB.buildDl(
                widgets = pickupsPreview.mapIndexedDynamic(tillDetach) { index: Int, pickupKind: Cell<PickupKind> ->
                    createPickupDropTarget(
                        pickupIndex = index,
                        pickupKind = pickupKind,
                    )
                },
                tillDetach = tillDetach,
            )

            val onCrateDragStarted = pickupWrappers.mergeBy { dropTarget ->
                dropTarget.onDragGestureStarted.map { dragGesture ->
                    CratePickupDragGesture(
                        crateIndex = dropTarget.crateIndex,
                        onEnd = dragGesture.onEnd,
                    )
                }
            }

            val draggedCratePickup: Cell<CratePickupDragGesture?> =
                Stream.follow<CratePickupDragGesture?>(
                    initialValue = null,
                    extractNext = { gesture ->
                        if (gesture != null) gesture.onEnd.map { null }
                        else onCrateDragStarted
                    },
                    till = tillDetach,
                )

            val draggedPickupIndex = draggedCratePickup.map { it?.crateIndex }

            val draggedOverDropTarget = pickupWrappers
                .fuseBy { dropTarget ->
                    dropTarget.draggedOverPickup.mapNested { pickupKind ->
                        DropTargetPickupDrag(
                            dropTargetIndex = dropTarget.crateIndex,
                            draggedOverPickupKind = pickupKind,
                        )
                    }
                }
                .firstNotNullOrNull()

            val onDropped = pickupWrappers.mergeBy { wrapper ->
                wrapper.onDropped.map {
                    DropTargetPickupDrag(
                        dropTargetIndex = wrapper.crateIndex,
                        draggedOverPickupKind = it,
                    )
                }
            }

            private fun buildIdleState(): Tilled<CrateStackSectionState.Idle> = Tilled.pure(
                CrateStackSectionState.Idle(
                    pickupsPreview = crateStack.pickups,
                ),
            )

            private fun buildInsertingState(): Tilled<CrateStackSectionState.Inserting> =
                object : Tilled<CrateStackSectionState.Inserting> {
                    val crateStackPickups = crateStack.pickups.sampleContent()

                    val pickupsPreview = DynamicList.diff(
                        draggedOverDropTarget.map { it ->
                            if (it == null) crateStackPickups
                            else crateStackPickups.withReplaced(
                                it.dropTargetIndex,
                                it.draggedOverPickupKind,
                            )
                        },
                    )

                    override fun build(till: Till): CrateStackSectionState.Inserting {
                        onDropped.reactTill(till) {
                            val newPickups = crateStackPickups.withReplaced(
                                it.dropTargetIndex,
                                it.draggedOverPickupKind,
                            )
                            crateStack.setPickups(newPickups)
                        }

                        return CrateStackSectionState.Inserting(
                            pickupsPreview = pickupsPreview,
                        )
                    }
                }

            private fun buildReorderingState(
                draggedPickupIndex: Int,
            ): Tilled<CrateStackSectionState.Reordering> =
                object : Tilled<CrateStackSectionState.Reordering> {
                    val crateStackPickups = crateStack.pickups.sampleContent()

                    val pickupsPreview = DynamicList.diff(
                        draggedOverDropTarget.map {
                            if (it == null) crateStackPickups
                            else crateStackPickups.withSwapped(
                                draggedPickupIndex,
                                it.dropTargetIndex,
                            )
                        },
                    )

                    override fun build(till: Till): CrateStackSectionState.Reordering {
                        onDropped.reactTill(till) { it ->
                            val newPickups = crateStackPickups.withSwapped(
                                draggedPickupIndex,
                                it.dropTargetIndex,
                            )
                            crateStack.setPickups(newPickups)
                        }

                        return CrateStackSectionState.Reordering(
                            pickupsPreview = pickupsPreview,
                        )
                    }
                }

            val state = draggedPickupIndex.switchMap { draggedPickupIndex ->
                if (draggedPickupIndex != null) constant(
                    buildReorderingState(draggedPickupIndex = draggedPickupIndex),
                ) else handleInsertion.map {
                    if (it) buildInsertingState()
                    else buildIdleState()
                }
            }.mapTillNext(tillDetach) { it, tillNext ->
                it.build(tillNext)
            }

            val root = createColumnWbDl(
                style = DynamicStyleDeclaration(
                    alignSelf = constant(Align.center),
                ),
                reverse = true,
                children = pickupWrappers,
            ).build(tillDetach)

            init {
                pickupsPreviewLoop.close(
                    state.map { it.pickupsPreview },
                )
            }
        }.root
    }

    private fun createPickupGallery() = object : HTMLWidgetB<PickupGalleryWidget> {
        override fun build(tillDetach: Till): PickupGalleryWidget {
            val pickupDraggables = HTMLWidgetB.build(
                PickupKind.values().map { pickupKind ->
                    createPickupDraggable(
                        pickupKind = pickupKind,
                    )
                },
                tillDetach,
            )

            val root = createGrid(
                gap = 8.px,
                columnCount = 4,
                children = pickupDraggables,
            ).build(tillDetach)

            return PickupGalleryWidget(
                draggedPickup = DynamicList.of(pickupDraggables)
                    .firstOrNullDynamic { it.isDragged }.map { it?.pickupKind },
                root = root,
            )
        }
    }

    private fun createRoot() = object : HTMLWidgetB<Dialog> {
        override fun build(tillDetach: Till): Dialog {
            val pickupGallery = createPickupGallery().build(tillDetach)

            val crateStackSection = createCrateStackSection(
                handleInsertion = pickupGallery.draggedPickup.map { it != null },
            ).build(tillDetach)

            return createBasicDialog(
                content = createColumnWb(
                    verticalGap = 8.px,
                    children = listOf(
                        createHeading4Wb(
                            text = constant("Edit crate stack"),
                        ),
                        createTextButtonWb(
                            text = "+",
                        ).alsoTillDetach { button, tillDetach ->
                            button.onPressed.reactTill(tillDetach) {
                                crateStack.pushCrate()
                            }
                        },
                        createTextButtonWb(
                            text = "-",
                        ).alsoTillDetach { button, tillDetach ->
                            button.onPressed.reactTill(tillDetach) {
                                crateStack.popCrate()
                            }
                        },
                        createRow(
                            style = DynamicStyleDeclaration(
                                padding = constant(20.px),
                            ),
                            horizontalGap = 40.px,
                            children = listOf(
                                pickupGallery,
                                crateStackSection,
                            ),
                        ),
                    ),
                ),
            ).build(tillDetach)
        }
    }

    val root = createRoot()
}
