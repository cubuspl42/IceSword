package icesword.frp

import kotlinx.browser.window

object DebugLog {
    init {
        try {
            val g: dynamic = window
            g.DebugLog = this
        } catch (e: Throwable) {
        }
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
