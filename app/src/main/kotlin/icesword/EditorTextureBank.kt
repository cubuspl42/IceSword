package icesword

import icesword.ui.world_view.scene.Texture

data class EditorTextureBank(
    val gameTextureBank: RezTextureBank,
    val wapObjectPlaceholder: Texture,
) {
    companion object {
        suspend fun load(gameTextureBank: RezTextureBank): EditorTextureBank = EditorTextureBank(
            gameTextureBank = gameTextureBank,
            wapObjectPlaceholder = Texture.load("images/wapObject.png"),
        )
    }
}
