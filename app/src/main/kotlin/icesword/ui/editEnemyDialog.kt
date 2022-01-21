package icesword.ui

import icesword.RezIndex
import icesword.editor.PickupKind
import icesword.editor.entities.Enemy
import icesword.frp.Cell.Companion.constant
import icesword.frp.dynamic_list.mapIndexed
import icesword.frp.units
import icesword.html.DynamicStyleDeclaration
import icesword.html.FlexStyleDeclaration
import icesword.html.FontFamily
import icesword.html.HTMLButton
import icesword.html.HTMLWidgetB
import icesword.html.createButtonWb
import icesword.html.createColumnWb
import icesword.html.createGrid
import icesword.html.createGridDl
import icesword.html.createHeading4Wb
import icesword.html.createHeading5Wb
import icesword.html.createRow
import icesword.html.createTextButtonWb
import icesword.html.createWrapperWb
import icesword.html.flatMap
import icesword.html.map
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.FlexDirection
import kotlinx.css.JustifyContent
import kotlinx.css.Overflow
import kotlinx.css.px


fun createEditEnemyDialog(
    rezIndex: RezIndex,
    enemy: Enemy,
): HTMLWidgetB<Dialog> {
    fun createPickupButton(
        pickupKind: PickupKind,
        onPressed: () -> Unit,
    ): HTMLWidgetB<HTMLButton> {
        val sideLength = 42.px

        return createButtonWb(
            style = DynamicStyleDeclaration(
                padding = constant(0.px),
                overflow = constant(Overflow.hidden),
            ),
            child = createWrapperWb(
                style = DynamicStyleDeclaration(
                    width = constant(sideLength),
                    height = constant(sideLength),
                    displayStyle = FlexStyleDeclaration(
                        direction = constant(FlexDirection.column),
                        justifyContent = constant(JustifyContent.center),
                        alignItems = constant(Align.center),
                    ),
                ),
                child = constant(createPickupImage(
                    rezIndex = rezIndex,
                    pickupKind = pickupKind,
                )),
            ),
            onPressed = onPressed,
        )
    }

    fun createPickupSelectionGrid() = createGrid(
        style = DynamicStyleDeclaration(
            alignSelf = constant(Align.center),
            justifyItems = constant(Align.center),
            alignItems = constant(Align.center),
        ),
        gap = 8.px,
        columnCount = 4,
        children = PickupKind.values().map { pickupKind ->
            createPickupButton(
                pickupKind = pickupKind,
                onPressed = {
                    enemy.addPickup(pickupKind)
                },
            )
        },
    )

    fun createEnemyPickupsGrid() = createGridDl(
        style = DynamicStyleDeclaration(
            alignSelf = constant(Align.center),
            justifyItems = constant(Align.center),
            alignItems = constant(Align.center),
        ),
        gap = 8.px,
        children = enemy.pickups.mapIndexed { index, pickupKind ->
            createPickupButton(
                pickupKind = pickupKind,
                onPressed = {
                    enemy.removePickupAt(index)
                },
            )
        },
    )

    fun createTitledSection(
        title: String,
        subtitle: String,
        child: HTMLWidgetB<*>,
    ): HTMLWidgetB<*> = createColumnWb(
        flexStyle = FlexStyleDeclaration(
            alignItems = constant(Align.center),
            gap = constant(8.px),
        ),
        children = listOf(
            createColumnWb(
                children = listOf(
                    createHeading4Wb(constant(title)),
                    createHeading5Wb(constant(subtitle)),
                ),
            ),
            child,
        ),
    )

    return createTextButtonWb(
        style = DynamicStyleDeclaration(
            alignSelf = constant(Align.flexEnd),
        ),
        text = "✕",
    ).flatMap { closeButton ->
        createColumnWb(
            style = DynamicStyleDeclaration(
                backgroundColor = constant(Color("#d1d1d1")),
                paddingString = constant("16px"),
                fontFamily = constant(FontFamily.sansSerif),
            ),
            flexStyle = FlexStyleDeclaration(
                alignItems = constant(Align.center),
            ),
            verticalGap = 8.px,
            children = listOf(
                closeButton,
                createRow(
                    style = DynamicStyleDeclaration(
                        alignItems = constant(Align.center),
                    ),
                    horizontalGap = 48.px,
                    children = listOf(
                        createTitledSection(
                            title = "All pickups",
                            subtitle = "(press to add)",
                            child = createPickupSelectionGrid(),
                        ),
                        createTitledSection(
                            title = "Enemy pickups",
                            subtitle = "(press to remove)",
                            child = createEnemyPickupsGrid(),
                        ),
                    ),
                ),
                createTextButtonWb(
                    text = "Save",
                ),
            ),
        ).map { content ->
            Dialog(
                content = content,
                onClose = closeButton.onPressed.units(),
            )
        }
    }
}
