package frp.dynamic_list

import kotlin.test.assertEquals
import kotlin.test.fail

fun <T> assertMatchesExclusivelyOrdered(
    list: List<T>,
    assertions: List<(T) -> Unit>,
) {
    assertEquals(list.size, assertions.size, "List size is ${list.size}, expected ${assertions.size}")

    assertions.forEachIndexed { index, assertion ->
        try {
            assertion(list[index])
        } catch (e: AssertionError) {
            throw AssertionError(
                message = "At index $index: ${e.message}",
                cause = e,
            )
        }
    }
}

fun <T> assertMatchesExclusivelyUnordered(
    collection: Collection<T>,
    matchers: Set<(T) -> Boolean>,
) {
    assertEquals(
        expected = matchers.size,
        actual = collection.size,
        message = "Collection size is ${collection.size}, expected ${matchers.size}",
    )

    matchers.forEachIndexed { index, matcher ->
        val matchCount = collection.count { matcher(it) }

        if (matchCount != 1) {
            fail("Exactly one matcher should match element at index $index, but there were $matchCount matches.")
        }
    }
}