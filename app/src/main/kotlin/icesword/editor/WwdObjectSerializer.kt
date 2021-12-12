package icesword.editor

import icesword.wwd.DataStreamObj.ByteString
import icesword.wwd.Geometry
import icesword.wwd.Wwd
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@kotlinx.serialization.Serializable
data class RectangleSurrogate(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    constructor(rect: Geometry.Rectangle) : this(
        left = rect.left,
        top = rect.top,
        right = rect.right,
        bottom = rect.bottom,
    )

    fun toRectangle() = Geometry.Rectangle(
        left = this.left,
        top = this.top,
        right = this.right,
        bottom = this.bottom,
    )

    companion object {
        val Zero = RectangleSurrogate(
            left = 0,
            top = 0,
            right = 0,
            bottom = 0,
        )
    }
}

@kotlinx.serialization.Serializable
data class WwdObjectSurrogate(
    val id: Int = 0,
    val name: String = "",
    val logic: String = "",
    val imageSet: String = "",
    val animation: String = "",
    val x: Int = 0,
    val y: Int = 0,
    val z: Int = 0,
    val i: Int = 0,
    val addFlags: Int = 0,
    val dynamicFlags: Int = 0,
    val drawFlags: Int = 0,
    val userFlags: Int = 0,
    val score: Int = 0,
    val points: Int = 0,
    val powerUp: Int = 0,
    val damage: Int = 0,
    val smarts: Int = 0,
    val health: Int = 0,
    val rangeRect: RectangleSurrogate = RectangleSurrogate.Zero,
    val moveRect: RectangleSurrogate = RectangleSurrogate.Zero,
    val hitRect: RectangleSurrogate = RectangleSurrogate.Zero,
    val attackRect: RectangleSurrogate = RectangleSurrogate.Zero,
    val clipRect: RectangleSurrogate = RectangleSurrogate.Zero,
    val userRect1: RectangleSurrogate = RectangleSurrogate.Zero,
    val userRect2: RectangleSurrogate = RectangleSurrogate.Zero,
    val userValue1: Int = 0,
    val userValue2: Int = 0,
    val userValue3: Int = 0,
    val userValue4: Int = 0,
    val userValue5: Int = 0,
    val userValue6: Int = 0,
    val userValue7: Int = 0,
    val userValue8: Int = 0,
    val speedX: Int = 0,
    val speedY: Int = 0,
    val xTweak: Int = 0,
    val yTweak: Int = 0,
    val counter: Int = 0,
    val speed: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
    val direction: Int = 0,
    val faceDir: Int = 0,
    val timeDelay: Int = 0,
    val frameDelay: Int = 0,
    val objectType: Int = 0,
    val hitTypeFlags: Int = 0,
    val xMoveRes: Int = 0,
    val yMoveRes: Int = 0,
)

object WwdObjectSerializer : KSerializer<Wwd.Object_> {
    override val descriptor: SerialDescriptor = WwdObjectSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Wwd.Object_) {
        val surrogate = WwdObjectSurrogate(
            id = value.id,
            name = value.name.decode(),
            logic = value.logic.decode(),
            imageSet = value.imageSet.decode(),
            animation = value.animation.decode(),
            x = value.x,
            y = value.y,
            z = value.z,
            i = value.i,
            addFlags = value.addFlags,
            dynamicFlags = value.dynamicFlags,
            drawFlags = value.drawFlags,
            userFlags = value.userFlags,
            score = value.score,
            points = value.points,
            powerUp = value.powerUp,
            damage = value.damage,
            smarts = value.smarts,
            health = value.health,
            rangeRect = RectangleSurrogate(value.rangeRect),
            moveRect = RectangleSurrogate(value.moveRect),
            hitRect = RectangleSurrogate(value.hitRect),
            attackRect = RectangleSurrogate(value.attackRect),
            clipRect = RectangleSurrogate(value.clipRect),
            userRect1 = RectangleSurrogate(value.userRect1),
            userRect2 = RectangleSurrogate(value.userRect2),
            userValue1 = value.userValue1,
            userValue2 = value.userValue2,
            userValue3 = value.userValue3,
            userValue4 = value.userValue4,
            userValue5 = value.userValue5,
            userValue6 = value.userValue6,
            userValue7 = value.userValue7,
            userValue8 = value.userValue8,
            speedX = value.speedX,
            speedY = value.speedY,
            xTweak = value.xTweak,
            yTweak = value.yTweak,
            counter = value.counter,
            speed = value.speed,
            width = value.width,
            height = value.height,
            direction = value.direction,
            faceDir = value.faceDir,
            timeDelay = value.timeDelay,
            frameDelay = value.frameDelay,
            objectType = value.objectType,
            hitTypeFlags = value.hitTypeFlags,
            xMoveRes = value.xMoveRes,
            yMoveRes = value.yMoveRes,
        )

        encoder.encodeSerializableValue(WwdObjectSurrogate.serializer(), surrogate)
    }

    override fun deserialize(decoder: Decoder): Wwd.Object_ {
        val surrogate = decoder.decodeSerializableValue(WwdObjectSurrogate.serializer())

        return Wwd.Object_(
            id = surrogate.id,
            name = ByteString.encode(surrogate.name),
            logic = ByteString.encode(surrogate.logic),
            imageSet = ByteString.encode(surrogate.imageSet),
            animation = ByteString.encode(surrogate.animation),
            x = surrogate.x,
            y = surrogate.y,
            z = surrogate.z,
            i = surrogate.i,
            addFlags = surrogate.addFlags,
            dynamicFlags = surrogate.dynamicFlags,
            drawFlags = surrogate.drawFlags,
            userFlags = surrogate.userFlags,
            score = surrogate.score,
            points = surrogate.points,
            powerUp = surrogate.powerUp,
            damage = surrogate.damage,
            smarts = surrogate.smarts,
            health = surrogate.health,
            rangeRect = surrogate.rangeRect.toRectangle(),
            moveRect = surrogate.moveRect.toRectangle(),
            hitRect = surrogate.hitRect.toRectangle(),
            attackRect = surrogate.attackRect.toRectangle(),
            clipRect = surrogate.clipRect.toRectangle(),
            userRect1 = surrogate.userRect1.toRectangle(),
            userRect2 = surrogate.userRect2.toRectangle(),
            userValue1 = surrogate.userValue1,
            userValue2 = surrogate.userValue2,
            userValue3 = surrogate.userValue3,
            userValue4 = surrogate.userValue4,
            userValue5 = surrogate.userValue5,
            userValue6 = surrogate.userValue6,
            userValue7 = surrogate.userValue7,
            userValue8 = surrogate.userValue8,
            speedX = surrogate.speedX,
            speedY = surrogate.speedY,
            xTweak = surrogate.xTweak,
            yTweak = surrogate.yTweak,
            counter = surrogate.counter,
            speed = surrogate.speed,
            width = surrogate.width,
            height = surrogate.height,
            direction = surrogate.direction,
            faceDir = surrogate.faceDir,
            timeDelay = surrogate.timeDelay,
            frameDelay = surrogate.frameDelay,
            objectType = surrogate.objectType,
            hitTypeFlags = surrogate.hitTypeFlags,
            xMoveRes = surrogate.xMoveRes,
            yMoveRes = surrogate.yMoveRes,
        )
    }
}
