package icesword.frp

class CellLoop<A>(placeholderValue: A) {
    private val _cell = MutCell(Cell.constant(placeholderValue))

    private var isClosed = false

    val asCell: Cell<A> = _cell.switch()

    fun close(cell: Cell<A>) {
        if (isClosed) {
            throw IllegalStateException()
        }

        _cell.set(cell)

        isClosed = true
    }
}
