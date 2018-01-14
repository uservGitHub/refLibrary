package lib.book.refLibrary.model

import lib.book.refLibrary.BackGrid.Companion.rowInd
import lib.book.refLibrary.BackGrid.Companion.colInd

/**
 * Created by Administrator on 2018/1/12.
 */
class CellBuffer(val rowLength:Int, val colLength:Int, cellSid:Int) {
    private val array: Array<BmpCell>
    private val lock = Any()
    init {
        array = Array<BmpCell>(rowLength * colLength, { BmpCell(cellSid) })
    }

    //private val array = Array<BmpCell>(rowLength * colLength, { BmpCell() })
    fun fromKey(key: Int) = synchronized(lock) {
        array[key.rowInd.rem(rowLength) * colLength + key.colInd.rem(colLength)]
    }

    fun reset() {
        synchronized(lock){
            array.forEach { it.reset() }
        }
    }
    fun reset(rows:Int,cols:Int) {
        this.rows = rows
        this.cols = cols
        //...
    }
    private var cols:Int = 0
    private var rows:Int = 0
    fun dispose(){
        synchronized(lock){
            array.forEach { it.dispose() }
        }
    }
}