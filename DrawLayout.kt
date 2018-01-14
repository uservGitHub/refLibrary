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
        /*backGrid.drawPageOutline(canvas,{
            pageInd: Int, canvas: Canvas, pageRect: Rect, clipPageRect: Rect ->
                canvas.drawRect(pageRect, pagePaint)
        })*/

        if(allReady) {
            allReady = false
            canvas.translate(-backGrid.shockX, -backGrid.shockY)
            backGrid.cellKeys.forEachIndexed { index, i ->
                info(index)
                val cell = cellBuffer.fromKey(i)
                info(BackGrid.keyToString(i))
                //val canDraw = cell.tryDraw(canvas, zoomFlag)
                canvas.drawBitmap(cell.bitmap,Rect(0,0,cellSide,cellSide) ,cell.rect, Paint())
                drawText(canvas, cell.rect, BackGrid.keyToString(i))
            }
            canvas.translate(backGrid.shockX, backGrid.shockY)
        }
    }
    private val textPaint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 38F
        color = Color.RED
    }
    private val fontDeltaHeight: Int
        get() {
            val f = textPaint.fontMetricsInt
            return (f.top + f.bottom) / 2
        }
    private fun drawText(canvas: Canvas, rect: Rect, msg: Any? = null) {
        //canvas.drawRect(rect, redPaint)
        msg?.let {
            canvas.drawText(msg.toString(), rect.centerX().toFloat(), (rect.centerY() - fontDeltaHeight).toFloat(), textPaint)
            info(rect)
        }
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
                                preRenderCells(zoomFlag, zoom)
                            }
                        })
            }
            isLoad = true
            backRenderManager?.canRunning = true
            onLoad()
            dragPinManager.enable()
            invalidate()
        }catch (e:Exception){
            info(e)
        }
    }
    private fun preRenderCells(zoomFlag:Int, zoom:Float){
        info("preRenderCells")
        backGrid.resetVisible(width, height)
        backGrid.visibleProcess({ pageInd: Int, rect: Rect ->
            pageBuffer.fromInd(pageInd).bmpFromRect(0, rect, zoom) ?:
                    Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.RGB_565).apply {
                        eraseColor(Color.YELLOW)
                    }
        },{
            cellKey: Int -> cellBuffer.fromKey(cellKey).mutilSelect(cellKey,zoomFlag,Color.GREEN)
        },{
            cellKey: Int, backBmp: Bitmap, sRect: Rect, tRect: Rect ->
             cellBuffer.fromKey(cellKey).past(backBmp,sRect, tRect)
        },{
            cellKey: Int ->
            cellBuffer.fromKey(cellKey).opEnd(zoomFlag)
        },{
            info("finish")
            this@DrawLayout.post {
                allReady = true
                this@DrawLayout.invalidate()
            }
        })
    }
    @Volatile
    private var allReady = false

    //region    move zoom
    fun moveOffset(deltaX:Float, deltaY:Float){
        backGrid.moveOffset(deltaX,deltaY)
        //invalidate()
        onLoad()
    }
    fun moveTo(x:Float, y:Float){
        backGrid.moveTo(x.toInt(),y.toInt())
        invalidate()
    }
    fun zoomOffset(dr:Float){
        val visRect = backGrid.rectVisible
        zoomOffset(dr, visRect.exactCenterX(), visRect.exactCenterY())
    }
    fun zoomOffsetFromScreen(dr:Float,cx:Float,cy:Float){
        val x = cx+backGrid.visibleX
        val y = cy+backGrid.visibleY
        zoomOffset(dr,x,y)
    }
    private fun zoomOffset(dr:Float,cx:Float,cy:Float){
        backGrid.moveOffset(-cx, -cy)
        backGrid.zoomTo(backGrid.zoom*dr)
        backGrid.moveOffset(cx*dr,cy*dr)
        //invalidate()
        onLoad()
    }
    //endregion

    fun onLoad(){
        //preRenderCells(zoomFlag, zoom)
        backRenderManager!!.request(zoomFlag, null)
    }
    fun onClick(e: MotionEvent):Boolean{
        zoomOffsetFromScreen(0.8F, e.x, e.y)
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