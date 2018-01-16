package lib.book.refLibrary.objuml

import kotlin.coroutines.experimental.*
/**
 * Created by work on 2018/1/16.
 */
data class Obj(val id:Int)

class PackSystem(count:Int) {
    private val objArray: Array<Obj>

    init {
        objArray = Array<Obj>(count, { i -> Obj(i + 1) })
    }

    var endToEnd: Boolean = false
    val count: Int get() = objArray.size
    var index: Int = 0
        private set
    fun jump(ind: Int) = if (ind<0) index = 0 else index = ind.rem(count)
    val objs = buildSequence<Obj> {
        while (count>0) {
            if (count == index) {
                if (endToEnd) index = 0
                else return@buildSequence
            }
            yield(objArray[index++])
        }
    }
}