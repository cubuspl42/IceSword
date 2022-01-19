package icesword.frp.dynamic_list

import icesword.frp.Cell
import icesword.frp.RawCell
import icesword.frp.SimpleStream
import icesword.frp.Stream
import icesword.frp.Subscription
import icesword.frp.map

abstract class InstantiatingDynamicList<E> : DynamicList<E> {
    private var mutableContent: MutableList<DynamicList.IdentifiedElement<E>>? = null

    override val changes: Stream<ListChange<E>> = NotifyingStream(
        start = { notifier ->
            val instanceMutableContent = buildContent()
            mutableContent = instanceMutableContent

            val changes = buildChanges()

            val changesSubscription = changes.subscribe {
                it.applyTo(instanceMutableContent)
                notifier.notify(it)
            }

            object : Subscription {
                override fun unsubscribe() {
                    changesSubscription.unsubscribe()

                    mutableContent = null
                }
            }
        }
    )

    override val content: Cell<List<E>>
        get() = RawCell(
            { volatileContentView.toList() },
            changes.map { volatileContentView.toList() },
        )

    override val volatileIdentifiedContentView: List<DynamicList.IdentifiedElement<E>>
        get() = mutableContent ?: buildContent()

    override val volatileContentView: List<E>
        // TODO: Lazy mapping?
        get() = volatileIdentifiedContentView.map { it.element }

    abstract fun buildContent(): MutableList<DynamicList.IdentifiedElement<E>>

    abstract fun buildChanges(): Stream<ListChange<E>>
}

private interface StreamNotifier<in E> {
    fun notify(event: E)
}

private class NotifyingStream<E>(
    private val start: (notifier: StreamNotifier<E>) -> Subscription,
) : SimpleStream<E>(Identity.build(tag = "NotifyingStream")) {
    private var subscription: Subscription? = null

    override fun onStart() {
        subscription = start(
            object : StreamNotifier<E> {
                override fun notify(event: E) {
                    this@NotifyingStream.notifyListeners(event)
                }
            }
        )
    }

    override fun onStop() {
        subscription!!.unsubscribe()
    }
}
