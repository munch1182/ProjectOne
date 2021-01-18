package com.munch.project.testsimple.queue

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import com.munch.lib.helper.LogLog
import com.munch.lib.helper.ServiceBindHelper
import com.munch.lib.helper.clickItem
import com.munch.lib.log
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.R

/**
 * Create by munch1182 on 2020/12/22 15:45.
 */
class TestQueueActivity : TestBaseTopActivity(), QueueService.NotifyListener {

    private val container: ViewGroup by lazy { findViewById(R.id.queue_container) }
    private val tvMsg: TextView by lazy { findViewById(R.id.queue_tv_msg) }
    private val scroll: ScrollView by lazy { findViewById(R.id.queue_scroll) }
    private val helper = ServiceBindHelper.bindActivity(this, QueueService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_simple_activity_test_queue)

        QueueService.UiNotifyManager.INSTANCE.setWhenResume(this, this)

        container.clickItem({
            val pos = it.tag as Int? ?: return@clickItem
            if (!helper.isBind()) {
                toast("binding, wait")
            }
            val s = helper.getService() ?: return@clickItem
            when (pos) {
                0 -> {
                    log("send")
                    QueueService.RequestService.sendMsgTest(s)
                }
                1 -> {
                    log("sendNow")
                    QueueService.RequestService.sendMsgTestNow(s)
                }
                2 -> {
                    tvMsg.text = ""
                    QueueService.RequestService.clearQueue(s)
                }
            }
        }, Button::class.java)

        initLogListener()
    }

    @SuppressLint("SetTextI18n")
    private fun initLogListener() {
        LogLog.setListener(this) { _, msg ->
            runOnUiThread {
                tvMsg.text = "${tvMsg.text}\r\n${msg.split("---")[0]}"
                scroll.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
    }

    override fun setPageBg(view: View) {
        /*super.setPageBg(view)*/
    }

    override fun update(what: Int, obj: Any?) {
        log("receive", what, obj)
        if (what == QueueService.MSG.UI_IDLE) {
            tvMsg.text = "消息队列已空"
            tvMsg.postDelayed({
                tvMsg.text = ""
            }, 5000)
        }
    }


}