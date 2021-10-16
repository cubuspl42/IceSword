import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.scene.Texture
import icesword.scene.Tileset
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.ImageBitmap

suspend fun loadTileset(): Tileset {
    suspend fun loadImage(): ImageBitmap {
        val textureImageResponse = window.fetch(textureImagePath).await()
        val textureImageBlob = textureImageResponse.blob().await()
        return window.createImageBitmap(textureImageBlob).await()
    }

    fun parseIndex(
        indexJson: dynamic,
    ): Map<Int, IntRect> {
        val frames = mapOfObject(indexJson.frames)

        return frames.map { (frameIdStr, frame) ->
            val frameId = frameIdStr.toInt()

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

            frameId to frameRect
        }.toMap()
    }

    suspend fun loadIndex(): Map<Int, IntRect> {
        val textureIndexResponse = window.fetch(textureIndexPath).await()
        val json = textureIndexResponse.json().await().asDynamic()
        return parseIndex(json)
    }

    val imageBitmap = loadImage()

    val index = loadIndex()

    val tileTextures = index.mapValues { (_, frameRect) ->
        Texture(imageBitmap, frameRect)
    }

    return Tileset(
        tileTextures = tileTextures,
    )
}
