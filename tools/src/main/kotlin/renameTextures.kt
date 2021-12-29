import java.io.File

private fun rename(file: File, newName: String) {
    val path = file.toPath()
    file.renameTo(path.parent.resolve(newName).toFile())
}

fun main() {
    val rootPath = "/Users/kuba/Projects/IceSword/app/src/main/resources/images/spritesheets"

    File(rootPath).walk().forEach { file ->
        if (file.name == "texture.png") {
            rename(file, "texture-0.png")
        }

        if (file.name == "texture.json") {
            rename(file, "texture-0.json")
        }
    }
}
