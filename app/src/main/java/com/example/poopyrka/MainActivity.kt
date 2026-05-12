package com.example.poopyrka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.poopyrka.ui.MainScreen
import com.example.poopyrka.ui.MainViewModel
import com.example.poopyrka.ui.theme.PoopyrkaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PoopyrkaTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
