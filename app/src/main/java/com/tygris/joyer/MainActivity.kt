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
    private var writer: PrintWriter? = null
    private val queue = LinkedBlockingQueue<String>()
    @Volatile private var currentIp: String? = null

    init {
        Thread {
            while (true) {
                val msg = queue.take() // blocks until there's something to send
                trySend(msg)
            }
        }.apply { isDaemon = true; start() }
    }

    fun enqueue(ip: String, message: String) {
        if (ip != currentIp) reconnect(ip)
        queue.offer(message)
    }

    private fun reconnect(ip: String) {
        runCatching { socket?.close() }
        currentIp = ip
        socket = null
        writer = null
    }

    private fun trySend(msg: String) {
        try {
            if (writer == null) {
                val s = Socket(currentIp, 8007)
                socket = s
                writer = PrintWriter(s.getOutputStream(), true)
            }
            writer!!.println(msg)
        } catch (e: Exception) {
            e.printStackTrace()
            // drop the broken connection; next send will reconnect
            runCatching { socket?.close() }
            socket = null
            writer = null
        }
    }
}

// Drop-in replacement — same signature, no behavior change needed in ControllerUI
fun sendPacket(ip: String, message: String) {
    ConnectionManager.enqueue(ip, message)
}
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



