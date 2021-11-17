package icesword.editor

import icesword.frp.Cell
import icesword.frp.DynamicSet
import icesword.frp.Stream
import icesword.frp.StreamSink
import icesword.frp.Till
import icesword.frp.Tilled
import icesword.frp.filterDynamic
import icesword.frp.map
import icesword.frp.mergeWith
import icesword.frp.reactTill
import icesword.geometry.IntRect
import icesword.geometry.IntVec2


class EntitySelectMode(
    private val editor: Editor,
    tillExit: Till,
) : EditorMode {
    private val world: World
        get() = editor.world

    val selectMode = SelectMode(
        factory = object : SelectModeFactory<EntityAreaSelectingMode> {
            override fun createSubAreaSelectingMode(
                selectionArea: Cell<IntRect>,
                confirm: Stream<Unit>,
                tillExit: Till,
            ) = EntityAreaSelectingMode(
                selectionArea = selectionArea,
                confirm = confirm,
                tillExit = tillExit,
            )
        },
        tillExit = tillExit,
    )

    val areaSelectingMode: Cell<SelectMode<EntityAreaSelectingMode>.AreaSelectingMode?> =
        selectMode.state.map { st -> (st as? SelectMode.AreaSelectingMode) }

    val entityAreaSelectingMode: Cell<EntityAreaSelectingMode?> =
        areaSelectingMode.map { it?.subMode }

    private fun getEntitiesInArea(area: Cell<IntRect>): DynamicSet<Entity> {
        return world.entities.filterDynamic { entity: Entity ->
            area.map { entity.isSelectableIn(it) }
        }
    }

    private fun selectEntities(entities: Set<Entity>) {
        editor.selectEntities(entities)
    }

    inner class EntityAreaSelectingMode(
        selectionArea: Cell<IntRect>,
        confirm: Stream<Unit>,
        tillExit: Till,
    ) {
        val coveredEntities: DynamicSet<Entity> =
            getEntitiesInArea(
                area = selectionArea,
            )

        init {
            confirm.reactTill(till = tillExit) {
                selectEntities(coveredEntities.volatileContentView)
            }
        }

        override fun toString(): String = "EntityAreaSelectingMode()"
    }

    override fun toString(): String = "EntitySelectMode()"
}
