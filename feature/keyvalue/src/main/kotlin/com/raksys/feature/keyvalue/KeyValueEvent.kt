package com.raksys.feature.keyvalue

import com.raksys.core.model.ConnectionProfile

sealed interface KeyValueEvent {
    data class Scan(val profile: ConnectionProfile, val pattern: String = "*") : KeyValueEvent
    data object Cancel : KeyValueEvent
}
