package icesword.frp

import icesword.frp.dynamic_list.DynamicList

data class KeyIdentity<K>(
    // A unique key
    val key: K,
) : DynamicList.ElementIdentity

data class IndexedIdentity(
    // An unique index
    val index: Int,
    val identity: DynamicList.ElementIdentity,
) : DynamicList.ElementIdentity
