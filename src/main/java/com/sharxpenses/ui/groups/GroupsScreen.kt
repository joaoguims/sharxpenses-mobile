package com.sharxpenses.ui.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sharxpenses.data.local.entity.GroupEntity
import kotlinx.coroutines.launch

@Composable
fun GroupsScreen(
    vm: GroupsViewModel = hiltViewModel()
) {
    val groups by vm.groups.collectAsState()
    val creating by vm.creating.collectAsState()
    val error by vm.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(error) {
        error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Novo grupo")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = "Meus Grupos",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            if (groups.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Você ainda não participa de nenhum grupo.")
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(groups, key = { it.id }) { g ->
                        GroupRow(g)
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { if (!creating) showDialog = false },
            title = { Text("Criar novo grupo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do grupo") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição (opcional)") },
                        singleLine = false
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !creating && name.isNotBlank(),
                    onClick = {
                        vm.create(name.trim(), description.trim().ifBlank { null })
                        name = ""
                        description = ""
                        showDialog = false
                    }
                ) { Text(if (creating) "Criando..." else "Criar") }
            },
            dismissButton = {
                TextButton(
                    enabled = !creating,
                    onClick = { showDialog = false }
                ) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun GroupRow(group: GroupEntity) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(group.name, style = MaterialTheme.typography.titleMedium)
        group.description?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}