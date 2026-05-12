package com.example.poopyrka.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.poopyrka.ui.AddEntryScreen
import com.example.poopyrka.ui.DayDetailsScreen
import com.example.poopyrka.ui.EditArchiveEntryScreen
import com.example.poopyrka.ui.MainScreen
import com.example.poopyrka.ui.MainViewModel
import com.example.poopyrka.ui.StatisticsScreen

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
        composable(Screen.Statistics.route) {
            StatisticsScreen(
                viewModel = viewModel,
                onNavigateToDetails = { shiftId ->
                    navController.navigate(Screen.DayDetails.createRoute(shiftId))
                }
            )
        }
        composable(
            route = Screen.DayDetails.route,
            arguments = listOf(navArgument("shiftId") { type = NavType.LongType })
        ) { backStackEntry ->
            val shiftId = backStackEntry.arguments?.getLong("shiftId") ?: 0L
            DayDetailsScreen(
                shiftId = shiftId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToEditEntry = { entryId ->
                    navController.navigate(Screen.EditEntry.createRoute(entryId))
                }
            )
        }
        composable(
            route = Screen.EditEntry.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
            EditArchiveEntryScreen(
                entryId = entryId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
