package icesword.ui.world_view.scene

import icesword.EditorTextureBank
import icesword.RezTextureBank
import icesword.editor.Editor
import icesword.editor.entities.Entity
import icesword.editor.entities.WapObject
import icesword.editor.DynamicWapSprite
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.map
import icesword.frp.mergeWith
import icesword.frp.units
import icesword.frp.values
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import icesword.ui.CanvasNode
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

class WapSpriteNode(
    editorTextureBank: EditorTextureBank,
    textureBank: RezTextureBank,
    private val wapSprite: DynamicWapSprite,
    private val alpha: Double = 1.0,
) : CanvasNode {
    private val dynamicTexture = wapSprite.imageMetadata.map { imageMetadataOrNull ->
        imageMetadataOrNull?.let { imageMetadata ->
            textureBank.getImageTexture(imageMetadata.pidPath)
        } ?: editorTextureBank.wapObjectPlaceholder
    }

    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        ctx.save()

        val boundingBox = wapSprite.boundingBox.sample()

        ctx.globalAlpha = alpha

        val texture = dynamicTexture.sample()

        drawWapSprite(
            ctx,
            texture = texture,
            bounds = boundingBox,
        )

        ctx.restore()
    }

    override val onDirty: Stream<Unit> =
        wapSprite.boundingBox.values().units()
            .mergeWith(dynamicTexture.values().units())
}

fun drawWapSprite(
    ctx: CanvasRenderingContext2D,
    texture: Texture,
    bounds: IntRect,
) {
    drawTexture(
        ctx,
        texture = texture,
        dv = bounds.topLeft,
    )
}

fun createWapObjectOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: DynamicTransform,
    wapObject: WapObject,
    tillDetach: Till,
): SVGElement =
    createWapSpriteOverlayElement(
        editor = editor,
        svg = svg,
        viewport = viewport,
        viewTransform = viewTransform,
        entity = wapObject,
        wapSprite = wapObject.sprite,
        tillDetach = tillDetach,
    )

fun createWapSpriteOverlayElement(
    editor: Editor,
    svg: SVGSVGElement,
    viewport: HTMLElement,
    viewTransform: DynamicTransform,
    entity: Entity,
    wapSprite: DynamicWapSprite,
    tillDetach: Till,
): SVGElement {
    val box = createEntityFrameElement(
        editor = editor,
        svg = svg,
        outer = viewport,
        entity = entity,
        viewBoundingBox = viewTransform.transform(wapSprite.boundingBox),
        tillDetach = tillDetach,
    )

    return box
}
