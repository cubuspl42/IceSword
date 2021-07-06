import icesword.World
import icesword.worldView
import kotlinx.browser.document

fun main() {
    val world = World()

    val root = document.getElementById("root")!!
    root.appendChild(worldView(world))
}
