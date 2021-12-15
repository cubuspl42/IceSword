package icesword.editor.retails

import icesword.ImageSetId
import icesword.editor.ElevatorPrototype
import icesword.editor.encode
import icesword.editor.knot_mesh.KnotStructurePattern
import icesword.editor.retails.retail1.retail1KnotStructurePatterns
import icesword.editor.retails.retail1.retail1LadderPattern
import icesword.wwd.Wwd

object Retail1 : Retail(naturalIndex = 1), RetailLadderPrototype {
    override val ladderGenerator = retail1LadderPattern.toElasticGenerator()

    override val elevatorPrototype = ElevatorPrototype(
        elevatorImageSetId = ImageSetId(
            fullyQualifiedId = "LEVEL1_IMAGES_ELEVATORS",
        ),
        wwdObjectPrototype = Wwd.Object_.empty().copy(
            logic = encode("Elevator"),
            imageSet = encode("LEVEL_ELEVATORS"),
        ),
    )

    override val knotStructurePatterns: List<KnotStructurePattern> =
        retail1KnotStructurePatterns
}
