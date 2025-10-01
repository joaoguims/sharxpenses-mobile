package com.sharxpenses.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharxpenses.data.local.entity.ExpenseEntity
import com.sharxpenses.data.remote.CustomShare
import com.sharxpenses.data.remote.PercentShare
import com.sharxpenses.data.remote.SplitMode
import com.sharxpenses.data.repo.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateExpenseState(
    val amount: String = "",
    val category: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val payerId: String = "",
    val participantsCsv: String = "", // ex: "u1,u2,u3"
    val mode: SplitMode = SplitMode.EQUAL,
    val percentsCsv: String = "",     // ex: "u1:50,u2:30,u3:20"
    val customCsv: String = "",       // ex: "u1:10.00,u2:20.50,u3:5.35"
    val loading: Boolean = false,
    val error: String? = null,
    val created: Boolean = false
)

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val repo: ExpenseRepository
) : ViewModel() {

    private val _feed = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val feed: StateFlow<List<ExpenseEntity>> = _feed.asStateFlow()

    private val _create = MutableStateFlow(CreateExpenseState())
    val create: StateFlow<CreateExpenseState> = _create.asStateFlow()

    fun observeGroup(groupId: String) {
        viewModelScope.launch {
            repo.observeGroupExpenses(groupId).collect { _feed.value = it }
        }
    }

    fun refreshGroup(groupId: String) {
        viewModelScope.launch { repo.refreshGroupExpenses(groupId) }
    }

    // Create state mutators
    fun setAmount(v: String) { _create.value = _create.value.copy(amount = v) }
    fun setCategory(v: String) { _create.value = _create.value.copy(category = v) }
    fun setDate(millis: Long) { _create.value = _create.value.copy(dateMillis = millis) }
    fun setPayer(v: String) { _create.value = _create.value.copy(payerId = v) }
    fun setParticipantsCsv(v: String) { _create.value = _create.value.copy(participantsCsv = v) }
    fun setMode(v: SplitMode) { _create.value = _create.value.copy(mode = v) }
    fun setPercentsCsv(v: String) { _create.value = _create.value.copy(percentsCsv = v) }
    fun setCustomCsv(v: String) { _create.value = _create.value.copy(customCsv = v) }
    fun clearError() { _create.value = _create.value.copy(error = null) }

    fun createExpense(groupId: String) {
        val s = _create.value
        val amt = s.amount.replace(",", ".").toDoubleOrNull()
        if (amt == null || amt <= 0.0) {
            _create.value = s.copy(error = "Valor inválido.")
            return
        }
        if (s.category.isBlank()) {
            _create.value = s.copy(error = "Informe a categoria.")
            return
        }
        val payer = s.payerId.trim()
        if (payer.isBlank()) {
            _create.value = s.copy(error = "Informe o pagador (payerId).")
            return
        }
        val participants = s.participantsCsv.split(",").mapNotNull { it.trim().ifBlank { null } }
        if (participants.isEmpty()) {
            _create.value = s.copy(error = "Informe ao menos 1 participante.")
            return
        }

        val percents = if (s.mode == SplitMode.PERCENT && s.percentsCsv.isNotBlank()) {
            s.percentsCsv.split(",").mapNotNull { kv ->
                val p = kv.split(":")
                if (p.size == 2) PercentShare(p[0].trim(), p[1].trim().toDoubleOrNull() ?: 0.0) else null
            }
        } else null

        val custom = if (s.mode == SplitMode.CUSTOM && s.customCsv.isNotBlank()) {
            s.customCsv.split(",").mapNotNull { kv ->
                val p = kv.split(":")
                if (p.size == 2) CustomShare(p[0].trim(), p[1].trim().replace(",", ".").toDoubleOrNull() ?: 0.0) else null
            }
        } else null

        viewModelScope.launch {
            try {
                _create.value = s.copy(loading = true, error = null, created = false)
                repo.createExpense(
                    groupId = groupId,
                    payerId = payer,
                    amount = amt,
                    category = s.category,
                    dateMillis = s.dateMillis,
                    participants = participants,
                    mode = s.mode,
                    percents = percents,
                    custom = custom
                )
                _create.value = _create.value.copy(created = true)
            } catch (t: Throwable) {
                _create.value = _create.value.copy(error = t.message ?: "Falha ao criar despesa.")
            } finally {
                _create.value = _create.value.copy(loading = false)
            }
        }
    }
}