package com.tygris.joyer

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tygris.joyer.elements.ControllerUI
import com.tygris.joyer.ui.theme.JoyerTheme
import java.io.PrintWriter
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
private const val PRESS:   Byte = 0x01
private const val RELEASE: Byte = 0x02
private const val AXIS:    Byte = 0x03


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContent {
            JoyerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
object ConnectionManager {
    private var socket: Socket? = null
    private var outputStream: java.io.OutputStream? = null
    private val queue = LinkedBlockingQueue<ByteArray>()
    @Volatile private var currentIp: String? = null

    init {
        Thread {
            while (true) {
                val packet = queue.take()
                trySend(packet)
            }
        }.apply { isDaemon = true; start() }
    }

    fun enqueue(ip: String, packet: ByteArray) {
        if (ip != currentIp) reconnect(ip)
        queue.offer(packet)
    }

    // Release events skip the queue entirely and send on their own thread
    // so a backed-up queue of axis events can't delay a button release
    fun enqueuePriority(ip: String, packet: ByteArray) {
        if (ip != currentIp) reconnect(ip)
        Thread {
            trySend(packet)
        }.start()
    }

    private fun reconnect(ip: String) {
        runCatching { socket?.close() }
        currentIp = ip
        socket = null
        outputStream = null
    }

    private fun trySend(packet: ByteArray) {
        try {
            if (outputStream == null) {
                val s = Socket(currentIp, 8007)
                socket = s
                outputStream = s.getOutputStream()
            }
            outputStream!!.write(packet + byteArrayOf(0x0A))
            outputStream!!.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            runCatching { socket?.close() }
            socket = null
            outputStream = null
        }
    }
}

fun sendButton(ip: String, btn: Byte, pressing: Boolean) {
    val packet = byteArrayOf(if (pressing) PRESS else RELEASE, btn)
    if (pressing) {
        ConnectionManager.enqueue(ip, packet)
    } else {
        // releases are priority — they must never get stuck behind axis spam
        ConnectionManager.enqueuePriority(ip, packet)
    }
}

fun sendAxis(ip: String, axisId: Byte, value: Int) {
    val v = value.toShort()
    val lo = (v.toInt() and 0xFF).toByte()
    val hi = ((v.toInt() shr 8) and 0xFF).toByte()
    ConnectionManager.enqueue(ip, byteArrayOf(AXIS, axisId, lo, hi))
}
//fun sendPacket(ip: String, message: String) {
//    ConnectionManager.enqueue(ip, message)
//}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var ipaddr by remember { mutableStateOf<String?>(null) }
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        if (ipaddr == null) {
            Button(onClick = { discoverServer() { ip -> ipaddr = ip } }) {
                Text("SEARCH FOR SERVER!")
            }
        } else {
            Text("SERVER: $ipaddr")
            ControllerUI(ipaddr)
        }
    }

}



