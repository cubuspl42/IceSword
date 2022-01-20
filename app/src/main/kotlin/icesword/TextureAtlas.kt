package icesword

import icesword.TextureAtlasIndex.FramePath
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.ui.world_view.scene.Texture
import kotlinx.browser.window
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import mapOfObject
import org.w3c.dom.ImageBitmap

class TextureAtlasIndex(
    val entries: Map<FramePath, Entry>,
) {
    value class FramePath(
        val path: String,
    )

    data class Entry(
        val sourceRect: IntRect,
    )

    companion object {
        suspend fun load(indexPath: String): TextureAtlasIndex? {
            fun parseEntries(
                indexJson: dynamic,
            ): Map<FramePath, Entry> {
                val frames = mapOfObject(indexJson.frames)

                return frames.map { (framePath, frame) ->
                    val frameRectJson = frame.asDynamic().frame

                    val frameRect = IntRect(
                        IntVec2(
                            frameRectJson.x as Int,
                            frameRectJson.y as Int,
                        ),
                        IntSize(
                            frameRectJson.w as Int,
                            frameRectJson.h as Int,
                        )
                    )

                    FramePath(framePath) to Entry(sourceRect = frameRect)
                }.toMap()
            }

            return try {
                fetchResource(indexPath)?.let { response ->
                    val json = response.json().await().asDynamic()

                    TextureAtlasIndex(
                        entries = parseEntries(json),
                    )
                }
            } catch (e: Throwable) {
                console.error("Error while loading texture atlas index [$indexPath]:", e)

                null
            }
        }
    }
}

data class TextureAtlasBucket(
    val index: TextureAtlasIndex,
    val bitmap: ImageBitmap,
) {
    fun buildTextureMap(): Map<FramePath, Texture> = index.entries.mapValues { (_, entry) ->
        Texture(
            path = "",
            imageBitmap = bitmap,
            sourceRect = entry.sourceRect,
        )
    }
}

data class TextureAtlas(
    val buckets: List<TextureAtlasBucket>,
) {
    companion object {
        suspend fun load(path: String): TextureAtlas {
            val loader = object {
                suspend fun loadBucket(bucketIndex: Int): TextureAtlasBucket? {
                    val indexPath = "$path/texture-$bucketIndex.json"
                    val bitmapPath = "$path/texture-$bucketIndex.png"

                    return coroutineScope {
                        val indexOrNull = async { TextureAtlasIndex.load(indexPath = indexPath) }
                        val bitmapOrNull = async { loadImageBitmap(imagePath = bitmapPath) }

                        indexOrNull.await()?.let { index ->
                            bitmapOrNull.await()?.let { bitmap ->
                                TextureAtlasBucket(
                                    index = index,
                                    bitmap = bitmap,
                                )
                            }
                        }
                    }

                }

                suspend fun loadBuckets(fromIndex: Int): List<TextureAtlasBucket> =
                    loadBucket(bucketIndex = fromIndex)?.let { bucket ->
                        listOf(bucket) + loadBuckets(fromIndex = fromIndex + 1)
                    } ?: emptyList()
            }

            return TextureAtlas(
                buckets = loader.loadBuckets(fromIndex = 0),
            )
        }
    }
}

suspend fun loadImageBitmap(imagePath: String): ImageBitmap? = try {
    fetchResource(imagePath)?.let { response ->
        val blob = response.blob().await()

        window.createImageBitmap(blob).await()
    }
} catch (e: Throwable) {
    console.error("Error while loading image bitmap [$imagePath]:", e)

    null
}
