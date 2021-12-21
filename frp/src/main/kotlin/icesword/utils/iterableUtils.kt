package icesword.utils

fun <E> Iterable<E>.updated(index: Int, elem: E) =
    mapIndexed { i, existing -> if (i == index) elem else existing }

fun <E> Iterable<E>.withRemoved(index: Int) =
    filterIndexed { i, _ -> i != index }

@OptIn(ExperimentalStdlibApi::class)
fun <K, V> Map<K, V?>.filterValuesNotNull(): Map<K, V> =
    buildMap { for ((k, v) in this@filterValuesNotNull) if (v != null) put(k, v) }

inline fun <K, V, R : Any> Map<out K, V>.mapValuesNotNull(transform: (Map.Entry<K, V>) -> R?): Map<K, R> =
    mapValues(transform).filterValuesNotNull()
