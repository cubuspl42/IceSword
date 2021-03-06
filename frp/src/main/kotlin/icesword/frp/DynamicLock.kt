package icesword.frp

class DynamicLock<Process : Any> {
    private val _owningProcess = MutCell<Process?>(null)

    val owningProcess: Cell<Process?>
        get() = _owningProcess

    val isLocked: Cell<Boolean> =
        _owningProcess.map { it != null }

    suspend fun runNow(
        process: Process,
        run: suspend () -> Unit,
    ) {
        val owningProcess = this.owningProcess.sample()
        if (owningProcess != null) {
            throw IllegalStateException("Lock is currently held by $owningProcess")
        }

        try {
            _owningProcess.set(process)
            run()
        } finally {
            _owningProcess.set(null)
        }
    }

    suspend fun runNowIfFree(
        process: Process,
        run: suspend () -> Unit,
    ) {
        val owningProcess = this.owningProcess.sample()
        if (owningProcess != null) {
            return
        }

        try {
            _owningProcess.set(process)
            run()
        } finally {
            _owningProcess.set(null)
        }
    }
}
