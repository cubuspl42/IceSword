package icesword.editor.retails

import icesword.ImageSetId
import icesword.editor.ElevatorPrototype
import icesword.editor.MetaTile
import icesword.editor.encode
import icesword.editor.knot_mesh.StructurePattern
import icesword.editor.retails.retail1.retail1StructurePatterns
import icesword.wwd.Wwd

object Retail1 : Retail(naturalIndex = 1), RetailLadderPrototype {
    override val ladderTop = MetaTile(310)

    override val ladder = MetaTile(311)

    override val ladderBottom = MetaTile(312)

    override val elevatorPrototype = ElevatorPrototype(
        elevatorImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL1_IMAGES_ELEVATORS",
        ),
        wwdObjectPrototype = Wwd.Object_.empty().copy(
            logic = encode("Elevator"),
            imageSet = encode("LEVEL_ELEVATORS"),
        ),
    )

    override val structurePatterns: List<StructurePattern> =
        retail1StructurePatterns
}
