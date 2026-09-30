package com.raksys.feature.navigator

import com.raksys.core.model.ConnectionProfile

sealed interface NavigatorEvent {
    data class Load(val profile: ConnectionProfile, val forceRefresh: Boolean = false) : NavigatorEvent
}
