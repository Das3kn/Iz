package com.das3kn.iz.ui.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.das3kn.iz.R
import kotlinx.coroutines.delay

@Composable
fun SocialHubSplashScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onFinished: () -> Unit
) {
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            delay(500)
            onFinished()
        }
    }

    val backgroundGradient = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF7C3AED),
                Color(0xFF8B5CF6),
                Color(0xFF22D3EE)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        GridOverlay(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.12f }
        )

        FloatingIcon(
            icon = Icons.Outlined.Lightbulb,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 40.dp, top = 80.dp),
            amplitude = 20.dp,
            rotation = 5f,
            durationMillis = 4000,
            iconSize = 48.dp
        )

        FloatingIcon(
            icon = Icons.Outlined.RocketLaunch,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 48.dp, top = 112.dp),
            amplitude = 24.dp,
            rotation = 6f,
            durationMillis = 5000,
            delayMillis = 800,
            iconSize = 40.dp
        )

        FloatingIcon(
            icon = Icons.Outlined.Bolt,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 56.dp, bottom = 120.dp),
            amplitude = 18.dp,
            rotation = 4f,
            durationMillis = 3600,
            delayMillis = 400,
            iconSize = 40.dp
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoWithGlow()

            Spacer(modifier = Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "İsimsiz Zihinler",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Fikirler",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    Icon(
                        imageVector = Icons.Outlined.RocketLaunch,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "İcatlar",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    Icon(
                        imageVector = Icons.Outlined.Bolt,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "İnovasyon",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Düşüncelerinizi paylaşın, projeleri keşfedin, geleceği birlikte inşa edin",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.8f)
                ),
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 200)),
                exit = fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                TechLoadingIndicator()
            }
        }

        Text(
            text = "\"Her büyük icat, bir fikirle başlar\"",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.6f),
                fontStyle = FontStyle.Italic
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        )
    }
}

@Composable
private fun LogoWithGlow(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
                .blur(100.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        )

        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .blur(24.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        )

        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(96.dp)
            )
        }
    }
}

@Composable
private fun FloatingIcon(
    icon: ImageVector,
    modifier: Modifier,
    amplitude: Dp,
    rotation: Float,
    durationMillis: Int,
    delayMillis: Int = 0,
    iconSize: Dp
) {
    val density = LocalDensity.current
    val amplitudePx = with(density) { amplitude.toPx() }

    val transition = rememberInfiniteTransition(label = "floatingIcon$delayMillis${icon.hashCode()}")
    val offsetY by transition.animateFloat(
        initialValue = -amplitudePx,
        targetValue = amplitudePx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delayMillis)
        ),
        label = "offset$delayMillis"
    )

    val rotationZ by transition.animateFloat(
        initialValue = -rotation,
        targetValue = rotation,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delayMillis)
        ),
        label = "rotation$delayMillis"
    )

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.2f),
        modifier = modifier
            .size(iconSize)
            .graphicsLayer {
                translationY = offsetY
                rotationZ = rotationZ
            }
    )
}

@Composable
private fun TechLoadingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "techIndicator")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(5) { index ->
            val height by transition.animateFloat(
                initialValue = 4f,
                targetValue = 4f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1000
                        16f at 500
                        4f at 1000
                    },
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(index * 100)
                ),
                label = "height$index"
            )

            val alpha by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1000
                        1f at 500
                        0.3f at 1000
                    },
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(index * 100)
                ),
                label = "alpha$index"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun GridOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val step = 40.dp.toPx()
        val strokeColor = Color.White
        var x = 0f
        while (x <= size.width) {
            drawLine(
                color = strokeColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += step
        }

        var y = 0f
        while (y <= size.height) {
            drawLine(
                color = strokeColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += step
        }
    }
}
