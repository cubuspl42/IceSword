package icesword.editor

import icesword.frp.Cell
import icesword.frp.CellLoop
import icesword.frp.Stream
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.map
import icesword.frp.reactTill
import icesword.frp.switchMap
import icesword.geometry.IntLineSeg
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2


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
        NonSelected;

        val opposite: SelectionState
            get() = when (this) {
                Selected -> NonSelected
                NonSelected -> Selected
            }
    }

    sealed interface SelectionForm

    data class PointSelection(
        val worldPoint: IntVec2,
    ) : SelectionForm

    data class AreaSelection(
        val worldArea: IntRect,
    ) : SelectionForm

    inner class SelectionProjection(
        val consideredEntities: Set<Entity>,
    ) {
        fun projectEntitySelectionState(entity: Entity): Cell<SelectionState> =
            editor.isEntitySelected(entity).switchMap { isSelected ->
                val currentSelectionState =
                    if (isSelected) SelectionState.Selected
                    else SelectionState.NonSelected

                selectionStrategy.map { selectionStrategyNow ->
                    when (selectionStrategyNow) {
                        SelectionStrategy.Select ->
                            if (consideredEntities.contains(entity)) SelectionState.Selected
                            else SelectionState.NonSelected
                        SelectionStrategy.Add ->
                            if (consideredEntities.contains(entity)) SelectionState.Selected
                            else currentSelectionState
                        SelectionStrategy.Subtract ->
                            if (consideredEntities.contains(entity)) SelectionState.NonSelected
                            else currentSelectionState
                        SelectionStrategy.Invert ->
                            if (consideredEntities.contains(entity)) currentSelectionState.opposite
                            else currentSelectionState
                    }
                }
            }

        fun applyStrategy() {
            val selectionStrategyNow = selectionStrategy.sample()

            when (selectionStrategyNow) {
                SelectionStrategy.Select -> editor.setSelectedEntities(consideredEntities)
                SelectionStrategy.Add -> editor.selectEntities(consideredEntities)
                SelectionStrategy.Subtract -> editor.unselectEntities(consideredEntities)
                SelectionStrategy.Invert -> editor.invertEntitySelection(consideredEntities)
            }
        }
    }

    sealed interface State {
        val selectionProjection: Cell<SelectionProjection?>

        val enterNextMode: Stream<Tilled<State>>
    }

    interface IdleMode : State {
        override val selectionProjection: Cell<SelectionProjection?>

        fun select(
            worldAnchor: IntVec2,
            worldTarget: Cell<IntVec2>,
            commit: Stream<Unit>,
        )
    }

    interface SelectingMode : State {
        val selectionForm: Cell<SelectionForm>

        override val selectionProjection: Cell<SelectionProjection?>
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

    private fun buildPointSelectionModification(worldPoint: IntVec2): SelectionProjection? {
        val entitiesAtPoint = findEntitiesAtPoint(worldPoint = worldPoint)
        val selectedEntitiesAtPoint = entitiesAtPoint.filter { editor.isEntitySelected(it).sample() }

        val entityToSelectOrNull = selectedEntitiesAtPoint.singleOrNull()?.let { singleSelectedEntity ->
            val selectedEntityIndex = entitiesAtPoint.indexOfOrNull(singleSelectedEntity)
            selectedEntityIndex?.let { entitiesAtPoint[(it + 1) % entitiesAtPoint.size] }
        } ?: entitiesAtPoint.firstOrNull()

        return entityToSelectOrNull?.let { entityToSelect ->
            SelectionProjection(
                consideredEntities = setOf(entityToSelect),
            )
        }
    }

    private fun buildAreaSelectionModification(
        worldAreaNow: IntRect,
    ): SelectionProjection {
        val entitiesInArea = findEntitiesInArea(worldAreaNow)

        return SelectionProjection(
            consideredEntities = entitiesInArea.toSet(),
        )
    }

    private fun buildIdleMode(): IdleMode = object : IdleMode {
        private val enterSelectingMode = StreamSink<Tilled<SelectingMode>>()

        override val selectionProjection: Cell<SelectionProjection?> =
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
                    selectionProjection.sample()?.applyStrategy()
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

            override val selectionProjection: Cell<SelectionProjection?> =
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

    val state: Cell<State> = Stream.followTillNext<State>(
        initialValue = Tilled.pure(buildIdleMode()),
        extractNext = { it.enterNextMode },
        till = tillExit,
    )

    val idleMode: Cell<IdleMode?> = state.map { it as? IdleMode }

    val selectingMode: Cell<SelectingMode?> = state.map { it as? SelectingMode }

    override fun toString(): String = "EntitySelectMode()"
}
