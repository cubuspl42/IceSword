package icesword.wwd

import icesword.wwd.DataStreamObj.ByteString
import indexOf
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get


object OutputDataStream {

    class OutputStream() {
        private val _arrayBuffer = ArrayBuffer(2 shl 24) // 16 MiB

        private val _dataView = DataView(_arrayBuffer)

        private var _offset = 0

        private val _littleEndian = true

        fun writeUint8(value: Byte) {
            this._dataView.setUint8(this._offset, value)
            this._offset += 1
        }

        fun writeInt32(value: Int) {
            this._dataView.setInt32(this._offset, value, this._littleEndian)
            this._offset += 4
        }

        fun writeUint32(value: UInt) {
            this._dataView.setUint32(this._offset, value.toInt(), this._littleEndian)
            this._offset += 4
        }

        fun writeByteString(byteString: ByteString, length: Int) {
            val byteArray = byteString.byteArray
            val subArray = byteArray.subarray(0, length)

            (0 until subArray.length).forEach { i ->
                val byte = subArray.get(i)
                _dataView.setUint8(_offset, byte)
                _offset += 1

            }

            _offset += (length - subArray.length)
        }

        fun writeByteStringNullTerminated(byteString: ByteString) {
            val byteArray = byteString.byteArray

            if (byteArray.contains(0)) {
                throw  IllegalArgumentException("byteString contains NUL character(s)")
            }

            byteArray.forEach { byte: Byte ->
                _dataView.setUint8(_offset, byte)
                _offset += 1
            }

            _dataView.setUint8(_offset, 0)
            _offset += 1
        }

        fun toArrayBuffer(): ArrayBuffer =
            _arrayBuffer.slice(0, _offset)
    }
}

private fun Uint8Array.contains(byte: Byte): Boolean =
    this.indexOf(byte) > 0

private fun Uint8Array.forEach(f: (Byte) -> Unit) {
    (0 until this.length).forEach { i ->
        val byte = this[i]
        f(byte)
    }
}



