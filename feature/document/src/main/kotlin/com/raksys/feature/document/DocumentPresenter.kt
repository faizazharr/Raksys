package com.raksys.feature.document

import com.raksys.core.database.DocumentDatabaseDriver
import com.raksys.core.model.CollectionInfo
import com.raksys.core.model.MongoDocument
import com.raksys.core.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DocumentPresenter(private val driver: DocumentDatabaseDriver) {
    private val _collections = MutableStateFlow<UiState<List<CollectionInfo>>>(UiState.Idle)
    val collections: StateFlow<UiState<List<CollectionInfo>>> = _collections

    private val _documents = MutableStateFlow<UiState<List<MongoDocument>>>(UiState.Idle)
    val documents: StateFlow<UiState<List<MongoDocument>>> = _documents

    private val _insertState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val insertState: StateFlow<UiState<Unit>> = _insertState

    suspend fun onEvent(event: DocumentEvent) {
        when (event) {
            is DocumentEvent.LoadCollections -> {
                _collections.value = UiState.Loading
                driver.listCollections(event.profile)
                    .onSuccess { _collections.value = UiState.Success(it) }
                    .onFailure { _collections.value = UiState.Error(it.message ?: "Failed to load collections") }
            }
            is DocumentEvent.LoadDocuments -> {
                _documents.value = UiState.Loading
                driver.findDocuments(event.profile, event.collection)
                    .onSuccess { _documents.value = UiState.Success(it) }
                    .onFailure { _documents.value = UiState.Error(it.message ?: "Failed to load documents") }
            }
            is DocumentEvent.InsertDocument -> {
                _insertState.value = UiState.Loading
                driver.insertDocument(event.profile, event.collection, event.json)
                    .onSuccess {
                        _insertState.value = UiState.Success(Unit)
                        onEvent(DocumentEvent.LoadDocuments(event.profile, event.collection))
                    }
                    .onFailure {
                        _insertState.value = UiState.Error(it.message ?: "Gagal menambahkan dokumen")
                    }
            }
            DocumentEvent.ResetInsertState -> {
                _insertState.value = UiState.Idle
            }
            DocumentEvent.Cancel -> driver.cancel()
        }
    }
}
