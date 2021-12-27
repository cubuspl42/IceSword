package icesword.editor

import fetchWorld
import icesword.JsonRezIndex
import icesword.editor.retails.Retail
import icesword.editor.retails.Retail6
import icesword.frp.Cell
import icesword.frp.DynamicLock
import icesword.frp.MutCell
import icesword.frp.Stream
import icesword.frp.StreamSink
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

sealed interface AppProcess

object CreatingNewProjectProcess : AppProcess

data class LoadingWorldProcess(
    val worldFilename: String,
) : AppProcess

interface NewProjectContext {
    fun createWithBase(retail: Retail)
}

class App(
    private val jsonRezIndex: JsonRezIndex,
    initialEditor: Editor,
) : CoroutineScope by MainScope() {
    companion object {
        suspend fun load(): App {
            val jsonRezIndex = JsonRezIndex.load()

            val wwdWorld: Wwd.World = fetchWorld(
                retail = Retail6,
            )

            val editor = Editor.importWwd(
                jsonRezIndex = jsonRezIndex,
                wwdWorld = wwdWorld,
            )

            return App(
                jsonRezIndex = jsonRezIndex,
                initialEditor = editor,
            )
        }
    }

    private val _editor = MutCell<Editor?>(initialEditor)

    val editor: Cell<Editor?>
        get() = _editor

    private val _appLock = DynamicLock<AppProcess>()

    val creatingNewProjectProcess: Cell<CreatingNewProjectProcess?> =
        _appLock.owningProcess.map { it as? CreatingNewProjectProcess }

    val loadingWorldProcess: Cell<LoadingWorldProcess?> =
        _appLock.owningProcess.map { it as? LoadingWorldProcess }

    val canLoadWorld: Cell<Boolean> = _appLock.isLocked.map { !it }

    private val _configureNewProject = StreamSink<NewProjectContext>()

    val configureNewProject: Stream<NewProjectContext>
        get() = _configureNewProject

    fun createNewProject() {
        _configureNewProject.send(
            object : NewProjectContext {
                override fun createWithBase(retail: Retail) {
                    launch {
                        _appLock.runNowIfFree(
                            process = CreatingNewProjectProcess,
                        ) {
                            setUpEditor {
                                Editor.createProject(
                                    jsonRezIndex = jsonRezIndex,
                                    retail = retail,
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    fun loadWorld(file: File) {
        launch {
            _appLock.runNow(
                process = LoadingWorldProcess(worldFilename = file.name),
            ) {
                if (file.name.endsWith(".json")) {
                    setUpEditor { loadProject(file = file) }
                } else {
                    setUpEditor { importWorld(file = file) }
                }
            }
        }
    }

    private suspend fun setUpEditor(load: suspend () -> Editor) {
        delay(100)

        val editor = load()

        _editor.set(editor)
    }

    private suspend fun importWorld(file: File): Editor {
        val worldBuffer = file.arrayBuffer().await()

        val world = Wwd.readWorld(worldBuffer)

        val editor = Editor.importWwd(
            jsonRezIndex = jsonRezIndex,
            wwdWorld = world,
        )

        return editor
    }

    private suspend fun loadProject(file: File): Editor {
        val projectDataString = file.text().await()

        val projectData = Json.decodeFromString<ProjectData>(projectDataString)

        val editor = Editor.loadProject(
            jsonRezIndex = jsonRezIndex,
            projectData = projectData,
        )

        return editor
    }
}

private fun Blob.arrayBuffer(): Promise<ArrayBuffer> =
    this.asDynamic().arrayBuffer() as Promise<ArrayBuffer>

private fun Blob.text(): Promise<String> =
    this.asDynamic().text() as Promise<String>
