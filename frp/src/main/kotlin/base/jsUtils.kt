package base

fun jsObjectOf(entries: Map<String, Any?>): dynamic {
    val obj = js("{}")

    entries.forEach { (k, v) ->
        obj[k] = v
    }

    return obj
}
