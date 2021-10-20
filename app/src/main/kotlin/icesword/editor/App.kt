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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import loadTileset
import org.khronos.webgl.ArrayBuffer
import org.w3c.files.Blob
import org.w3c.files.File
import kotlin.js.Promise


data class LoadingWorldProcess(
    val worldFilename: String,
)

class App(
    private val wwdWorldTemplate: Wwd.World,
    private val tileset: Tileset,
    initialEditor: Editor,
    till: Till,
) : CoroutineScope by MainScope() {
    companion object {
        suspend fun load(): App {
            val tileset = loadTileset()

            val wwdWorldTemplate: Wwd.World = fetchWorld()

            val editor = Editor.importWwd(
                tileset = tileset,
                wwdWorld = wwdWorldTemplate,
            )

            return App(
                wwdWorldTemplate = wwdWorldTemplate,
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
        suspend fun loadEditor(load: suspend () -> Editor) {
            delay(100)

            val editor = load()

            _editor.set(editor)
        }

        launch {
            _loadWorldLock.synchronized(
                process = LoadingWorldProcess(worldFilename = file.name),
            ) {
                if (file.name.endsWith(".json")) {
                    loadEditor { loadProject(file = file) }
                } else {
                    loadEditor { importWorld(file = file) }
                }
            }
        }
    }

    private suspend fun importWorld(file: File): Editor {
        val worldBuffer = file.arrayBuffer().await()

        val world = Wwd.readWorld(worldBuffer)

        val editor = Editor.importWwd(
            tileset = tileset,
            wwdWorld = world,
        )

        return editor
    }

    private suspend fun loadProject(file: File): Editor {
        val projectDataString = file.text().await()

        val projectData = Json.decodeFromString<ProjectData>(projectDataString)

        val editor = Editor.loadProject(
            wwdWorldTemplate = wwdWorldTemplate,
            tileset = tileset,
            projectData = projectData,
        )

        return editor
    }
}

private fun Blob.arrayBuffer(): Promise<ArrayBuffer> =
    this.asDynamic().arrayBuffer() as Promise<ArrayBuffer>

private fun Blob.text(): Promise<String> =
    this.asDynamic().text() as Promise<String>
