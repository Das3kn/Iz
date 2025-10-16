package com.das3kn.iz.ui.presentation.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateColor
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathParser
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun SocialHubSplashScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onFinished: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "progressAnimation"
    )

    LaunchedEffect(isLoading) {
        if (isLoading) {
            progress = 0f
            while (true) {
                delay(200)
                val diff = Random.nextFloat() * 30f
                progress = min(progress + diff, 95f)
            }
        } else {
            while (progress < 100f) {
                delay(30)
                progress = min(progress + 6f, 100f)
            }
            delay(300)
            onFinished()
        }
    }

    val backgroundGradient = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF8B5CF6),
                Color(0xFF7C3AED),
                Color(0xFF06B6D4)
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "backgroundTransition")

    val topCircleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1.2f at 2000
                1f at 4000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "topCircleScale"
    )

    val topCircleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                0.5f at 2000
                0.3f at 4000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "topCircleAlpha"
    )

    val bottomCircleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                1.3f at 2500
                1f at 5000
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(1000)
        ),
        label = "bottomCircleScale"
    )

    val bottomCircleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                0.6f at 2500
                0.3f at 5000
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(1000)
        ),
        label = "bottomCircleAlpha"
    )

    val shadowColor by infiniteTransition.animateColor(
        initialValue = Color(0x40000000),
        targetValue = Color(0x40000000),
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                Color(0x40000000) at 0
                Color(0x6606B6D4) at 1000
                Color(0x668B5CF6) at 2000
                Color(0x40000000) at 3000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "shadowColor"
    )

    var startAnimations by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimations = true
    }

    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }

    LaunchedEffect(startAnimations) {
        if (startAnimations) {
            launch {
                logoScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600, easing = FastOutLinearInEasing)
                )
            }
            launch {
                logoAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600)
                )
            }
        }
    }

    val textOffset by animateDpAsState(
        targetValue = if (startAnimations) 0.dp else 20.dp,
        animationSpec = tween(durationMillis = 600, delayMillis = 300, easing = FastOutLinearInEasing),
        label = "textOffset"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 300),
        label = "textAlpha"
    )

    val dotsAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 800),
        label = "dotsAlpha"
    )

    val density = LocalDensity.current
    val textOffsetPx = with(density) { textOffset.toPx() }
    val bottomOffsetTarget = if (startAnimations) 0f else with(density) { 50.dp.toPx() }
    val bottomOffset by animateFloatAsState(
        targetValue = bottomOffsetTarget,
        animationSpec = tween(durationMillis = 600, delayMillis = 1000, easing = FastOutLinearInEasing),
        label = "bottomOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 40.dp, y = 80.dp)
                .size(256.dp)
                .graphicsLayer {
                    scaleX = topCircleScale
                    scaleY = topCircleScale
                    alpha = topCircleAlpha
                }
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .blur(radius = 90.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-40).dp, y = (-80).dp)
                .size(320.dp)
                .graphicsLayer {
                    scaleX = bottomCircleScale
                    scaleY = bottomCircleScale
                    alpha = bottomCircleAlpha
                }
                .clip(CircleShape)
                .background(Color(0xFF06B6D4).copy(alpha = 0.1f))
                .blur(radius = 100.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 80.dp)
                .graphicsLayer { },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        alpha = logoAlpha.value
                    }
            ) {
                LogoCard(shadowColor = shadowColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = textOffsetPx
                        alpha = textAlpha
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SocialHub",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Birlikte daha güçlüyüz",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 18.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Column(
                modifier = Modifier.graphicsLayer { alpha = textAlpha }
            ) {
                ProgressBar(progress = animatedProgress)
                Spacer(modifier = Modifier.height(12.dp))
                LoadingInfo(progress = animatedProgress)
            }

            Spacer(modifier = Modifier.height(24.dp))

            LoadingDots(
                modifier = Modifier.graphicsLayer { alpha = dotsAlpha }
            )
        }

        Text(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .graphicsLayer {
                    translationY = bottomOffset
                    alpha = textAlpha
                },
            text = "© 2025 SocialHub",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 14.sp
            )
        )
    }
}

@Composable
private fun LogoCard(
    modifier: Modifier = Modifier,
    shadowColor: Color
) {
    Box(
        modifier = modifier
            .size(128.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .shadowWithCustomColor(elevation = 25.dp, color = shadowColor)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        ChatBubbleIcon()
    }
}

@Composable
private fun ProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .width(256.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth((progress / 100f).coerceIn(0f, 1f))
                .clip(RoundedCornerShape(50))
                .background(Color.White)
        )
    }
}

@Composable
private fun LoadingInfo(progress: Float) {
    Box(modifier = Modifier.width(256.dp)) {
        Text(
            text = "Yükleniyor...",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            ),
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            text = "${progress.roundToInt()}%",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun LoadingDots(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dots = List(3) { index ->
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000
                    1.5f at 500
                    1f at 1000
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(index * 200)
            ),
            label = "dotScale$index"
        )
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000
                    1f at 500
                    0.5f at 1000
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(index * 200)
            ),
            label = "dotAlpha$index"
        )
        scale to alpha
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            dots.forEach { (scale, alpha) ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

@Composable
private fun ChatBubbleIcon() {
    val pathData = remember {
        PathParser().parsePathString(
            "M32 8C18.745 8 8 17.52 8 29.333c0 5.867 2.507 11.2 6.613 15.04v9.294c0 1.386 1.547 2.213 2.72 1.453l7.254-4.693c2.213.506 4.533.773 6.906.773 13.255 0 24-9.52 24-21.333C56 17.52 45.255 8 32 8z"
        ).toPath()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(64.dp)) {
            val scale = size.width / 64f
            withTransform({
                scale(scale, scale)
            }) {
                drawPath(
                    path = pathData,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6),
                            Color(0xFF06B6D4)
                        )
                    )
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f,
                    center = androidx.compose.ui.geometry.Offset(24f, 29f)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f,
                    center = androidx.compose.ui.geometry.Offset(32f, 29f)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f,
                    center = androidx.compose.ui.geometry.Offset(40f, 29f)
                )
            }
        }
    }
}

private fun Modifier.shadowWithCustomColor(elevation: Dp, color: Color): Modifier {
    return this.graphicsLayer {
        shadowElevation = elevation.toPx()
        shape = RoundedCornerShape(32.dp)
        clip = false
        ambientShadowColor = color
        spotShadowColor = color
    }
}
