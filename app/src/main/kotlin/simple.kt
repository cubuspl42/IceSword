import icesword.JsonRezIndex
import icesword.createAppView
import icesword.createEditFloorSpikeRowDialog
import icesword.editor.App
import icesword.editor.FloorSpikeRow
import icesword.frp.Till
import icesword.geometry.IntVec2
import icesword.wwd.Wwd
import icesword.wwd.Wwd.readWorld
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.dom.Document
import org.w3c.dom.HTMLElement

const val textureImagePath = "images/spritesheets/LEVEL3_ACTION/texture.png"
const val textureIndexPath = "images/spritesheets/LEVEL3_ACTION/texture.json"

val objectEntries = js("Object.entries") as (dynamic) -> Array<Array<Any?>>

fun mapOfObject(jsObject: dynamic): Map<String, Any?> {
    val entries = objectEntries(jsObject)
        .map { entry -> entry[0] as String to entry[1] }
    return entries.toMap()
}

external interface FrameIndex {
    val frames: Map<String, Frame>
}

external interface Frame {
    val frame: FrameRect
}

external interface FrameRect {
    val x: Int
    val y: Int
}


suspend fun fetchWorld(): Wwd.World {
    val response = window.fetch("worlds/WORLD.WWD").await()
    val worldBuffer = response.arrayBuffer().await()
    val world = readWorld(worldBuffer)

//    println("World name: ${world.name.decode()}")

    return world
}

private fun run() {
    // A dirty hack to make `nodeTest` work (why does it invoke main?)
    fun getDocument(): Document? = try {
        document
    } catch (e: Throwable) {
        null
    }

    val document = getDocument() ?: return

    document.body?.style?.apply {
        width = "100vw"
        height = "100vh"

        margin = "0px"
        padding = "0px"

        asDynamic().overscrollBehavior = "none"
    }

    val root = document.getElementById("root")!! as HTMLElement

    root.style.apply {
        width = "100vw"
        height = "100vh"

//        overflow = "hidden"

        display = "grid"
        setProperty("grid-template-columns", "minmax(0, 1fr)")
        setProperty("grid-template-rows", "minmax(0, 1fr)")
    }

    GlobalScope.launch {
        val app = App.load()

        root.appendChild(
            createAppView(
                app = app,
                tillDetach = Till.never,
            )
        )
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun main() {
//    GlobalScope.launch { test() }

    run()
}
