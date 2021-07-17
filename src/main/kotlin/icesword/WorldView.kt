package icesword

import icesword.scene.SceneContext
import icesword.scene.TileLayer
import icesword.scene.Tileset
import icesword.scene.scene
import org.w3c.dom.HTMLElement
import org.w3c.dom.Image

fun createLevelTileset(
    context: SceneContext,
    levelIndex: Int,
): Tileset = Tileset(
    tileTextures = (0 until 1024).associateWith { tileId ->
        val tileIdStr = tileId.toString().padStart(3, '0')
        val tileImage = Image().apply {
            src = "images/CLAW/LEVEL${levelIndex}/TILES/ACTION/$tileIdStr.png"
        }
        context.createTexture(tileImage)
    },
)

fun worldView(world: World): HTMLElement = scene { context ->
    TileLayer(
        tileset = createLevelTileset(context, levelIndex = 3),
        tiles = world.tiles,
    )
}
