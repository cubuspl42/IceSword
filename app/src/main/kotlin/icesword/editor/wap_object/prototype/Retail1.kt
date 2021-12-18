package icesword.editor.wap_object.prototype

import icesword.ImageSetId
import icesword.editor.encode
import icesword.wwd.Wwd
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
object Level1CrumblingPegPrototype : WapObjectPrototype() {
    @Transient
    override val imageSetId: ImageSetId = ImageSetId(
        fullyQualifiedId = "LEVEL1_IMAGES_CRUMBLINGPEG",
    )

    @Transient
    override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
        logic = encode("CrumblingPeg"),
        imageSet = encode("LEVEL_CRUMBLINGPEG"),
    )
}

@Serializable
object Level1RatPrototype : WapObjectPrototype() {
    @Transient
    override val imageSetId: ImageSetId = ImageSetId(
        fullyQualifiedId = "LEVEL1_IMAGES_RAT",
    )

    @Transient
    override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
        logic = encode("Rat"),
        imageSet = encode("LEVEL_RAT"),
    )
}

@Serializable
object SoldierPrototype : WapObjectPrototype() {
    @Transient
    override val imageSetId: ImageSetId = ImageSetId(
        fullyQualifiedId = "LEVEL1_IMAGES_SOLDIER",
    )

    @Transient
    override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
        logic = encode("Soldier"),
        imageSet = encode("LEVEL_SOLDIER"),
    )
}

@Serializable
object OfficerPrototype : WapObjectPrototype() {
    @Transient
    override val imageSetId: ImageSetId = ImageSetId(
        fullyQualifiedId = "LEVEL1_IMAGES_OFFICER",
    )

    @Transient
    override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
        logic = encode("Officer"),
        imageSet = encode("LEVEL_OFFICER"),
    )
}

@Serializable
object Retail1Health : WapObjectPrototype() {
    @Transient
    override val imageSetId: ImageSetId = ImageSetId(
        fullyQualifiedId = "LEVEL1_IMAGES_HEALTH",
    )

    @Transient
    override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
        logic = encode("HealthPowerup"),
        imageSet = encode("LEVEL_HEALTH"),
    )
}

@Serializable
object Retail1PowderKeg : WapObjectPrototype() {
    @Transient
    override val imageSetId: ImageSetId = ImageSetId(
        fullyQualifiedId = "LEVEL1_IMAGES_POWDERKEG",
    )

    @Transient
    override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
        logic = encode("Powderkeg"),
        imageSet = encode("LEVEL_POWDERKEG"),
    )
}
