package icesword

import TextureBank
import icesword.editor.CrateStack
import icesword.editor.PickupKind
import icesword.frp.Cell.Companion.constant
import icesword.frp.CellLoop
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.map
import icesword.frp.map
import icesword.frp.reactTill
import icesword.frp.units
import icesword.html.BorderStyleDeclaration
import icesword.html.DragState
import icesword.html.DropTargetState
import icesword.html.DynamicStyleDeclaration
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
import icesword.html.flatMap
import icesword.html.flatMapTillDetach
import icesword.html.handleDrags
import icesword.html.map
import icesword.html.mapTillDetach
import icesword.html.onDragStart
import icesword.html.resolve
import icesword.ui.createPickupImage
import kotlinx.css.Align
import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.PointerEvents
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
    val pickupElement: PickupElementWidget,
    override val root: HTMLWidget,
) : HTMLWidget.HTMLShadowWidget

fun createEditCrateStackDialog(
    rezIndex: RezIndex,
    textureBank: TextureBank,
    crateStack: CrateStack,
) = object : HTMLWidgetB<Dialog> {
    override fun build(tillDetach: Till): Dialog {
        fun createPickupElement(pickupKind: PickupKind) =
            createWrapperWb(
                attrs = HTMLElementAttrs(
                    draggable = constant(true),
                ),
//                style = DynamicStyleDeclaration(
//                    pointerEvents = constant(PointerEvents.none),
//                ),
                child = constant(createPickupImage(
                    rezIndex = rezIndex,
                    textureBank = textureBank,
                    pickupKind = pickupKind,
                )),
            ).mapTillDetach { root, tillDetach ->
                val rootElement = root.resolve() as HTMLElement

                val onDragStart = rootElement.onDragStart()

                onDragStart.reactTill(tillDetach) { ev ->
                    ev.dataTransfer?.setData(pickupKindMimeType, pickupKind.name)
                }

                PickupElementWidget(
                    pickupKind = pickupKind,
                    onDragStarted = onDragStart.units(),
                    root = root,
                )
            }

        fun test(dataTransfer: DataTransfer): Boolean =
            dataTransfer.items[0]?.type == pickupKindMimeType

        fun createPickupWrapper(pickupKind: PickupKind): HTMLWidgetB<PickupWrapperWidget> =
            object : HTMLWidgetB<PickupWrapperWidget> {
                override fun build(tillDetach: Till): PickupWrapperWidget {
                    val pickupElement = createPickupElement(
                        pickupKind = pickupKind,
                    ).build(tillDetach = tillDetach)

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
                                        padding = constant(4.px),
                                        border = BorderStyleDeclaration(
                                            style = constant(BorderStyle.solid),
                                            color = constant(Color.yellow),
                                            width = constant(2.px),
                                        ),
                                    ),
                                    child = constant(pickupElement),
                                ),
                            ),
                        ),
                        test = { test(it) },
                    ).build(tillDetach = tillDetach)

                    dropTargetStateLoop.close(dropTarget.state)

                    return PickupWrapperWidget(
                        pickupElement = pickupElement,
                        root = dropTarget,
                    )
                }
            }

        val pickupWrappers: DynamicList<PickupWrapperWidget> = HTMLWidgetB.buildDl(
            widgets = crateStack.pickups.map { createPickupWrapper(it) },
            tillDetach = tillDetach,
        )

        @Suppress("NAME_SHADOWING")
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
                    createColumnWbDl(
                        style = DynamicStyleDeclaration(
                            alignSelf = constant(Align.center),
                        ),
                        children = pickupWrappers,
                    ),
                ),
            ),
        ).build(tillDetach)
    }
}
