package com.tygris.joyer

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.tygris.joyer.elements.ControllerUI
import com.tygris.joyer.elements.JoyButton
import com.tygris.joyer.ui.theme.JoyerTheme
import java.io.OutputStream
import java.io.PrintWriter
import java.net.Socket

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

fun sendPacket(ip: String, message: String){
    Thread{
        try {
            val socket = Socket(ip, 8007);
            val output: OutputStream = socket.getOutputStream();
            val writer = PrintWriter(output, true);

            writer.println(message + "\n");
            writer.flush();
            writer.close();
            socket.close();
        }
        catch(e: Exception){
            e.printStackTrace()
        }
    }.start()
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



