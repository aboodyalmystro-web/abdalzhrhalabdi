package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.EnglishViewModel

enum class MainTab(val title: String, val icon: ImageVector, val tag: String) {
    LEARN("الدروس", Icons.Default.MenuBook, "nav_tab_learn"),
    VOCAB("المفردات", Icons.Default.Style, "nav_tab_vocab"),
    PHONETICS("الصوتيات", Icons.Default.RecordVoiceOver, "nav_tab_phonetics"),
    SKILLS("المهارات 4", Icons.Default.Psychology, "nav_tab_skills"),
    ASSESSMENT("التقدم", Icons.Default.EmojiEvents, "nav_tab_assessment")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val viewModel: EnglishViewModel = viewModel()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(MainTab.LEARN) }

    // Audio recording permission launcher
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                MainTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            viewModel.closeLesson()
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        modifier = Modifier.testTag(tab.tag)
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
                MainTab.LEARN -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToPlacement = { selectedTab = MainTab.ASSESSMENT },
                    onNavigateToVocab = { selectedTab = MainTab.VOCAB },
                    onNavigateToPhonetics = { selectedTab = MainTab.PHONETICS },
                    onNavigateToSkills = { selectedTab = MainTab.SKILLS }
                )
                MainTab.VOCAB -> VocabSrsScreen(viewModel = viewModel)
                MainTab.PHONETICS -> PhoneticsScreen(viewModel = viewModel)
                MainTab.SKILLS -> FourSkillsScreen(viewModel = viewModel)
                MainTab.ASSESSMENT -> AssessmentScreen(
                    viewModel = viewModel,
                    onNavigateBack = { selectedTab = MainTab.LEARN }
                )
            }
        }
    }
}
