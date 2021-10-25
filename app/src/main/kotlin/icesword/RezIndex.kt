package icesword

import TextureBank
import icesword.editor.WapObjectPrototype
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2

value class ImageSetId(
    val fullyQualifiedId: String,
)

data class ImageMetadata(
    val pidImagePath: String,
    val offset: IntVec2,
    val size: IntSize,
)

interface RezIndex {
    fun getImageMetadata(
        imageSetId: ImageSetId,
        i: Int,
    ): ImageMetadata?
}

/// REZ index combining information from `rezIndex.json` and textures' metadata
class CombinedRezIndex(
    private val delegate: JsonRezIndex,
    private val textureBank: TextureBank,
) : RezIndex {
    override fun getImageMetadata(
        imageSetId: ImageSetId,
        i: Int,
    ): ImageMetadata? =
        delegate.getImageMetadata(
            imageSetId = imageSetId,
            i = i,
        )?.let { metadata ->
            val textureOrNull = textureBank.getImageTexture(
                pidImagePath = metadata.pidImagePath,
            )

            val size = textureOrNull?.sourceRect?.size ?: IntSize.ZERO

            metadata.copy(
                size = size,
            )
        }
}
