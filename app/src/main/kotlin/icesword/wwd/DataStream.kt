package icesword.wwd

//import firesword.base.TextDecoder.decoder
//import firesword.wwd.Geometry.Rectangle

import icesword.wwd.Geometry.Rectangle
import indexOf
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView
import org.khronos.webgl.Uint8Array
import slice


object DataStreamObj {
//  @js.native
//  trait Uint8ArrayExt extends Uint8Array {
//    fun indexOf(searchElement: Short): Int
//
//    fun slice(start: Int, end: Int): Uint8Array
//  }

//  implicit fun implicitUint8ArrayExt(self: Uint8Array): Uint8ArrayExt =
//    self.asInstanceOf[Uint8ArrayExt]


    class ByteString(
        val byteArray: Uint8Array,
    ) {
        companion object {
            fun empty(): ByteString =
                ByteString(Uint8Array(0))

            fun decode(b: ByteString): String =
                b.decode()

        }

        val length get() = byteArray.length

        override fun toString(): String = decode(byteArray)

        fun decode(): String =
            decode(byteArray)
    }


    class DataStream(
        private val _arrayBuffer: ArrayBuffer,
        initialOffset: Int = 0,
    ) {

        private val _dataView = DataView(_arrayBuffer)

        private var _offset = initialOffset

        private val _littleEndian = true


        fun readInt32(): Int {
            val value = this._dataView.getInt32(this._offset, this._littleEndian)
            this._offset += 4
            return value
        }

        fun expectInt32(i: Int) {
            readInt32()
        }

        fun readUint32(): Long {
            val value = this._dataView.getUint32(this._offset, this._littleEndian).toLong()
            this._offset += 4
            return value
        }

        fun readByteString(length: Int): ByteString {
            val fullByteString = Uint8Array(this._arrayBuffer, this._offset, length)
            val firstZeroIndex = fullByteString.indexOf(0)
            val byteString: Uint8Array = if (firstZeroIndex >= 0)
                fullByteString.slice(0, firstZeroIndex) else
                fullByteString
            this._offset += length
            return ByteString(byteString)
        }

        fun readByteStringNullTerminated(): ByteString {
            val bytes = mutableListOf<Byte>()

            fun readByte() {
                val byte = this._dataView.getUint8(this._offset)
                this._offset += 1
                if (byte != 0.toByte()) {
                    bytes.add(byte)
                    readByte()
                }
            }

            readByte()

            return ByteString(Uint8Array(bytes.toTypedArray()))
        }

        fun readRectangle(): Rectangle {
            return Rectangle.fromBounds(
                left = this.readInt32(),
                top = this.readInt32(),
                right = this.readInt32(),
                bottom = this.readInt32(),
            )
        }
    }

}

