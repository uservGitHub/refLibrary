package lib.book.refLibrary

import android.view.MotionEvent;
import android.content.Context
import android.graphics.*
import android.os.HandlerThread
import android.util.Log
import android.widget.RelativeLayout
import lib.book.refLibrary.model.CellBuffer
import lib.book.refLibrary.model.PageBuffer
import lib.book.refLibrary.model.PageGrid

/**
 * Created by Administrator on 2018/1/13.
 */

class DrawLayout(ctx:Context):RelativeLayout(ctx){
    companion object {
        fun info(any: Any?) {
            any?.let {
                Log.i("_DrawLayout", any.toString())
            }
        }

        fun assert(result: Boolean) {
            if (!result) {
                Log.i("_DrawLayout", "assert false")
            }
        }
    }
    val backGrid: BackGrid
    var backRenderManager: BackRenderManager? = null
    var dragPinManager: DragPinManager
    val renderingHandlerThread: HandlerThread
    val pageBuffer:PageBuffer
    val cellBuffer:CellBuffer
    val cellSide:Int by lazy {
        300
    }
    var zoomFlag: Int = -1
        private set
    var zoom:Float = 1.0F
        private set
    var isLoad = false
        private set
    init {
        setWillNotDraw(false)
        dragPinManager = DragPinManager(this)
        backGrid = BackGrid(cellSide)
        pageBuffer = PageBuffer(this.context)
        cellBuffer = CellBuffer(12,12,cellSide)
        renderingHandlerThread = HandlerThread("rendercell")
    }

    override fun onDraw(canvas: Canvas) {
        if (isInEditMode()) {
            return;
        }
        canvas.drawColor(Color.WHITE)

        if(!isLoad) return

        backGrid.resetVisible(width,height)
        val pagePaint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.BLUE
        }
        backGrid.drawPageOutline(canvas,{
            pageInd: Int, canvas: Canvas, pageRect: Rect, clipPageRect: Rect ->
                canvas.drawRect(pageRect, pagePaint)
        })
    }
    fun load(names:List<String>){
        try {
            dragPinManager.disable()
            pageBuffer.reset(names)
            val pageGrid = PageGrid(0,4,4,600,800,3,0, Color.RED)
            backGrid.load(names.size, pageGrid)
            backGrid.setOnZoomListener { zoomFlag, zooom ->
                this.zoomFlag = zoomFlag
                this.zoom = zoom
            }
            cellBuffer.reset()
            if (!renderingHandlerThread.isAlive) renderingHandlerThread.start()
            backRenderManager?.canRunning = false
            if(backRenderManager == null){
                backRenderManager = BackRenderManager(renderingHandlerThread.looper,
                        {
                            info("已经过期")
                        },
                        {
                            timeTick: Int, data: Any? ->
                            if(timeTick == zoomFlag){
                                preRenderCells(zoomFlag)
                            }
                        })
            }
            isLoad = true
            dragPinManager.enable()
            invalidate()
        }catch (e:Exception){
            info(e)
        }
    }
    private fun preRenderCells(zoomFlag:Int){
        backGrid.visibleProcess({ pageInd: Int, rect: Rect ->
            pageBuffer.fromInd(pageInd).bmpFromRect(pageInd, rect, zoom) ?:
                    Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.RGB_565).apply {
                        eraseColor(Color.YELLOW)
                    }
        },{
            cellKey: Int -> cellBuffer.fromKey(cellKey).select(cellKey,zoomFlag,Color.GREEN)
        },{
            cellKey: Int, backBmp: Bitmap, sRect: Rect, tRect: Rect ->
             cellBuffer.fromKey(cellKey).past(backBmp,sRect, tRect)
        },{
            cellKey: Int ->  cellBuffer.fromKey(cellKey).opEnd(zoomFlag)
        })
    }

    fun onMove(deltaX:Float, deltaY:Float){
        backGrid.moveOffset(deltaX,deltaY)
        invalidate()
    }
    fun onZoom(deltaZoom: Float, centerX:Float, centerY:Float){

    }
    fun onLoad(){

    }
    fun onClick(e: MotionEvent):Boolean{
        /*val pageInd = backMap.getPageIndFromScreen(e.x,e.y)
        val page = pageBuffer.fromInd(pageInd)
        if(page.rawFile.next()){
            synchronized(lockKeys) {
                backMap.getCellKeyFromInd(pageInd).forEach {
                    cellBuffer.fromKey(it).reset()
                }
            }
            invalidate()
        }*/
        return true
    }
}