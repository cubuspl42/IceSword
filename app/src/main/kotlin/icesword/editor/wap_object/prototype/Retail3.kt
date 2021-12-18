package icesword.editor.wap_object.prototype

import icesword.ImageSetId
import icesword.editor.encode
import icesword.wwd.Wwd
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
object Retail3Health : WapObjectPrototype() {
    @Transient
    override val imageSetId: ImageSetId = ImageSetId(
        fullyQualifiedId = "LEVEL3_IMAGES_HEALTH",
    )

    @Transient
    override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
        logic = encode("HealthPowerup"),
        imageSet = encode("LEVEL_HEALTH"),
    )
}

@Serializable
object Retail3PowderKeg : WapObjectPrototype() {
    @Transient
    override val imageSetId: ImageSetId = ImageSetId(
        fullyQualifiedId = "LEVEL3_IMAGES_POWDERKEG",
    )

    @Transient
    override val wwdObjectPrototype: Wwd.Object_ = Wwd.Object_.empty().copy(
        logic = encode("Powderkeg"),
        imageSet = encode("LEVEL_POWDERKEG"),
    )
}
