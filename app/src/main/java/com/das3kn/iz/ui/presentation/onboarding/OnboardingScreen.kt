package com.das3kn.iz.ui.presentation.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class OnboardingSlide(
    val title: String,
    val description: String,
    val gradientColors: List<Color>,
    val icon: String
)

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onComplete: () -> Unit
) {
    var currentSlide by rememberSaveable { mutableStateOf(0) }

    val slides = remember {
        listOf(
            OnboardingSlide(
                title = "Hoş Geldiniz",
                description = "Yeni nesil sosyal medya deneyimine katılın. Arkadaşlarınızla bağlantıda kalın, fikirlerinizi paylaşın.",
                gradientColors = listOf(
                    Color(0xFFA855F7),
                    Color(0xFF22D3EE)
                ),
                icon = "👋"
            ),
            OnboardingSlide(
                title = "Gruplar Oluşturun",
                description = "İlgi alanlarınıza göre gruplar oluşturun veya mevcut gruplara katılın. Birlikte daha güçlüyüz!",
                gradientColors = listOf(
                    Color(0xFF22D3EE),
                    Color(0xFF8B5CF6)
                ),
                icon = "👥"
            ),
            OnboardingSlide(
                title = "Etkileşimde Bulunun",
                description = "Paylaşımları beğenin, yorum yapın ve kendi hikayenizi anlatın. Topluluğun bir parçası olun.",
                gradientColors = listOf(
                    Color(0xFF9333EA),
                    Color(0xFF06B6D4)
                ),
                icon = "💬"
            ),
            OnboardingSlide(
                title = "Hemen Başlayın",
                description = "Her şey hazır! Şimdi giriş yapın veya hesap oluşturun ve keşfetmeye başlayın.",
                gradientColors = listOf(
                    Color(0xFF06B6D4),
                    Color(0xFF7C3AED)
                ),
                icon = "🚀"
            )
        )
    }

    val current = slides[currentSlide]

    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "emojiBounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = with(density) { -16.dp.toPx() },
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emojiOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = current.gradientColors.map { it.copy(alpha = 0.85f) }
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = current.icon,
                    fontSize = 64.sp,
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .graphicsLayer { translationY = bounceOffset }
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = current.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = current.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4B5563),
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.forEachIndexed { index, _ ->
                        val isSelected = index == currentSlide
                        val indicatorModifier = if (isSelected) {
                            Modifier
                                .width(32.dp)
                                .height(8.dp)
                                .background(
                                    color = Color(0xFF8B5CF6),
                                    shape = CircleShape
                                )
                        } else {
                            Modifier
                                .size(8.dp)
                                .background(
                                    color = Color(0xFFD1D5DB),
                                    shape = CircleShape
                                )
                        }
                        Box(modifier = indicatorModifier)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (currentSlide < slides.lastIndex) {
                            currentSlide += 1
                        } else {
                            onComplete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5CF6),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentSlide < slides.lastIndex) "İleri" else "Başlayalım",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (currentSlide < slides.lastIndex) {
                            Spacer(modifier = Modifier.size(8.dp))
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null
                            )
                        }
                    }
                }

                if (currentSlide < slides.lastIndex) {
                    TextButton(
                        onClick = onComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF4B5563)
                        )
                    ) {
                        Text(
                            text = "Atla",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
