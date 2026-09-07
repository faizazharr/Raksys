package com.raksys.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RaksysEmptyState(
    iconLabel: String = "",
    iconContent: (@Composable () -> Unit)? = null,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.widthIn(max = 420.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RaksysThemeColors.SurfaceElevated)
                    .border(1.dp, RaksysThemeColors.Border, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (iconContent != null) {
                    iconContent()
                } else {
                    Text(
                        text = iconLabel,
                        fontSize = 28.sp,
                        color = RaksysThemeColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RaksysThemeColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = RaksysThemeColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (actionButtonText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RaksysThemeColors.PrimaryDark,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(actionButtonText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun RaksysLoadingState(
    title: String = "Memuat data...",
    subtitle: String? = "Harap tunggu sebentar",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = RaksysThemeColors.Primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = RaksysThemeColors.TextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = RaksysThemeColors.TextMuted
                )
            }
        }
    }
}

@Composable
fun RaksysErrorState(
    title: String = "Terjadi Kesalahan",
    errorMessage: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = RaksysThemeColors.ErrorBg.copy(alpha = 0.7f),
            modifier = Modifier
                .widthIn(max = 500.dp)
                .border(1.dp, RaksysThemeColors.ErrorBorder, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(RaksysThemeColors.Error.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = RaksysThemeColors.Error, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF7B72)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(1.dp, RaksysThemeColors.ErrorBorder.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFFFA198),
                        lineHeight = 17.sp
                    )
                }

                if (onRetry != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onRetry,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF7B72)),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = androidx.compose.ui.graphics.SolidColor(RaksysThemeColors.Error)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Coba Lagi", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RaksysStatusBadge(
    text: String,
    statusColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = statusColor
        )
    }
}
