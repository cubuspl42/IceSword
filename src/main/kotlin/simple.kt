import icesword.editor.Editor
import icesword.editor.World
import icesword.editorView
import icesword.geometry.IntRect
import icesword.geometry.IntSize
import icesword.geometry.IntVec2
import icesword.scene.Texture
import icesword.scene.Tileset
import icesword.frp.Till
import icesword.worldView
import icesword.wwd.Wwd
import icesword.wwd.Wwd.readWorld
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.ImageBitmap

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

suspend fun loadTileset(): Tileset {
    suspend fun loadImage(): ImageBitmap {
        val textureImageResponse = window.fetch(textureImagePath).await()
        val textureImageBlob = textureImageResponse.blob().await()
        return window.createImageBitmap(textureImageBlob).await()
    }

    fun parseIndex(
        indexJson: dynamic,
    ): Map<Int, IntRect> {
        val frames = mapOfObject(indexJson.frames)

        return frames.map { (frameIdStr, frame) ->
            val frameId = frameIdStr.toInt()

            val frameRectJson = frame.asDynamic().frame
            val frameRect = IntRect(
                IntVec2(
                    frameRectJson.x as Int,
                    frameRectJson.y as Int,
                ),
                IntSize(
                    frameRectJson.w as Int,
                    frameRectJson.h as Int,
                )
            )

            frameId to frameRect
        }.toMap()
    }

    suspend fun loadIndex(): Map<Int, IntRect> {
        val textureIndexResponse = window.fetch(textureIndexPath).await()
        val json = textureIndexResponse.json().await().asDynamic()
        return parseIndex(json)
    }

    val imageBitmap = loadImage()

    val index = loadIndex()

    val tileTextures = index.mapValues { (_, frameRect) ->
        Texture(imageBitmap, frameRect)
    }

    return Tileset(
        tileTextures = tileTextures,
    )
}


suspend fun fetchWorld(): Wwd.World {
    val response = window.fetch("worlds/WORLD.WWD").await()
    val worldBuffer = response.arrayBuffer().await()
    val world = readWorld(worldBuffer)

    println("World name: ${world.name.decode()}")

    return world
}

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    document.body?.style?.apply {
        margin = "0px"
        padding = "0px"

        width = "100vw"
        height = "100vh"

        asDynamic().overscrollBehavior = "none"
    }

    val root = document.getElementById("root")!! as HTMLElement

    root.style.apply {
        position = "relative"
        width = "100%"
        height = "100%"

        overflowX = "hidden"
        overflowY = "hidden"
    }

    GlobalScope.launch {
        val editor = Editor.load()

        root.appendChild(
            editorView(
                editor = editor,
                tillDetach = Till.never,
            )
        )
    }
}
