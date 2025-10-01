package com.sharxpenses.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sharxpenses.ui.login.LoginScreen
import com.sharxpenses.ui.register.RegisterScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = \"login\"
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(\"login\") { LoginScreen(nav = navController) }
        composable(\"register\") { RegisterScreen(nav = navController) }
        composable(\"home\") {
            // Tela placeholder da Home
            androidx.compose.material3.Text(
                text = \"Bem-vindo ao SharXpenses!\",
                modifier = Modifier
            )
        }
    }
}