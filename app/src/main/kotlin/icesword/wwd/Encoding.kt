package icesword.wwd

import org.khronos.webgl.Uint8Array

external class TextDecoder(
    utfLabel: String,
) {
    fun decode(buffer: Uint8Array): String
}

external class TextEncoder(
    utfLabel: String,
) {
    fun encode(string: String): Uint8Array
}

private val decoder = TextDecoder("utf-8")
private val encoder = TextEncoder("utf-8")

fun decode(buffer: Uint8Array): String =
    decoder.decode(buffer)

fun encodeString(string: String): Uint8Array =
    encoder.encode(string)
