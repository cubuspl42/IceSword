package icesword

import icesword.scene.TileLayer
import icesword.scene.Tileset
import icesword.scene.scene
import org.w3c.dom.HTMLElement

fun worldView(
    world: World,
    tileset: Tileset,
): HTMLElement = scene { context ->
    TileLayer(
        tileset = tileset,
        tiles = world.tiles,
    )
}
