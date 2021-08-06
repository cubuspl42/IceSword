package icesword.frp
/// Currently, [added], [updated] and [removed] are assumed to be
/// mutually-exclusive.

data class MapChange<K, V>(
    val added: Map<K, V>,
    val updated: Map<K, V>,
    val removed: Set<K>,
) {

    companion object {
        /// A no-op change with all groups being empty is valid.
        fun <K, V> empty(): MapChange<K, V> =
            MapChange(
                added = emptyMap(),
                updated = emptyMap(),
                removed = emptySet(),
            )

        fun <K, V> diff(
            oldMap: Map<K, V>,
            newMap: Map<K, V>,
        ): MapChange<K, V> =
            MapChange(
                added = newMap.filter { (key, _) ->
                    !oldMap.containsKey(key)
                },
                updated = newMap.filter { (key, newValue) ->
                    oldMap[key]?.let { it != newValue } ?: false
                },
                removed = oldMap.keys
                    .filter { key -> !newMap.containsKey(key) }
                    .toSet(),
            )
    }

    fun applyTo(mutableMap: MutableMap<K, V>) {
        added.forEach { (key, value) ->
            mutableMap[key] = value
        }

        updated.forEach { (key, value) ->
            mutableMap[key] = value
        }

        removed.forEach { key ->
            mutableMap.remove(key)
        }
    }

    fun <K2> mapKeys(
        f: (Map.Entry<K, V>) -> K2,
        keyMap: Map<K, K2>,
    ): MapChange<K2, V> {
        val added = this.added.mapKeys(f)
        val updated = this.updated.mapKeys(f)
        val removed = this.removed.map { keyMap[it]!! }.toSet()

        return MapChange<K2, V>(
            added = added,
            updated = updated,
            removed = removed,
        )
    }

//
//  MapChange<K, V2> mapValues<V2>(V2 f(K key, V value)) {
//    final added = this.added.mapValues(f);
//    final updated = this.updated.mapValues(f);
//    return MapChange(
//      added: added,
//      updated: updated,
//      removed: this.removed,
//    );
//  }
//
//  Future<MapChange<K, V2>> mapValuesAsync<V2>(
//    Future<V2> f(K key, V value),
//  ) async {
//    final addedFuture = this.added.mapValuesAsync(f);
//    final updatedFuture = this.updated.mapValuesAsync(f);
//
//    return MapChange(
//      added: await addedFuture,
//      updated: await updatedFuture,
//      removed: this.removed,
//    );
//  }
//
//  MapChange<K, V> withoutUpdated() => MapChange(
//        added: added,
//        updated: const {},
//        removed: removed,
//      );
//
//  void applyTo(Map<K, V> mutableMap) {
//    added.forEach((key, value) {
//      mutableMap[key] = value;
//    });
//
//    updated.forEach((key, value) {
//      mutableMap[key] = value;
//    });
//
//    removed.forEach((key) {
//      mutableMap.remove(key);
//    });
//  }
//
//  static MapChange<K, V> diff<K, V>(
//    Map<K, V> oldMap,
//    Map<K, V> newMap,
//  ) =>
//      MapChange(
//        added: newMap.where(
//          (key, _) => !oldMap.safeContainsKey(key),
//        ),
//        updated: newMap.where(
//          (key, newValue) => oldMap
//              .getOpt(key)
//              .fold(() => false, (oldValue) => oldValue != newValue),
//        ),
//        removed: oldMap.keys
//            .where(
//              (key) => !newMap.safeContainsKey(key),
//            )
//            .toSet(),
//      );
//
//  @override
//  bool operator ==(Object other) =>
//      other is MapChange<K, V> &&
//      MapEquality<K, V>().equals(added, other.added) &&
//      MapEquality<K, V>().equals(updated, other.updated) &&
//      SetEquality<K>().equals(removed, other.removed);
//
//  @override
//  int get hashCode =>
//      MapEquality<K, V>().hash(added) ^
//      MapEquality<K, V>().hash(updated) ^
//      SetEquality<K>().hash(removed);
//
//  @override
//  String toString() =>
//      '$runtimeType{added: $added, updated: $updated, removed: $removed}';
}
//
//// The approach for implementing `ReactiveMap` can be improved, so complexity
//// of updating a value under key K is O(number of listeners for key K), not
//// O(number of all listeners for all keys). But to benefit from that, major app
//// components that depend on VL<Map<K, V>> would first need to be ported to
//// use `ReactiveMap`.
//abstract class ReactiveMap<K, V> implements Pinnable {
//  // Note: The inner maps may be views to the internal map representation. To
//  // create a permanent snapshot of the reactive map, a copy of `content.value`
//  // must be created.
//  Map<K, V> get content;
//
//  late final ValueListenable<Map<K, V>> contentVl =
//      ValueListenableUtils.sampled(
//    () => content,
//    changes.mapTo(unit),
//  );
//
//  late final ValueListenable<int> length = contentVl.map((m) => m.length);
//
//  late final ValueListenable<bool> isEmpty = contentVl.map((m) => m.isEmpty);
//
//  late final ValueListenable<bool> isNotEmpty =
//      contentVl.map((m) => m.isNotEmpty);
//
//  Stream<icesword.frp.MapChange<K, V>> get changes;
//
//  late final ValueListenable<List<V>> valuesList =
//      contentVl.map((m) => m.values.toList());
//
//  late final ReactiveSet<V> valuesSet = ReactiveSet.diff(
//    contentVl.map((c) => c.values.toSet()),
//  );
//
//  late final ReactiveList<V> values = ReactiveList.diff(valuesList);
//
//  ValueListenable<Option<V>> getOpt(K key) =>
//      changes.holdUnitRc().map((_) => content.getOpt(key)).calm();
//
//  Future<V> waitFor(K key) {
//    final valueOpt = content.getOpt(key);
//    return valueOpt.foldAsync(
//      () => changes.mapSome((change) => change.added.getOpt(key)).first,
//      (value) => Future.value(value),
//    );
//  }
//
//  ReactiveMap<K, V2> mapValues<V2>(V2 f(K key, V value)) {
//    final initialContent = this.content.mapValues(f);
//    final changes = this.changes.map((c) => c.mapValues(f));
//    return ReactiveMap.holdRc(
//      initialContent,
//      changes: changes,
//    );
//  }
//
//  Future<ReactiveMap<K, V2>> mapValuesAsync<V2>(
//    Future<V2> f(K key, V value),
//  ) =>
//      // Note: as we map to Future up front, side effects happen in parallel
//      ReactiveMap.awaitValues<K, V2>(
//        this.mapValues(f),
//      );
//
//  ReactiveMap<K, V> whereVl(ValueListenable<bool> predicate(V value)) =>
//      fuseMapValues((v) => predicate(v).map((b) => Tuple2(v, b)))
//          .where((_, t) => t.value2)
//          .mapValues((_, t) => t.value1);
//
//  ReactiveMap<K, V> where(bool predicate(K key, V value)) =>
//      FilteredReactiveMap(this, predicate);
//
//  // TODO: remove "flapping", i.e. unnecessary listener addition and removal
//  ReactiveMap<K, V2> fuseMapValues<V2>(ValueListenable<V2> f(V v)) {
//    final mapVl = contentVl
//        .flatMap(
//          (map) => map.entries.traverseValueListenable(
//              (e) => f(e.value).map((v2) => MapEntry(e.key, v2))),
//        )
//        .map((entries) => Map.fromEntries(entries));
//    return ReactiveMap.diff(mapVl);
//  }
//
//  ReactiveMap<K, ValueListenable<V?>> defuse() {
//    ValueListenable<V?> _get(K key, V initialValue) =>
//        ValueListenableUtils.create(
//          () => content.getOpt(key).getOrNull(),
//          this.changes.mapSome((change) => change.updated.getOpt(key)),
//        );
//
//    final initialContent = this.content.mapValues(
//          (key, initialValue) => _get(key, initialValue),
//        );
//
//    final changes = this.changes.map(
//          (change) => change
//              .mapValues((key, initialValue) => _get(key, initialValue))
//              .withoutUpdated(),
//        );
//
//    return holdRc(
//      initialContent,
//      changes: changes,
//    );
//  }
//
//  ValueListenable<List<V>> valuesSortedBy<C extends Comparable<C>>(
//    ValueListenable<C> key(V v),
//  ) {
//    final tuplesVl = valuesList.flatMap(
//      (values) => values.traverseValueListenable(
//        (v) => key(v).map((c) => Tuple2(v, c)),
//      ),
//    );
//
//    return tuplesVl.map(
//      (tuples) =>
//          tuples.sortedBy((t) => t.value2).map((t) => t.value1).toList(),
//    );
//  }
//
//  ValueListenable<bool> containsKey(K key) => getOpt(key).valueIsSome();
//
//  static ReactiveMap<K, V> holdRc<K, V>(
//    Map<K, V> initialContent, {
//    required Stream<icesword.frp.MapChange<K, V>> changes,
//  }) =>
//      ReactiveMapHoldRc(initialContent, changes);
//
//  static ReactiveMap<K, V> of<K, V>(Map<K, V> map) => holdRc(
//        map,
//        changes: Stream.empty(),
//      );
//
//  static ReactiveMap<K, V> empty<K, V>() => holdRc(
//        const {},
//        changes: Stream.empty(),
//      );
//
//  // Note: Currently this operator assumes that maps inside the cell are
//  // immutable
//  static ReactiveMap<K, V> diff<K, V>(ValueListenable<Map<K, V>> vm) {
//    final initialContent = vm.value;
//
//    final changes = vm.changes().map((valueChange) {
//      final oldMap = valueChange.oldValue;
//      final newMap = valueChange.newValue;
//      return icesword.frp.MapChange.diff(oldMap, newMap);
//    });
//
//    return holdRc(
//      initialContent,
//      changes: changes,
//    );
//  }
//
//  static ReactiveMap<K, V> fromEntries<K, V>(
//    ReactiveSet<MapEntry<K, V>> entries,
//  ) =>
//      ReactiveMap.diff(
//        entries.contentVl.map(
//          (entries) => Map.fromEntries(entries),
//        ),
//      );
//
//  static Future<ReactiveMap<K, V>> awaitValues<K, V>(
//    ReactiveMap<K, Future<V>> mf,
//  ) async {
//    final initialContentFuture = mf.content.mapValuesAsync<V>((_, v) => v);
//
//    // Buffer for changes which futures complete before [initialContentFuture]
//    // completes
//    final changeBuffer = <icesword.frp.MapChange<K, V>>[];
//
//    // Here, [asyncMap] guards only the order in which future results arrive
//    final changes = mf.changes.asyncMap((c) => c.mapValuesAsync((_, v) => v));
//
//    final sub = changes.listen(changeBuffer.add);
//
//    try {
//      final initialContent = await initialContentFuture;
//
//      // Apply changes that came early and include them in the output reactive
//      // map initial content
//      changeBuffer.forEach((change) {
//        change.applyTo(initialContent);
//      });
//
//      return holdRc(
//        initialContent,
//        changes: changes,
//      );
//    } finally {
//      unawaited(sub.cancel());
//    }
//  }
//
//  @override
//  ValueListenable<Object> get pinVl => changes.mapTo(unit).holdRc(unit);
//
//  @override
//  String toString() => "$runtimeType{content=$content}";
//}
//
//abstract class MutableReactiveMap<K, V> extends ReactiveMap<K, V> {
//  void put(K key, V value);
//
//  void remove(K key);
//}
