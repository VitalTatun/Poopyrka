package com.example.poopyrka.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.poopyrka.ui.AddEntryScreen
import com.example.poopyrka.ui.MainScreen
import com.example.poopyrka.ui.MainViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                viewModel = viewModel,
                onNavigateToAddEntry = { navController.navigate(Screen.AddEntry.route) }
            )
        }
        composable(Screen.AddEntry.route) {
            AddEntryScreen(
                onBack = { navController.popBackStack() },
                onSave = { point, count, group ->
                    viewModel.addEntry(point, count, group)
                    navController.popBackStack()
                }
            )
        }
    }
}
