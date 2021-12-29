package icesword.editor

import icesword.frp.*
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import kotlinx.serialization.Serializable

sealed class Entity {
    abstract val entityPosition: EntityPosition

    val position by lazy { entityPosition.position }

    fun setPosition(newPosition: IntVec2) {
        entityPosition.setPosition(newPosition)
    }

    abstract fun isSelectableIn(area: IntRect): Boolean

    fun move(
        positionDelta: Cell<IntVec2>,
        tillStop: Till,
    ) {
        println("Starting to move entity...")

        val initialPosition = position.sample()
        val targetPosition = positionDelta.map { d -> initialPosition + d }

        targetPosition.reactTill(tillStop) {
            if (position.sample() != it) {
                setPosition(it)
            }
        }
    }

    open fun toEntityData(): EntityData? = null
}

@Serializable
sealed class EntityData
