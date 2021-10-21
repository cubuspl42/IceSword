import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.scene.Texture
import icesword.scene.Tileset
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.ImageBitmap

data class TextureBank(
    val tileset: Tileset,
    val rope: Texture,
    val crumblingPeg: Texture,
) {
    companion object {
        suspend fun load(): TextureBank {
            val tileset = loadTileset()
            val rope = loadImageTexture(imagePath = "images/rope.png")
            val crumblingPeg = loadImageTexture(imagePath = "images/crumblingPeg.png")

            return TextureBank(
                tileset = tileset,
                rope = rope,
                crumblingPeg = crumblingPeg,
            )
        }
    }
}

private suspend fun loadTileset(): Tileset {
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

    val imageBitmap = loadImage(imagePath = textureImagePath)

    val index = loadIndex()

    val tileTextures = index.mapValues { (_, frameRect) ->
        Texture(imageBitmap, frameRect)
    }

    return Tileset(
        tileTextures = tileTextures,
    )
}

private suspend fun loadRopeTexture(): Texture {
    val ropeBitmap = loadImage(imagePath = "images/rope.png")

    val ropeTexture = Texture(
        imageBitmap = ropeBitmap,
        sourceRect = IntRect(
            position = IntVec2.ZERO,
            size = IntSize(
                width = ropeBitmap.width,
                height = ropeBitmap.height,
            ),
        )
    )

    return ropeTexture
}

private suspend fun loadImageTexture(imagePath: String): Texture {
    val ropeBitmap = loadImage(imagePath = imagePath)

    val ropeTexture = Texture(
        imageBitmap = ropeBitmap,
        sourceRect = IntRect(
            position = IntVec2.ZERO,
            size = IntSize(
                width = ropeBitmap.width,
                height = ropeBitmap.height,
            ),
        )
    )

    return ropeTexture
}

private suspend fun loadImage(imagePath: String): ImageBitmap {
    val textureImageResponse = window.fetch(imagePath).await()
    val textureImageBlob = textureImageResponse.blob().await()
    return window.createImageBitmap(textureImageBlob).await()
}
