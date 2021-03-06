import icesword.ui.createAppView
import icesword.editor.App
import icesword.editor.retails.Retail
import icesword.frp.Till
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

data class TilesetSpritesheetReference(
    val imagePath: String,
    val indexPath: String,
)

fun buildTilesetSpritesheetReference(retail: Retail): TilesetSpritesheetReference {
    return TilesetSpritesheetReference(
        imagePath = "images/spritesheets/LEVEL${retail.naturalIndex}_ACTION/texture.png",
        indexPath = "images/spritesheets/LEVEL${retail.naturalIndex}_ACTION/texture.json",
    )
}

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

suspend fun fetchWorld(retail: Retail): Wwd.World {
    val infix = retail.naturalIndex.toString().padStart(2, '0')
    val response = window.fetch("worlds/RETAIL$infix.WWD").await()
    val worldBuffer = response.arrayBuffer().await()
    val world = readWorld(worldBuffer)

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

        overflowX = "hidden"
        overflowY = "hidden"

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
