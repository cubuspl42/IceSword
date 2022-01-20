@file:UseSerializers(IntVec2Serializer::class)

package icesword.editor.entities

import icesword.ImageSetId
import icesword.RezIndex
import icesword.editor.DynamicWapSprite
import icesword.editor.IntVec2Serializer
import icesword.editor.retails.Retail
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.geometry.IntRect
import icesword.geometry.IntVec2
import icesword.wwd.Wwd
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class TogglePegPrototype(
    val imageSetId: ImageSetId,
    val shortImageSetId: String,
)

class TogglePeg(
    rezIndex: RezIndex,
    retail: Retail,
    private val prototype: TogglePegPrototype,
    initialPosition: IntVec2,
    initialTimeOnMs: Int,
    initialTimeOffMs: Int,
    initialDelayMs: Int,
) : Entity(),
    WapObjectExportable {

    companion object {
        fun load(
            rezIndex: RezIndex,
            retail: Retail,
            data: TogglePegData,
        ): TogglePeg = TogglePeg(
            rezIndex = rezIndex,
            retail = retail,
            prototype = data.prototype,
            initialPosition = data.position,
            initialTimeOnMs = data.timeOnMs,
            initialTimeOffMs = data.timeOffMs,
            initialDelayMs = data.delayMs,
        )
    }

    private val _timeOnMs = MutCell(
        initialValue = initialTimeOnMs,
    )

    val timeOnMs: Cell<Int> = _timeOnMs

    fun setTimeOnMs(newValue: Int) {
        _timeOnMs.set(newValue)
    }

    private val _timeOffMs = MutCell(
        initialValue = initialTimeOffMs,
    )

    val timeOffMs: Cell<Int> = _timeOffMs

    fun setTimeOffMs(newValue: Int) {
        _timeOffMs.set(newValue)
    }

    private val _delayMs = MutCell(
        initialValue = initialDelayMs,
    )

    val delayMs: Cell<Int> = _delayMs

    fun setDelayMs(newValue: Int) {
        _delayMs.set(newValue)
    }

    override val entityPosition = EntityPixelPosition(
        initialPosition = initialPosition,
    )

    override val zOrder: Cell<Int> = Cell.constant(0)

    override val asZOrderedEntity: ZOrderedEntity = SimpleZOrderedEntity(
        initialZOrder = 0,
    )

    val wapSprite = DynamicWapSprite.fromImageSet(
        rezIndex = rezIndex,
        imageSetId = expandImageSetId(
            retail = retail,
            shortImageSetId = prototype.shortImageSetId,
        ),
        position = position,
        z = asZOrderedEntity.zOrder,
    )

    override fun isSelectableIn(area: IntRect): Boolean =
        wapSprite.isSelectableIn(area)

    override fun toEntityData() = TogglePegData(
        prototype = prototype,
        position = position.sample(),
        timeOnMs = timeOnMs.sample(),
        timeOffMs = timeOffMs.sample(),
        delayMs = delayMs.sample(),
    )

    override fun exportWapObject(): Wwd.Object_ {
        val position = position.sample()
        val timeOnMs = timeOnMs.sample()
        val timeOffMs = timeOffMs.sample()
        val delayMs = delayMs.sample()

        return WapObjectPropsData(
            logic = "TogglePeg",
            imageSet = prototype.shortImageSetId,
            x = position.x,
            y = position.y,
            i = -1,
            speedX = timeOnMs,
            speedY = timeOffMs,
            speed = delayMs,
        ).toWwdObject()
    }
}

@Serializable
@SerialName("TogglePeg")
data class TogglePegData(
    val prototype: TogglePegPrototype,
    val position: IntVec2,
    val timeOnMs: Int,
    val timeOffMs: Int,
    val delayMs: Int,
) : EntityData()
