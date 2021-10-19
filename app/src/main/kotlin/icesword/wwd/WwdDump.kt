package icesword.wwd

import icesword.wwd.DataStreamObj.ByteString
import icesword.wwd.Geometry.Rectangle
import icesword.wwd.OutputDataStream.OutputStream
import icesword.wwd.Wwd.Object_
import icesword.wwd.Wwd.Plane
import icesword.wwd.Wwd.TileDescription
import icesword.wwd.Wwd.World
import icesword.wwd.Wwd.WwdHeaderFlags
import icesword.wwd.Wwd.authorLength
import icesword.wwd.Wwd.birthLength
import icesword.wwd.Wwd.imageDirLength
import icesword.wwd.Wwd.imageSetLength
import icesword.wwd.Wwd.launchAppLength
import icesword.wwd.Wwd.levelNameLength
import icesword.wwd.Wwd.palRezLength
import icesword.wwd.Wwd.planeNameBufferSize
import icesword.wwd.Wwd.prefixLength
import icesword.wwd.Wwd.rezFileLength
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get


object DumpWwd {


    private val tileWidth = 64
    private val SIZEOF_INT = 4
    private val SIZEOF_NULL_BYTE = 1

    val WAP_WWD_HEADER_SIZE = 1524
    val WAP_WWD_PLANE_DESCRIPTION_SIZE = 160
    val WAP_WWD_OBJECT_DESCRIPTION_SIZE = 284

    val WAP_TILE_TYPE_SINGLE = 1
    val WAP_TILE_TYPE_DOUBLE = 2

    class WwdOutputStream(private val _stream: OutputStream) {

//    private fun writeNullByte(): Unit {
//      _stream.writeUint8(0)
////      dataOutputStream.write(NULL_BYTE.toInt())
//    }

        fun writeInt(i: Int): Unit {
            _stream.writeInt32(i)
        }

        fun writeBuffer(byteString: ByteString): Unit {
            _stream.writeByteString(byteString, byteString.length)
        }

        fun writeFixedString(byteString: ByteString): Unit {
            writeBuffer(byteString)
        }

        fun writeStaticString(byteString: ByteString, bufferSize: Int): Unit {
            _stream.writeByteString(byteString, bufferSize)
        }

        fun writeNullTerminatedString(byteString: ByteString): Unit {
            _stream.writeByteStringNullTerminated(byteString)

        }

        fun writeRect(r: Rectangle): Unit {
            writeInt(r.left)
            writeInt(r.top)
            writeInt(r.right)
            writeInt(r.bottom)
        }
    }

    class PlaneOffsets(
        val plane: Plane,
        var tilesOffset: Int = 0,
        var imageSetsOffset: Int = 0,
        var objectsOffset: Int = 0,
    )

    class WorldOffsets(
        val planeOffsets: List<PlaneOffsets>,
        var mainBlockOffset: Int = 0,
        var tileDescriptionsOffset: Int = 0,
        var decompressedMainBlockSize: Int = 0,
    )

    private fun calculateOffsets(wwd: World): WorldOffsets {
        val planes = wwd.planes

        val planesOffsets = planes.map { PlaneOffsets(it) }
        val worldOffsets = WorldOffsets(planesOffsets)

        var offset = WAP_WWD_HEADER_SIZE
        worldOffsets.mainBlockOffset = offset

        offset += wwd.planes.size * WAP_WWD_PLANE_DESCRIPTION_SIZE
        worldOffsets.tileDescriptionsOffset = offset


        //     for (p in planesOffsets) {
        //      p.tilesOffset = offset
        //      offset += p.tilesWide * p.tilesHigh * SIZEOF_INT // TODO: -> tiles.size ? + other similar stuff
        //      // TODO: Clone-on-dump + unserscores for "private" fields
        //    }

        planesOffsets.forEach { po ->
            val p = po.plane

            po.tilesOffset = offset
            offset += p.tilesWide * p.tilesHigh * SIZEOF_INT // TODO: -> tiles.size ? + other similar stuff
            // TODO: Clone-on-dump + unserscores for "private" fields
        }


//    for (p in planes) {
//      p.imageSetsOffset = offset
//      for (imageSet in p.imageSets) {
//        offset += imageSet.length + SIZEOF_NULL_BYTE
//      }
//    }


        planesOffsets.forEach { po ->
            val p = po.plane

            po.imageSetsOffset = offset

            p.imageSets.forEach { imageSet ->
                offset += imageSet.length + SIZEOF_NULL_BYTE
            }
        }

//    for (p in planes) {
//      p.objectsOffset = offset
//      for (o in p.objects) {
//        offset += WAP_WWD_OBJECT_DESCRIPTION_SIZE
//        offset += o.name.length + o.logic.length + o.imageSet.length + o.animation.length
//      }
//    }

        planesOffsets.forEach { po ->
            val p = po.plane

            po.objectsOffset = offset

            p.objects.forEach { o ->
                offset += WAP_WWD_OBJECT_DESCRIPTION_SIZE
                offset += o.name.length + o.logic.length + o.imageSet.length + o.animation.length
            }
        }

        worldOffsets.tileDescriptionsOffset = offset
        offset += 8 * SIZEOF_INT

//    for (td in wwd.tileDescriptions) {
//      offset += when (td.type) {
//        WAP_TILE_TYPE_SINGLE -> 5 * SIZEOF_INT
//        else -> 10 * SIZEOF_INT
//      }
//    }

        wwd.tileDescriptions.forEach { td ->
            offset +=
                if (td._type == WAP_TILE_TYPE_SINGLE) 5 * SIZEOF_INT
                else 10 * SIZEOF_INT
        }

//    for (td in wwd.tileDescriptions) {
//      offset += when (td.type) {
//        WAP_TILE_TYPE_SINGLE -> 5 * SIZEOF_INT
//        else -> 10 * SIZEOF_INT
//      }
//    }

        worldOffsets.decompressedMainBlockSize = offset - WAP_WWD_HEADER_SIZE

     return   worldOffsets
    }

    private fun compress(buffer: ArrayBuffer): Uint8Array =
        Pako.deflate(Uint8Array(buffer))

    fun dumpWwd(outputStream: OutputStream, wwd: World) {
        val wwdOutputStream = WwdOutputStream(outputStream)
        val header = wwd

        val worldOffsets = calculateOffsets(wwd)
        dumpWwdHeader(wwdOutputStream, wwd, worldOffsets)

        if ((header.flags and WwdHeaderFlags.COMPRESS) != 0) {
            val mainBlockStream = OutputStream()
            dumpMainBlock(WwdOutputStream(mainBlockStream), wwd, worldOffsets)
            val mainBlockBuffer = mainBlockStream.toArrayBuffer()
            val compressedMainBlockBuffer = compress(mainBlockBuffer)
            wwdOutputStream.writeBuffer(ByteString(compressedMainBlockBuffer))
        } else {
            dumpMainBlock(wwdOutputStream, wwd, worldOffsets)
        }
    }

    fun dumpWwdHeader(stream: WwdOutputStream, wwd: World, worldOffsets: WorldOffsets) {
        val header = wwd

        stream.writeInt(WAP_WWD_HEADER_SIZE)
        stream.writeInt(0)
        stream.writeInt(header.flags)
        stream.writeInt(0)
        stream.writeStaticString(header.name, levelNameLength)
        stream.writeStaticString(header.author, authorLength)
        stream.writeStaticString(header.dateCreatedString, birthLength)
        stream.writeStaticString(header.rezFilePath, rezFileLength)
        stream.writeStaticString(header.imageDir, imageDirLength)
        stream.writeStaticString(header.palRez, palRezLength)
        stream.writeInt(header.startX)
        stream.writeInt(header.startY)
        stream.writeInt(0)
        stream.writeInt(wwd.planes.size)
        stream.writeInt(WAP_WWD_HEADER_SIZE) // TODO: Shouldn't it be plane header size?
        stream.writeInt(worldOffsets.tileDescriptionsOffset)
        stream.writeInt(worldOffsets.decompressedMainBlockSize)
        stream.writeInt(0) // TODO: Calculate checksum
        stream.writeInt(0)
        stream.writeStaticString(header.launchApp, launchAppLength)
        stream.writeStaticString(header.imageSet1, imageSetLength)
        stream.writeStaticString(header.imageSet2, imageSetLength)
        stream.writeStaticString(header.imageSet3, imageSetLength)
        stream.writeStaticString(header.imageSet4, imageSetLength)
        stream.writeStaticString(header.prefix1, prefixLength)
        stream.writeStaticString(header.prefix2, prefixLength)
        stream.writeStaticString(header.prefix3, prefixLength)
        stream.writeStaticString(header.prefix4, prefixLength)
    }

    fun dumpMainBlock(stream: WwdOutputStream, wwd: World, worldOffsets: WorldOffsets) {
        dumpPlanes(stream, worldOffsets.planeOffsets)
        dumpTileDescriptions(stream, wwd.tileDescriptions)
    }

    fun dumpPlanes(stream: WwdOutputStream, planes: List<PlaneOffsets>) {
        planes.forEach { p-> dumpPlaneHeader (stream, p) }
        planes.forEach { p-> dumpTiles (stream, p.plane) }
        planes.forEach { p-> dumpImageSets (stream, p.plane.imageSets) }
        planes.forEach { p-> dumpObjects (stream, p.plane.objects) }
    }

    fun dumpPlaneHeader(stream: WwdOutputStream, planeOffsets: PlaneOffsets) {
        val plane = planeOffsets.plane

        stream.writeInt(WAP_WWD_PLANE_DESCRIPTION_SIZE)
        stream.writeInt(0)
        stream.writeInt(plane.flags)
        stream.writeInt(0)
        stream.writeStaticString(plane.name, planeNameBufferSize)
        stream.writeInt(plane.tilesWide * tileWidth)
        stream.writeInt(plane.tilesHigh * tileWidth)
        stream.writeInt(plane.tileWidth)
        stream.writeInt(plane.tileHeight)
        stream.writeInt(plane.tilesWide)
        stream.writeInt(plane.tilesHigh)
        stream.writeInt(0)
        stream.writeInt(0)
        stream.writeInt(plane.movementXPercent)
        stream.writeInt(plane.movementYPercent)
        stream.writeInt(plane.fillColor)
        stream.writeInt(plane.imageSets.size)
        stream.writeInt(plane.objects.size)
        stream.writeInt(planeOffsets.tilesOffset)
        stream.writeInt(planeOffsets.imageSetsOffset)
        stream.writeInt(planeOffsets.objectsOffset)
        stream.writeInt(plane.zCoord)
        stream.writeInt(0)
        stream.writeInt(0)
        stream.writeInt(0)
    }

    fun dumpTiles(stream: WwdOutputStream, plane: Plane) {
        var k = 0
        for (i in 0 until plane.tilesHigh) {
            for (j in 0 until plane.tilesWide) {
                val t = plane.tiles[k++]
                stream.writeInt(t)
            }
        }
    }

    fun dumpImageSets(stream: WwdOutputStream, imageSets: List<ByteString>) {
        imageSets.forEach { stream.writeNullTerminatedString(it) }
    }

    fun dumpObjects(stream: WwdOutputStream, objects: List<Object_>) {
        objects.forEach { dumpObject(stream, it) }
    }

    fun dumpObject(stream: WwdOutputStream, obj: Object_) {
        stream.writeInt(obj.id)
        stream.writeInt(obj.name.length)
        stream.writeInt(obj.logic.length)
        stream.writeInt(obj.imageSet.length)
        stream.writeInt(obj.animation.length)

        stream.writeInt(obj.x)
        stream.writeInt(obj.y)
        stream.writeInt(obj.z)
        stream.writeInt(obj.i)
        stream.writeInt(obj.addFlags)
        stream.writeInt(obj.dynamicFlags)
        stream.writeInt(obj.drawFlags)
        stream.writeInt(obj.userFlags)
        stream.writeInt(obj.score)
        stream.writeInt(obj.points)
        stream.writeInt(obj.powerUp)
        stream.writeInt(obj.damage)
        stream.writeInt(obj.smarts)
        stream.writeInt(obj.health)
        stream.writeRect(obj.moveRect)
        stream.writeRect(obj.hitRect)
        stream.writeRect(obj.attackRect)
        stream.writeRect(obj.clipRect)
        stream.writeRect(obj.userRect1)
        stream.writeRect(obj.userRect2)
        stream.writeInt(obj.userValue1)
        stream.writeInt(obj.userValue2)
        stream.writeInt(obj.userValue3)
        stream.writeInt(obj.userValue4)
        stream.writeInt(obj.userValue5)
        stream.writeInt(obj.userValue6)
        stream.writeInt(obj.userValue7)
        stream.writeInt(obj.userValue8)
        stream.writeInt(obj.xMin)
        stream.writeInt(obj.yMin)
        stream.writeInt(obj.xMax)
        stream.writeInt(obj.yMax)
        stream.writeInt(obj.speedX)
        stream.writeInt(obj.speedY)
        stream.writeInt(obj.xTweak)
        stream.writeInt(obj.yTweak)
        stream.writeInt(obj.counter)
        stream.writeInt(obj.speed)
        stream.writeInt(obj.width)
        stream.writeInt(obj.height)
        stream.writeInt(obj.direction)
        stream.writeInt(obj.faceDir)
        stream.writeInt(obj.timeDelay)
        stream.writeInt(obj.frameDelay)
        stream.writeInt(obj.objectType)
        stream.writeInt(obj.hitTypeFlags)
        stream.writeInt(obj.xMoveRes)
        stream.writeInt(obj.yMoveRes)

        stream.writeFixedString(obj.name)
        stream.writeFixedString(obj.logic)
        stream.writeFixedString(obj.imageSet)
        stream.writeFixedString(obj.animation)
    }

    fun dumpTileDescriptions(stream: WwdOutputStream, tileDescriptions: List<TileDescription>) {
        stream.writeInt(32)
        stream.writeInt(0)
        stream.writeInt(tileDescriptions.size)
        stream.writeInt(0)
        stream.writeInt(0)
        stream.writeInt(0)
        stream.writeInt(0)
        stream.writeInt(0)

        tileDescriptions.forEach {
            dumpTileDescription(stream, it)
        }
    }

    fun dumpTileDescription(stream: WwdOutputStream, td: TileDescription) {
        stream.writeInt(td._type)
        stream.writeInt(0)
        stream.writeInt(td.width)
        stream.writeInt(td.height)

        if (td._type == WAP_TILE_TYPE_SINGLE) {
            stream.writeInt(td.insideAttrib)
        } else {
            stream.writeInt(td.outsideAttr)
            stream.writeInt(td.insideAttrib)
            stream.writeRect(td.rect)
        }
    }
}
