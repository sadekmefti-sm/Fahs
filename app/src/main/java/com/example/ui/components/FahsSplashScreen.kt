package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.i18n.AppLanguage
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Slate900
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FahsSplashScreen(
    language: AppLanguage,
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.86f,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "splash_logo_scale"
    )

    val progressAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.08f,
        animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing),
        label = "splash_progress"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1050L)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Emerald900,
                        Color(0xFF053D2E),
                        Slate900
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onSplashFinished()
            }
            .testTag("fahs_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top Algerian Market Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, Emerald400.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.AR -> "100% بدون إنترنت • مصمم للسوق الجزائري"
                            AppLanguage.FR -> "100% HORS-LIGNE • SPÉCIAL MARCHÉ ALGÉRIEN"
                            AppLanguage.EN -> "100% OFFLINE • BUILT FOR ALGERIA"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Glowing Shield Emblem Container
            Box(
                modifier = Modifier
                    .scale(scaleAnim)
                    .size(136.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF064E3B)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(32.dp),
                    color = Color(0xFF064E3B),
                    border = BorderStroke(2.dp, Emerald400.copy(alpha = 0.65f)),
                    shadowElevation = 12.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_fahs_shield_logo_1794321364551),
                        contentDescription = "Fahs Emblem",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bilingual Brand Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Fahs",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Emerald400)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "فحص",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Emerald400
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (language) {
                    AppLanguage.AR -> "صديقك الخبير لفحص السيارات المستعملة في الجزائر"
                    AppLanguage.FR -> "Votre ami connaisseur pour inspecter une voiture d'occasion"
                    AppLanguage.EN -> "Your knowledgeable friend for inspecting used cars in Algeria"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Emerald100,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 3 Pillars Readiness Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SplashFeaturePill(
                    icon = Icons.Default.DirectionsCar,
                    label = when (language) {
                        AppLanguage.AR -> "28 سيارة جزائرية"
                        AppLanguage.FR -> "28 Modèles DZ"
                        AppLanguage.EN -> "28 DZ Models"
                    }
                )
                SplashFeaturePill(
                    icon = Icons.Default.Verified,
                    label = when (language) {
                        AppLanguage.AR -> "شاسيه VIN والوثائق"
                        AppLanguage.FR -> "Carte Grise & VIN"
                        AppLanguage.EN -> "VIN & Documents"
                    }
                )
                SplashFeaturePill(
                    icon = Icons.Default.Speed,
                    label = when (language) {
                        AppLanguage.AR -> "فحص كمبيوتر OBD2"
                        AppLanguage.FR -> "Scanner OBD2"
                        AppLanguage.EN -> "OBD2 Scanner"
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Diagnostic Module Loading Progress Bar
            Column(
                modifier = Modifier.fillMaxWidth(0.78f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { progressAnim },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Emerald400,
                    trackColor = Emerald800,
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = when (language) {
                        AppLanguage.AR -> "جاري تحميل قاعدة بيانات السيارات وأكواد الأعطال..."
                        AppLanguage.FR -> "Chargement du catalogue hors-ligne et des codes OBD2..."
                        AppLanguage.EN -> "Loading offline vehicle catalog & OBD2 database..."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Emerald100.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SplashFeaturePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Emerald800.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, Emerald600)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Emerald400,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
