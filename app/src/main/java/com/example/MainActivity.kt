package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.design.adaptive.LocalAdaptiveDimensions
import com.example.ui.animation.ExpressiveScreenTransition
import com.example.ui.animation.ScreenTransitionType
import com.example.ui.screens.AutomationsScreen
import com.example.ui.screens.CommandsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IntroPermissionsScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishNavActive
import com.example.ui.theme.PolishNavIndicator
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.PolishTextTertiary
import com.example.ui.theme.UnityXTheme
import com.example.ui.viewmodel.UnityXViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Assistant", Icons.Default.Mic)
    data object Automations : Screen("automations", "Routines", Icons.Default.Bolt)
    data object Commands : Screen("commands", "Tasks", Icons.Default.RecordVoiceOver)
    data object History : Screen("history", "History", Icons.Default.History)
    data object Permissions : Screen("permissions", "Access", Icons.Default.Security)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: UnityXViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnityXTheme {
                val isIntroCompleted by viewModel.isIntroCompleted.collectAsState()
                AnimatedContent(
                    targetState = isIntroCompleted,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.96f, animationSpec = tween(400)))
                            .togetherWith(fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 1.04f, animationSpec = tween(300)))
                    },
                    label = "intro_to_main_app_transition"
                ) { introDone ->
                    if (!introDone) {
                        IntroPermissionsScreen(viewModel = viewModel)
                    } else {
                        MainAppScaffold(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }
}

@Composable
fun MainAppScaffold(viewModel: UnityXViewModel) {
    val adaptive = LocalAdaptiveDimensions.current
    var currentScreen by rememberSaveable { mutableStateOf<String>(Screen.Home.route) }
    var previousIndex by rememberSaveable { mutableIntStateOf(0) }

    val navItems = listOf(
        Screen.Home,
        Screen.Automations,
        Screen.Commands,
        Screen.History,
        Screen.Permissions,
        Screen.Settings
    )

    val currentIndex = navItems.indexOfFirst { it.route == currentScreen }.coerceAtLeast(0)
    val isForward = currentIndex >= previousIndex

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = PolishSurfaceVariant,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .background(PolishSurfaceVariant)
                    .border(width = 1.dp, color = PolishBorder)
                    .testTag("main_bottom_nav")
            ) {
                navItems.forEachIndexed { index, screen ->
                    val isSelected = currentScreen == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentScreen != screen.route) {
                                previousIndex = currentIndex
                                currentScreen = screen.route
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(if (adaptive.isCompact) 20.dp else 22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = if (adaptive.isCompact) 9.sp else 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PolishNavActive,
                            selectedTextColor = PolishNavActive,
                            indicatorColor = PolishNavIndicator,
                            unselectedIconColor = PolishTextSecondary,
                            unselectedTextColor = PolishTextTertiary
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(PolishBackground)
    ) { innerPadding ->
        ExpressiveScreenTransition(
            targetState = currentScreen,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            transitionType = ScreenTransitionType.SharedAxisX,
            isForward = isForward
        ) { route ->
            when (route) {
                Screen.Home.route -> HomeScreen(viewModel = viewModel)
                Screen.Automations.route -> AutomationsScreen(viewModel = viewModel)
                Screen.Commands.route -> CommandsScreen(viewModel = viewModel)
                Screen.History.route -> HistoryScreen(viewModel = viewModel)
                Screen.Permissions.route -> PermissionsScreen(viewModel = viewModel)
                Screen.Settings.route -> SettingsScreen(viewModel = viewModel)
                else -> HomeScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
