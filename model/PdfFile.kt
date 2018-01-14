package lib.book.refLibrary.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.SparseBooleanArray
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import java.io.File

/**
 * Created by work on 2018/1/10.
 */

class PdfFile(private val filePath:String,private val ctx: Context) {
    companion object {
        @Volatile
        private var globalPdfiumCore: PdfiumCore? = null

        fun initPdfiumCore(ctx: Context) {
            if (globalPdfiumCore == null) {
                synchronized(PdfFile::class) {
                    if (globalPdfiumCore == null) {
                        globalPdfiumCore = PdfiumCore(ctx)
                    }
                }
            }
        }

        val pdfiumCore: PdfiumCore
            get() {
                return globalPdfiumCore!!
            }

        fun info(any: Any?) {
            any?.let {
                Log.i("_PdfFile", any.toString())
            }
        }

        fun assert(result: Boolean) {
            if (!result) {
                Log.i("_PdfFile", "assert false")
            }
        }
    }

    private lateinit var pdfDocument: PdfDocument
    private var canDisposed = false
    private val openedPages: SparseBooleanArray
    private val pageLock = Any()
    /*var currentInd: Int = -1
        private set*/
    val pageCount: Int by lazy(LazyThreadSafetyMode.NONE) {
        pdfiumCore!!.getPageCount(pdfDocument)
    }

    init {
        initPdfiumCore(ctx)
        val f = File(filePath)
        val pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfDocument = pdfiumCore.newDocument(pfd)
        canDisposed = true
        openedPages = SparseBooleanArray()
    }


    fun pageSize(ind: Int) = pdfiumCore.getPageSize(pdfDocument, ind)
/*    fun takeRect(rect: Rect, pageWidth: Int, pageHeight: Int, ind: Int, fetch: (Bitmap) -> Unit): Boolean {
        if (openPage(ind)) {
            val bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.RGB_565)
            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, ind,
                    -rect.left, -rect.top, pageWidth, pageHeight)
            fetch(bitmap)
            return true
        }
        return false
    }
    fun takeRect(rect: Rect, zoom:Float, ind: Int, fetch: (Bitmap) -> Unit): Boolean {
        if (openPage(ind)) {
            val bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.RGB_565)
            val rectSize = pageSize(ind)
            val pageWidth = (zoom* rectSize.width).toInt()
            val pageHeight = (zoom*rectSize.height).toInt()
            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, ind,
                    -rect.left, -rect.top, pageWidth, pageHeight)
            fetch(bitmap)

            return true
        }
        return false
    }*/
    fun bmpFromRect(rect: Rect, zoom:Float, ind: Int):Bitmap?{
        if (openPage(ind)) {
            val bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.RGB_565)
            val rectSize = pageSize(ind)
            info("$ind,$filePath")
            val pageWidth = (zoom* rectSize.width).toInt()
            val pageHeight = (zoom*rectSize.height).toInt()
            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, ind,
                    -rect.left, -rect.top, pageWidth, pageHeight)
            return bitmap
        }
        return null
    }

    fun dispose() {
        if (canDisposed) pdfiumCore.closeDocument(pdfDocument)
    }

    fun preLoad(indBeg: Int, count: Int = 5) {
        if (indBeg > pageCount) return
        val indEnd = Math.min(pageCount - 1, indBeg + count - 1)
        for (i in indBeg..indEnd) {
            openPage(i)
        }
    }

    private fun openPage(ind: Int): Boolean {
        if (ind < 0) return false
        synchronized(pageLock) {
            if (openedPages.indexOfKey(ind) < 0) {
                try {
                    pdfiumCore.openPage(pdfDocument, ind)
                    openedPages.put(ind, true)
                    return true
                } catch (e: Exception) {
                    openedPages.put(ind, false)
                    //throw ...
                }
            } else {
                return openedPages.get(ind)
            }
        }
        return false
    }

}