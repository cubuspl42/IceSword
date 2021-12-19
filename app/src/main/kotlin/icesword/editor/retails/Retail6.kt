package icesword.editor.retails

import icesword.editor.MetaTile

object Retail6 : Retail(naturalIndex = 6) {
    object MetaTiles {
        object Fence {
            val top = MetaTile(22)

            val bottomLeft = MetaTile(89)

            val bottomCenter = MetaTile(27)

            val bottomRightInner = MetaTile(99)

            val bottomRightOuter = MetaTile(100)
        }

        object HorizontalRoof {
            val topLeft = MetaTile(38)

            val topCenter = MetaTile(39)

            val topRightInner = MetaTile(40)

            val topRightOuter = MetaTile(41)

            val bottomLeft = MetaTile(44)

            val bottomCenter = MetaTile(42)

            val bottomRightInner = MetaTile(46)

            val bottomRightOuter = MetaTile(47)
        }

        object Ladder : LadderPattern {
            override val top = MetaTile(16)

            override val center = MetaTile(18)

            override val bottom = MetaTile(20)
        }
    }
}
