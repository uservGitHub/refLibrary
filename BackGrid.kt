package lib.book.refLibrary

import android.graphics.*
import android.util.Log
import lib.book.refLibrary.model.PageGrid
import lib.book.refLibrary.model.PagePosition
import kotlin.coroutines.experimental.*

/**
 * Created by Administrator on 2018/1/7.
 */

class BackGrid(cellSide: Int=BackGrid.CellSide) {
    companion object {
        inline fun buildKey(rowInd: Int, colInd: Int, type: Int = 0)
                = (rowInd shl 14) or colInd

        const val MaxInd = (1 shl 14) - 1
        const val InValid = -1
        inline val Int.rowInd: Int
            get() = (this ushr 14) and MaxInd
        inline val Int.colInd: Int
            get() = this and MaxInd

        inline fun keyToString(key: Int) = "(${key.rowInd},${key.colInd})"
        //[0,maxValue], [a1,a2)
        inline fun boundUpper(a: Int, b: Int, value: Int, maxValue: Int): Int {
            if (value < a + b) return 0
            var result = value / (a + b)

            if (result > maxValue) return InValid
            return result
        }

        //[0,maxValue], (a1,a2]
        inline fun boundLower(a: Int, b: Int, value: Int, maxValue: Int): Int {
            if (value <= a) return InValid
            var adValue = value - a
            var result = adValue / (a + b)
            if (adValue.rem(a + b) == 0) result--
            if (result > maxValue) result = maxValue
            return result
        }

        private var CellSide = 200

        fun info(any: Any?) {
            any?.let {
                Log.i("_BackGrid", any.toString())
            }
        }

        fun assert(result: Boolean) {
            if (!result) {
                Log.i("_BackGrid", "assert false")
            }
        }
    }

    var pageGrid: PageGrid
        private set

    init {
        if (cellSide != CellSide) {
            CellSide = cellSide
        }
        pageGrid = PageGrid(0, 4, 4, 400, 600, 2, 0, Color.GRAY)
    }

    //region    ZoomListener
    private var onZoomFlagListener: ((Int, Float) -> Unit)? = null

    fun setOnZoomListener(listener: (zoomFlag: Int, zooom: Float) -> Unit) {
        onZoomFlagListener = listener
    }
    //endregion

    //region    PagePosition
    private var pagePosition = PagePosition(0, 0, 0, -1)

    fun loadPagePosition(pagePosition: PagePosition): Boolean {
        this.pagePosition = pagePosition
        return true
    }

    fun savePagePosition() = pagePosition.copy()
    //endregion

    //region    pageCount width height innerEndX innerEndY pageColCount pageRowCount
    var pageCount: Int = 0
        private set
    val width: Int get() = calcWidth(pageColCount)
    val height: Int get() = calcHeight(pageRowCount)

    private inline fun pageLeft(colInd:Int) = pageGrid.pageHorSize * (colInd + 1) + (pageGrid.pagePerWidth * colInd * zoom).toInt()
    private inline fun pageRight(colInd:Int) = pageGrid.pageHorSize * (colInd + 1) + (pageGrid.pagePerWidth * (colInd+1) * zoom).toInt()
    private inline fun pageTop(rowInd: Int) = pageGrid.pageVerSize * (rowInd + 1) + (pageGrid.pagePerHeight * rowInd * zoom).toInt()
    private inline fun pageBottom(rowInd: Int) = pageGrid.pageVerSize * (rowInd + 1) + (pageGrid.pagePerHeight * (rowInd+1) * zoom).toInt()
    private inline fun calcWidth(colCount: Int) = pageGrid.pageHorSize * (colCount + 1) + (pageGrid.pagePerWidth * colCount * zoom).toInt()
    private inline fun calcHeight(rowCount: Int) = pageGrid.pageVerSize * (rowCount + 1) + (pageGrid.pagePerHeight * rowCount * zoom).toInt()
    val innerEndX: Int
        get() {
            var value = pageCount.rem(pageColCount)
            if (value == 0) value = pageColCount
            return calcWidth(value)
        }
    val innerEndY: Int get() = calcHeight(pageRowCount - 1)
    var pageColCount = 0
        private set
    var pageRowCount = 0
        private set
    //endregion

    //region    zoom zoomFlag zoomTo
    var zoom: Float = 1.0F
        private set(value) {
            if (value == field) return
            field = value
            ++zoomFlag
            if (zoomFlag == Int.MAX_VALUE) zoomFlag = 1
            onZoomFlagListener?.invoke(zoomFlag, field)
        }
    var zoomFlag: Int = 1
        private set

    fun zoomTo(zoom: Float) {
        this.zoom = zoom
    }
    //endregion

    //region visibleX visibleY visibleWidth visibleHeight
    var visibleX: Int = 0
        private set
    var shockX = 0F
        private set
    var visibleY: Int = 0
        private set
    var shockY = 0F
        private set
    var visibleWidth = 0
        private set
    var visibleHeight = 0
        private set
    //endregion

    //region    resetVisible hasVisible
    fun resetVisible(width: Int, height: Int) {
        visibleWidth = width
        visibleHeight = height
    }

    val hasVisible: Boolean get() = visibleWidth > 0 && visibleHeight > 0
    //endregion

    //region    load
    fun load(pageCount: Int, pageGrid: PageGrid? = null) {
        preLoad()
        this.pageCount = pageCount
        pageGrid?.let {
            this.pageGrid = pageGrid.copy()
        }
        calcBackGrid(this.pageGrid.pageCols, this.pageGrid.pageRows)
    }

    private fun preLoad() {
        onZoomFlagListener = null
        zoom = 1F
        zoomFlag = 0
        visibleX = 0
        shockX = 0F
        visibleY = 0
        shockY = 0F
    }

    private fun calcBackGrid(colCount: Int, rowCount: Int = 0) {
        if (colCount > 0) {
            this.pageColCount = colCount
            this.pageRowCount = pageCount / colCount + if (pageCount.rem(colCount) == 0) 0 else 1
        } else if (rowCount > 0) {
            this.pageRowCount = rowCount
            this.pageColCount = pageCount / rowCount + if (pageCount.rem(rowCount) == 0) 0 else 1
        } else {
            this.pageColCount = Math.pow(pageCount.toDouble(), 0.5).toInt()
            this.pageRowCount = pageCount / colCount + if (pageCount.rem(colCount) == 0) 0 else 1
        }
    }
    //endregion

    //region    moveTo moveOffset
    fun moveTo(x: Int, y: Int) {
        visibleX = x
        visibleY = y
        shockX = visibleX.toFloat()
        shockY = visibleY.toFloat()
    }

    fun moveXTo(x: Int) {
        visibleX = x
        shockX = visibleX.toFloat()
    }

    fun moveYTo(y: Int) {
        visibleY = y
        shockY = visibleY.toFloat()
    }

    fun moveOffset(deltaX: Float, deltaY: Float, checkOutBound: Boolean = false) {
        if (checkOutBound) {
            val nums = arrayOf(0F, (width - visibleWidth).toFloat(), 0F, (height - visibleHeight).toFloat())
            if (deltaX > 0 && shockX < nums[1]) {
                shockX += deltaX
                if (shockX > nums[1]) shockX = nums[1]
            }
            if (deltaX < 0 && shockX > nums[0]) {
                shockX += deltaX
                if (shockX < nums[0]) shockX = nums[0]
            }
            if (deltaY > 0 && shockY < nums[3]) {
                shockY += deltaY
                if (shockY > nums[3]) shockY = nums[3]
            }
            if (deltaY < 0 && shockY > nums[2]) {
                shockY += deltaY
                if (shockY < nums[2]) shockY = nums[2]
            }
        } else {
            shockX += deltaX
            shockY += deltaY
        }

        visibleX = shockX.toInt()
        visibleY = shockY.toInt()
    }

    //endregion

    //region    pageInds cellKeys
    val cellKeys = buildSequence<Int> {
        val xBeg = if (visibleX < 0) 0 else visibleX
        val yBeg = if (visibleY < 0) 0 else visibleY
        val xEnd = Math.min(visibleX + visibleWidth, width)
        val yEnd = Math.min(visibleY + visibleHeight, height)
        if (xBeg >= xEnd || yBeg >= yEnd) return@buildSequence

        val cellColMin = toCellLowerInd(xBeg)
        val cellColMax = toCellUpperInd(xEnd)
        val cellRowMin = toCellLowerInd(yBeg)
        val cellRowMax = toCellUpperInd(yEnd)

        for (row in cellRowMin..cellRowMax){
            for (col in cellColMin..cellColMax){
                yield(buildKey(row, col))
            }
        }

/*        return Rect(toCellLowerValue(xBeg),
                toCellLowerValue(yBeg),
                toCellUpperValue(xEnd),
                toCellUpperValue(yEnd))*/
    }
    val pageInds = buildSequence<Int> {
        val pageWidth = (zoom * pageGrid.pagePerWidth).toInt()
        val maxColInd = pageColCount - 1
        val colMin = boundUpper(pageGrid.pageHorSize, pageWidth, visibleX, maxColInd)
        if (colMin == BackGrid.InValid) return@buildSequence
        val colMax = boundLower(pageGrid.pageHorSize, pageWidth, visibleX + visibleWidth, maxColInd)
        if (colMax == BackGrid.InValid) return@buildSequence
        if (colMin > colMax) return@buildSequence
        val pageHeight = (zoom * pageGrid.pagePerHeight).toInt()
        val maxRowInd = pageRowCount - 1
        val rowMin = boundUpper(pageGrid.pageVerSize, pageHeight, visibleY, maxRowInd)
        if (rowMin == BackGrid.InValid) return@buildSequence
        val rowMax = boundLower(pageGrid.pageVerSize, pageHeight, visibleY + visibleHeight, maxRowInd)
        if (rowMax == BackGrid.InValid) return@buildSequence
        if (rowMin > rowMax) return@buildSequence
        var ind = 0
        for (row in rowMin..rowMax) {
            for (col in colMin..colMax) {
                ind = row * pageColCount + col
                if (ind < pageCount) yield(ind)
                else return@buildSequence
            }
        }
    }
    //endregion

    //region    rectFromAllVisibleCell

    val rectFromAllVisibleCell: Rect?
        get() {
            val xBeg = if (visibleX < 0) 0 else visibleX
            val yBeg = if (visibleY < 0) 0 else visibleY
            val xEnd = Math.min(visibleX + visibleWidth, width)
            val yEnd = Math.min(visibleY + visibleHeight, height)
            if (xBeg >= xEnd || yBeg >= yEnd) return null

            return Rect(toCellLowerValue(xBeg),
                    toCellLowerValue(yBeg),
                    toCellUpperValue(xEnd),
                    toCellUpperValue(yEnd))
        }

    private inline fun toCellUpperInd(value: Int) = value / CellSide
    private inline fun toCellLowerInd(value: Int): Int {
        if (value < CellSide) return 0
        var ind = value / CellSide
        if (value.rem(CellSide) == 0) ind--
        return ind
    }

    private inline fun toCellUpperValue(value: Int): Int {
        val rem = value.rem(CellSide)
        if (rem == 0) return value
        return value + CellSide - rem
    }

    private inline fun toCellLowerValue(value: Int): Int {
        val rem = value.rem(CellSide)
        if (rem == 0) return value
        return value - rem
    }
    //endregion

    //region    rectVisible
    val rectVisible:Rect
        get() = Rect(visibleX,visibleY,visibleX+visibleWidth,visibleY+visibleHeight)
    //endregion

    //region    rectFromPage
    private inline fun rectFromPage(ind: Int): Rect {
        val row = indToRowInd(ind)
        var col = indToColInd(ind)
        return Rect(pageLeft(col),pageTop(row),
                pageRight(col),pageBottom(row))
    }

    private inline fun indToRowInd(ind: Int) = ind / pageColCount
    private inline fun indToColInd(ind: Int) = ind.rem(pageColCount)
    //endregion

    //region    rectFromCell pageIndFromScreen
    fun pageIndFromScreen(x: Float, y: Float): Int {
        var vx = x.toInt() + visibleX
        var vy = y.toInt() + visibleY
        pageInds.forEach {
            val rect = rectFromPage(it)
            if (rect.contains(vx, vy)) return it
        }
        return InValid
    }

    private inline fun rectFromCell(key: Int): Rect {
        val x = key.rowInd * CellSide
        val y = key.colInd * CellSide
        return Rect(x, y, x + CellSide, y + CellSide)
    }
    //endregion

    //region    visibleProcess: build cellBitmap
    fun visibleProcess(pagePorc: (pageInd: Int, rect: Rect) -> Bitmap,
                       cellCanProc: (cellKey: Int) -> Boolean,
                       cellProc: (cellKey: Int, backBmp: Bitmap, sRect: Rect, tRect: Rect) -> Unit,
                       procEnd:(cellKey:Int) -> Unit,
                       finished:()->Unit): Boolean {
        if (!hasVisible) return false

        //region    check valid point
        var xBeg = if (visibleX < 0) 0 else visibleX
        var yBeg = if (visibleY < 0) 0 else visibleY
        var xEnd = Math.min(visibleX + visibleWidth, width)
        var yEnd = Math.min(visibleY + visibleHeight, height)
        if (xBeg >= xEnd || yBeg >= yEnd) return false
        //endregion

        var cellColMin = toCellUpperInd(xBeg)
        var cellColMax = toCellLowerInd(xEnd)
        var cellRowMin = toCellUpperInd(yBeg)
        var cellRowMax = toCellLowerInd(yEnd)

        //region    big rectangle
        xBeg = cellColMin * CellSide
        xEnd = (cellColMax + 1) * CellSide
        yBeg = cellRowMin * CellSide
        yEnd = (cellRowMax + 1) * CellSide
        //endregion

        val pageWidth = (zoom * pageGrid.pagePerWidth).toInt()
        val pageHeight = (zoom * pageGrid.pagePerHeight).toInt()
        val maxColInd = pageColCount - 1
        val maxRowInd = pageRowCount - 1
        val pageColMin = boundUpper(pageGrid.pageHorSize, pageWidth, xBeg, maxColInd)
        val pageColMax = boundLower(pageGrid.pageHorSize, pageWidth, xEnd, maxColInd)
        val pageRowMin = boundUpper(pageGrid.pageVerSize, pageHeight, yBeg, maxRowInd)
        val pageRowMax = boundLower(pageGrid.pageVerSize, pageHeight, yEnd, maxRowInd)

        var ind = 0
        //cell boundary
        val visRect = Rect(xBeg, yBeg, xEnd, yEnd)
        info("Begin --")
        info(visRect)
        var hasIntersect = true
        for (row in pageRowMin..pageRowMax) {
            for (col in pageColMin..pageColMax) {
                ind = row * pageColCount + col
                if (ind < pageCount) {
                    val bigRect = Rect(visRect)
                    val pageRect = Rect(pageLeft(col), pageTop(row), pageRight(col), pageBottom(row))
                    info("page=$ind:${pageRect.toString()}")
                    hasIntersect = bigRect.intersect(pageRect)
                    assert(hasIntersect)
                    //region    cellRects completed cell rendering
                    //bigRect already changed
                    val pageCellColMin = toCellUpperInd(bigRect.left)
                    val pageCellColMax = toCellLowerInd(bigRect.right)
                    val pageCellRowMin = toCellUpperInd(bigRect.top)
                    val pageCellRowMax = toCellLowerInd(bigRect.bottom)

                    bigRect.offset(-pageRect.left, -pageRect.top)
                    val pageBmp by lazy {
                        pagePorc(ind, bigRect)
                    }

                    for (cellRow in pageCellRowMin..pageCellRowMax) {
                        for (cellCol in pageCellColMin..pageCellColMax) {
                            val key = buildKey(cellRow, cellCol)
                            if (cellCanProc(key)) {
                                val cellRect = Rect(cellRow * CellSide, cellCol * CellSide, (cellRow + 1) * CellSide, (cellCol + 1) * CellSide)
                                info("cell=${keyToString(key)}:")//${cellRect.toString()}
                                val interRect = Rect(cellRect)
                                hasIntersect = interRect.intersect(pageRect)
                                if(!hasIntersect){
                                    assert(hasIntersect)
                                }

                                val sRect = Rect(interRect).apply { offset(-pageRect.left, -pageRect.top) }
                                val tRect = Rect(interRect).apply { offset(-cellRect.left, -cellRect.top) }
                                cellProc(key, pageBmp, sRect, tRect)
                            }else{
                                info("cell=!${keyToString(key)}:")
                            }
                        }
                    }
                    //endregion
                }
            }
        }

        for (row in cellRowMin..cellRowMax){
            for(col in cellColMin..cellColMax){
                procEnd(buildKey(row,col))
            }
        }
        info("End --")
        finished()
        return true
    }
    //endregion

    //region    drawPageOutline
    fun drawPageOutline(canvas: Canvas, drawPage: (pageInd: Int, canvas: Canvas, pageRect: Rect, clipPageRect: Rect) -> Unit) {
        var horPaint: Paint? = null
        var verPaint: Paint? = null
        if (pageGrid.pageHorSize > 0) {
            horPaint = Paint().apply {
                color = pageGrid.pageMarginColor
                style = Paint.Style.STROKE
                strokeWidth = pageGrid.pageHorSize.toFloat()
            }
        }
        if (pageGrid.pageVerSize > 0) {
            verPaint = Paint().apply {
                color = pageGrid.pageMarginColor
                style = Paint.Style.STROKE
                strokeWidth = pageGrid.pageVerSize.toFloat()
            }
        }

        val rectVisible = Rect(visibleX, visibleY, visibleX + visibleWidth, visibleY + visibleHeight)
        canvas.translate(-shockX, -shockY)
        pageInds.forEach {
            val pageRect = rectFromPage(it)
            horPaint?.let {
                val x1 = pageRect.left.toFloat() - it.strokeWidth
                val x2 = pageRect.right.toFloat() + it.strokeWidth
                val yTop = pageRect.top.toFloat() - it.strokeWidth / 2
                canvas.drawLine(x1, yTop, x2, yTop, it)
                val yBottom = pageRect.bottom.toFloat() + it.strokeWidth / 2
                canvas.drawLine(x1, yBottom, x2, yBottom, it)
            }
            verPaint?.let {
                val y1 = pageRect.top.toFloat() - it.strokeWidth
                val y2 = pageRect.bottom.toFloat() + it.strokeWidth
                val xLeft = pageRect.left.toFloat() - it.strokeWidth / 2
                canvas.drawLine(xLeft, y1, xLeft, y2, it)
                val xRight = pageRect.right.toFloat() + it.strokeWidth / 2
                canvas.drawLine(xRight, y1, xRight, y2, it)
            }
            val clipPageRect = Rect(pageRect)
            val isIntersect = clipPageRect.intersect(rectVisible)
            assert(isIntersect)
            if(!isIntersect){
                info(it)
            }
            drawPage(it, canvas, pageRect, clipPageRect)
        }
        canvas.translate(shockX, shockY)
        //info(pageInds.toList())
    }
    //endregion

    //region    fitPageWidth fitFullWidth
    var fitPageWidth:Boolean = false
        set(value) {
            field = value
            if(field){
                val pageInd = pageIndFromScreen(visibleWidth.toFloat()/2,visibleHeight.toFloat()/2)
                if(pageInd != InValid) {
                    fitFullWidth = false
                    zoom = (visibleWidth - pageGrid.pageHorSize * 2) / pageGrid.pagePerWidth.toFloat()
                    val rect = rectFromPage(pageInd)
                    moveTo(rect.left-pageGrid.pageHorSize, rect.top-pageGrid.pageVerSize)
                }
            }
        }
    var fitFullWidth: Boolean = false
        set(value) {
            field = value
            if (field) {
                fitPageWidth = false
                zoom = (visibleWidth - pageGrid.pageHorSize * (pageColCount + 1)) / (pageGrid.pagePerWidth * pageColCount).toFloat()
            }
        }
    var fitPageHeight: Boolean = false
        set(value) {
            field = value
            if (field) {
                val pageInd = pageIndFromScreen(visibleWidth.toFloat()/2,visibleHeight.toFloat()/2)
                if(pageInd != -1) {
                    fitFullHeight = false
                    zoom = (visibleHeight - pageGrid.pageVerSize * 2) / pageGrid.pagePerHeight.toFloat()
                    val rect = rectFromPage(pageInd)
                    moveTo(rect.left-pageGrid.pageHorSize, rect.top-pageGrid.pageVerSize)
                }
            }
        }
    var fitFullHeight: Boolean = false
        set(value) {
            field = value
            if (field) {
                fitPageHeight = false
                zoom = (visibleHeight - pageGrid.pageVerSize * (pageRowCount + 1)) / (pageGrid.pagePerHeight * pageRowCount).toFloat()
            }
        }
    //endregion
}










