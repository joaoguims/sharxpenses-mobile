package com.sharxpenses.ui.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sharxpenses.data.remote.SplitMode
import kotlinx.coroutines.launch

@Composable
fun CreateExpenseScreen(
    groupId: String,
    vm: ExpensesViewModel = hiltViewModel(),
    onCreated: () -> Unit = {}
) {
    val state by vm.create.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (state.created) {
        LaunchedEffect(Unit) { onCreated() }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Nova despesa", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = state.amount,
                onValueChange = vm::setAmount,
                label = { Text("Valor (ex: 123.45)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.category,
                onValueChange = vm::setCategory,
                label = { Text("Categoria") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.payerId,
                onValueChange = vm::setPayer,
                label = { Text("PayerId (usuário que pagou)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.participantsCsv,
                onValueChange = vm::setParticipantsCsv,
                label = { Text("Participantes (u1,u2,u3)") },
                modifier = Modifier.fillMaxWidth()
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = state.mode.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Modo de rateio") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    SplitMode.values().forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m.name) },
                            onClick = { vm.setMode(m); expanded = false }
                        )
                    }
                }
            }

            if (state.mode == SplitMode.PERCENT) {
                OutlinedTextField(
                    value = state.percentsCsv,
                    onValueChange = vm::setPercentsCsv,
                    label = { Text("Percentuais (u1:50,u2:30,u3:20)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.mode == SplitMode.CUSTOM) {
                OutlinedTextField(
                    value = state.customCsv,
                    onValueChange = vm::setCustomCsv,
                    label = { Text("Custom shares (u1:10.00,u2:20.50)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = { vm.createExpense(groupId) },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (state.loading) "Salvando..." else "Salvar") }

            state.error?.let { msg ->
                LaunchedEffect(msg) {
                    scope.launch { snackbar.showSnackbar(msg) }
                    vm.clearError()
                }
            }
        }
    }
}