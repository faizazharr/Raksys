package com.raksys.feature.document

import com.raksys.core.model.ConnectionProfile

sealed interface DocumentEvent {
    data class LoadCollections(val profile: ConnectionProfile) : DocumentEvent
    data class LoadDocuments(val profile: ConnectionProfile, val collection: String) : DocumentEvent
    data object Cancel : DocumentEvent
}
