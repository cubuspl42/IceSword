package icesword

import kotlinx.browser.window

object Debug {
    init {
        val window: dynamic = window
        window.iceswordDebug = this
    }

    var showTileIds = false
}
