package icesword.ui.world_view.scene

import icesword.editor.entities.Enemy
import icesword.editor.entities.HorizontalElevator
import icesword.editor.entities.VerticalElevator
import icesword.ui.CanvasNode
import icesword.ui.world_view.EntityNode
import icesword.ui.world_view.EntityNodeB
import icesword.ui.world_view.scene.base.HybridNode
import org.w3c.dom.svg.SVGElement

fun createHorizontalElevatorNode(
    elevator: HorizontalElevator,
): EntityNodeB = object : EntityNodeB {
    override fun build(context: EntityNodeB.BuildContext): EntityNode = context.run {
        EntityNode(
            hybridNode = object : HybridNode() {
                override fun buildCanvasNode(context: CanvasNodeBuildContext): CanvasNode =
                    WapSpriteNode(
                        editorTextureBank = editorTextureBank,
                        textureBank = textureBank,
                        wapSprite = elevator.wapSprite,
                    )

                override fun buildOverlayElement(context: OverlayBuildContext): SVGElement =
                    context.run {
                        createHorizontalElevatorOverlayElement(
                            editor = editor,
                            svg = svg,
                            viewport = viewport,
                            viewTransform = viewTransform,
                            elevator = elevator,
                            tillDetach = tillDetach,
                        )
                    }
            },
        )
    }
}

fun createVerticalElevatorNode(
    elevator: VerticalElevator,
): EntityNodeB = object : EntityNodeB {
    override fun build(context: EntityNodeB.BuildContext): EntityNode = context.run {
        EntityNode(
            hybridNode = object : HybridNode() {
                override fun buildCanvasNode(context: CanvasNodeBuildContext): CanvasNode =
                    WapSpriteNode(
                        editorTextureBank = editorTextureBank,
                        textureBank = textureBank,
                        wapSprite = elevator.wapSprite,
                    )

                override fun buildOverlayElement(context: OverlayBuildContext): SVGElement =
                    context.run {
                        createVerticalElevatorOverlayElement(
                            editor = editor,
                            svg = svg,
                            viewport = viewport,
                            viewTransform = viewTransform,
                            elevator = elevator,
                            tillDetach = tillDetach,
                        )
                    }
            },
        )
    }
}
