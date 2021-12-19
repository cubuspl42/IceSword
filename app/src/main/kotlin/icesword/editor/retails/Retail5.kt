package icesword.editor.retails

import icesword.editor.MetaTile
import icesword.editor.TileGenerator
import icesword.editor.TileGeneratorContext

private val retailTileGenerator = object : TileGenerator {
    override fun buildTile(context: TileGeneratorContext): Int? = context.run {
        when {
            // Metal platform / ladder
            containsAll(Retail5.MetaTiles.MetalPlatform.topCenter, Retail5.MetaTiles.Ladder.top) -> 204
            containsAll(Retail5.MetaTiles.MetalPlatform.bottomCenter, Retail5.MetaTiles.Ladder.center) -> 221

            else -> null
        }
    }
}

object Retail5 : Retail(naturalIndex = 5) {
    object MetaTiles {
        object MetalPlatform {
            val topLeftOuter = MetaTile(201)

            val topLeftInner = MetaTile(202)

            val topCenter = MetaTile(203)

            val topRightInner = MetaTile(205)

            val topRightOuter = MetaTile(206)

            val bottomLeft = MetaTile(207)

            val bottomCenter = MetaTile(219)

            val bottomRight = MetaTile(222)
        }

        object Ladder : LadderPattern {
            override val top = MetaTile(516)

            override val center = MetaTile(231)

            override val bottom = MetaTile(498)
        }
    }

    override val tileGenerator: TileGenerator =
        retailTileGenerator
}
