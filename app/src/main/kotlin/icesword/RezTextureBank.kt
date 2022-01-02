package icesword

import icesword.editor.retails.Retail
import icesword.ui.scene.Texture
import icesword.ui.scene.Tileset
import kotlinx.browser.window
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import org.w3c.fetch.Response

value class RezPath(
    val path: String,
)

interface RezTextureBank {
    companion object {
        fun chained(
            textureBank1: RezTextureBank,
            textureBank2: RezTextureBank,
        ): RezTextureBank = ChainedRezTextureBank(
            textureBank1 = textureBank1,
            textureBank2 = textureBank2,
        )
    }

    fun getImageTexture(pidPath: RezPath): Texture?
}

fun RezTextureBank.buildTileset(rezIndex: RezIndex, retail: Retail): Tileset {
    val tileTextures = (1..1024).mapNotNull { tileId ->
        val imageSetId = ImageSetId("LEVEL${retail.naturalIndex}_TILES_ACTION")
        val tileMetadata = rezIndex.getImageMetadata(imageSetId, tileId)

        tileMetadata?.pidPath?.let { pidPath ->
            getImageTexture(pidPath = pidPath)?.let { imageTexture ->
                tileId to imageTexture
            }
        }
    }.toMap()

    return Tileset(
        tileTextures = tileTextures,
    )
}

class TextureAtlasTextureBank(
    private val textureMap: Map<RezPath, Texture>,
) : RezTextureBank {
    companion object {
        suspend fun load(
            path: String,
            prefix: String,
        ): TextureAtlasTextureBank {
            val textureAtlas = TextureAtlas.load(path = path)

            val textureMap = textureAtlas.buckets.fold(emptyMap<RezPath, Texture>()) { acc, bucket ->
                acc + bucket.buildTextureMap().mapKeys { (framePath, _) ->
                    RezPath(path = prefix + framePath.path + ".PID")
                }
            }

            return TextureAtlasTextureBank(
                textureMap = textureMap,
            )
        }
    }

    override fun getImageTexture(pidPath: RezPath): Texture? =
        textureMap[pidPath]
}


suspend fun loadGameTextureBank(): RezTextureBank =
    TextureAtlasTextureBank.load(
        path = "images/spritesheets/GAME_IMAGES",
        prefix = "GAME/",
    )

suspend fun loadRetailTextureBank(retail: Retail): RezTextureBank {
    val prefix = "LEVEL${retail.naturalIndex}/"

    return coroutineScope {
        val imagesBank = async {
            TextureAtlasTextureBank.load(
                path = "images/spritesheets/LEVEL${retail.naturalIndex}_IMAGES",
                prefix = prefix,
            )
        }

        val tilesBank = async {
            TextureAtlasTextureBank.load(
                path = "images/spritesheets/LEVEL${retail.naturalIndex}_TILES",
                prefix = prefix,
            )
        }

        ChainedRezTextureBank(
            textureBank1 = imagesBank.await(),
            textureBank2 = tilesBank.await(),
        )
    }


}

class ChainedRezTextureBank(
    private val textureBank1: RezTextureBank,
    private val textureBank2: RezTextureBank,
) : RezTextureBank {
    override fun getImageTexture(pidPath: RezPath): Texture? =
        textureBank1.getImageTexture(pidPath = pidPath)
            ?: textureBank2.getImageTexture(pidPath = pidPath)
}

suspend fun fetchResource(resourcePath: String): Response? {
    val response = window.fetch(resourcePath).await()
    return if (response.ok) response else null
}
