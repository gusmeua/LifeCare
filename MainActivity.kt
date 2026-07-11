package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HealthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LifeCareNavigation()
                }
            }
        }
    }
}

@Composable
fun LifeCareNavigation() {
    val navController = rememberNavController()
    val viewModel: HealthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "onboarding"
    ) {
        // 1. Onboarding Screen
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToRegister = { role ->
                    navController.navigate("register/$role")
                },
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }

        // 2. Register Screen
        composable(
            route = "register/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "PATIENT"
            RegisterScreen(
                role = role,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    when (role) {
                        "PATIENT" -> navController.navigate("patient_home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                        "FAMILY" -> navController.navigate("family_home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                        "DOCTOR" -> navController.navigate("doctor_home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                }
            )
        }

        // 3. Login Screen
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    val user = viewModel.currentUser.value
                    if (user != null) {
                        val destination = when (user.role) {
                            "PATIENT" -> "patient_home"
                            "FAMILY" -> "family_home"
                            "DOCTOR" -> "doctor_home"
                            else -> "onboarding"
                        }
                        navController.navigate(destination) {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                }
            )
        }

        // 4. Patient Home Screen
        composable("patient_home") {
            PatientHomeScreen(
                viewModel = viewModel,
                onNavigateToMeasurements = { navController.navigate("patient_measurements") },
                onNavigateToReminders = { navController.navigate("patient_reminders") },
                onNavigateToFiles = { navController.navigate("patient_files") },
                onNavigateToReport = { navController.navigate("patient_report") },
                onNavigateToPricing = { navController.navigate("pricing") },
                onLogout = {
                    viewModel.logout()
                    navController.navigate("onboarding") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 5. Patient Vital Measurements Log Screen
        composable("patient_measurements") {
            PatientMeasurementsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. Patient Reminders Screen
        composable("patient_reminders") {
            PatientRemindersScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. Patient Medical Files Screen
        composable("patient_files") {
            PatientFilesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 8. Patient AI Report/Trend Screen
        composable("patient_report") {
            PatientReportScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 9. Family Home Screen Portal
        composable("family_home") {
            FamilyHomeScreen(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    navController.navigate("onboarding") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 10. Doctor Dashboard Monitor Screen
        composable("doctor_home") {
            DoctorHomeScreen(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    navController.navigate("onboarding") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 11. Monetization Pricing Options Screen
        composable("pricing") {
            PricingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
