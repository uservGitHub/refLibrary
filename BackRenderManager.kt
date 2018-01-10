package lib.book.refLibrary

import android.os.Looper
import android.os.Message

/**
 * Created by work on 2018/1/10.
 */

class BackRenderManager(looper: Looper,
                        val expire:()->Unit,
                        val task:(timeTick:Int, data:Any?)->Unit):android.os.Handler(looper) {
    companion object {
        val MSG_RENDER_BACKTASK = 1
    }

    //back request interrupt the front
    @Volatile
    var runTimeTick = 0
        private set

    var canRunning: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (!field) {
                removeMessages(MSG_RENDER_BACKTASK)
                //取消正在执行的任务
                //...
            } else {
                ++runTimeTick
                if (runTimeTick == Int.MAX_VALUE) runTimeTick = 1
            }
        }

    fun request(timeTick: Int, data: Any?) {
        if (canRunning) {
            val msg = TaskMsg(timeTick, data, runTimeTick)
            sendMessage(obtainMessage(MSG_RENDER_BACKTASK, msg))
        }
    }

    override fun handleMessage(msg: Message) {
        if (msg.what == MSG_RENDER_BACKTASK) {
            val taskMsg = msg.obj as TaskMsg
            if (taskMsg.selfTimeTick != runTimeTick) {
                expire()
                return
            }
            task(taskMsg.taskTimeTick, taskMsg.data)
        }
    }

    private data class TaskMsg(val taskTimeTick: Int, val data: Any?, val selfTimeTick: Int)
}