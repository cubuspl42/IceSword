import icesword.World
import icesword.worldView
import kotlinx.browser.document

fun main() {
    val world = World()

    document.body?.apply {
        style.margin = "0px";
        style.padding = "0px";
    }

    val root = document.getElementById("root")!!
    root.appendChild(worldView(world))
}
