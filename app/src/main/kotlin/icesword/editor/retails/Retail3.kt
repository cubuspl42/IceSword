package icesword.editor.retails

import icesword.ImageSetId
import icesword.editor.ElevatorPrototype
import icesword.editor.MetaTile
import icesword.editor.encode
import icesword.editor.knot_mesh.KnotStructurePattern
import icesword.editor.retails.retail3.retail3KnotStructurePatterns
import icesword.wwd.Wwd

object Retail3 : Retail(naturalIndex = 3), RetailLadderPrototype {
    override val ladderTop = MetaTile(668)

    override val ladder = MetaTile(669)

    override val ladderBottom = MetaTile(670)

    override val elevatorPrototype = ElevatorPrototype(
        elevatorImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL3_IMAGES_ELEVATOR1",
        ),
        wwdObjectPrototype = Wwd.Object_.empty().copy(
            logic = encode("Elevator"),
            imageSet = encode("LEVEL_ELEVATOR1"),
        ),
    )

    override val knotStructurePatterns: List<KnotStructurePattern> =
        retail3KnotStructurePatterns
}
