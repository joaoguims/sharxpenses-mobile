package com.sharxpenses.ui.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sharxpenses.data.local.entity.ExpenseEntity

@Composable
fun GroupFeedScreen(
    groupId: String,
    vm: ExpensesViewModel = hiltViewModel()
) {
    val feed by vm.feed.collectAsState()

    LaunchedEffect(groupId) {
        vm.observeGroup(groupId)
        vm.refreshGroup(groupId)
    }

    Scaffold { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(feed) { e ->
                ExpenseCard(e)
            }
        }
    }
}

@Composable
private fun ExpenseCard(e: ExpenseEntity) {
    Card {
        Column(Modifier.padding(12.dp)) {
            Text(text = e.category, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(text = "Valor: R\$ " + "%.2f".format(e.amountCents / 100.0))
            Spacer(Modifier.height(2.dp))
            Text(text = "Pagador: {e.payerId}")
        }
    }
}