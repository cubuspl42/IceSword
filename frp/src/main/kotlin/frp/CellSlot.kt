package icesword.frp

class CellSlot<A> {
    private val _linkedCell = MutCell<Cell<A>?>(null)

    val linkedCell: Cell<Cell<A>?> = _linkedCell

    fun link(cell: Cell<A>, till: Till) {
        if (_linkedCell.sample() != null) {
            throw IllegalStateException("Cannot link to already linked slot")
        }

        _linkedCell.set(cell)

        till.subscribe {
            _linkedCell.set(null)
        }
    }
}
