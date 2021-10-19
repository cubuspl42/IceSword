package icesword.editor

import fetchWorld
import icesword.frp.Cell
import icesword.frp.DynamicLock
import icesword.frp.MutCell
import icesword.frp.Till
import icesword.frp.map
import icesword.scene.Tileset
import icesword.wwd.Wwd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import loadTileset
import org.khronos.webgl.ArrayBuffer
import org.w3c.files.Blob
import org.w3c.files.File
import kotlin.js.Promise


data class LoadingWorldProcess(
    val worldFilename: String,
)

class App(
    private val tileset: Tileset,
    initialEditor: Editor,
    till: Till,
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
                till = Till.never,
            )
        }
    }

    private val _editor = MutCell<Editor?>(initialEditor)

    val editor: Cell<Editor?>
        get() = _editor

    private val _loadWorldLock = DynamicLock<LoadingWorldProcess>()

    val loadingWorldProcess: Cell<LoadingWorldProcess?> = _loadWorldLock.owningProcess

    val canLoadWorld: Cell<Boolean> = _loadWorldLock.isLocked.map { !it }

    fun loadWorld(file: File) {
        launch {
            _loadWorldLock.synchronized(
                process = LoadingWorldProcess(worldFilename = file.name),
            ) {
                delay(100)

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
}

private fun Blob.arrayBuffer(): Promise<ArrayBuffer> =
    this.asDynamic().arrayBuffer() as Promise<ArrayBuffer>
