package icesword.frp.dynamic_map

import frpjs.FastMap
import icesword.frp.*
import icesword.frpjs.hybridMapOf

private enum class RemovedLinkKind {
    LAST,
    NOT_LAST,
}

class ProjectDynamicMap<K, V, K2, V2>(
    private val source: DynamicMap<K, V>,
    private val projectKey: (K) -> Iterable<K2>,
    private val buildValue: (K2, Map<K, V>) -> V2,
    tag: String? = null,
) : SimpleDynamicMap<K2, V2>(tag = tag ?: "ProjectDynamicMap") {
    private var linksMap: MutableMap<K2, MutableSet<K>>? = null

    private var subscription: Subscription? = null

    private var mutableContent: MutableMap<K2, V2>? = null

    override val volatileContentView: Map<K2, V2>
        get() = this.mutableContent!!

    override fun onStart(): Unit {
        fun processChange(change: MapChange<K, V>) {
            val addedProjections = change.added
                .flatMap { (k, v) -> projectKey(k).map { k2 -> k to k2 } }

            val addedAffectedKeys = addedProjections.map { (k, k2) -> k2 }.toList()

            val addedAddedKeys = addedAffectedKeys.asSequence()
                .filter { !volatileContentView.containsKey(it) }

            val addedUpdatedKeys = addedAffectedKeys.asSequence()
                .filter { volatileContentView.containsKey(it) }

            val updatedKeys = change.updated.asSequence()
                .flatMap { (k, v) -> projectKey(k) }

            val removedProjections = change.removedEntries.asSequence()
                .flatMap { (k, _) -> projectKey(k).map { k2 -> k to k2 } }

            val removedProjectionsGrouped = removedProjections
                .groupByTo(hybridMapOf()) { (k, k2) -> k2 }
                .map { (k2, links) ->
                    val linksKeys = links.map { (k, k2) -> k }
                    val wereLast = removeLinks(k2, linksKeys)
                    if (wereLast) (RemovedLinkKind.LAST to k2) else (RemovedLinkKind.NOT_LAST to k2)
                }
                .groupBy { it.first }

            val lastLinks = (removedProjectionsGrouped[RemovedLinkKind.LAST] ?: emptyList()).map { (k, k2) -> k2 }
            val notLastLinks =
                (removedProjectionsGrouped[RemovedLinkKind.NOT_LAST] ?: emptyList()).map { (k, k2) -> k2 }

            val removedUpdatedKeys = notLastLinks.asSequence()
            val removedRemovedKeys = lastLinks.toSet()

            val added = addedAddedKeys.associateWithTo(hybridMapOf()) { k2 ->
                buildValue(k2, source.volatileContentView)
            }

            val updated = (addedUpdatedKeys + updatedKeys + removedUpdatedKeys)
                .associateWithTo(hybridMapOf()) { buildValue(it, source.volatileContentView) }

            val actuallyRemovedKeys = removedRemovedKeys - addedAffectedKeys

            val removed = actuallyRemovedKeys.associateWithTo(hybridMapOf()) { k2 ->
                volatileContentView[k2]!!
            }

//            if (updated.keys.intersect(removed.keys).isNotEmpty()) {
//                println("updated intersects removed")
//            }
//
//            if (removedUpdatedKeys.toSet().intersect(removed.keys).isNotEmpty()) {
//                println("removedUpdatedKeys intersects removed")
//            }
//
//            if (updatedKeys.toSet().intersect(removed.keys).isNotEmpty()) {
//                println("updatedKeys intersects removed")
//            }
//
//            if (lastLinks.intersect(notLastLinks.toSet()).isNotEmpty()) {
//                println("lastLinks intersects notLastLinks")
//            }

            addedProjections.forEach { (k, k2) -> addLink(k2, k) }

//            val changeDump = change.dump()

            val outChange = MapChange(
                added = added,
                updated = updated,
                removedEntries = removed,
            )

//            val outChangeDump = outChange.dump()

//            validateChange(tag = tag, change = outChange)

            processChange(outChange)
        }

        this.subscription = this.source.changes.subscribe {
            processChange(it)
        }

        val sourceContent = source.volatileContentView

        val initialProjections = sourceContent.asSequence().flatMap { (k, v) ->
            projectKey(k).map { k2 -> k to k2 }
        }

        val initialLinksMap = initialProjections
            .groupBy { (k, k2) -> k2 }.asSequence()
            .map { (k2, group) ->
                k2 to group.asSequence().map { (k, k2) -> k }.toMutableSet()
            }
            .toMap(hybridMapOf())

        this.linksMap = initialLinksMap

        val initialContent = initialLinksMap.keys
            .associateWithTo(hybridMapOf()) { buildValue(it, sourceContent) }

        this.mutableContent = initialContent
    }

    override fun onStop() {
        this.mutableContent = null

        this.subscription!!.unsubscribe()
        this.subscription = null

    }

    private fun addLink(b: K2, a: K): Unit {
//        const links = Maps.getOrPut(this.linksMap!, b, () => Sets.emptyMutable<K>());
//
//        const wasAdded = Sets.add(links, a);
//
//        if (!wasAdded) {
//            throw new Error("Link was already present");
//        }

        val links = this.linksMap!!.getOrPut(b) { mutableSetOf() }

        val wasAdded = links.add(a)

        if (!wasAdded) {
            throw IllegalStateException("Link was already present")
        }
    }

    // Returns: if it were last links
    private fun removeLinks(k2: K2, keys: Iterable<K>): Boolean {
        val linksMap = this.linksMap!!
        val links = linksMap[k2]!!

        keys.forEach {
            val wasThere = links.remove(it)

            if (!wasThere) {
                throw IllegalStateException("Link isn't present");
            }
        }

        return if (links.isEmpty()) {
            val removed = linksMap.remove(k2)
            if (removed !== links) throw IllegalStateException()
            true
        } else false
    }


    private fun processChange(change: MapChange<K2, V2>) {
        if (!change.isEmpty()) {
            change.applyTo(mutableContent!!)
            notifyListeners(change)
        }
    }
}
