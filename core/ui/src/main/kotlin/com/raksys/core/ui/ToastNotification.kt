package com.raksys.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ToastManager {
    private val _currentToast = MutableStateFlow<ToastMessage?>(null)
    val currentToast = _currentToast.asStateFlow()

    fun show(message: String, isError: Boolean = false) {
        _currentToast.value = ToastMessage(message, isError)
    }

    fun clear() {
        _currentToast.value = null
    }
}

data class ToastMessage(val text: String, val isError: Boolean = false)

@Composable
fun ToastHost(modifier: Modifier = Modifier) {
    val toast by ToastManager.currentToast.collectAsState()

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(2500)
            ToastManager.clear()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = toast != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        ) {
            toast?.let { item ->
                val bg = if (item.isError) RaksysThemeColors.ErrorBg else RaksysThemeColors.SurfaceElevated
                val border = if (item.isError) RaksysThemeColors.Error else RaksysThemeColors.Primary
                val icon = if (item.isError) "❌" else "✓"

                Row(
                    modifier = Modifier
                        .shadow(12.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .border(1.dp, border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(icon, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.text,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = RaksysThemeColors.TextPrimary
                    )
                }
            }
        }
    }
}
