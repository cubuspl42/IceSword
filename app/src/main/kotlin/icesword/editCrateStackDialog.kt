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
import icesword.frp.dynamic_list.firstOrNullDynamic
import icesword.frp.dynamic_list.mapIndexedDynamic
import icesword.frp.dynamic_list.mergeBy
import icesword.frp.dynamic_list.sampleContent
import icesword.frp.map
import icesword.frp.mapNested
import icesword.frp.reactTill
import icesword.frp.units
import icesword.html.BorderStyleDeclaration
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
import icesword.html.createHeading4Wb
import icesword.html.createTextButtonWb
import icesword.html.createWrapperWb
import icesword.html.mapTillDetach
import icesword.html.onDragStart
import icesword.html.resolve
import icesword.ui.createPickupImage
import kotlinx.css.Align
import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.JustifyContent
import kotlinx.css.px
import org.w3c.dom.DataTransfer
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

private const val pickupKindMimeType = "application/x-pickupkind"

private class PickupElementWidget(
    val pickupKind: PickupKind,
    val onDragStarted: Stream<Unit>,
    override val root: HTMLWidget,
) : HTMLWidget.HTMLShadowWidget

private class PickupWrapperWidget(
    val pickupIndex: Int,
    val pickupKind: Cell<PickupKind>,
    val onDragStarted: Stream<Unit>,
    val isDraggedOver: Cell<Boolean>,
    val onDropped: Stream<Unit>,
    override val root: HTMLWidget,
) : HTMLWidget.HTMLShadowWidget

private sealed interface CrateStackSectionState {
    val nextState: Stream<Tilled<CrateStackSectionState>>

    val pickupsPreview: DynamicList<PickupKind>

    class Idle(
        override val pickupsPreview: DynamicList<PickupKind>,
        override val nextState: Stream<Tilled<CrateStackSectionState>>,
    ) : CrateStackSectionState

    class Dragged(
        override val pickupsPreview: DynamicList<PickupKind>,
        override val nextState: Stream<Tilled<CrateStackSectionState>>,
    ) : CrateStackSectionState
}

fun createCrateStackSection(
    rezIndex: RezIndex,
    textureBank: TextureBank,
    crateStack: CrateStack,
) = object : HTMLWidgetB<HTMLWidget> {
    fun test(dataTransfer: DataTransfer): Boolean =
        dataTransfer.items[0]?.type == pickupKindMimeType

    val pickupsPreviewLoop = CellLoop(crateStack.pickups)

    val pickupsPreview: DynamicList<PickupKind> =
        DynamicList.diff(pickupsPreviewLoop.asCell)

    fun createPickupElement(pickupKind: PickupKind) =
        createWrapperWb(
            attrs = HTMLElementAttrs(
                draggable = constant(true),
            ),
            child = constant(createPickupImage(
                rezIndex = rezIndex,
                textureBank = textureBank,
                pickupKind = pickupKind,
            )),
        ).mapTillDetach { root, tillDetach ->
            val rootElement = root.resolve() as HTMLElement

            val onDragStart = rootElement.onDragStart()

            onDragStart.reactTill(tillDetach) { ev ->
                ev.dataTransfer?.apply {
                    effectAllowed = "move"
                    setData(pickupKindMimeType, pickupKind.name)
                }
            }

            PickupElementWidget(
                pickupKind = pickupKind,
                onDragStarted = onDragStart.units(),
                root = root,
            )
        }

    private fun createPickupWrapper(
        pickupIndex: Int,
        pickupKind: Cell<PickupKind>,
    ): HTMLWidgetB<PickupWrapperWidget> =
        object : HTMLWidgetB<PickupWrapperWidget> {
            override fun build(tillDetach: Till): PickupWrapperWidget {
                val pickupElement = HTMLWidgetB.build(
                    widget = pickupKind.map {
                        createPickupElement(
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
                                    if (it is DropTargetState.DragOver) Color.red else Color.transparent
                                },
                                width = constant(2.px),
                            ),
                        ),
                        child = constant(
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
                                        color = constant(Color.yellow),
                                        width = constant(2.px),
                                    ),
                                ),
                                child = pickupElement,
                            ),
                        ),
                    ),
                    test = { test(it) },
                ).build(tillDetach = tillDetach)

                dropTargetStateLoop.close(dropTarget.state)

                return PickupWrapperWidget(
                    pickupIndex = pickupIndex,
                    pickupKind = pickupKind,
                    onDragStarted = pickupElement.divertMap { it.onDragStarted },
                    isDraggedOver = dropTarget.state.map { it is DropTargetState.DragOver },
                    onDropped = dropTarget.onDrop,
                    root = dropTarget,
                )
            }
        }

    override fun build(tillDetach: Till): HTMLWidget = object {
        val pickupWrappers: DynamicList<PickupWrapperWidget> = HTMLWidgetB.buildDl(
            widgets = pickupsPreview.mapIndexedDynamic(tillDetach) { index: Int, pickupKind: Cell<PickupKind> ->
                createPickupWrapper(
                    pickupIndex = index,
                    pickupKind = pickupKind,
                )
            },
            tillDetach = tillDetach,
        )

        private fun buildIdleState(): Tilled<CrateStackSectionState.Idle> {
            val nextState = pickupWrappers.mergeBy { wrapper ->
                wrapper.onDragStarted.map {
                    buildDraggedState(
                        crateStackPickups = crateStack.pickups.sampleContent(),
                        draggedPickupIndex = wrapper.pickupIndex,
                    )
                }
            }

            return Tilled.pure(
                CrateStackSectionState.Idle(
                    pickupsPreview = crateStack.pickups,
                    nextState = nextState,
                ),
            )
        }

        private fun buildDraggedState(
            crateStackPickups: List<PickupKind>,
            draggedPickupIndex: Int,
        ): Tilled<CrateStackSectionState.Dragged> = object : Tilled<CrateStackSectionState.Dragged> {
            val draggedOverPickupIndex = pickupWrappers
                .firstOrNullDynamic { it.isDraggedOver }
                .mapNested { it.pickupIndex }

            val onDropped = pickupWrappers.mergeBy { wrapper ->
                wrapper.onDropped.map { wrapper.pickupIndex }
            }

            val pickupsPreview = DynamicList.diff(
                draggedOverPickupIndex.map { targetPickupIndex ->
                    if (targetPickupIndex == null) crateStackPickups
                    else crateStackPickups.withSwapped(draggedPickupIndex, targetPickupIndex)
                },
            )

            override fun build(till: Till): CrateStackSectionState.Dragged {
                onDropped.reactTill(till) { targetPickupIndex ->
                    val newPickups = crateStackPickups.withSwapped(draggedPickupIndex, targetPickupIndex)
                    crateStack.setPickups(newPickups)
                }

                return CrateStackSectionState.Dragged(
                    pickupsPreview = pickupsPreview,
                    nextState = onDropped.map { buildIdleState() },
                )
            }
        }

        val state: Cell<CrateStackSectionState> = Stream.follow<CrateStackSectionState>(
            initialValue = buildIdleState(),
            extractNext = { it.nextState },
            till = tillDetach,
        )

        val root = createColumnWbDl(
            style = DynamicStyleDeclaration(
                alignSelf = constant(Align.center),
            ),
            children = pickupWrappers,
        ).build(tillDetach)

        init {
            pickupsPreviewLoop.close(
                state.map { it.pickupsPreview },
            )
        }
    }.root
}

fun createEditCrateStackDialog(
    rezIndex: RezIndex,
    textureBank: TextureBank,
    crateStack: CrateStack,
): HTMLWidgetB<Dialog> = createBasicDialog(
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
            createCrateStackSection(
                rezIndex = rezIndex,
                textureBank = textureBank,
                crateStack = crateStack,
            ),
        ),
    ),
)
