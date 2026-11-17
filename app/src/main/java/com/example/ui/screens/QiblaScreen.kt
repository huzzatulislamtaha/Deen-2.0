package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.SensorsOff
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalAppStrings
import com.example.domain.calculator.QiblaCalculator
import com.example.location.CompassAccuracy
import com.example.location.CompassReading
import com.example.ui.components.DeenCard
import com.example.ui.viewmodel.MainUiState
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    state: MainUiState,
    compassReading: CompassReading?,
    onDetectGps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val lang = state.prefs.language
    val bearing = state.qiblaBearing
    val distanceKm = state.distanceToKaabaKm

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        ) {
            onDetectGps()
        }
    }

    val rawAzimuth = compassReading?.azimuthDegrees ?: 0f
    var continuousHeading by remember { mutableFloatStateOf(rawAzimuth) }
    val deltaStep = ((rawAzimuth - (continuousHeading % 360f) + 540f) % 360f) - 180f
    continuousHeading += deltaStep

    val animatedHeading by animateFloatAsState(
        targetValue = continuousHeading,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "qibla_heading"
    )

    val signedOffset = QiblaCalculator.shortestAngularDelta(rawAzimuth, bearing)
    val isAligned = compassReading?.hasSensor == true && abs(signedOffset) <= 4.0f

    val primaryColor = MaterialTheme.colorScheme.primary
    val goldColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.qiblaTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        val cityLabel = if (lang == AppLanguage.BENGALI) {
                            state.prefs.location.nameBn
                        } else {
                            state.prefs.location.nameEn
                        }
                        Text(
                            text = cityLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        onClick = {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) onDetectGps() else {
                                locationLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("qibla_detect_gps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.detectLocation)
                    }
                }
            }

            if (compassReading != null && !compassReading.hasSensor) {
                item {
                    DeenCard(highlighted = false) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SensorsOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column {
                                Text(
                                    text = strings.noCompassSensorTitle,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = strings.noCompassSensorBody,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                DeenCard(highlighted = isAligned) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val statusText = when {
                            compassReading == null || !compassReading.hasSensor -> strings.bearingFromNorth
                            isAligned -> strings.alignedWithQibla
                            signedOffset > 0f -> "${strings.turnRight} (${abs(signedOffset).toInt()}°)"
                            else -> "${strings.turnLeft} (${abs(signedOffset).toInt()}°)"
                        }
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (isAligned) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAligned) {
                                        Icons.Outlined.CheckCircle
                                    } else {
                                        Icons.Outlined.Explore
                                    },
                                    contentDescription = null,
                                    tint = if (isAligned) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isAligned) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.76f)
                                .aspectRatio(1f)
                                .testTag("qibla_compass_dial"),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val radius = size.minDimension / 2f
                                val center = Offset(size.width / 2f, size.height / 2f)

                                drawCircle(
                                    color = surfaceColor,
                                    radius = radius * 0.94f,
                                    center = center
                                )
                                drawCircle(
                                    color = if (isAligned) primaryColor else outlineColor,
                                    radius = radius * 0.94f,
                                    center = center,
                                    style = Stroke(width = if (isAligned) 6.dp.toPx() else 2.dp.toPx())
                                )

                                val dialRotation = if (compassReading?.hasSensor == true) -animatedHeading else 0f
                                rotate(degrees = dialRotation, pivot = center) {
                                    // Tick marks every 30 degrees
                                    for (deg in 0 until 360 step 15) {
                                        val rad = Math.toRadians(deg.toDouble())
                                        val isCardinal = deg % 90 == 0
                                        val innerR = radius * if (isCardinal) 0.76f else 0.84f
                                        val outerR = radius * 0.90f
                                        val start = Offset(
                                            center.x + (innerR * sin(rad)).toFloat(),
                                            center.y - (innerR * cos(rad)).toFloat()
                                        )
                                        val end = Offset(
                                            center.x + (outerR * sin(rad)).toFloat(),
                                            center.y - (outerR * cos(rad)).toFloat()
                                        )
                                        drawLine(
                                            color = if (deg == 0) Color(0xFFD32F2F) else onSurfaceColor.copy(alpha = 0.55f),
                                            start = start,
                                            end = end,
                                            strokeWidth = if (isCardinal) 3.dp.toPx() else 1.5.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    }

                                    // Qibla pointer rotating with true north dial
                                    rotate(degrees = bearing.toFloat(), pivot = center) {
                                        val tip = Offset(center.x, center.y - radius * 0.78f)
                                        val leftBase = Offset(center.x - 12.dp.toPx(), center.y - radius * 0.14f)
                                        val rightBase = Offset(center.x + 12.dp.toPx(), center.y - radius * 0.14f)
                                        val pointerPath = Path().apply {
                                            moveTo(tip.x, tip.y)
                                            lineTo(rightBase.x, rightBase.y)
                                            lineTo(leftBase.x, leftBase.y)
                                            close()
                                        }
                                        drawPath(
                                            path = pointerPath,
                                            color = if (isAligned) primaryColor else goldColor
                                        )
                                        // Stylized Kaaba cube marker at tip
                                        drawCircle(
                                            color = primaryColor,
                                            radius = 12.dp.toPx(),
                                            center = Offset(center.x, center.y - radius * 0.68f)
                                        )
                                    }
                                }

                                // Center pivot
                                drawCircle(
                                    color = primaryColor,
                                    radius = 8.dp.toPx(),
                                    center = center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = strings.qiblaDirectionLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f°", bearing),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = strings.distanceToKaaba,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%,.0f", distanceKm)} ${strings.kmUnit}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            item {
                val accuracyLabel = when (compassReading?.accuracy) {
                    CompassAccuracy.HIGH -> strings.sensorAccuracyHigh
                    CompassAccuracy.MEDIUM -> strings.sensorAccuracyMedium
                    else -> strings.sensorAccuracyLow
                }
                DeenCard {
                    Text(
                        text = strings.calibrateCompassTitle,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = accuracyLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.calibrateCompassHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}
