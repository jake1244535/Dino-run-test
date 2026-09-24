package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.GameViewModel
import com.example.ui.game.GameScreen
import com.example.ui.shop.ShopScreen

enum class AppDestination {
    GAME,
    SHOP
}

@Composable
fun DinoApp(
    viewModel: GameViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(AppDestination.GAME) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = currentDestination,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition"
        ) { destination ->
            when (destination) {
                AppDestination.GAME -> {
                    GameScreen(
                        viewModel = viewModel,
                        onNavigateToShop = {
                            viewModel.pauseGame()
                            currentDestination = AppDestination.SHOP
                        }
                    )
                }
                AppDestination.SHOP -> {
                    ShopScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentDestination = AppDestination.GAME
                        }
                    )
                }
            }
        }
    }
}
