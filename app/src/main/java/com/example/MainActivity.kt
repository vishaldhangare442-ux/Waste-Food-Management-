package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DonationScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RecipeRecommendationScreen
import com.example.ui.theme.FoodWasteTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodWasteTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var triggerAddFoodDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (currentUser == null) {
        AuthScreen(viewModel = viewModel)
    } else {
        // Handle Android hardware/gesture back press to return to HOME tab
        if (currentTab != AppTab.HOME) {
            BackHandler {
                viewModel.setTab(AppTab.HOME)
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                    NavigationBarItem(
                        selected = currentTab == AppTab.HOME,
                        onClick = { viewModel.setTab(AppTab.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.INVENTORY,
                        onClick = { viewModel.setTab(AppTab.INVENTORY) },
                        icon = { Icon(Icons.Default.Kitchen, contentDescription = "Pantry") },
                        label = { Text("Pantry") },
                        modifier = Modifier.testTag("nav_inventory")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.RECIPES,
                        onClick = { viewModel.setTab(AppTab.RECIPES) },
                        icon = { Icon(Icons.Default.Restaurant, contentDescription = "Recipes") },
                        label = { Text("Recipes") },
                        modifier = Modifier.testTag("nav_recipes")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.IMPACT,
                        onClick = { viewModel.setTab(AppTab.IMPACT) },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Impact") },
                        label = { Text("Impact") },
                        modifier = Modifier.testTag("nav_impact")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.DONATE,
                        onClick = { viewModel.setTab(AppTab.DONATE) },
                        icon = { Icon(Icons.Default.VolunteerActivism, contentDescription = "Donate") },
                        label = { Text("Donate") },
                        modifier = Modifier.testTag("nav_donate")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.PROFILE,
                        onClick = { viewModel.setTab(AppTab.PROFILE) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                    when (tab) {
                        AppTab.HOME -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToInventory = { viewModel.setTab(AppTab.INVENTORY) },
                            onNavigateToRecipes = { viewModel.setTab(AppTab.RECIPES) },
                            onOpenAddFoodDialog = {
                                viewModel.setTab(AppTab.INVENTORY)
                                triggerAddFoodDialog = true
                            }
                        )
                        AppTab.INVENTORY -> InventoryScreen(
                            viewModel = viewModel,
                            onNavigateToRecipes = { viewModel.setTab(AppTab.RECIPES) },
                            showAddDialogInitially = triggerAddFoodDialog,
                            onDismissInitialAddDialog = { triggerAddFoodDialog = false }
                        )
                        AppTab.RECIPES -> RecipeRecommendationScreen(
                            viewModel = viewModel
                        )
                        AppTab.IMPACT -> AnalyticsScreen(
                            viewModel = viewModel
                        )
                        AppTab.DONATE -> DonationScreen(
                            viewModel = viewModel
                        )
                        AppTab.PROFILE -> ProfileScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
