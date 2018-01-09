package lib.book.refLibrary

import android.graphics.Rect
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
    }

    var pageGrid: PageGrid
        private set

    init {
        if (cellSide != CellSide) {
            CellSide = cellSide
        }
        pageGrid = PageGrid(0, 4, 4, 400, 600, 2, 0)
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
    private var shockX = 0F
    var visibleY: Int = 0
        private set
    private var shockY = 0F
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

    //region    pageInds
    val pageInds = buildSequence<Int> {
        val pageWidth = (zoom * pageGrid.pagePerWidth).toInt()
        val maxColInd = pageColCount - 1
        val colMin = boundUpper(pageGrid.pageHorSize, pageWidth, visibleX, maxColInd)
        val colMax = boundLower(pageGrid.pageHorSize, pageWidth, visibleX + visibleWidth, maxColInd)
        if (colMin > colMax) return@buildSequence
        val pageHeight = (zoom * pageGrid.pagePerHeight).toInt()
        val maxRowInd = pageRowCount - 1
        val rowMin = boundUpper(pageGrid.pageVerSize, pageHeight, visibleY, maxRowInd)
        val rowMax = boundLower(pageGrid.pageVerSize, pageHeight, visibleY + visibleHeight, maxRowInd)
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

    //region    rectFromPage
    fun rectFromPage(ind:Int):Rect {
        val row = indToRowInd(ind)
        var col = indToColInd(ind)
        return Rect(calcWidth(row),
                calcHeight(col),
                calcWidth(row + 1),
                calcHeight(col + 1))
    }
    private inline fun indToRowInd(ind:Int) = ind/pageColCount
    private inline fun indToColInd(ind:Int) = ind.rem(pageColCount)
    //endregion

    //region    rectFromCell
    private inline fun rectFromCell(key:Int):Rect{
        val x = key.rowInd* CellSide
        val y = key.colInd * CellSide
        return Rect(x,y,x+ CellSide,y+ CellSide)
    }
    //endregion
}










