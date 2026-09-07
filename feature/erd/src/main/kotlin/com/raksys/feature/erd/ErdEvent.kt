package com.raksys.feature.erd

import com.raksys.core.model.ConnectionProfile

sealed interface ErdEvent {
    data class Load(val profile: ConnectionProfile) : ErdEvent
}
