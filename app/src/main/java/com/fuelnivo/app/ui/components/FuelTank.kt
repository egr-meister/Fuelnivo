package com.fuelnivo.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fuelnivo.app.ui.theme.DeepGraphite
import com.fuelnivo.app.ui.theme.FuelAmber
import com.fuelnivo.app.ui.theme.LowVisualLevel
import com.fuelnivo.app.ui.theme.WarningColor

/**
 * The "Segmented Fuel Tank Dashboard" visual identity. Draws a tank silhouette
 * with an inlet cap, 8 horizontal measurement segments, an amber fill level, a
 * percentage label, and a small fuel drop marker. This is a manual progress
 * representation only; it does not read a real fuel level.
 */
@Composable
fun FuelTankView(
    fillFraction: Float,
    percentLabel: String,
    overCapacity: Boolean,
    isEmpty: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier,
    width: Dp = 220.dp,
    height: Dp = 260.dp,
    segments: Int = 8
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val outline = DeepGraphite
    val fillColor = when {
        isEmpty -> Color.Transparent
        overCapacity -> WarningColor
        fillFraction < 0.2f -> LowVisualLevel
        else -> FuelAmber
    }
    val safeFraction = fillFraction.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .size(width, height)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = with(density) { 4.dp.toPx() }
            val capHeight = with(density) { 26.dp.toPx() }
            val capWidth = size.width * 0.30f
            val tankTop = capHeight
            val tankHeight = size.height - capHeight
            val corner = with(density) { 22.dp.toPx() }

            // Inlet cap
            val capLeft = size.width / 2f - capWidth / 2f
            drawRoundRect(
                color = outline,
                topLeft = Offset(capLeft, 0f),
                size = Size(capWidth, capHeight + corner),
                cornerRadius = CornerRadius(with(density) { 8.dp.toPx() })
            )

            // Tank body path (rounded rectangle)
            val bodyRect = Rect(
                left = strokePx / 2f,
                top = tankTop,
                right = size.width - strokePx / 2f,
                bottom = size.height - strokePx / 2f
            )
            val bodyPath = Path().apply {
                addRoundRect(RoundRect(bodyRect, CornerRadius(corner)))
            }

            // Fill from the bottom, clipped to the tank body.
            if (!isEmpty && safeFraction > 0f) {
                clipPath(bodyPath) {
                    val fillTop = bodyRect.bottom - bodyRect.height * safeFraction
                    drawRect(
                        color = fillColor,
                        topLeft = Offset(bodyRect.left, fillTop),
                        size = Size(bodyRect.width, bodyRect.bottom - fillTop)
                    )
                }
            }

            // Horizontal measurement segments
            val dash = PathEffect.dashPathEffect(
                floatArrayOf(with(density) { 6.dp.toPx() }, with(density) { 6.dp.toPx() })
            )
            for (i in 1 until segments) {
                val y = bodyRect.top + bodyRect.height * (i.toFloat() / segments)
                drawLine(
                    color = outline.copy(alpha = 0.35f),
                    start = Offset(bodyRect.left + strokePx, y),
                    end = Offset(bodyRect.right - strokePx, y),
                    strokeWidth = with(density) { 1.dp.toPx() },
                    pathEffect = dash,
                    cap = StrokeCap.Round
                )
            }

            // Tank outline
            drawPath(bodyPath, color = outline, style = Stroke(width = strokePx))

            // Fuel drop marker near the cap
            val dropCenter = Offset(size.width / 2f, tankTop + with(density) { 24.dp.toPx() })
            val dropRadius = with(density) { 7.dp.toPx() }
            val dropPath = Path().apply {
                moveTo(dropCenter.x, dropCenter.y - dropRadius * 1.6f)
                cubicTo(
                    dropCenter.x + dropRadius * 1.4f, dropCenter.y - dropRadius * 0.2f,
                    dropCenter.x + dropRadius, dropCenter.y + dropRadius,
                    dropCenter.x, dropCenter.y + dropRadius
                )
                cubicTo(
                    dropCenter.x - dropRadius, dropCenter.y + dropRadius,
                    dropCenter.x - dropRadius * 1.4f, dropCenter.y - dropRadius * 0.2f,
                    dropCenter.x, dropCenter.y - dropRadius * 1.6f
                )
                close()
            }
            drawPath(dropPath, color = if (isEmpty) outline.copy(alpha = 0.4f) else FuelAmber)

            // Percentage label centered in the tank
            if (!isEmpty) {
                val style = TextStyle(
                    color = if (safeFraction > 0.45f) DeepGraphite else outline,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
                val measured = textMeasurer.measure(percentLabel, style)
                drawText(
                    textMeasurer = textMeasurer,
                    text = percentLabel,
                    style = style,
                    topLeft = Offset(
                        x = size.width / 2f - measured.size.width / 2f,
                        y = bodyRect.top + bodyRect.height / 2f - measured.size.height / 2f
                    )
                )
            }
        }
    }
}
