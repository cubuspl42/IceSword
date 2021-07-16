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


suspend fun fetchWorld(): Wwd.World {
    val response = window.fetch("worlds/WORLD.WWD").await()
    val worldBuffer = response.arrayBuffer().await()
    val world = readWorld(worldBuffer)

    println("World name: ${world.name.decode()}")

    return world
}

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    document.body?.apply {
        style.margin = "0px";
        style.padding = "0px";
    }

    val root = document.getElementById("root")!!

    GlobalScope.launch {
        val wwdWorld = fetchWorld()
        val world = World.load(wwdWorld)
        root.appendChild(worldView(world))
    }
}
