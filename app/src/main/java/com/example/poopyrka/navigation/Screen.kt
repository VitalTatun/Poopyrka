package com.example.poopyrka.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object AddEntry : Screen("add_entry")
    object Statistics : Screen("statistics")
    object DayDetails : Screen("day_details/{shiftId}") {
        fun createRoute(shiftId: Long) = "day_details/$shiftId"
    }
}
