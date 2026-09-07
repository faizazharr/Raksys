package com.raksys.feature.query

import com.raksys.core.model.ConnectionProfile

sealed interface QueryEvent {
    data class Execute(val profile: ConnectionProfile, val sql: String) : QueryEvent
    data object Cancel : QueryEvent
    data object ClearHistory : QueryEvent
}
