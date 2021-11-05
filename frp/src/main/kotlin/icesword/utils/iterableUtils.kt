package icesword.utils

fun <E> Iterable<E>.updated(index: Int, elem: E) =
    mapIndexed { i, existing -> if (i == index) elem else existing }

fun <E> Iterable<E>.withRemoved(index: Int) =
    filterIndexed { i, _ -> i != index }
