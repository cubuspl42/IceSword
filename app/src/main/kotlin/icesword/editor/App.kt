package icesword.editor

import fetchWorld
import icesword.frp.Cell
import icesword.frp.MutCell
import icesword.scene.Tileset
import icesword.wwd.Wwd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import loadTileset
import org.khronos.webgl.ArrayBuffer
import org.w3c.files.Blob
import org.w3c.files.File
import kotlin.js.Promise

class App(
    private val tileset: Tileset,
    initialEditor: Editor,
) : CoroutineScope by MainScope() {
    companion object {
        suspend fun load(): App {
            val tileset = loadTileset()

            val initialWwdWorld = fetchWorld()

            val editor = Editor.load(
                tileset = tileset,
                wwdWorld = initialWwdWorld,
            )

            return App(
                tileset = tileset,
                initialEditor = editor,
            )
        }
    }

    private val _loadingWorld = MutCell<Unit?>(null)

    private val loadingWorld: MutCell<Unit?>
        get() = _loadingWorld

    private val _editor = MutCell<Editor?>(initialEditor)

    val editor: Cell<Editor?>
        get() = _editor

    fun loadWorld(file: File) {
        launch {
            val worldBuffer = file.arrayBuffer().await()
            val world = Wwd.readWorld(worldBuffer)

            val editor = Editor.load(
                tileset = tileset,
                wwdWorld = world,
            )

            _editor.set(editor)
        }
    }
}

private fun Blob.arrayBuffer(): Promise<ArrayBuffer> =
    this.asDynamic().arrayBuffer() as Promise<ArrayBuffer>
