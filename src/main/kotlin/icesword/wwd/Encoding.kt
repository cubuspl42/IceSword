package icesword.wwd

import org.khronos.webgl.Uint8Array

external class TextDecoder(
    utfLabel: String,
) {
    fun decode(buffer: Uint8Array): String
}

private val decoder = TextDecoder("utf-8")

fun decode(buffer: Uint8Array): String =
    decoder.decode(buffer)
