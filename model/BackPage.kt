package lib.book.refLibrary.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect

/**
 * Created by Administrator on 2018/1/12.
 */

class BackPage(val name:String, val ind:Int, val ctx: Context) {
    val pdfFile: PdfFile by lazy(LazyThreadSafetyMode.NONE) {
        PdfFile(name, ctx)
    }
    var left: Int = 0
    var top: Int = 0
    fun tryTakeRect(pageInd:Int, rect: Rect, zoom: Float, fetch: (bmp: Bitmap) -> Unit): Boolean {
        rect.offset((zoom * left).toInt(), (zoom * top).toInt())
        return pdfFile.takeRect(rect, zoom, pageInd, fetch)
    }
    fun bmpFromRect(pageInd:Int, rect: Rect, zoom: Float): Bitmap? {
        rect.offset((zoom * left).toInt(), (zoom * top).toInt())
        //return pdfFile.takeRect(rect, zoom, pageInd, fetch)
        return pdfFile.bmpFromRect(rect, zoom, pageInd)
    }
}