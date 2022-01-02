package icesword.frp.dynamic_set

import icesword.frp.*
import icesword.frpjs.hybridMapOf

data class Link<B>(
    val marker: TillMarker,
    val value: B,
)

class MapTillRemovedDynamicSet<A, B>(
    source: DynamicSet<A>,
    transform: (element: A, tillRemoved: Till) -> B,
    tillAbort: Till,
) : SimpleDynamicSet<B>(
    identity = SimpleObservable.Identity.build("MapDynamicSet"),
) {
    private var linksMap: MutableMap<A, Link<B>>? = null

    private var mutableContent: MutableSet<B>? = null

    override val volatileContentView: Set<B>
        get() = mutableContent!!

    init {
        subscribeTill(source.changes, till = tillAbort) { change ->
            val added: Set<B> = change.added.asSequence().map { a: A ->
                val tillMarker = TillMarker()

                val b = transform(a, tillMarker.or(tillAbort))

                val previousLink = linksMap!!.put(
                    a,
                    Link(marker = tillMarker, value = b),
                )

                if (previousLink != null) throw IllegalStateException()

                b
            }.toSet()

            val removed = change.removed.asSequence().map { a: A ->
                val link = linksMap!!.remove(a)!!

                link.marker.markReached()

                link.value
            }.toSet()

            val outChange = SetChange(
                added = added,
                removed = removed,
            )

            outChange.applyTo(mutableContent!!)

            if (!outChange.isEmpty()) {
                notifyListeners(
                    outChange,
                )
            }
        }

        val initialLinksMap = source.volatileContentView.asSequence().map { a ->
            val tillMarker = TillMarker()
            a to Link(marker = tillMarker, value = transform(a, tillMarker))
        }.toMap(hybridMapOf())

        linksMap = initialLinksMap

        mutableContent = initialLinksMap.asSequence().map { it.value.value }.toMutableSet()
    }

}
