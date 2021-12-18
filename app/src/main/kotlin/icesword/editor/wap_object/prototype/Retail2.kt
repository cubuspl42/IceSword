package icesword.editor.wap_object.prototype

import icesword.ImageSetId
import icesword.editor.encode
import icesword.wwd.Wwd
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
object Retail2Health : WapObjectPrototype() {
    @Transient
    override val imageSetId: ImageSetId = ImageSetId(
        fullyQualifiedId = "LEVEL2_IMAGES_HEALTH",
    )

    @Transient
    override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
        logic = encode("HealthPowerup"),
        imageSet = encode("LEVEL_HEALTH"),
    )
}

@Serializable
object Retail2PowderKeg : WapObjectPrototype() {
    @Transient
    override val imageSetId: ImageSetId = ImageSetId(
        fullyQualifiedId = "LEVEL2_IMAGES_POWDERKEG",
    )

    @Transient
    override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
        logic = encode("Powderkeg"),
        imageSet = encode("LEVEL_POWDERKEG"),
    )
}

@Serializable
object Retail2SoldierPrototype : WapObjectPrototype() {
    @Transient
    override val imageSetId: ImageSetId = ImageSetId(
        fullyQualifiedId = "LEVEL2_IMAGES_SOLDIER",
    )

    @Transient
    override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
        logic = encode("Soldier"),
        imageSet = encode("LEVEL_SOLDIER"),
    )
}

@Serializable
object Retail2OfficerPrototype : WapObjectPrototype() {
    @Transient
    override val imageSetId: ImageSetId = ImageSetId(
        fullyQualifiedId = "LEVEL2_IMAGES_OFFICER",
    )

    @Transient
    override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
        logic = encode("Officer"),
        imageSet = encode("LEVEL_OFFICER"),
    )
}

@Serializable
object LaRauxPrototype : WapObjectPrototype() {
    @Transient
    override val imageSetId: ImageSetId = ImageSetId(
        fullyQualifiedId = "LEVEL2_IMAGES_RAUX",
    )

    @Transient
    override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
        logic = encode("Raux"),
        imageSet = encode("LEVEL_RAUX"),
    )
}

@Serializable
object PunkRatCannon : WapObjectPrototype() {
    @Transient
    override val imageSetId: ImageSetId = ImageSetId(
        fullyQualifiedId = "LEVEL2_IMAGES_PUNKRAT",
    )

    @Transient
    override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
        logic = encode("PunkRat"),
        imageSet = encode("LEVEL_PUNKRAT"),
    )
}
