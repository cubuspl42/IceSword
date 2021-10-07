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

    override fun containsKeyNow(key: K2): Boolean =
        volatileContentView.containsKey(key)

    override fun getNow(key: K2): V2? =
        volatileContentView.get(key)

    override fun onStart(): Unit {
        this.subscription = this.source.changes.subscribe { change ->
//            change.added.forEach { (k, v) ->
//                val projectedKeys = projectKey(k)
//                projectedKeys.forEach { k2 ->
//                   addLink(k2, k)
//                }
//            }

            // const addedProjections: ReadonlyArray<[K, K2]> = [...change.removed].flatMap(
            //   ([k,]) => [...Iterables.map(
            //     this.projectKey(k),
            //     (k2): [K, K2] => [k, k2],
            //   )],
            // );

            val addedProjections = change.added
                .flatMap { (k, v) -> projectKey(k).map { k2 -> k to k2 } }

            addedProjections.forEach { (k, k2) -> addLink(k2, k) }

            val addedAffectedKeys = addedProjections.map { (k, k2) -> k2 }.toList()

            val addedAddedKeys = addedAffectedKeys.asSequence()
                .filter { !volatileContentView.containsKey(it) }

            val addedUpdatedKeys = addedAffectedKeys.asSequence()
                .filter { volatileContentView.containsKey(it) }

            val updatedKeys = change.updated.asSequence()
                .flatMap { (k, v) -> projectKey(k) }

            val removedProjections = change.removedEntries.asSequence()
                .flatMap { (k, _) -> projectKey(k).map { k2 -> k to k2 } }

            val removedProjectionsGrouped = removedProjections.groupByTo(hybridMapOf()) { (k, k2) ->
                if (removeLink(k2, k)) RemovedLinkKind.LAST else RemovedLinkKind.NOT_LAST
            }

            val lastLinks = removedProjectionsGrouped[RemovedLinkKind.LAST] ?: emptyList()
            val notLastLinks = removedProjectionsGrouped[RemovedLinkKind.NOT_LAST] ?: emptyList()

            val removedUpdatedKeys = notLastLinks.asSequence().map { (k, k2) -> k2 }

            val added = addedAddedKeys.associateWithTo(hybridMapOf()) { k2 ->
                buildValue(k2, source.volatileContentView)
            }

            val updated = (addedUpdatedKeys + updatedKeys + removedUpdatedKeys)
                .associateWithTo(hybridMapOf()) { buildValue(it, source.volatileContentView) }

            val removed = lastLinks.map { (k, k2) -> k2 }.associateWithTo(hybridMapOf()) { k2 ->
                volatileContentView[k2]!!
            }

            val outChange = MapChange(
                added = added,
                updated = updated,
                removedEntries = removed,
            )

            processChange(outChange)
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

    // Returns: if it was the last link
    private fun removeLink(b: K2, a: K): Boolean {
//        const linksMap = this.linksMap!;
//        const links = linksMap.get(b)!;
//
//        const wasThere = links.delete(a);
//
//        if (!wasThere) {
//            throw new Error("Link isn't present");
//        }
//
//        if (links.size === 0) {
//            linksMap.delete(b);
//            return true;
//        } else {
//            return false;
//        }

        val linksMap = this.linksMap!!
        val links = linksMap[b]!!

        val wasThere = links.remove(a)

        if (!wasThere) {
            throw IllegalStateException("Link isn't present");
        }

        if (links.isEmpty()) {
            val removed = linksMap.remove(b)
            if (removed !== links) throw IllegalStateException()
            return true
        } else {
            return false
        }
    }

    private fun processChange(change: MapChange<K2, V2>) {
        if (!change.isEmpty()) {
            change.applyTo(mutableContent!!)
            notifyListeners(change)
        }
    }
}
