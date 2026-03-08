package com.example.stcc

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.stcc.ui.theme.STCCTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            STCCTheme(darkTheme = isDarkMode) {
                var showSplash by remember { mutableStateOf(true) }
                
                if (showSplash) {
                    SplashScreen(onTimeout = { showSplash = false })
                } else {
                    MainScreen(isDarkMode = isDarkMode, onThemeChange = { isDarkMode = it })
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A237E)),
        contentAlignment = Alignment.Center
    ) {
        // Logo taking up most of the space with small margin
        Image(
            painter = painterResource(id = R.drawable.my_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp) // Little corner/margin left as requested
                .clip(RoundedCornerShape(32.dp)),
            contentScale = ContentScale.Fit
        )
        
        // Progress indicator smaller and bit upward
        LinearProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp) // Moved bit upward
                .width(160.dp) // Made it smaller
                .height(4.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.2f)
        )
    }
}

enum class Screen {
    HOME, PROFILE, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(isDarkMode: Boolean, onThemeChange: (Boolean) -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val homeUrl = "https://www.smashfoundation.com/"
    var currentUrl by remember { mutableStateOf(homeUrl) }
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var isLoggedIn by remember { mutableStateOf(true) }
    val webView = remember { mutableStateOf<WebView?>(null) }
    val context = LocalContext.current
    val updateUrl = "https://github.com/itsdanish786/Smash-Foundation/releases/latest"

    BackHandler(enabled = currentScreen != Screen.HOME || drawerState.isOpen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            currentScreen = Screen.HOME
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 0.dp,
                modifier = Modifier.width(300.dp)
            ) {
                // Creative Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0D47A1), Color(0xFF1A237E), Color(0xFF311B92))
                            )
                        )
                ) {
                    // Decorative background logo
                    Image(
                        painter = painterResource(id = R.drawable.my_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(200.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 60.dp, y = (-40).dp)
                            .rotate(15f)
                            .alpha(0.1f),
                        contentScale = ContentScale.Fit
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier
                                .size(80.dp)
                                .shadow(12.dp, CircleShape),
                            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.my_logo),
                                contentDescription = null,
                                modifier = Modifier.padding(4.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "SMASH FOUNDATION",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "Shihab Thangal Cultural Centre",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(8.dp))
                    
                    DrawerMenuItem(
                        label = "Home",
                        icon = Icons.Default.Home,
                        isSelected = currentScreen == Screen.HOME,
                        onClick = {
                            currentScreen = Screen.HOME
                            scope.launch { drawerState.close() }
                        }
                    )

                    DrawerMenuItem(
                        label = "About Developer",
                        icon = Icons.Default.Person,
                        isSelected = currentScreen == Screen.PROFILE,
                        onClick = {
                            currentScreen = Screen.PROFILE
                            scope.launch { drawerState.close() }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    DrawerMenuItem(
                        label = "Settings",
                        icon = Icons.Default.Settings,
                        isSelected = currentScreen == Screen.SETTINGS,
                        onClick = {
                            currentScreen = Screen.SETTINGS
                            scope.launch { drawerState.close() }
                        }
                    )

                    DrawerMenuItem(
                        label = "App Update",
                        icon = Icons.Default.Update,
                        isSelected = false,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
                            context.startActivity(intent)
                            scope.launch { drawerState.close() }
                        }
                    )

                    DrawerMenuItem(
                        label = "Exit App",
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        isSelected = false,
                        onClick = {
                            (context as? android.app.Activity)?.finish()
                        }
                    )
                }

                // Footer Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoggedIn) {
                        Button(
                            onClick = {
                                isLoggedIn = false
                                webView.value?.apply {
                                    clearCache(true)
                                    clearHistory()
                                    clearFormData()
                                    loadUrl("about:blank")
                                }
                                CookieManager.getInstance().apply {
                                    removeAllCookies(null)
                                    flush()
                                }
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sign Out", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Version 1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when(currentScreen) {
                                Screen.HOME -> "SMASH Foundation"
                                Screen.PROFILE -> "About Developer"
                                Screen.SETTINGS -> "Settings"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when(currentScreen) {
                    Screen.HOME -> {
                        if (isLoggedIn) {
                            NGOWebView(url = currentUrl, onWebViewCreated = { webView.value = it })
                        } else {
                            SignInScreen(onSignIn = { isLoggedIn = true })
                        }
                    }
                    Screen.PROFILE -> DeveloperProfileView()
                    Screen.SETTINGS -> SettingsView(isDarkMode = isDarkMode, onThemeChange = onThemeChange)
                }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsView(isDarkMode: Boolean, onThemeChange: (Boolean) -> Unit) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val updateUrl = "https://github.com/itsdanish786/Smash-Foundation/releases/download/v1.0.0/STCC.apk"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Preferences",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Notifications
        SettingsItem(
            title = "Notifications",
            subtitle = "Enable or disable app notifications",
            icon = if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
            trailing = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
        )

        // Dark Theme
        SettingsItem(
            title = "Dark Theme",
            subtitle = "Toggle light and dark mode",
            icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
            trailing = {
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onThemeChange
                )
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "System",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // App Update
        SettingsItem(
            title = "App Update",
            subtitle = "Check for the latest version",
            icon = Icons.Default.Update,
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
                context.startActivity(intent)
            }
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (trailing != null) {
                trailing()
            }
        }
    }
}

@Composable
fun SignInScreen(onSignIn: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.my_logo),
                contentDescription = null,
                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Welcome to SMASH Foundation",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onSignIn,
                modifier = Modifier.width(200.dp).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sign In", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DeveloperProfileView() {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profile_pic),
                        contentDescription = "Developer",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Fahad Hussain Danish",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Lead Developer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:fahad.12325061@lpu.in")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Contact Me", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NGOWebView(url: String, onWebViewCreated: (WebView) -> Unit) {
    AndroidView(
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                onWebViewCreated(this)
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
