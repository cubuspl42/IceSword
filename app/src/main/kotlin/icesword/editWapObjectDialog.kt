package icesword

import icesword.editor.WapObject
import icesword.frp.Cell.Companion.constant
import icesword.frp.Till
import icesword.html.HTMLWidgetB
import icesword.html.createColumnWb
import icesword.html.createHeading4Wb
import icesword.html.createRow
import kotlinx.css.px

fun createWapObjectDialog(
    wapObject: WapObject,
) = object : HTMLWidgetB<Dialog> {
    override fun build(tillDetach: Till): Dialog = createBasicDialog(
        content = createColumnWb(
            children = listOf(
                createHeading4Wb(
                    text = constant("Edit WAP32 object"),
                ),
                createRow(
                    horizontalGap = 8.px,
                    children = listOf(
                        createInputColumn(
                            inputs = listOf(
                                createSimpleIntegerInput(
                                    labelText = "id",
                                    property = wapObject.props.id,
                                ),
                                createStringInput(
                                    labelText = "name",
                                    property = wapObject.props.name,
                                ),
                                createStringInput(
                                    labelText = "logic",
                                    property = wapObject.props.logic,
                                ),
                                createStringInput(
                                    labelText = "imageSet",
                                    property = wapObject.props.imageSet,
                                ),
                                createStringInput(
                                    labelText = "animation",
                                    property = wapObject.props.animation,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "x",
                                    property = wapObject.props.x,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "y",
                                    property = wapObject.props.y,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "z",
                                    property = wapObject.props.z,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "i",
                                    property = wapObject.props.i,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "addFlags",
                                    property = wapObject.props.addFlags,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "dynamicFlags",
                                    property = wapObject.props.dynamicFlags,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "drawFlags",
                                    property = wapObject.props.drawFlags,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "userFlags",
                                    property = wapObject.props.userFlags,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "score",
                                    property = wapObject.props.score,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "points",
                                    property = wapObject.props.points,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "powerUp",
                                    property = wapObject.props.powerUp,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "damage",
                                    property = wapObject.props.damage,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "smarts",
                                    property = wapObject.props.smarts,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "health",
                                    property = wapObject.props.health,
                                ),
                            ),
                        ),
                        createInputColumn(
                            inputs = listOf(
                                createRectangleInputs(
                                    labelText = "rangeRect",
                                    property = wapObject.props.rangeRect,
                                ),
                                createRectangleInputs(
                                    labelText = "moveRect",
                                    property = wapObject.props.moveRect,
                                ),
                                createRectangleInputs(
                                    labelText = "hitRect",
                                    property = wapObject.props.hitRect,
                                ),
                                createRectangleInputs(
                                    labelText = "attackRect",
                                    property = wapObject.props.attackRect,
                                ),
                                createRectangleInputs(
                                    labelText = "clipRect",
                                    property = wapObject.props.clipRect,
                                ),
                                createRectangleInputs(
                                    labelText = "userRect1",
                                    property = wapObject.props.userRect1,
                                ),
                                createRectangleInputs(
                                    labelText = "userRect2",
                                    property = wapObject.props.userRect2,
                                ),
                            ).flatten()
                        ),
                        createInputColumn(
                            inputs = listOf(
                                createSimpleIntegerInput(
                                    labelText = "userValue1",
                                    property = wapObject.props.userValue1,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "userValue2",
                                    property = wapObject.props.userValue2,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "userValue3",
                                    property = wapObject.props.userValue3,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "userValue4",
                                    property = wapObject.props.userValue4,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "userValue5",
                                    property = wapObject.props.userValue5,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "userValue6",
                                    property = wapObject.props.userValue6,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "userValue7",
                                    property = wapObject.props.userValue7,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "userValue8",
                                    property = wapObject.props.userValue8,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "speedX",
                                    property = wapObject.props.speedX,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "speedY",
                                    property = wapObject.props.speedY,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "xTweak",
                                    property = wapObject.props.xTweak,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "yTweak",
                                    property = wapObject.props.yTweak,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "counter",
                                    property = wapObject.props.counter,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "speed",
                                    property = wapObject.props.speed,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "width",
                                    property = wapObject.props.width,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "height",
                                    property = wapObject.props.height,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "direction",
                                    property = wapObject.props.direction,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "faceDir",
                                    property = wapObject.props.faceDir,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "timeDelay",
                                    property = wapObject.props.timeDelay,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "frameDelay",
                                    property = wapObject.props.frameDelay,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "objectType",
                                    property = wapObject.props.objectType,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "hitTypeFlags",
                                    property = wapObject.props.hitTypeFlags,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "xMoveRes",
                                    property = wapObject.props.xMoveRes,
                                ),
                                createSimpleIntegerInput(
                                    labelText = "yMoveRes",
                                    property = wapObject.props.yMoveRes,
                                ),
                            ),
                        ),
                    )
                ),
            ),
        )
    ).build(tillDetach)
}
