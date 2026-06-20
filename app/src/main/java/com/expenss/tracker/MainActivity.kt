package com.expenss.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.expenss.tracker.ui.analytics.AnalyticsScreen
import com.expenss.tracker.ui.auth.ForgotPasswordScreen
import com.expenss.tracker.ui.auth.LoginScreen
import com.expenss.tracker.ui.auth.OnboardingScreen
import com.expenss.tracker.ui.auth.RegisterScreen
import com.expenss.tracker.ui.dashboard.DashboardScreen
import com.expenss.tracker.ui.goals.GoalsScreen
import com.expenss.tracker.ui.savings.SavingsScreen
import com.expenss.tracker.ui.theme.ExpenssTheme
import com.expenss.tracker.util.TokenManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenssTheme {
                val navController = rememberNavController()
                val tokenManager = remember { TokenManager(this) }
                val startDest = if (tokenManager.isLoggedIn()) "dashboard" else "login"

                NavHost(navController = navController, startDestination = startDest) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNeedsOnboarding = {
                                navController.navigate("onboarding") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToRegister = { navController.navigate("register") },
                            onNavigateToForgotPassword = { navController.navigate("forgot-password") }
                        )
                    }

                    composable("forgot-password") {
                        ForgotPasswordScreen(
                            onNavigateToLogin = {
                                navController.navigate("login") {
                                    popUpTo("forgot-password") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("onboarding") {
                        OnboardingScreen(
                            onDone = {
                                navController.navigate("dashboard") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onNavigateToLogin = {
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("dashboard") {
                        DashboardScreen(
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo("dashboard") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable("goals") {
                        GoalsScreen(
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo("dashboard") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable("savings") {
                        SavingsScreen(
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo("dashboard") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable("analytics") {
                        AnalyticsScreen(
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo("dashboard") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
