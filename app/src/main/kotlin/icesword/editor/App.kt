package icesword.editor

import TextureBank
import fetchWorld
import icesword.JsonRezIndex
import icesword.CombinedRezIndex
import icesword.RezIndex
import icesword.frp.Cell
import icesword.frp.DynamicLock
import icesword.frp.MutCell
import icesword.frp.map
import icesword.wwd.Wwd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.khronos.webgl.ArrayBuffer
import org.w3c.files.Blob
import org.w3c.files.File
import kotlin.js.Promise


data class LoadingWorldProcess(
    val worldFilename: String,
)

class App(
    private val wwdWorldTemplate: Wwd.World,
    val rezIndex: RezIndex,
    val textureBank: TextureBank,
    initialEditor: Editor,
) : CoroutineScope by MainScope() {
    companion object {
        suspend fun load(): App {
            val jsonRezIndex = JsonRezIndex.load()

            val textureBank = TextureBank.load(
                rezIndex = jsonRezIndex,
                retail = Retail.theRetail,
            )

            val combinedRezIndex = CombinedRezIndex(
                delegate = jsonRezIndex,
                textureBank = textureBank,
            )

            val wwdWorldTemplate: Wwd.World = fetchWorld()

            val editor = Editor.importWwd(
                rezIndex = combinedRezIndex,
                wwdWorld = wwdWorldTemplate,
            )

            return App(
                wwdWorldTemplate = wwdWorldTemplate,
                rezIndex = combinedRezIndex,
                textureBank = textureBank,
                initialEditor = editor,
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
            rezIndex = rezIndex,
            wwdWorld = world,
        )

        return editor
    }

    private suspend fun loadProject(file: File): Editor {
        val projectDataString = file.text().await()

        val projectData = Json.decodeFromString<ProjectData>(projectDataString)

        val editor = Editor.loadProject(
            rezIndex = rezIndex,
            wwdWorldTemplate = wwdWorldTemplate,
            projectData = projectData,
        )

        return editor
    }
}

private fun Blob.arrayBuffer(): Promise<ArrayBuffer> =
    this.asDynamic().arrayBuffer() as Promise<ArrayBuffer>

private fun Blob.text(): Promise<String> =
    this.asDynamic().text() as Promise<String>
