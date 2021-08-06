package icesword.editor

import fetchWorld
import icesword.scene.Tileset
import loadTileset

class Editor(
    val world: World,
    val tileset: Tileset,
) {
    companion object {
        suspend fun load(): Editor {
            val tileset = loadTileset()

            val wwdWorld = fetchWorld()

            val world = World.load(wwdWorld)

            return Editor(
                world = world,
                tileset = tileset,
            )
        }
    }
}
