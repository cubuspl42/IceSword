import icesword.createAppView
import icesword.editor.App
import icesword.frp.Till
import icesword.wwd.Wwd
import icesword.wwd.Wwd.readWorld
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
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

@OptIn(DelicateCoroutinesApi::class)
fun main() {
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

        overflowX = "hidden"
        overflowY = "hidden"

        display = "flex"
    }

    GlobalScope.launch {
        val app = App.load()

        root.appendChild(
            createAppView(
                app = app,
                tillDetach = Till.never,
            ).apply {
                style.apply {
                    flex = "1"
                }
            }
        )
    }
}
