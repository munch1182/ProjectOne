package com.munch.project.testsimple.net

import android.net.LocalServerSocket
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.os.SystemClock
import com.munch.lib.closeWhenEnd
import com.munch.lib.helper.ThreadHelper
import com.munch.lib.log
import java.io.*
import java.lang.Exception
import java.util.*

/**
 * Create by munch1182 on 2021/1/21 15:00.
 */
class SocketHelper {

    private val name = "1231"
    private val socketService by lazy { SocketService(name) }
    private val socketClient by lazy { SocketClient(name) }


    fun startSocketService() {
        socketService.startService()
    }

    fun stopSocketService() {
        socketService.stopService()
    }

    fun connect() {
        socketClient.connect()
    }

    fun send(msg: String) {
        ThreadHelper.getExecutor().execute {
            socketClient.send(msg.toByteArray())
        }
    }

    fun disconnect() {
        socketClient.disconnect()
    }


    class SocketService(socketAddress: String) : Thread() {

        private val localServerSocket by lazy { LocalServerSocket(socketAddress) }
        private var start = false

        override fun run() {
            while (start) {
                log("s:开始接收消息")
                val socket = localServerSocket.accept()
                val br = socket.inputStream.bufferedReader()
                while (true) {
                    log("s:等待解析消息")
                    //readLine会堵塞线程
                    val line = br.readLine()
                    if (line.toLowerCase(Locale.ROOT) == "exit") {
                        log("s:此次解析完毕")
                        break
                    } else {
                        log(line)
                    }
                }
                socket.outputStream.write("已收到消息\n".toByteArray())
                socket.outputStream.write("exit".toByteArray())
                log("s:发送回复消息")
                sleep(500L)
            }
            log("s:退出服务")
        }

        fun startService() {
            if (this.start) {
                return
            }
            this.start = true
            start()
        }

        fun stopService() {
            start = false
            localServerSocket.closeWhenEnd()
            log("s:关闭服务")
        }

    }

    class SocketClient(private val socketName: String) {

        private var sender: LocalSocket? = null

        fun connect(): Boolean {
            if (sender == null) {
                sender = LocalSocket()
            }
            log("c:开始连接服务端")
            try {
                sender!!.connect(LocalSocketAddress(socketName))
            } catch (e: IOException) {
                log(e.message)
            }
            val connected = sender!!.isConnected
            log("c:连接服务端结果: $connected")
            return connected
        }

        fun send(byteArray: ByteArray) {
            try {
                val outputStream = sender?.outputStream ?: return
                log("c:发送消息：${byteArray}")
                outputStream.write(byteArray)
                outputStream.flush()
                outputStream.write("\n".toByteArray())
                outputStream.write("exit".toByteArray())
                outputStream.write("\n".toByteArray())
                outputStream.flush()
                SystemClock.sleep(500L)
                val br = sender?.inputStream?.bufferedReader()
                while (br != null) {
                    val line = br.readLine()
                    if (line.toLowerCase(Locale.ROOT) == "exit") {
                        log("c:已收到回复，停止等待")
                        break
                    } else {
                        log("c:收到回复：$line")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                log(e.message)
            }
        }

        fun disconnect() {
            log("断开连接")
            sender?.closeWhenEnd()
        }
    }
}