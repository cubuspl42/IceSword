package icesword.html

import icesword.frp.Cell
import icesword.frp.Till
import icesword.frp.dynamic_list.DynamicList
import icesword.frp.dynamic_list.mapTillRemoved
import icesword.frp.mapTillNext
import org.w3c.dom.Node

sealed interface HTMLWidget : HTMLWidgetB<HTMLWidget> {
    override fun build(tillDetach: Till): HTMLWidget = this

    companion object {
        fun of(element: Node): HTMLWidget = object : HTMLElementWidget {
            override val element: Node = element
        }

        fun resolve(widget: HTMLWidget): Node = when (widget) {
            is HTMLElementWidget -> widget.element
            is HTMLNestedWidget -> resolve(widget.widget)
        }
    }

    interface HTMLElementWidget : HTMLWidget {
        val element: Node
    }

    interface HTMLNestedWidget : HTMLWidget {
        val widget: HTMLWidget
    }
}

interface HTMLWidgetB<out W : HTMLWidget> {
    companion object {
        fun <W : HTMLWidget> pure(widget: W): HTMLWidgetB<W> = object : HTMLWidgetB<W> {
            override fun build(tillDetach: Till): W = widget
        }

        fun <W : HTMLWidget> build(widget: Cell<HTMLWidgetB<W>?>, tillDetach: Till): Cell<W?> =
            widget.mapTillNext(tillDetach) { elementB, tillNext ->
                elementB?.build(tillNext)
            }

        fun build(widgets: List<HTMLWidgetB<*>>, tillDetach: Till): List<HTMLWidget> =
            widgets.map { widgetB -> widgetB.build(tillDetach) }

        fun buildDl(widgets: DynamicList<HTMLWidgetB<*>>, tillAbort: Till): DynamicList<HTMLWidget> =
            widgets.mapTillRemoved(tillAbort) { childB, tillNext ->
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
