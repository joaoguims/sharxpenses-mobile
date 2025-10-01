package com.sharxpenses.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState

    val name = remember(state.name) { mutableStateOf(TextFieldValue(state.name)) }
    val avatar = remember(state.avatarUrl) { mutableStateOf(TextFieldValue(state.avatarUrl)) }
    val currency = remember(state.currency) { mutableStateOf(TextFieldValue(state.currency)) }
    var notifyEnabled by remember(state.notifyEnabled) { mutableStateOf(state.notifyEnabled) }

    LaunchedEffect(name.value.text) { viewModel.onNameChange(name.value.text) }
    LaunchedEffect(avatar.value.text) { viewModel.onAvatarChange(avatar.value.text) }
    LaunchedEffect(currency.value.text) { viewModel.onCurrencyChange(currency.value.text) }
    LaunchedEffect(notifyEnabled) { viewModel.onNotifyToggle(notifyEnabled) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Perfil") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (state.error != null) {
                Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
            }
            if (state.message != null) {
                Text(state.message ?: "", color = MaterialTheme.colorScheme.primary)
            }

            OutlinedTextField(
                value = TextFieldValue(state.email),
                onValueChange = {},
                label = { Text("E-mail") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = avatar.value,
                onValueChange = { avatar.value = it },
                label = { Text("Avatar URL") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = currency.value,
                onValueChange = { currency.value = it },
                label = { Text("Moeda (ex.: BRL, USD)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Notificações")
                Switch(checked = notifyEnabled, onCheckedChange = { notifyEnabled = it })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.save() },
                    enabled = !state.isLoading
                ) { Text("Salvar") }

                OutlinedButton(
                    onClick = { viewModel.load() },
                    enabled = !state.isLoading
                ) { Text("Recarregar") }
            }
        }
    }
}