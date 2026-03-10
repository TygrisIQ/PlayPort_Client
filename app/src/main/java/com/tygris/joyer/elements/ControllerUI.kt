package com.tygris.joyer.elements

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
import com.tygris.joyer.sendAxis
import com.tygris.joyer.sendButton

private const val BTN_A:          Byte = 0x00
private const val BTN_B:          Byte = 0x01
private const val BTN_X:          Byte = 0x02
private const val BTN_Y:          Byte = 0x03
private const val BTN_LB:         Byte = 0x04
private const val BTN_LB1:        Byte = 0x05
private const val BTN_RB:         Byte = 0x06
private const val BTN_RB1:        Byte = 0x07
private const val BTN_DPAD_UP:    Byte = 0x08
private const val BTN_DPAD_DOWN:  Byte = 0x09
private const val BTN_DPAD_LEFT:  Byte = 0x0A
private const val BTN_DPAD_RIGHT: Byte = 0x0B
private const val BTN_SELECT:     Byte = 0x0C
private const val BTN_START:      Byte = 0x0D

private const val AXIS_LS_X: Byte = 0x00
private const val AXIS_LS_Y: Byte = 0x01
private const val AXIS_RS_X: Byte = 0x02
private const val AXIS_RS_Y: Byte = 0x03

@Composable
fun ControllerUI(ipaddr: String?) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top row: shoulder buttons
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            if (ipaddr != null) {
                Column {
                    JoyButton(text = "LB1", onPress = { sendButton(ipaddr, BTN_LB1, true) }, onRelease = { sendButton(ipaddr, BTN_LB1, false) })
                    JoyButton(text = "LB",  onPress = { sendButton(ipaddr, BTN_LB,  true) }, onRelease = { sendButton(ipaddr, BTN_LB,  false) })
                }
                Column {
                    JoyButton(text = "RB1", onPress = { sendButton(ipaddr, BTN_RB1, true) }, onRelease = { sendButton(ipaddr, BTN_RB1, false) })
                    JoyButton(text = "RB",  onPress = { sendButton(ipaddr, BTN_RB,  true) }, onRelease = { sendButton(ipaddr, BTN_RB,  false) })
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            // DPad
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (ipaddr != null) {
                    JoyButton(text = "↑", onPress = { sendButton(ipaddr, BTN_DPAD_UP,    true) }, onRelease = { sendButton(ipaddr, BTN_DPAD_UP,    false) })
                    Row {
                        JoyButton(text = "←", onPress = { sendButton(ipaddr, BTN_DPAD_LEFT,  true) }, onRelease = { sendButton(ipaddr, BTN_DPAD_LEFT,  false) })
                        Spacer(Modifier.width(8.dp))
                        JoyButton(text = "→", onPress = { sendButton(ipaddr, BTN_DPAD_RIGHT, true) }, onRelease = { sendButton(ipaddr, BTN_DPAD_RIGHT, false) })
                    }
                    JoyButton(text = "↓", onPress = { sendButton(ipaddr, BTN_DPAD_DOWN,  true) }, onRelease = { sendButton(ipaddr, BTN_DPAD_DOWN,  false) })
                }
            }

            // Start / Select
            Row {
                if (ipaddr != null) {
                    JoyButton(text = "START",  onPress = { sendButton(ipaddr, BTN_START,  true) }, onRelease = { sendButton(ipaddr, BTN_START,  false) })
                    JoyButton(text = "SELECT", onPress = { sendButton(ipaddr, BTN_SELECT, true) }, onRelease = { sendButton(ipaddr, BTN_SELECT, false) })
                }
            }

            // Face buttons
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (ipaddr != null) {
                    JoyButton(text = "Y", onPress = { sendButton(ipaddr, BTN_Y, true) }, onRelease = { sendButton(ipaddr, BTN_Y, false) })
                    Row {
                        JoyButton(text = "X", onPress = { sendButton(ipaddr, BTN_X, true) }, onRelease = { sendButton(ipaddr, BTN_X, false) })
                        Spacer(Modifier.width(8.dp))
                        JoyButton(text = "B", onPress = { sendButton(ipaddr, BTN_B, true) }, onRelease = { sendButton(ipaddr, BTN_B, false) })
                    }
                    JoyButton(text = "A", onPress = { sendButton(ipaddr, BTN_A, true) }, onRelease = { sendButton(ipaddr, BTN_A, false) })
                }
            }
        }

        // Analog sticks
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            if (ipaddr != null) {
                VirtualStick { dx, dy ->
                    sendAxis(ipaddr, AXIS_LS_X, (dx * 32767).toInt())
                    sendAxis(ipaddr, AXIS_LS_Y, (dy * 32767).toInt())
                }
                VirtualStick { dx, dy ->
                    sendAxis(ipaddr, AXIS_RS_X, (dx * 32767).toInt())
                    sendAxis(ipaddr, AXIS_RS_Y, (dy * 32767).toInt())
                }
            }
        }
    }
}

@Composable
fun VirtualStick(onMove: (Float, Float) -> Unit) {
    val maxRadius = 60f
    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.width(200.dp).height(200.dp)
    ) {
        Canvas(modifier = Modifier
            .matchParentSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val raw = knobOffset + dragAmount
                        // clamp knob visually to circle boundary
                        knobOffset = if (raw.getDistance() <= maxRadius) raw
                        else raw / raw.getDistance() * maxRadius
                        onMove(
                            (knobOffset.x / maxRadius).coerceIn(-1f, 1f),
                            (knobOffset.y / maxRadius).coerceIn(-1f, 1f)
                        )
                    },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                )
            }
        ) {
            val cx = size.width / 2
            val cy = size.height / 2
            drawCircle(Color.Gray, radius = 100f, center = Offset(cx, cy))
            drawCircle(Color.DarkGray, radius = 40f, center = Offset(cx + knobOffset.x, cy + knobOffset.y))
        }
    }
}