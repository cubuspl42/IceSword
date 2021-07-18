package icesword

import icesword.geometry.IntVec2
import icesword.scene.Scene
import icesword.scene.TileLayer
import icesword.scene.Tileset
import icesword.scene.scene
import org.w3c.dom.HTMLElement

fun worldView(
    world: World,
    tileset: Tileset,
): HTMLElement = scene { context ->
    Scene(
        root = TileLayer(
            tileset = tileset,
            tiles = world.tiles,
        ),
        cameraFocusPoint = IntVec2(1024, 32),
    )
}
