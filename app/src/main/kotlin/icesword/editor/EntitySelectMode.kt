package icesword.editor

import icesword.frp.Cell
import icesword.frp.CellLoop
import icesword.frp.DynamicMap
import icesword.frp.Stream
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.map
import icesword.frp.reactTill
import icesword.frp.staticMapOf
import icesword.frp.switchMap
import icesword.geometry.IntLineSeg
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.utils.filterValuesNotNull


class EntitySelectMode(
    private val editor: Editor,
    tillExit: Till,
) : EditorMode {
    interface Input {
        object Noop : Input {
            override val cursorWorldPosition: Cell<IntVec2?> =
                Cell.constant(null)

            override val enableAddModifier: Cell<Boolean> =
                Cell.constant(false)

            override val enableSubtractModifier: Cell<Boolean> =
                Cell.constant(false)
        }

        companion object {
            fun switch(input: Cell<Input>): Input = object : Input {
                override val cursorWorldPosition: Cell<IntVec2?> =
                    input.switchMap { it.cursorWorldPosition }

                override val enableAddModifier: Cell<Boolean> =
                    input.switchMap { it.enableAddModifier }

                override val enableSubtractModifier: Cell<Boolean> =
                    input.switchMap { it.enableSubtractModifier }
            }
        }

        val cursorWorldPosition: Cell<IntVec2?>

        val enableAddModifier: Cell<Boolean>

        val enableSubtractModifier: Cell<Boolean>
    }

    enum class SelectionStrategy {
        Select,
        Add,
        Subtract,
        Invert,
    }

    enum class SelectionState {
        Selected,
        NonSelected,
    }

    sealed interface SelectionForm

    data class PointSelection(
        val worldPoint: IntVec2,
    ) : SelectionForm

    data class AreaSelection(
        val worldArea: IntRect,
    ) : SelectionForm

    data class SelectionModification(
        val stateAdjustments: DynamicMap<Entity, SelectionState>,
    ) {
        fun isEntitySelected(entity: Entity): Cell<Boolean> =
            stateAdjustments.get(entity).map { it == SelectionState.Selected }

        fun getEntitiesToSelect() = stateAdjustments.volatileContentView
            .mapNotNull { (entity, selectionState) ->
                if (selectionState == SelectionState.Selected) entity
                else null
            }
    }

    sealed interface State {
        val enterNextMode: Stream<Tilled<State>>
    }

    interface IdleMode : State {
        val projectedSelectionModification: Cell<SelectionModification?>

        fun select(
            worldAnchor: IntVec2,
            worldTarget: Cell<IntVec2>,
            commit: Stream<Unit>,
        )
    }

    interface SelectingMode : State {
        val selectionForm: Cell<SelectionForm>

        val selectionModification: Cell<SelectionModification>
    }

    private val inputLoop: CellLoop<Input> =
        CellLoop(placeholderValue = Input.Noop)

    private val input: Input = Input.switch(inputLoop.asCell)

    fun closeInputLoop(input: Input) {
        inputLoop.close(Cell.constant(input))
    }

    private val selectionStrategy: Cell<SelectionStrategy> = Cell.map2(
        input.enableAddModifier,
        input.enableSubtractModifier,
    ) {
            enableAddModifier,
            enableSubtractModifier,
        ->
        when {
            enableAddModifier && enableSubtractModifier -> SelectionStrategy.Invert
            enableAddModifier -> SelectionStrategy.Add
            enableSubtractModifier -> SelectionStrategy.Subtract
            else -> SelectionStrategy.Select
        }
    }

    private fun findEntitiesAtPoint(worldPoint: IntVec2): List<Entity> =
        editor.world.entities.volatileContentView.filter {
            it.isSelectableIn(IntRect(position = worldPoint, size = IntSize.UNIT))
        }

    private fun findEntitiesInArea(area: IntRect): List<Entity> =
        editor.world.entities.volatileContentView.filter {
            it.isSelectableIn(area)
        }

    private fun buildPointSelectionModification(worldPoint: IntVec2): SelectionModification {
        val entitiesAtPoint = findEntitiesAtPoint(worldPoint = worldPoint)
        val selectedEntitiesAtPoint = entitiesAtPoint.filter { editor.isEntitySelected(it).sample() }

        val entityToSelect = selectedEntitiesAtPoint.singleOrNull()?.let { singleSelectedEntity ->
            val selectedEntityIndex = entitiesAtPoint.indexOfOrNull(singleSelectedEntity)
            selectedEntityIndex?.let { entitiesAtPoint[(it + 1) % entitiesAtPoint.size] }
        } ?: entitiesAtPoint.firstOrNull()

        return SelectionModification(
            stateAdjustments = entityToSelect?.let {
                staticMapOf(it to SelectionState.Selected)
            } ?: DynamicMap.empty()
        )
    }

    private fun buildAreaSelectionModification(
        worldAreaNow: IntRect,
    ): SelectionModification {
        val entitiesInArea = findEntitiesInArea(worldAreaNow)

        return SelectionModification(
            stateAdjustments = DynamicMap.diff(
                selectionStrategy.map { selectionStrategyNow ->
                    entitiesInArea.associateWith { entity ->
                        val isEntitySelected = editor.isEntitySelected(entity).sample()

                        when (selectionStrategyNow) {
                            SelectionStrategy.Select -> SelectionState.Selected
                            SelectionStrategy.Add -> SelectionState.Selected
                            SelectionStrategy.Subtract -> SelectionState.NonSelected
                            SelectionStrategy.Invert ->
                                if (isEntitySelected) SelectionState.NonSelected
                                else SelectionState.Selected
                        }
                    }.filterValuesNotNull()
                },
            ),
        )
    }

    private fun buildIdleMode(): IdleMode = object : IdleMode {
        private val enterSelectingMode = StreamSink<Tilled<SelectingMode>>()

        override val projectedSelectionModification: Cell<SelectionModification?> =
            input.cursorWorldPosition.map { cursorWorldPositionNow ->
                cursorWorldPositionNow?.let { buildPointSelectionModification(worldPoint = it) }
            }

        override fun select(
            worldAnchor: IntVec2,
            worldTarget: Cell<IntVec2>,
            commit: Stream<Unit>,
        ) {
            enterSelectingMode.send(
                buildSelectingMode(
                    worldAnchor = worldAnchor,
                    worldTarget = worldTarget,
                    commit = commit,
                )
            )
        }

        override val enterNextMode: Stream<Tilled<State>> = enterSelectingMode
    }

    private fun buildSelectingMode(
        worldAnchor: IntVec2,
        worldTarget: Cell<IntVec2>,
        commit: Stream<Unit>,
    ) = object : Tilled<SelectingMode> {
        override fun build(till: Till) = object : SelectingMode {
            init {
                commit.reactTill(till = till) {
                    val entitiesToSelect = selectionModification.sample().getEntitiesToSelect()

                    editor.selectEntities(
                        entities = entitiesToSelect,
                    )
                }
            }

            override val selectionForm: Cell<SelectionForm> = worldTarget.switchMap { worldTargetNow ->
                editor.camera.transform.transform(
                    lineSeg = IntLineSeg(worldAnchor, worldTargetNow),
                ).map { viewShift ->
                    if (viewShift.length > 2.0) AreaSelection(
                        worldArea = IntRect.fromDiagonal(worldAnchor, worldTargetNow),
                    ) else PointSelection(
                        worldPoint = worldAnchor,
                    )
                }
            }

            override val selectionModification: Cell<SelectionModification> =
                selectionForm.map { selectionFormNow ->
                    when (selectionFormNow) {
                        is PointSelection -> buildPointSelectionModification(selectionFormNow.worldPoint)
                        is AreaSelection -> buildAreaSelectionModification(selectionFormNow.worldArea)
                    }
                }

            override val enterNextMode: Stream<Tilled<State>> =
                commit.map { Tilled.pure(buildIdleMode()) }
        }
    }

    private val world: World
        get() = editor.world

    val state: Cell<State> = Stream.followTillNext<State>(
        initialValue = Tilled.pure(buildIdleMode()),
        extractNext = { it.enterNextMode },
        till = tillExit,
    )

    val idleMode: Cell<IdleMode?> = state.map { it as? IdleMode }

    val selectingMode: Cell<SelectingMode?> = state.map { it as? SelectingMode }

    override fun toString(): String = "EntitySelectMode()"
}
