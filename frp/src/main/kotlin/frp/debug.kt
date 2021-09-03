package icesword.frp

import kotlinx.browser.window

object DebugLog {
    init {
        val g: dynamic = window
        g.DebugLog = this
    }

    private var _isEnabled = false

    val isEnabled: Boolean
        get() = _isEnabled

    fun enable() {
        _isEnabled = true
    }
}

fun debugLog(str: () -> String) {
    if (DebugLog.isEnabled) {
        console.log(str())
    }
}
