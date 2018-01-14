package lib.book.refLibrary.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

/**
 * Created by Administrator on 2018/1/12.
 */

/**
 * 使用方法：
 * 1 能否使用 key==key && flag==flag && canUse show(cell)
 * 2 进行请求 if(select(key,flag)) request(cell)
 * 3 进行处理 if(check(tick)){ processs opEnd(tick) }
 *
 * 作用：
 * 1 只有select 操作可以修改key和flag
 * 2 防止重复请求
 * 3 方便快速推出
 *
 * 问题：
 * 1 已选中，但未处理（发送不成功，或消息队列被清空）
 */
class BmpCell(val CellSide: Int) {
    val flag: Int get() = opTick
    var key: Int = -1
        private set
    @Volatile var opTick: Int = -1
        private set
    var canUse: Boolean = false
        private set
        get() {
            enterCount++
            return field
        }
    var enterCount: Int = 0
        private set

    //选中返回true，可以进行请求操作
    fun select(key: Int, flag: Int, colorInt:Int):Boolean {
        if (this.key == key && opTick == flag) return false

        this.key = key
        this.opTick = flag
        canUse = false
        bitmap.eraseColor(colorInt)
        return true
    }
    fun past(backBmp: Bitmap, sRect: Rect, tRect: Rect){
        if(!canUse){
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(backBmp,sRect,tRect, Paint())
        }
    }
    fun check(flagTick:Int):Boolean{
        return flagTick == opTick
    }
    //操作后,操作标志相同，表示成功
    fun opEnd(flagTick: Int) {
        if (!canUse) {// && flagTick == opTick
            canUse = true
            enterCount = 0
            //Log.i("_abc", "完成:${BackMap.keyToString(key)}")
        }
    }
    //过期时调用
    fun reset() {
        canUse = false
        key = -1
        opTick = -1
        enterCount = 0
    }

    private var isCreated = false
    fun dispose() {
        if (isCreated && !bitmap.isRecycled) {
            bitmap.recycle()
        }
    }

    val bitmap: Bitmap by lazy {
        isCreated = true
        Bitmap.createBitmap(CellSide,CellSide,Bitmap.Config.RGB_565)
    }
}