package icesword

import icesword.geometry.IntSize
import icesword.geometry.IntVec2

value class ImageSetId(
    val fullyQualifiedId: String,
) {
    companion object {
        val empty = ImageSetId(fullyQualifiedId = "")
    }
}

data class ImageMetadata(
    val pidPath: RezPath,
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
    private val textureBank: RezTextureBank,
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
                pidPath = metadata.pidPath,
            )

            val size = textureOrNull?.sourceRect?.size ?: IntSize.ZERO

            metadata.copy(
                size = size,
            )
        }
}
