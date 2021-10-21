package icesword

import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/// REZ index based on `rezIndex.json`, without image size information
class JsonRezIndex(
    private val data: RezIndexData,
) : RezIndex {
    companion object {
        suspend fun load(): JsonRezIndex {
            val response = window.fetch("rezIndex.json").await()
            val rezIndexJson: String = response.text().await()
            val rezIndexData = Json.decodeFromString<RezIndexData>(rezIndexJson)

            return JsonRezIndex(
                data = rezIndexData,
            )
        }
    }

    override fun getImageMetadata(
        imageSetId: ImageSetId,
        i: Int,
    ): ImageMetadata? =
        data.imageSets[imageSetId.fullyQualifiedId]?.let { imageSetData ->
            imageSetData.frames[i]?.let { pidFilename ->
                val metadata = imageSetData.sprites[pidFilename]!!
                val offset = IntVec2(metadata.offset[0], metadata.offset[1])

                ImageMetadata(
                    pidImagePath = metadata.path,
                    offset = offset,
                    size = IntSize.ZERO,
                )
            }
        }
}

@Serializable
data class RezIndexData(
    /// Fully-qualified image-set ID to image set data
    val imageSets: Map<String, ImageSetData>,
)

@Serializable
data class ImageSetData(
    /// Frame index (i) to PID filename
    val frames: Map<Int, String>,

    /// PID filename to image metadata
    val sprites: Map<String, ImageMetadataData>,
)

@Serializable
class ImageMetadataData(
    /// Image's offset that should be used for calculating sprite's center
    val offset: IntArray,

    /// Path to a PID file, relative from REZ index
    val path: String,
)
