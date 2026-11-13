package com.example.ui.components

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FahsBrand
import com.example.ui.theme.FahsInk

/**
 * Checks whether system animations are enabled via Settings.Global.ANIMATOR_DURATION_SCALE.
 * When animations are turned off in Android settings, motion reduces to simple cross-fades.
 */
@Composable
fun rememberAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val scale = runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
        }.getOrDefault(1f)
        scale > 0.05f
    }
}

/**
 * Custom vector line illustration of the 16-pin ELM327 Bluetooth dongle in a single consistent
 * stroke weight. Optionally pulses two soft concentric rings while actively searching/connecting.
 */
@Composable
fun DongleVectorIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    strokeColor: Color = FahsInk,
    accentColor: Color = FahsBrand,
    isPulsing: Boolean = false,
    isConnected: Boolean = false
) {
    val animationsEnabled = rememberAnimationsEnabled()
    val infiniteTransition = rememberInfiniteTransition(label = "dongle_pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPulsing && animationsEnabled) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_ring"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val strokePx = 2.4.dp.toPx()
        val stroke = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Optional pulsing rings while searching/connecting
        if (isPulsing) {
            if (animationsEnabled) {
                val r1 = (w * 0.28f) + (w * 0.20f) * pulseProgress
                val a1 = (1f - pulseProgress).coerceIn(0f, 1f) * 0.45f
                drawCircle(
                    color = accentColor.copy(alpha = a1),
                    radius = r1,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.dp.toPx())
                )
                val p2 = (pulseProgress + 0.5f) % 1f
                val r2 = (w * 0.28f) + (w * 0.20f) * p2
                val a2 = (1f - p2).coerceIn(0f, 1f) * 0.45f
                drawCircle(
                    color = accentColor.copy(alpha = a2),
                    radius = r2,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.dp.toPx())
                )
            } else {
                drawCircle(
                    color = accentColor.copy(alpha = 0.25f),
                    radius = w * 0.42f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.8.dp.toPx())
                )
            }
        }

        // Top 16-pin trapezoidal OBD-II connector plug
        val plugTop = h * 0.16f
        val plugBottom = h * 0.36f
        val plugPath = Path().apply {
            moveTo(cx - w * 0.16f, plugTop)
            lineTo(cx + w * 0.16f, plugTop)
            lineTo(cx + w * 0.22f, plugBottom)
            lineTo(cx - w * 0.22f, plugBottom)
            close()
        }
        drawPath(path = plugPath, color = strokeColor, style = stroke)

        // Two rows of 4 pin ticks inside the trapezoid
        for (i in -1..1) {
            val px = cx + i * (w * 0.065f)
            drawLine(
                color = strokeColor,
                start = Offset(px, plugTop + h * 0.06f),
                end = Offset(px, plugBottom - h * 0.05f),
                strokeWidth = strokePx * 0.8f,
                cap = StrokeCap.Round
            )
        }

        // Main ELM327 dongle body
        val bodyLeft = cx - w * 0.28f
        val bodyTop = plugBottom
        val bodyWidth = w * 0.56f
        val bodyHeight = h * 0.46f
        drawRoundRect(
            color = strokeColor,
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
            style = stroke
        )

        // Inner label plate + LED indicator dots
        val innerTop = bodyTop + bodyHeight * 0.22f
        drawRoundRect(
            color = accentColor.copy(alpha = 0.12f),
            topLeft = Offset(cx - w * 0.19f, innerTop),
            size = Size(w * 0.38f, bodyHeight * 0.54f),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )

        // 3 small status LEDs on the dongle face
        val ledY = innerTop + bodyHeight * 0.18f
        drawCircle(color = strokeColor, radius = 2.5.dp.toPx(), center = Offset(cx - w * 0.09f, ledY))
        drawCircle(color = accentColor, radius = 2.5.dp.toPx(), center = Offset(cx, ledY))
        drawCircle(color = strokeColor, radius = 2.5.dp.toPx(), center = Offset(cx + w * 0.09f, ledY))

        // Wireless signal arc or checkmark inside dongle
        val arcY = innerTop + bodyHeight * 0.39f
        if (isConnected) {
            val checkPath = Path().apply {
                moveTo(cx - w * 0.07f, arcY)
                lineTo(cx - w * 0.015f, arcY + h * 0.05f)
                lineTo(cx + w * 0.08f, arcY - h * 0.05f)
            }
            drawPath(path = checkPath, color = accentColor, style = stroke)
        } else {
            drawLine(
                color = strokeColor,
                start = Offset(cx - w * 0.10f, arcY),
                end = Offset(cx + w * 0.10f, arcY),
                strokeWidth = strokePx * 0.85f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Custom vector line illustration showing the 16-pin OBD port location under the steering wheel
 * and dashboard, near the pedals.
 */
@Composable
fun ObdPortUnderSteeringVectorIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    strokeColor: Color = FahsInk,
    accentColor: Color = FahsBrand
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokePx = 2.2.dp.toPx()
        val stroke = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Dashboard lower edge
        drawLine(
            color = strokeColor,
            start = Offset(w * 0.10f, h * 0.52f),
            end = Offset(w * 0.90f, h * 0.52f),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )

        // Steering wheel upper right
        val wheelCx = w * 0.50f
        val wheelCy = h * 0.32f
        val wheelR = w * 0.19f
        drawCircle(
            color = strokeColor,
            radius = wheelR,
            center = Offset(wheelCx, wheelCy),
            style = stroke
        )
        drawCircle(
            color = strokeColor,
            radius = wheelR * 0.32f,
            center = Offset(wheelCx, wheelCy),
            style = stroke
        )

        // Highlighted OBD trapezoidal socket under dashboard (bottom-left of steering column)
        val portCx = w * 0.32f
        val portCy = h * 0.68f
        drawCircle(
            color = accentColor.copy(alpha = 0.15f),
            radius = w * 0.15f,
            center = Offset(portCx, portCy)
        )
        val portPath = Path().apply {
            moveTo(portCx - w * 0.09f, portCy - h * 0.05f)
            lineTo(portCx + w * 0.09f, portCy - h * 0.05f)
            lineTo(portCx + w * 0.07f, portCy + h * 0.05f)
            lineTo(portCx - w * 0.07f, portCy + h * 0.05f)
            close()
        }
        drawPath(path = portPath, color = accentColor, style = stroke)

        // Two pedals at bottom right for spatial reference
        drawRoundRect(
            color = strokeColor.copy(alpha = 0.65f),
            topLeft = Offset(w * 0.56f, h * 0.66f),
            size = Size(w * 0.08f, h * 0.16f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = stroke
        )
        drawRoundRect(
            color = strokeColor.copy(alpha = 0.65f),
            topLeft = Offset(w * 0.70f, h * 0.63f),
            size = Size(w * 0.07f, h * 0.19f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = stroke
        )
    }
}

/**
 * Custom vector line illustration of the engine warning light (Check Engine) symbol on the dashboard.
 */
@Composable
fun EngineWarningLightVectorIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeColor: Color = FahsInk
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = 2.3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Classic engine block silhouette
        val path = Path().apply {
            moveTo(w * 0.24f, h * 0.36f)
            lineTo(w * 0.66f, h * 0.36f)
            lineTo(w * 0.74f, h * 0.44f)
            lineTo(w * 0.82f, h * 0.44f)
            lineTo(w * 0.82f, h * 0.64f)
            lineTo(w * 0.72f, h * 0.64f)
            lineTo(w * 0.64f, h * 0.72f)
            lineTo(w * 0.28f, h * 0.72f)
            lineTo(w * 0.24f, h * 0.62f)
            close()
        }
        drawPath(path = path, color = strokeColor, style = stroke)

        // Top filler cap T-bar
        drawLine(
            color = strokeColor,
            start = Offset(w * 0.45f, h * 0.26f),
            end = Offset(w * 0.45f, h * 0.36f),
            strokeWidth = 2.3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = strokeColor,
            start = Offset(w * 0.35f, h * 0.26f),
            end = Offset(w * 0.55f, h * 0.26f),
            strokeWidth = 2.3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Left pulley / fan vertical bar
        drawLine(
            color = strokeColor,
            start = Offset(w * 0.16f, h * 0.42f),
            end = Offset(w * 0.16f, h * 0.60f),
            strokeWidth = 2.3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = strokeColor,
            start = Offset(w * 0.16f, h * 0.51f),
            end = Offset(w * 0.24f, h * 0.51f),
            strokeWidth = 2.3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
