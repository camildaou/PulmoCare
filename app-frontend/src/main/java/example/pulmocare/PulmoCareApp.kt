package com.example.pulmocare

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pulmocare.data.UserRepository
import com.example.pulmocare.ui.screens.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulmoCareApp() {
    val userRepository = remember { UserRepository() }
    var isLoggedIn by remember { mutableStateOf(userRepository.isLoggedIn()) }

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val items = listOf(
        Screen.Dashboard,
        Screen.Appointments,
        Screen.Doctors,
        Screen.MedicalInfo,
        Screen.AIAssessment,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.padding(16.dp))
                Text(
                    text = "PulmoCare",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
                Divider()
                Spacer(modifier = Modifier.padding(8.dp))

                items.forEach { screen ->
                    NavigationDrawerItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = null) },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        userRepository.logout()
                        isLoggedIn = false
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.padding(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (isLoggedIn && currentDestination?.route != Screen.Login.route && currentDestination?.route != Screen.Signup.route) {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (currentDestination?.route) {
                                    Screen.Dashboard.route -> "Dashboard"
                                    Screen.Appointments.route -> "Appointments"
                                    Screen.Doctors.route -> "Doctors"
                                    Screen.MedicalInfo.route -> "Medical Info"
                                    Screen.AIAssessment.route -> "AI Assessment"
                                    Screen.Profile.route -> "Profile"
                                    else -> "PulmoCare"
                                }
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (isLoggedIn && currentDestination?.route != Screen.Login.route && currentDestination?.route != Screen.Signup.route) {
                    NavigationBar {
                        items.forEach { screen ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route
                ) {
                    composable(Screen.Login.route) {
                        LoginScreen(
                            onLoginSuccess = {
                                isLoggedIn = true
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToSignup = {
                                navController.navigate(Screen.Signup.route)
                            }
                        )
                    }

                    composable(Screen.Signup.route) {
                        SignupScreen(
                            onSignupSuccess = {
                                isLoggedIn = true
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Signup.route) { inclusive = true }
                                }
                            },
                            onNavigateToLogin = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Signup.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.Dashboard.route) {
                        if (isLoggedIn) {
                            DashboardScreen(
                                onNavigateToAppointments = { navController.navigate(Screen.Appointments.route) },
                                onNavigateToDoctors = { navController.navigate(Screen.Doctors.route) },
                                onNavigateToMedicalInfo = { navController.navigate(Screen.MedicalInfo.route) },
                                onNavigateToAIAssessment = { navController.navigate(Screen.AIAssessment.route) },
                                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        }
                    }

                    composable(Screen.Appointments.route) {
                        if (isLoggedIn) {
                            AppointmentScreen()
                        } else {
                            LaunchedEffect(Unit) {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        }
                    }

                    composable(Screen.Doctors.route) {
                        if (isLoggedIn) {
                            DoctorsScreen(
                                onBookAppointment = { doctorId ->
                                    // Navigate to appointment booking screen with doctor ID
                                    navController.navigate(Screen.Appointments.route)
                                }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        }
                    }

                    composable(Screen.MedicalInfo.route) {
                        if (isLoggedIn) {
                            MedicalInfoScreen()
                        } else {
                            LaunchedEffect(Unit) {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        }
                    }

                    composable(Screen.AIAssessment.route) {
                        if (isLoggedIn) {
                            AIAssessmentScreen()
                        } else {
                            LaunchedEffect(Unit) {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        }
                    }

                    composable(Screen.Profile.route) {
                        if (isLoggedIn) {
                            ProfileScreen(
                                onLogout = {
                                    userRepository.logout()
                                    isLoggedIn = false
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                    }
                                }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Login : Screen("login", "Login", Icons.Default.Login)
    object Signup : Screen("signup", "Signup", Icons.Default.PersonAdd)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Appointments : Screen("appointments", "Appointments", Icons.Default.CalendarMonth)
    object Doctors : Screen("doctors", "Doctors", Icons.Default.MedicalServices)
    object MedicalInfo : Screen("medical_info", "Medical Info", Icons.Default.HealthAndSafety)
    object AIAssessment : Screen("ai_assessment", "AI Assessment", Icons.Default.Psychology)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}