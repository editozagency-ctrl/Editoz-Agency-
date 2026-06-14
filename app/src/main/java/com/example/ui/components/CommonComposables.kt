package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.05f),
    borderColor: Color = Color.White.copy(alpha = 0.10f),
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.4f)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column(
            modifier = Modifier
                .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun LiveIndicator(
    modifier: Modifier = Modifier,
    text: String = "LIVE STUDY"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(OrangePrimary.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(OrangePrimary.copy(alpha = alpha))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = OrangePrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun CustomNotificationBanner(
    message: String,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(message) {
        isVisible = true
    }

    if (isVisible) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(12.dp, RoundedCornerShape(12.dp))
                .testTag("notification_banner"),
            color = OrangePrimary,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Alert",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        isVisible = false
                        onDismiss()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricLineChart(
    data: List<Pair<String, Float>>,
    accentColor: Color = OrangePrimary,
    gridColor: Color = BorderGray.copy(alpha = 0.5f)
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 16.dp)
            .drawBehind {
                val width = size.width
                val height = size.height
                val paddingLeft = 50f
                val paddingBottom = 40f
                val chartWidth = width - paddingLeft
                val chartHeight = height - paddingBottom

                val maxVal = data.maxOfOrNull { it.second } ?: 100f
                val gridLinesCount = 4

                // Draw horizontal grid lines
                for (i in 0..gridLinesCount) {
                    val y = chartHeight - (chartHeight / gridLinesCount) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(paddingLeft, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (data.size < 2) return@drawBehind

                val stepX = chartWidth / (data.size - 1)
                val points = data.mapIndexed { index, pair ->
                    val x = paddingLeft + index * stepX
                    val fraction = pair.second / maxVal
                    val y = chartHeight - (fraction * chartHeight)
                    Offset(x, y)
                }

                // Draw gradient fill under line
                val fillBrush = Brush.verticalGradient(
                    colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent),
                    startY = 0f,
                    endY = chartHeight
                )

                val fillPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points.first().x, chartHeight)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, chartHeight)
                    close()
                }
                drawPath(path = fillPath, brush = fillBrush)

                // Draw actual line
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = accentColor,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 3.dp.toPx()
                    )
                }

                // Draw dots on data points
                points.forEach { point ->
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 2.5.dp.toPx(),
                        center = point
                    )
                }
            }
    ) {
        // Overlay standard display titles
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { pair ->
                Text(
                    text = pair.first,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
    }
}

@Composable
fun InteractiveDeliverableSlider(
    title: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$value items/mo",
                color = OrangePrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = OrangePrimary,
                activeTrackColor = OrangePrimary,
                inactiveTrackColor = PremiumGray
            )
        )
    }
}
