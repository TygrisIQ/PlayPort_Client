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
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
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
@Composable
fun ControllerUI(ipaddr: String?) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top row: Start / Select
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            if (ipaddr != null) {
                Column {
                    JoyButton(text = "LB1", onPress = { sendPacket(ipaddr, "PR_LB1")}, onRelease = { sendPacket(ipaddr, "RE_LB1")})
                    JoyButton(text = "LB", onPress = { sendPacket(ipaddr, "PR_LB")}, onRelease = { sendPacket(ipaddr, "RE_LB")})

                }
                Column {
                    JoyButton(text = "RB1", onPress = { sendPacket(ipaddr, "PR_RB1")}, onRelease = { sendPacket(ipaddr, "RE_RB1")})
                    JoyButton(text = "RB", onPress = { sendPacket(ipaddr, "PR_RB")}, onRelease = { sendPacket(ipaddr, "RE_RB")})

                }

            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (ipaddr != null) {
                    JoyButton(text = "↑", onPress = { sendPacket(ipaddr, "PR_DPAD_UP")},
                        onRelease = { sendPacket(ipaddr, "RE_DPAD_UP")})
                    Row {
                        JoyButton(text = "←", onPress = { sendPacket(ipaddr, "PR_DPAD_LEFT")},
                            onRelease = { sendPacket(ipaddr, "RE_DPAD_LEFT")})
                        Spacer(Modifier.width(8.dp))
                        JoyButton(text = "→", onPress = { sendPacket(ipaddr, "PR_DPAD_RIGHT")},
                            onRelease = { sendPacket(ipaddr, "RE_DPAD_RIGHT")})
                    }
                    JoyButton(text = "↓", onPress = { sendPacket(ipaddr, "PR_DPAD_DOWN")}, onRelease = { sendPacket(ipaddr, "RE_DPAD_DOWN")})
                }
            }

            Row{
                if(ipaddr != null){
                    JoyButton(text = "START", onPress = { sendPacket(ipaddr, "PR_START")},
                        onRelease = { sendPacket(ipaddr, "RE_START")})
                    JoyButton(text = "SELECT", onPress = { sendPacket(ipaddr, "PR_SELECT")},
                        onRelease = { sendPacket(ipaddr, "RE_SELECT ")})
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (ipaddr != null) {
                    JoyButton(text = "X", onPress = { sendPacket(ipaddr, "PR_Y")}, onRelease = { sendPacket(ipaddr, "RE_Y")})
                    Row {
                        JoyButton(text = "X", onPress = { sendPacket(ipaddr, "PR_X")}, onRelease = { sendPacket(ipaddr, "RE_X")})
                        Spacer(Modifier.width(8.dp))
                        JoyButton(text = "B", onPress = { sendPacket(ipaddr, "PR_B")}, onRelease = { sendPacket(ipaddr, "RE_B")})
                    }
                    JoyButton(text = "A", onRelease = {sendPacket(ipaddr, "RE_A")}, onPress = { sendPacket(ipaddr, "PR_A")})
//                    Button(onClick = { sendPacket(ipaddr, "A")}) {  Text("A")}
                }
            }
        }


        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            if (ipaddr != null) {

            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly){
            if (ipaddr != null) {
                VirtualStick { dx, dy ->
                    val xVal = (dx * 32767).toInt()
                    val yVal = (dy * 32767).toInt()
                    sendPacket(ipaddr, "LS:X:$xVal")
                    sendPacket(ipaddr, "LS:Y:$yVal")
                }
                VirtualStick { dx, dy ->
                    val xVal = (dx * 32767).toInt()
                    val yVal = (dy * 32767).toInt()
                    sendPacket(ipaddr, "RS:X:$xVal")
                    sendPacket(ipaddr, "RS:Y:$yVal")
                }
            }
        }
    }
}
@Composable
fun VirtualStick(onMove: (Float, Float) -> Unit) {
    val radius = 100f
    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(200.dp)
            .height(200.dp)
    ) {
        Canvas(modifier = Modifier
            .matchParentSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        knobOffset += dragAmount
                        val normX = (knobOffset.x / radius).coerceIn(-1f, 1f)
                        val normY = (knobOffset.y / radius).coerceIn(-1f, 1f)
                        onMove(normX, normY)
                    },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                )
            }
        ) {
            // outer circle
            drawCircle(Color.Gray, radius = radius)
            // knob
            drawCircle(Color.DarkGray, radius = 40f, center = Offset(radius + knobOffset.x, radius + knobOffset.y))
        }
    }
}



