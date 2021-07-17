import icesword.World
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
        val wwdWorld = fetchWorld()
        val world = World.load(wwdWorld)
        root.appendChild(worldView(world))
    }
}
