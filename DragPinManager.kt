package lib.book.refLibrary

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
/**
 * Created by Administrator on 2018/1/14.
 */

class DragPinManager(host: DrawLayout):
        GestureDetector.OnDoubleTapListener,
        View.OnTouchListener,
        GestureDetector.OnGestureListener,
        ScaleGestureDetector.OnScaleGestureListener {
    private val gestureDetector: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector
    private val host:DrawLayout
    init {
        this.host = host
        gestureDetector = GestureDetector(host.context, this)
        scaleGestureDetector = ScaleGestureDetector(host.context, this)
        host.setOnTouchListener(this)
    }
    fun enable(){canTouch = true}
    fun disable(){canTouch = false}
    private var canTouch = false
    private var scrolling = false
    private var scaling = false

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        return host.onClick(e)
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        host.onZoom(1.2F, e.x, e.y)
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return false
    }

    override fun onTouch(view: View?, e: MotionEvent?): Boolean {
        val event = e!!
        if (!canTouch) return false

        var retVal = scaleGestureDetector.onTouchEvent(event)
        retVal = gestureDetector.onTouchEvent(event) || retVal

        if (event.action == MotionEvent.ACTION_UP) {
            if (scrolling) {
                scrolling = false
                onScrollEnd(event)
            }
        }
        return retVal
    }

    private fun onScrollEnd(event: MotionEvent){

    }

    override fun onShowPress(e: MotionEvent) = Unit

    override fun onSingleTapUp(e: MotionEvent) = false

    override fun onDown(e: MotionEvent?) = true //终止 如动画
    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float)
            = true //不允许惯性滚动 收了

    override fun onLongPress(e: MotionEvent?) = Unit

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        scrolling = true
        host.onMove(distanceX, distanceY)
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        scaling = true
        //info { "onScaleBegin" }
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        //info { "onScaleEnd" }
        //pdfView.loadPages()
        scaling = false
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val factor = detector.scaleFactor
        //val dr = detector?.getScaleFactor()
        host.onZoom(factor, detector.focusX,detector.focusY)

        return true
    }
}
