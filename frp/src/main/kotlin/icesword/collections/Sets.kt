package icesword.collections

inline fun <K, V> Sequence<K>.associateWithThrough(
    through: MapFactory<K, V>,
    valueSelector: (K) -> V,
): MutableMap<K, V> = this.associateWithTo(
    through.create(),
    valueSelector,
)
