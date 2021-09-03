import org.khronos.webgl.Uint8Array

external interface Uint8ArrayExt {
    fun indexOf(b: Byte): Int

    fun slice(start: Int, end: Int): Uint8Array
}

fun Uint8Array.slice(start: Int, end: Int): Uint8Array =
    this.unsafeCast<Uint8ArrayExt>().slice(start, end)

fun Uint8Array.indexOf(b: Byte): Int =
    this.unsafeCast<Uint8ArrayExt>().indexOf(b)