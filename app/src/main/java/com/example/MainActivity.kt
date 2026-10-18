package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.model.AiMode
import com.example.ui.EvViewModel
import com.example.ui.auth.SignInScreen
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.screens.ActionsScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.LearningScreen
import com.example.ui.screens.MemoryVaultScreen
import com.example.ui.theme.EvCyan
import com.example.ui.theme.EvMint
import com.example.ui.theme.EvTheme
import com.example.ui.theme.EvViolet
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

enum class EvNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    CHAT("Ev Chat", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline, "tab_chat"),
    LEARN("Learn", Icons.Filled.School, Icons.Outlined.School, "tab_learn"),
    ACTIONS("Actions", Icons.Filled.PlayArrow, Icons.Outlined.PlayCircleOutline, "tab_actions"),
    BRAIN("Ev's Brain", Icons.Filled.Memory, Icons.Outlined.Memory, "tab_brain")
}

class MainActivity : ComponentActivity() {
    private val viewModel: EvViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EvTheme {
                AppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppRoot(viewModel: EvViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    // Listen to Firebase Auth state
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                viewModel.syncWithFirestore()
            }
        }
        Firebase.auth.addAuthStateListener(listener)

        // Attempt silent auto-sign-in if not signed in
        if (Firebase.auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                    viewModel.syncWithFirestore()
                },
                onUnauthenticated = {},
                scope = scope
            )
        } else {
            viewModel.syncWithFirestore()
        }

        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        SignInScreen(
            onSignInSuccess = {
                currentUser = Firebase.auth.currentUser
                viewModel.syncWithFirestore()
            }
        )
    } else {
        MainAppContainer(
            viewModel = viewModel,
            currentUser = currentUser!!
        )
    }
}

@Composable
fun MainAppContainer(
    viewModel: EvViewModel,
    currentUser: FirebaseUser
) {
    var selectedTab by remember { mutableStateOf(EvNavTab.CHAT) }
    val snackbarHostState = remember { SnackbarHostState() }
    val userFeedback by viewModel.userFeedback.collectAsState()

    // Handle feedback snackbars
    LaunchedEffect(userFeedback) {
        userFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    // Predictive Back handling
    if (selectedTab != EvNavTab.CHAT) {
        BackHandler {
            selectedTab = EvNavTab.CHAT
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                EvNavTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabColor = when (tab) {
                        EvNavTab.CHAT -> EvCyan
                        EvNavTab.LEARN -> EvViolet
                        EvNavTab.ACTIONS -> EvMint
                        EvNavTab.BRAIN -> EvViolet
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = tabColor,
                            selectedTextColor = tabColor,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = tabColor.copy(alpha = 0.16f)
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                EvNavTab.CHAT -> ChatScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
                EvNavTab.LEARN -> LearningScreen(
                    viewModel = viewModel,
                    onNavigateToChatWithPrompt = { prompt, mode ->
                        viewModel.setAiMode(mode)
                        viewModel.sendMessage(prompt)
                        selectedTab = EvNavTab.CHAT
                    },
                    modifier = Modifier.fillMaxSize()
                )
                EvNavTab.ACTIONS -> ActionsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
                EvNavTab.BRAIN -> MemoryVaultScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
