package icesword.frp.dynamic_list

data class OrderIdentity<E>(
    val element: E,
    val order: Int,
) : DynamicList.ElementIdentity

fun <E /* : Eq */> identifyByOrder(list: List<E>): List<DynamicList.IdentifiedElement<E>> {
    val elementCounter = CounterMap<E>()

    return list.map {
        val oldCount = elementCounter.increaseCount(it)

        DynamicList.IdentifiedElement(
            element = it,
            identity = OrderIdentity(
                element = it,
                order = oldCount,
            ),
        )
    }
}

private class CounterMap<A> {
    private val map = mutableMapOf<A, Int>()

    private fun getCount(a: A) =
        map.getOrElse(a) { 0 }

    fun increaseCount(a: A): Int {
        val oldCount = getCount(a)
        map[a] = oldCount + 1
        return oldCount
    }

    private fun decreaseCount(a: A) {
        val oldCount = map.getOrElse(a) { throw IllegalStateException() }

        if (oldCount == 0) {
            map.remove(a)
        } else {
            map[a] = oldCount - 1
        }
    }
}
