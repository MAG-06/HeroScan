package com.example.heroscan.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heroscan.model.Comic
import com.example.heroscan.repository.SearchRepository
import kotlinx.coroutines.launch
import com.example.heroscan.network.RetrofitClient

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val comic: Comic) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class GeneralSearchViewModel : ViewModel() {

    private val repository = SearchRepository(RetrofitClient.metronApi, RetrofitClient.translationApi)

    var uiState: SearchUiState by mutableStateOf(SearchUiState.Idle)
        private set

    fun searchComic(code: String, scanType: String) {
        uiState = SearchUiState.Loading

        viewModelScope.launch {
            uiState = try {
                val comic = repository.searchByCode(code, scanType)
                SearchUiState.Success(comic)
            } catch (e: Exception) {
                SearchUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetState() {
        uiState = SearchUiState.Idle
    }
}