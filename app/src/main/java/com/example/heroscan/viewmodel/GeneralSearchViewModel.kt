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
import com.example.heroscan.model.dto.MetronIssueListItem
import com.example.heroscan.repository.SearchResult

// Identifica el tipo de código ingresado por texto (UPC-A, EAN-13, ISBN, ISSN) según su longitud y prefijo.
sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val comic: Comic) : SearchUiState
    data class MultipleResults(val results: List<MetronIssueListItem>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class GeneralSearchViewModel : ViewModel() {

    private val repository = SearchRepository()

    var uiState: SearchUiState by mutableStateOf(SearchUiState.Idle)
        private set

    fun searchComic(code: String, scanType: String) {
        uiState = SearchUiState.Loading

        viewModelScope.launch {
            uiState = try {
                when (val result = repository.searchByCode(code, scanType)) {
                    is SearchResult.Single -> SearchUiState.Success(result.comic)
                    is SearchResult.Multiple -> SearchUiState.MultipleResults(result.results)
                }
            } catch (e: Exception) {
                SearchUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Cuando el usuario elige un comic de la lista
    fun selectComic(issueId: Int, scanType: String) {
        uiState = SearchUiState.Loading

        viewModelScope.launch {
            uiState = try {
                val comic = repository.getComicDetail(issueId, scanType)
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