package com.tygris.joyer.elements
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.widget.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier

@SuppressLint("ClickableViewAccessibility")
@Composable
fun JoyButton(
    text: String,
    modifier: Modifier = Modifier,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            Button(context).apply {
                setText(text)

                setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            onPress()
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            onRelease()
                            true
                        }
                        else -> false
                    }
                }
            }
        }
    )
}
