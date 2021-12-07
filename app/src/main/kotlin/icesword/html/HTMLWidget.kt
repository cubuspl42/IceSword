package icesword.html

import icesword.frp.Cell
import icesword.frp.Stream
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.mapTillRemoved
import icesword.frp.mapTillNext
import org.w3c.dom.DragEvent
import org.w3c.dom.Element
import org.w3c.dom.Node

sealed interface HTMLWidget : HTMLWidgetB<HTMLWidget> {
    override fun build(tillDetach: Till): HTMLWidget = this

    companion object {
        fun of(element: Node): HTMLWidget = object : HTMLElementWidget {
            override val element: Node = element
        }

        fun resolve(widget: HTMLWidget): Node = when (widget) {
            is HTMLElementWidget -> widget.element
            is HTMLShadowWidget -> resolve(widget.root)
        }
    }

    interface HTMLElementWidget : HTMLWidget {
        val element: Node
    }

    interface HTMLShadowWidget : HTMLWidget {
        val root: HTMLWidget
    }
}

fun HTMLWidget.resolve(): Node =
    HTMLWidget.resolve(this)

interface HTMLWidgetB<out W : HTMLWidget> {
    companion object {
        fun <W : HTMLWidget> pure(widget: W): HTMLWidgetB<W> = object : HTMLWidgetB<W> {
            override fun build(tillDetach: Till): W = widget
        }

        fun <W : HTMLWidget> build(widget: Cell<HTMLWidgetB<W>?>, tillDetach: Till): Cell<W?> =
            widget.mapTillNext(tillDetach) { elementB, tillNext ->
                elementB?.build(tillNext)
            }

        fun <W : HTMLWidget> build(widget: Cell<HTMLWidgetB<W>>, tillDetach: Till): Cell<W> =
            widget.mapTillNext(tillDetach) { elementB, tillNext ->
                elementB.build(tillNext)
            }

        fun build(widgets: List<HTMLWidgetB<*>>, tillDetach: Till): List<HTMLWidget> =
            widgets.map { widgetB -> widgetB.build(tillDetach) }

        fun <W : HTMLWidget> buildDl(widgets: DynamicList<HTMLWidgetB<W>>, tillDetach: Till): DynamicList<W> =
            widgets.mapTillRemoved(tillDetach) { childB, tillNext ->
                childB.build(tillNext)
            }
    }

    fun build(tillDetach: Till): W
}

fun <Wa : HTMLWidget, Wb : HTMLWidget> HTMLWidgetB<Wa>.map(transform: (widget: Wa) -> Wb): HTMLWidgetB<Wb> {
    val self = this
    return object : HTMLWidgetB<Wb> {
        override fun build(tillDetach: Till): Wb {
            val widgetA = self.build(tillDetach)
            val widgetB = transform(widgetA)
            return widgetB
        }
    }
}

fun <Wa : HTMLWidget, Wb : HTMLWidget> HTMLWidgetB<Wa>.mapTillDetach(
    transform: (widget: Wa, tillDetach: Till) -> Wb,
): HTMLWidgetB<Wb> {
    val self = this
    return object : HTMLWidgetB<Wb> {
        override fun build(tillDetach: Till): Wb {
            val widgetA = self.build(tillDetach)
            val widgetB = transform(widgetA, tillDetach)
            return widgetB
        }
    }
}

fun <Wa : HTMLWidget, Wb : HTMLWidget> HTMLWidgetB<Wa>.flatMapTillDetach(
    transform: (widget: Wa, tillDetach: Till) -> HTMLWidgetB<Wb>,
): HTMLWidgetB<Wb> {
    val self = this
    return object : HTMLWidgetB<Wb> {
        override fun build(tillDetach: Till): Wb {
            val widgetA = self.build(tillDetach)
            val widgetB = transform(widgetA, tillDetach).build(tillDetach)
            return widgetB
        }
    }
}

fun <Wa : HTMLWidget> HTMLWidgetB<Wa>.alsoTillDetach(
    block: (widget: Wa, tillDetach: Till) -> Unit,
): HTMLWidgetB<Wa> {
    val self = this
    return object : HTMLWidgetB<Wa> {
        override fun build(tillDetach: Till): Wa {
            val widgetA = self.build(tillDetach)
            block(widgetA, tillDetach)
            return widgetA
        }
    }
}

fun <Wa : HTMLWidget, Wb : HTMLWidget> HTMLWidgetB<Wa>.flatMap(transform: (widget: Wa) -> HTMLWidgetB<Wb>): HTMLWidgetB<Wb> {
    val self = this
    return object : HTMLWidgetB<Wb> {
        override fun build(tillDetach: Till): Wb {
            val widgetA = self.build(tillDetach)
            val widgetB = transform(widgetA).build(tillDetach)
            return widgetB
        }
    }
}

fun HTMLWidget.onDragEnter(): Stream<DragEvent> =
    (this.resolve() as Element).onDragEnter()

fun HTMLWidget.onDragOver(): Stream<DragEvent> =
    (this.resolve() as Element).onDragOver()

fun HTMLWidget.onDragLeave(): Stream<DragEvent> =
    (this.resolve() as Element).onDragLeave()

fun HTMLWidget.onDrop(): Stream<DragEvent> =
    (this.resolve() as Element).onDrop()
