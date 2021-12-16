package icesword.frp

abstract class SimpleDynamicMap<K, V>(
    tag: String,
) : DynamicMap<K, V>, SimpleObservable<MapChange<K, V>>(
    tag = tag,
) {
//    override val content: Cell<Map<K, V>>
//        get() = TODO("Not yet implemented")

    override val changes: Stream<MapChange<K, V>> by lazy {
        Stream.source(this::subscribe, tag = "$tag.changes")
    }

    override val content: Cell<Map<K, V>>
        get() = RawCell(
            { volatileContentView.toMap() },
            changes.map { volatileContentView.toMap() },
        )

    override fun toString(): String = "SimpleDynamicMap(id=$id)"
}
