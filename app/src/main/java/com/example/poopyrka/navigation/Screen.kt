package com.example.poopyrka.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object AddEntry : Screen("add_entry")
}
