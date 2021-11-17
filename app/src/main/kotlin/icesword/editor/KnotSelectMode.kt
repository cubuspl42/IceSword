package icesword.editor

import icesword.frp.Till

class KnotSelectMode(
    private val editor: Editor,
    tillExit: Till,
) : EditorMode {
    private val world: World
        get() = editor.world

}
