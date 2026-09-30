package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DeliveryTrackerScreen
import com.example.ui.screens.DonorScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.PantryOverviewScreen
import com.example.ui.screens.ReceiverScreen
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

@OptIn(ExperimentalMaterial3Api::class)
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
        // Handle Android hardware/gesture back press to return to DONOR tab
        if (currentTab != AppTab.DONOR) {
            BackHandler {
                viewModel.setTab(AppTab.DONOR)
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Eco,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "FoodWaste Rescue",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentUser?.name ?: "Eco Member",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                    maxLines = 1
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.testTag("btn_logout")
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                    NavigationBarItem(
                        selected = currentTab == AppTab.DONOR,
                        onClick = { viewModel.setTab(AppTab.DONOR) },
                        icon = { Icon(Icons.Default.VolunteerActivism, contentDescription = "Donor") },
                        label = { Text("Donor") },
                        modifier = Modifier.testTag("nav_donor")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.RECOVER,
                        onClick = { viewModel.setTab(AppTab.RECOVER) },
                        icon = { Icon(Icons.Default.Diversity3, contentDescription = "Receiver") },
                        label = { Text("Receiver") },
                        modifier = Modifier.testTag("nav_receiver")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.DELIVERY,
                        onClick = { viewModel.setTab(AppTab.DELIVERY) },
                        icon = { Icon(Icons.Default.DeliveryDining, contentDescription = "Tracker") },
                        label = { Text("Tracker") },
                        modifier = Modifier.testTag("nav_delivery")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.PANTRY,
                        onClick = { viewModel.setTab(AppTab.PANTRY) },
                        icon = { Icon(Icons.Default.Kitchen, contentDescription = "Pantry") },
                        label = { Text("Pantry") },
                        modifier = Modifier.testTag("nav_pantry")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.IMPACT,
                        onClick = { viewModel.setTab(AppTab.IMPACT) },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Impact") },
                        label = { Text("Impact") },
                        modifier = Modifier.testTag("nav_impact")
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
                        AppTab.DONOR -> DonorScreen(
                            viewModel = viewModel
                        )
                        AppTab.RECOVER -> ReceiverScreen(
                            viewModel = viewModel
                        )
                        AppTab.DELIVERY -> DeliveryTrackerScreen(
                            viewModel = viewModel
                        )
                        AppTab.PANTRY -> PantryOverviewScreen(
                            viewModel = viewModel,
                            onNavigateToRecipes = { viewModel.setTab(AppTab.DONOR) },
                            showAddDialogInitially = triggerAddFoodDialog,
                            onDismissInitialAddDialog = { triggerAddFoodDialog = false }
                        )
                        AppTab.IMPACT -> AnalyticsScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
