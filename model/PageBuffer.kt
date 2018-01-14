package lib.book.refLibrary.model

import android.content.Context

/**
 * Created by Administrator on 2018/1/12.
 */

class PageBuffer(ctx: Context){
    private var array: Array<BackPage> = emptyArray()
    private var ctx: Context
    fun reset(names: List<String>){
        dispose()
        array = Array<BackPage>(names.size, {i -> BackPage(names[i], i, ctx) })
    }
    init {
        this.ctx = ctx
    }
    fun dispose(){
        array.forEach { it.pdfFile.dispose() }
    }
    val size: Int get() = array.size
    fun fromInd(key: Int) = array[key]
}