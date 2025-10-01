package com.sharxpenses.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharxpenses.data.local.entity.GroupEntity
import com.sharxpenses.data.repo.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val repo: GroupRepository
) : ViewModel() {

    val groups: StateFlow<List<GroupEntity>> =
        repo.observeGroups().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _creating = MutableStateFlow(false)
    val creating: StateFlow<Boolean> = _creating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            repo.refresh().onFailure { _error.value = it.message }
        }
    }

    fun create(name: String, description: String?, currency: String = "BRL") {
        viewModelScope.launch {
            _creating.value = true
            val res = repo.create(name, description, currency)
            _creating.value = false
            _error.value = res.exceptionOrNull()?.message
        }
    }

    init {
        refresh()
    }
}