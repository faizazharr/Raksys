package com.raksys.feature.query

import com.raksys.core.model.ConnectionProfile

sealed interface QueryEvent {
    data class Execute(val profile: ConnectionProfile, val sql: String) : QueryEvent
    data object Cancel : QueryEvent
    /** The workspace now shows [profileId]; drops the previous connection's result if it differs. */
    data class UseProfile(val profileId: String) : QueryEvent
    data object ClearHistory : QueryEvent
}
