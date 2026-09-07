package com.raksys.feature.erd

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.UiState
import com.raksys.core.ui.RaksysEmptyState
import com.raksys.core.ui.RaksysErrorState
import com.raksys.core.ui.RaksysLoadingState
import com.raksys.core.ui.RaksysThemeColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ErdScreen(profile: ConnectionProfile, modifier: Modifier = Modifier) {
    val presenter = koinInject<ErdPresenter>()
    val state by presenter.state.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(profile.id) { presenter.onEvent(ErdEvent.Load(profile)) }

    Box(modifier = modifier.fillMaxSize().background(RaksysThemeColors.Background)) {
        when (val current = state) {
            is UiState.Idle -> {}
            is UiState.Loading -> RaksysLoadingState(title = "Membangun ERD...", subtitle = "Membaca skema dan foreign key dari ${profile.name}")
            is UiState.Error -> RaksysErrorState(
                errorMessage = current.message,
                onRetry = { scope.launch { presenter.onEvent(ErdEvent.Load(profile)) } },
            )
            is UiState.Success -> {
                if (current.data.isEmpty()) {
                    RaksysEmptyState(iconLabel = "🗺️", title = "Belum Ada Tabel", description = "Database ini belum punya tabel untuk digambar ERD-nya.")
                } else {
                    ErdCanvas(tables = current.data, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
