package icesword.editor

import icesword.geometry.IntVec2
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class IntVec2Serializer : KSerializer<IntVec2> {
    private val delegateSerializer = IntArraySerializer()
    override val descriptor = SerialDescriptor("IntVec2", delegateSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: IntVec2) {
        encoder.encodeSerializableValue(
            delegateSerializer,
            intArrayOf(
                value.x, value.y
            ),
        )
    }

    override fun deserialize(decoder: Decoder): IntVec2 {
        val array = decoder.decodeSerializableValue(delegateSerializer)
        return IntVec2(array[0], array[1])
    }
}
