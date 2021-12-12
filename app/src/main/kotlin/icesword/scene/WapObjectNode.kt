package icesword.scene

import TextureBank
import icesword.editor.Editor
import icesword.editor.Entity
import icesword.editor.WapObject
import icesword.editor.WapSprite
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.units
import icesword.frp.values
import icesword.geometry.DynamicTransform
import icesword.geometry.IntRect
import icesword.scene.HybridNode.OverlayBuildContext
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGSVGElement

class WapSpriteNode(
    textureBank: TextureBank,
    private val wapSprite: WapSprite,
    private val alpha: Double = 1.0,
) : CanvasNode {
    private val texture = wapSprite.imageMetadata?.let { imageMetadata ->
        textureBank.getImageTexture(pidImagePath = imageMetadata.pidImagePath)!!
    } ?: textureBank.wapObject

    override fun draw(ctx: CanvasRenderingContext2D, windowRect: IntRect) {
        ctx.save()

        val boundingBox = wapSprite.boundingBox.sample()

        ctx.globalAlpha = alpha

        drawWapSprite(
            ctx,
            texture = texture,
            bounds = boundingBox,
        )

        ctx.restore()
    }

    override val onDirty: Stream<Unit> =
        wapSprite.boundingBox.values().units()
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
    wapSprite: WapSprite,
    tillDetach: Till,
): SVGElement {
    val box = createEntityFrameElement(
        editor = editor,
        svg = svg,
        outer = viewport,
        entity = entity,
        boundingBox = viewTransform.transform(wapSprite.boundingBox),
        tillDetach = tillDetach,
    )

    return box
}
