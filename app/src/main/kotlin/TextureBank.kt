import icesword.ImageMetadata
import icesword.JsonRezIndex
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.scene.Texture
import icesword.scene.Tileset
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.ImageBitmap

data class TextureBank(
    private val imagesTextures: Map<String, Texture>,
    val tileset: Tileset,
) {
    companion object {
        suspend fun load(
            rezIndex: JsonRezIndex,
        ): TextureBank {
            val imagesTextures = rezIndex.getAllImagesMetadata()
                // TODO: Support all retails and/or remove this performance trick
                .filter { metadata ->
                    setOf("GAME/", "LEVEL3/").any { metadata.pidImagePath.startsWith(it) } &&
                            setOf("001", "/TREASURE/", "LOGO0").any { metadata.pidImagePath.contains(it) }
                }
                .associate {
                    val imagePath = "images/CLAW/${it.pidImagePath.replace(".PID", ".png")}"
                    it.pidImagePath to loadImageTexture(imagePath = imagePath)
                }

            val tileset = loadTileset()

            return TextureBank(
                imagesTextures = imagesTextures,
                tileset = tileset,
            )
        }
    }

    fun getImageTexture(pidImagePath: String): Texture? =
        imagesTextures[pidImagePath]

    fun getImageTexture(imageMetadata: ImageMetadata): Texture? =
        getImageTexture(pidImagePath = imageMetadata.pidImagePath)
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
        val textureIndexResponse = window.fetch(tilesTextureIndexPath).await()
        val json = textureIndexResponse.json().await().asDynamic()
        return parseIndex(json)
    }

    val imageBitmap = loadImage(imagePath = tilesTextureImagePath)

    val index = loadIndex()

    val tileTextures = index.mapValues { (_, frameRect) ->
        Texture(
            path = tilesTextureImagePath,
            imageBitmap = imageBitmap,
            sourceRect = frameRect,
        )
    }

    return Tileset(
        tileTextures = tileTextures,
    )
}

private suspend fun loadImageTexture(imagePath: String): Texture {
    val imageBitmap = loadImage(imagePath = imagePath)

    val texture = Texture(
        path = imagePath,
        imageBitmap = imageBitmap,
        sourceRect = IntRect(
            position = IntVec2.ZERO,
            size = IntSize(
                width = imageBitmap.width,
                height = imageBitmap.height,
            ),
        )
    )

    return texture
}

private suspend fun loadImage(imagePath: String): ImageBitmap {
    val textureImageResponse = window.fetch(imagePath).await()
    val textureImageBlob = textureImageResponse.blob().await()
    return window.createImageBitmap(textureImageBlob).await()
}
