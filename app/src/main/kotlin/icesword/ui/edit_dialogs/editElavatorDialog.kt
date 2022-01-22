package icesword.ui.edit_dialogs

import icesword.editor.entities.Elevator
import icesword.editor.entities.ElevatorMovementCondition
import icesword.editor.entities.ElevatorMovementPattern
import icesword.frp.Cell.Companion.constant
import icesword.frp.dynamic_list.staticListOf
import icesword.frp.reactTill
import icesword.html.ColumnStyleDeclaration
import icesword.html.DynamicStyleDeclaration
import icesword.html.HTMLWidget
import icesword.html.HTMLWidgetB
import icesword.html.RadioGroup
import icesword.html.RowStyleDeclaration
import icesword.html.createColumnWbDl
import icesword.html.createHeading4Wb
import icesword.html.createHeading5Wb
import icesword.html.createLabel
import icesword.html.createRow
import icesword.ui.Dialog
import kotlinx.css.Align
import kotlinx.css.px

fun createElevatorDialog(
    elevator: Elevator<*>,
): HTMLWidgetB<Dialog> = HTMLWidgetB.createTillDetach { tillDetach ->
    val props = elevator.props

    val movementConditionGroup = RadioGroup(
        initialSelectedValue = props.movementCondition.sample(),
        dump = { it.name },
    ).also {
        it.selectedValue.reactTill(tillDetach, props.movementCondition::set)
    }

    val movementPatternGroup = RadioGroup(
        initialSelectedValue = props.movementPattern.sample(),
        dump = { it.name },
    ).also {
        it.selectedValue.reactTill(tillDetach, props.movementPattern::set)
    }

    fun <A> createLabeledRadioInput(
        group: RadioGroup<A>,
        value: A,
        label: String,
    ) = createRow(
        rowStyle = RowStyleDeclaration(
            horizontalGap = constant(8.px),
            alignVertically = constant(Align.center),
        ),
        children = listOf(
            group.createRadioInput(
                style = DynamicStyleDeclaration(
                    margin = constant(0.px),
                ),
                value = value,
            ),
            createLabel(text = label),
        )
    )

    fun createMovementConditionSection(): HTMLWidgetB<HTMLWidget> {
        val group = movementConditionGroup

        return createColumnWbDl(
            columnStyle = ColumnStyleDeclaration(
                verticalGap = constant(4.px),
            ),
            children = staticListOf(
                createHeading5Wb(
                    text = constant("Movement condition"),
                ),
                createColumnWbDl(
                    columnStyle = ColumnStyleDeclaration(
                        verticalGap = constant(8.px),
                    ),
                    children = staticListOf(
                        createLabeledRadioInput(
                            group = group,
                            value = ElevatorMovementCondition.MovesAlways,
                            label = "Always moves",
                        ),
                        createLabeledRadioInput(
                            group = group,
                            value = ElevatorMovementCondition.MovesWithPlayer,
                            label = "Moves when the player is standing on it",
                        ),
                        createLabeledRadioInput(
                            group = group,
                            value = ElevatorMovementCondition.MovesWithoutPlayer,
                            label = "Moves when the player is not standing on it",
                        ),
                        createLabeledRadioInput(
                            group = group,
                            value = ElevatorMovementCondition.MovesOnceTriggered,
                            label = "Starts to move after the players stands on it",
                        ),
                    ),
                ),
            ),
        )
    }

    fun createMovementPatternSection(): HTMLWidgetB<HTMLWidget> {
        val group = movementPatternGroup

        return createColumnWbDl(
            columnStyle = ColumnStyleDeclaration(
                verticalGap = constant(4.px),
            ),
            children = staticListOf(
                createHeading5Wb(
                    text = constant("Movement pattern"),
                ),
                createColumnWbDl(
                    columnStyle = ColumnStyleDeclaration(
                        verticalGap = constant(8.px),
                    ),
                    children = staticListOf(
                        createLabeledRadioInput(
                            group = group,
                            value = ElevatorMovementPattern.OneWay,
                            label = "Moves from start to stop and stops (once)",
                        ),
                        createLabeledRadioInput(
                            group = group,
                            value = ElevatorMovementPattern.TwoWay,
                            label = "Moves from start to stop and comes back (repeatedly)",
                        ),
                    ),
                ),
            ),
        )
    }

    createBasicDialog(
        content = createColumnWbDl(
            children = staticListOf(
                createHeading4Wb(
                    text = constant("Edit elevator"),
                ),
                createColumnWbDl(
                    columnStyle = ColumnStyleDeclaration(
                        verticalGap = constant(8.px),
                    ),
                    children = staticListOf(
                        createMovementConditionSection(),
                        createMovementPatternSection(),
                    ),
                ),
            ),
        ),
    )
}
