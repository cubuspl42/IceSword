import icesword.editor.*
import icesword.geometry.IntVec2

object RockKnotFormulas {
    //         relativeKnots.contains(intVec2(0, 0)) &&
    //        !relativeKnots.contains(intVec2(-1, -1)) &&
    //        !relativeKnots.contains(intVec2(-1, 0)) &&
    //        !relativeKnots.contains(intVec2(0, -1))

//    fun foo () {
//
//        return if (
//            relativeKnots.contains(intVec2(0, 0)) &&
//            !relativeKnots.contains(intVec2(-1, -1)) &&
//            !relativeKnots.contains(intVec2(-1, 0)) &&
//            !relativeKnots.contains(intVec2(0, -1))
//        ) 620
//

    private object Formula620 : KnotFormula {
        override fun buildMetaTile(relativeKnots: Map<IntVec2, KnotPrototype>): Int? = if (
            relativeKnots[IntVec2(0, 0)] is UndergroundRockPrototype &&
            !relativeKnots.contains(IntVec2(-1, -1)) &&
            !relativeKnots.contains(IntVec2(0, -1)) &&
            !relativeKnots.contains(IntVec2(-1, 0))
        ) 620 else null
    }


//
//        else if (
//            relativeKnots.contains(intVec2(0, 0)) &&
//            relativeKnots.contains(intVec2(0, -1)) &&
//            !relativeKnots.contains(intVec2(-1, 0)) &&
//            !relativeKnots.contains(intVec2(-1, -1)) &&
//            !relativeKnots.contains(intVec2(0, -2))
//        ) 621

    private object Formula621 : KnotFormula {
        override fun buildMetaTile(relativeKnots: Map<IntVec2, KnotPrototype>): Int? = if (
            relativeKnots[IntVec2(0, 0)] is UndergroundRockPrototype &&
            relativeKnots[IntVec2(-1, 0)] is UndergroundRockPrototype &&
            !relativeKnots.contains(IntVec2(0, -1)) &&
            !relativeKnots.contains(IntVec2(-1, -1)) &&
            !relativeKnots.contains(IntVec2(-2, 0))
        ) 620 else null
    }


//        if (
//            relativeKnots.contains(intVec2(0, 0)) &&
//            relativeKnots.contains(intVec2(0, -1)) &&
//            !relativeKnots.contains(intVec2(-1, 0)) &&
//            !relativeKnots.contains(intVec2(-1, -1))
//        ) 622

//        if (
//            relativeKnots.contains(intVec2(0, 0)) &&
//            relativeKnots.contains(intVec2(-1, 0)) &&
//            !relativeKnots.contains(intVec2(0, -1)) &&
//            !relativeKnots.contains(intVec2(-1, -1))
//        ) 624

//        if (
//            relativeKnots.contains(intVec2(-1, -1)) &&
//            relativeKnots.contains(intVec2(-1, 0)) &&
//            relativeKnots.contains(intVec2(0, -1)) &&
//            relativeKnots.contains(intVec2(0, 0)) &&
//            !relativeKnots.contains(intVec2(-1, -2)) &&
//            !relativeKnots.contains(intVec2(0, -2))
//        ) 613

//        if (
//            relativeKnots.contains(intVec2(-1, 0)) &&
//            !relativeKnots.contains(intVec2(-1, -1)) &&
//            !relativeKnots.contains(intVec2(0, -1)) &&
//            !relativeKnots.contains(intVec2(0, 0))
//        ) 632

//        if (
//            relativeKnots.contains(intVec2(-1, -1)) &&
//            relativeKnots.contains(intVec2(-1, 0)) &&
//            !relativeKnots.contains(intVec2(-1, -2)) &&
//            !relativeKnots.contains(intVec2(0, -2)) &&
//            !relativeKnots.contains(intVec2(0, -1)) &&
//            !relativeKnots.contains(intVec2(0, 0))
//        ) 633

//        if (
//            relativeKnots.contains(intVec2(-1, -1)) &&
//            relativeKnots.contains(intVec2(-1, 0)) &&
//            relativeKnots.contains(intVec2(-1, -2)) &&
//            !relativeKnots.contains(intVec2(0, -1)) &&
//            !relativeKnots.contains(intVec2(0, 0))
//        ) 634

//        if (
//            relativeKnots.contains(intVec2(-1, -1)) &&
//            !relativeKnots.contains(intVec2(0, -1)) &&
//            !relativeKnots.contains(intVec2(0, 0)) &&
//            !relativeKnots.contains(intVec2(-1, 0))
//        ) 635

//        if (
//            relativeKnots.contains(intVec2(-1, -1)) &&
//            relativeKnots.contains(intVec2(0, -1)) &&
//            !relativeKnots.contains(intVec2(-1, 0)) &&
//            !relativeKnots.contains(intVec2(0, 0))
//        ) 615

//        if (
//            relativeKnots.contains(intVec2(0, -1)) &&
//            !relativeKnots.contains(intVec2(-1, -1)) &&
//            !relativeKnots.contains(intVec2(-1, 0)) &&
//            !relativeKnots.contains(intVec2(0, 0))
//        ) 623

//        if (
//            relativeKnots.contains(intVec2(0, -1)) &&
//            relativeKnots.contains(intVec2(-1, 0)) &&
//            relativeKnots.contains(intVec2(-1, -1)) &&
//            !relativeKnots.contains(intVec2(0, 0))
//        ) 617

//        if (
//            relativeKnots.contains(intVec2(0, 0)) &&
//            relativeKnots.contains(intVec2(-1, 0)) &&
//            relativeKnots.contains(intVec2(-1, -1)) &&
//            !relativeKnots.contains(intVec2(0, -1))
//        ) 618
//
    //
//         630
//    }


}