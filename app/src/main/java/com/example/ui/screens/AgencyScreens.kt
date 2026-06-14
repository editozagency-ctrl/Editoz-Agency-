package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AILog
import com.example.data.Lead
import com.example.data.Payment
import com.example.data.Project
import com.example.data.SupportTicket
import com.example.ui.AgencyViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ----------------------------------------------------
// SCREEN COORDINATOR
// ----------------------------------------------------
@Composable
fun MainScreenCoordinator(viewModel: AgencyViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RichBlack)
    ) {
        // Active Screen render
        when (viewModel.currentScreen) {
            "splash" -> SplashView(viewModel)
            "auth" -> AuthView(viewModel)
            else -> {
                // Main app scaffold with unified navigation
                Scaffold(
                    bottomBar = {
                        AgencyBottomNavigationBar(
                            selectedRoute = viewModel.currentScreen,
                            onRouteSelected = { route ->
                                viewModel.currentScreen = route
                            }
                        )
                    },
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (viewModel.currentScreen) {
                            "home" -> HomeView(viewModel)
                            "services" -> ServicesView(viewModel)
                            "packages" -> PackagesView(viewModel)
                            "portfolio" -> PortfolioView(viewModel)
                            "portal" -> ClientPortalView(viewModel)
                            "support" -> AIConsultationView(viewModel)
                            "aitools" -> AIToolsView(viewModel)
                            "admin" -> AdminView(viewModel)
                        }
                    }
                }
            }
        }

        // Overlay Notification Banner
        viewModel.lastNotificationMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
            ) {
                CustomNotificationBanner(
                    message = msg,
                    onDismiss = { viewModel.dismissNotification() }
                )
            }
        }

        // Floating WhatsApp Button on Every Screen (except Splash)
        if (viewModel.currentScreen != "splash") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomEnd)
            ) {
                FloatingActionButton(
                    onClick = {
                        val message = "Hello Editoz Agency, I want to know more about your digital marketing services."
                        val encodedMsg = java.net.URLEncoder.encode(message, "UTF-8")
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://wa.me/919006822583?text=$encodedMsg")
                        }
                        context.startActivity(intent)
                    },
                    containerColor = Color(0xFF25D366),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = if (viewModel.currentScreen == "auth") 24.dp else 90.dp, end = 16.dp)
                        .testTag("floating_whatsapp_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Contact WhatsApp",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// BOTTOM NAVIGATION BAR
// ----------------------------------------------------
@Composable
fun AgencyBottomNavigationBar(
    selectedRoute: String,
    onRouteSelected: (String) -> Unit
) {
    val items = listOf(
        NavigationItem("home", "Home", Icons.Default.Dashboard),
        NavigationItem("services", "Solutions", Icons.Default.Category),
        NavigationItem("packages", "Pricing", Icons.Default.CardMembership),
        NavigationItem("portfolio", "Videos", Icons.Default.FolderSpecial),
        NavigationItem("portal", "Portal", Icons.Default.FolderShared),
        NavigationItem("support", "Consult", Icons.Default.SupportAgent),
        NavigationItem("aitools", "AI Tool", Icons.Default.AutoAwesome),
        NavigationItem("admin", "Admin", Icons.Default.AdminPanelSettings)
    )

    Surface(
        color = RichBlack,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                thickness = 1.dp,
                color = Color.White.copy(alpha = 0.05f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val active = selectedRoute == item.route
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_item_${item.route}")
                            .clickable { onRouteSelected(item.route) }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (active) OrangePrimary else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.label,
                            color = if (active) OrangePrimary else Color.White.copy(alpha = 0.3f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

data class NavigationItem(val route: String, val label: String, val icon: ImageVector)

// ----------------------------------------------------
// SPLASH VIEW
// ----------------------------------------------------
@Composable
fun SplashView(viewModel: AgencyViewModel) {
    var scale by remember { mutableStateOf(0.4f) }
    var rotation by remember { mutableStateOf(0f) }
    var welcomeOpacity by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        animate(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        ) { valValue, _ -> scale = valValue }
        
        animate(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        ) { valValue, _ -> rotation = valValue }

        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(500)
        ) { valValue, _ -> welcomeOpacity = valValue }

        delay(1200)
        viewModel.currentScreen = "auth"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(OrangePrimary.copy(alpha = 0.25f), RichBlack),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // High-End Agency Logo Mark
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .rotate(rotation)
                    .background(
                        Brush.linearGradient(listOf(OrangePrimary, OrangeSecondary)),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(2.dp)
                    .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "E",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "EDITOZ AGENCY",
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "CREATIVE SOCIAL ENGINE",
                color = OrangePrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
        }
    }
}

// ----------------------------------------------------
// AUTHENTICATION VIEW
// ----------------------------------------------------
@Composable
fun AuthView(viewModel: AgencyViewModel) {
    var emailInput by remember { mutableStateOf("client@fitlife.com") }
    var passwordInput by remember { mutableStateOf("••••••••") }
    var phoneInput by remember { mutableStateOf("+1 (555) 019-2831") }
    var otpInput by remember { mutableStateOf("") }
    
    var loginMethod by remember { mutableStateOf("email") } // email, phone
    var showOtpFields by remember { mutableStateOf(false) }

    var inputName by remember { mutableStateOf("Alex Mercer") }
    var inputCompany by remember { mutableStateOf("FitLife Athletic") }
    var selectedRole by remember { mutableStateOf("Client") } // Client, Admin

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Brand Header Mark
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "DIGITAL MARKETING",
                    color = OrangePrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "EDITOZ",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.5).sp
                    )
                    Text(
                        text = ".",
                        color = OrangePrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Enterprise Partner Portal",
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Secure Sign In",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gain secure access to campaign calendars, master metrics, and pending revision requests",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Navigation Toggles for Auth Method
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PremiumGray)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (loginMethod == "email") OrangePrimary else Color.Transparent)
                            .clickable { loginMethod = "email"; showOtpFields = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Email Access", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (loginMethod == "phone") OrangePrimary else Color.Transparent)
                            .clickable { loginMethod = "phone" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("OTP Access", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Simulation Parameters
                Text("Role & Context Configuration", color = OrangeSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Your Full Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = BorderGray
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("auth_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = inputCompany,
                    onValueChange = { inputCompany = it },
                    label = { Text("Brand / Enterprise Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = BorderGray
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("auth_company_input")
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Segmented Role Selection
                Text("Simulate Portal View as:", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Client", "Admin").forEach { role ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedRole == role) OrangePrimary.copy(alpha = 0.2f) else PremiumGray)
                                .border(1.dp, if (selectedRole == role) OrangePrimary else BorderGray, RoundedCornerShape(8.dp))
                                .clickable { selectedRole = role }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (role == "Client") Icons.Default.Person else Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = if (selectedRole == role) OrangePrimary else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = role,
                                    color = if (selectedRole == role) OrangePrimary else TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = BorderGray)
                Spacer(modifier = Modifier.height(16.dp))

                if (loginMethod == "email") {
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    if (!showOtpFields) {
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Mobile Number") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = BorderGray
                            ),
                            placeholder = { Text("+1 (555) 019-2831") },
                            modifier = Modifier.fillMaxWidth().testTag("auth_phone_input")
                        )
                    } else {
                        Text(
                            text = "OTP sent to $phoneInput!",
                            color = OrangeSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { otpInput = it },
                            label = { Text("Enter 6-digit Verification OTP code") },
                            placeholder = { Text("e.g. 520419") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = BorderGray
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("auth_otp_input")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (loginMethod == "phone" && !showOtpFields) {
                            showOtpFields = true
                            viewModel.initiateMockPayment(0.00, "Otp Request (System)")
                        } else {
                            viewModel.performLogin(
                                email = emailInput,
                                name = inputName,
                                company = inputCompany,
                                role = selectedRole
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_login")
                ) {
                    Text(
                        text = if (loginMethod == "phone" && !showOtpFields) "Request OTP Code" else "Proceed Secure Check-in",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Social Sign-ins
            Text("or access with", color = TextMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Google Mock Sign In
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(PremiumCharcoal, CircleShape)
                        .border(1.dp, BorderGray, CircleShape)
                        .clickable {
                            viewModel.performLogin(
                                email = "google.partner@gmail.com",
                                name = inputName.ifEmpty { "Google Admin" },
                                company = inputCompany.ifEmpty { "Apex Branding" },
                                role = selectedRole
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.AlternateEmail, contentDescription = "Google Access", tint = Color.White)
                }
                
                // Fingerprint biometric mock
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(PremiumCharcoal, CircleShape)
                        .border(1.dp, BorderGray, CircleShape)
                        .clickable {
                            viewModel.performLogin(
                                email = "bio.verify@editoz.com",
                                name = inputName.ifEmpty { "Secured Executive" },
                                company = inputCompany.ifEmpty { "Standard Logistics" },
                                role = selectedRole
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Biometrics Verified", tint = OrangePrimary)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ----------------------------------------------------
// DASHBOARD / HOME VIEW
// ----------------------------------------------------
@Composable
fun HomeView(viewModel: AgencyViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Upper Brand Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DIGITAL MARKETING",
                        color = OrangePrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "EDITOZ",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = ".",
                            color = OrangePrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Notification Icon Circle
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                            .clickable { viewModel.switchRole() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // Profile Badge
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(OrangePrimary, CircleShape)
                            .border(1.dp, OrangePrimary.copy(alpha = 0.2f), CircleShape)
                            .clickable { viewModel.switchRole() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.userName.take(2).uppercase(),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Large Bold Greeting Headline
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Hello,",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 42.sp,
                    letterSpacing = (-1.5).sp
                )
                Text(
                    text = viewModel.userName,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 42.sp,
                    letterSpacing = (-1.5).sp
                )
            }

            // Premium Notification Callout
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(OrangePrimary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .border(1.dp, OrangePrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = OrangePrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "New! AI Content Tools Live",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Generate premium caption copies, custom reel video scripts, and key hashtags instantly with Gemini 3.5-Flash.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Metric statistics highlight cards
            Text(
                text = "Partner Growth Metrics",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Views Generated", color = TextMuted, fontSize = 11.sp)
                        Text("3.4M+", color = OrangePrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("Last 30 days (+89%)", color = Color.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Meta ROI Setup", color = TextMuted, fontSize = 11.sp)
                        Text("4.85x", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("CPA optimization active", color = TextLightGray, fontSize = 9.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Showcase Services Quick Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "High-Growth Solutions",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View 8 Channels",
                    color = OrangePrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.currentScreen = "services" }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val demoServices = listOf(
                    Triple("Reel Production", "Organic Reach", Icons.Default.VideoCall),
                    Triple("Meta Funnels", "High Converting", Icons.Default.TrendingUp),
                    Triple("Branding & Assets", "Premium Trust", Icons.Default.Palette),
                    Triple("TikTok Growth", "Engagement Suite", Icons.Default.Campaign)
                )
                items(demoServices) { service ->
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .background(PremiumCharcoal, RoundedCornerShape(12.dp))
                            .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                            .clickable { viewModel.currentScreen = "services" }
                            .padding(16.dp)
                    ) {
                        Column {
                            Icon(imageVector = service.third, contentDescription = null, tint = OrangePrimary)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(text = service.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = service.second, color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Success Stories
            Text(
                text = "Partner Success Journal",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiveIndicator()
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("FITLIFE HEALTH RETAINER", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "How Editoz scale fitness subscription revenues by 340% via premium color grading, pacing hooks, and high-CTR lead gen ad campaigns in under 45 days.",
                    color = TextLightGray,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ad Spend Return: 5.6x", color = OrangeSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Case Study ->", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clickable { viewModel.currentScreen = "portfolio" })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Client Testimonials
            Text(
                text = "Client Testimonials & Feedback",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val reviews = listOf(
                    "Editoz took over our Instagram edit loop and within 3 weeks our organic lead flow overtook our paid ad inquiries! Mindblowing editing speed. - Sarah K. (FitLife Founder)",
                    "Their high-end corporate rebrand stylebook was pristine. Very clean visual aesthetic, completely understood our luxury focus. - Marcus T. (Apex CEO)"
                )
                items(reviews) { r ->
                    Box(
                        modifier = Modifier
                            .width(280.dp)
                            .background(PremiumCharcoal, RoundedCornerShape(12.dp))
                            .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = r,
                            color = TextLightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ----------------------------------------------------
// SERVICES VIEW
// ----------------------------------------------------
@Composable
fun ServicesView(viewModel: AgencyViewModel) {
    var selectedServiceIndex by remember { mutableStateOf<Int?>(null) }
    var nameInput by remember { mutableStateOf(viewModel.userName) }
    var emailInput by remember { mutableStateOf(viewModel.userEmail) }
    var serviceFormType by remember { mutableStateOf("Social Media Management") }
    var descriptionInput by remember { mutableStateOf("") }

    val servicesList = listOf(
        ServiceItem("Social Media Management", "Complete channel audits, structured publishing grids, and community scale strategies.", Icons.Default.Campaign, "$1,200/mo", "24/7 Channel Care • Monthly Grid Blueprints"),
        ServiceItem("Content Creation", "Premium organic capture, visual hook formulation, and aesthetic post designs.", Icons.Default.Image, "$800/mo", "Aesthetic Photoshoots • High Retention Static Carousels"),
        ServiceItem("Reel Editing", "Premium speed transitions, beat synching, sound design, and custom graphical hooks.", Icons.Default.VideoLibrary, "$1,100/mo", "Sound Grading • Auto Cap Rendering"),
        ServiceItem("Graphic Design", "Branded assets, pitch books, high-converting banner designs, and product layouts.", Icons.Default.Brush, "$600/mo", "Infinite Revisions • Vector Native Assets"),
        ServiceItem("Branding", "Luxurious logo concepts, guidelines, curated colors, and overall brand identity.", Icons.Default.Palette, "$1,500 One-time", "Vip Presentation Stylebook • Master Brand Typography"),
        ServiceItem("Meta Ads", "Sales funnels, strategic custom lookalikes, creative A/B testing and copywriting.", Icons.Default.AdsClick, "$1,800/mo", "CBO Campaign Config • High-CTR Creative Swaps"),
        ServiceItem("Google Ads", "Curated intent search setups, Youtube placements, and maximum conversion PPC assets.", Icons.Default.TrendingUp, "$1,850/mo", "Intent Keywords Swarms • Target CPA Adjusters"),
        ServiceItem("Website Development", "High-conversion responsive landing structures, WebFlow master, and custom portfolios.", Icons.Default.Computer, "$3,000 Setup", "Ultra Swift LCP Indexing • Premium Framer Mockups")
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            item {
                Text(
                    text = "High-End Capabilities",
                    color = OrangePrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Editoz Channels",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Tap on any high-growth capability card below to unpack expected deliverables, retainer pricing, and request custom details.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )
            }

            items(servicesList.chunked(2)) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { service ->
                        val index = servicesList.indexOf(service)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp)
                                .background(PremiumCharcoal, RoundedCornerShape(14.dp))
                                .border(1.dp, BorderGray, RoundedCornerShape(14.dp))
                                .clickable { selectedServiceIndex = index }
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = service.icon,
                                        contentDescription = service.title,
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = service.price,
                                        color = TextWhite,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(OrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = service.title,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = service.shortDesc,
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Free Audit Callout Form
                GlassmorphicCard(modifier = Modifier.fillMaxWidth().testTag("audit_form")) {
                    Text(
                        text = "Claim Free Channel Audit",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Our senior content strategists will review your brand's active posting consistency, editing hooks, and paid ads ROI and return a detailed PDF outline completely free.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Your Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("audit_name")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("E-mail Address") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("audit_email")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Service Type Dropdown simulator
                    Text("Select Target Channel Audit Service:", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Social Media Management", "Reel Editing", "Meta Ads", "Google Ads").forEach { type ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (serviceFormType == type) OrangePrimary.copy(alpha = 0.2f) else PremiumGray)
                                    .border(1.dp, if (serviceFormType == type) OrangePrimary else BorderGray, RoundedCornerShape(8.dp))
                                    .clickable { serviceFormType = type }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = type, color = if (serviceFormType == type) OrangePrimary else TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("Short Account Handle / Website Link") },
                        placeholder = { Text("e.g. instagram.com/fitlife.gym") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("audit_details")
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.submitLeadForm(
                                name = nameInput,
                                email = emailInput,
                                phone = "",
                                businessName = viewModel.companyName,
                                serviceType = serviceFormType,
                                websiteUrl = descriptionInput,
                                description = "Requested Free Audit from Editoz Channels page.",
                                inquiryType = "Free Audit"
                            )
                            descriptionInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("submit_audit")
                    ) {
                        Text("Dispatch Free Audit Request", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Full Detail Modal Sheet
        selectedServiceIndex?.let { index ->
            val s = servicesList[index]
            AlertDialog(
                onDismissRequest = { selectedServiceIndex = null },
                containerColor = PremiumCharcoal,
                shape = RoundedCornerShape(16.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = s.icon, contentDescription = null, tint = OrangePrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = s.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(text = s.shortDesc, color = TextLightGray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "What is included:", color = OrangePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = s.details, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp, top = 4.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Starting Retainer: ${s.price}", color = Color.Green, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        onClick = {
                            selectedServiceIndex = null
                            serviceFormType = s.title
                        }
                    ) {
                        Text("Inject custom details", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedServiceIndex = null }) {
                        Text("Close details", color = Color.White)
                    }
                }
            )
        }
    }
}

data class ServiceItem(val title: String, val shortDesc: String, val icon: ImageVector, val price: String, val details: String)

// ----------------------------------------------------
// PACKAGES VIEW
// ----------------------------------------------------
@Composable
fun PackagesView(viewModel: AgencyViewModel) {
    // Sliders for dynamic packaging pricing
    var expectedReels by remember { mutableStateOf(8) }
    var expectedPosters by remember { mutableStateOf(12) }
    var expectedAdsCampaigns by remember { mutableStateOf(2) }

    // Custom pricing math
    // Reels = $80 each, Posters = $40 each, Ads = $250 each. Standard premium overhead $150.
    val customMonthlyCost = (expectedReels * 80) + (expectedPosters * 40) + (expectedAdsCampaigns * 250) + 150

    var quoteName by remember { mutableStateOf(viewModel.userName) }
    var quoteContact by remember { mutableStateOf("") }
    var quoteBusiness by remember { mutableStateOf(viewModel.companyName) }
    var quoteTimeline by remember { mutableStateOf("Immediate") } // Immediate, Custom, Later

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text(
                text = "Secure Business Subscriptions",
                color = OrangePrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Premium Agency Packages",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Select an existing enterprise scope or contact our senior team directly. Pay a minimal booking deposit to seed production immediately.",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // SPECIAL OFFER SECTION BANNER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = OrangePrimary.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, OrangePrimary)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .background(OrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, OrangePrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = "Offer icon",
                            tint = OrangePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "20% Discount On Every Plan",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Offer Valid Till: 20 May 2026",
                            color = OrangeSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3 Standard Tier Cards (PLAN A, PLAN B, PLAN C)
        val standardTiers = listOf(
            TierItem(
                name = "PLAN A",
                price = "₹11,999",
                marketPrice = "₹25,999",
                validity = "1 Month",
                tagline = "Starter package targeting direct multichannels growth & audience connection.",
                features = "• Instagram Management\n• YouTube Management\n• Facebook Management\n• Meta Ads Setup\n• 1 Model for Brand\n• 8-10 Reels",
                icon = Icons.Default.RocketLaunch
            ),
            TierItem(
                name = "PLAN B",
                price = "₹21,999",
                marketPrice = "₹39,999",
                validity = "3 Months",
                tagline = "Full channel scaling retainer for extreme organic & ad impact.",
                features = "• Instagram Management\n• YouTube Management\n• Facebook Management\n• Meta Ads Setup\n• 1 Model for Brand\n• 10-15 Reels Per Month",
                icon = Icons.Default.AutoAwesome
            ),
            TierItem(
                name = "PLAN C",
                price = "₹39,999",
                marketPrice = "₹64,999",
                validity = "6 Months",
                tagline = "The absolute social authority takeover & elite content dominance.",
                features = "• Instagram Management\n• YouTube Management\n• Facebook Management\n• Meta Ads Setup\n• 1 Model for Brand\n• 15-20 Reels Per Month",
                icon = Icons.Default.Diamond
            )
        )

        items(standardTiers) { tier ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = PremiumCharcoal),
                border = BorderStroke(1.dp, if (tier.name == "PLAN B") OrangePrimary else BorderGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(OrangePrimary.copy(alpha = 0.1f), CircleShape)
                                    .border(1.dp, OrangePrimary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tier.icon,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = tier.name,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Validity: ${tier.validity}",
                                    color = OrangeSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (tier.name == "PLAN B") {
                            Text(
                                text = "BEST VALUE",
                                color = RichBlack,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .background(OrangePrimary, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    // Price Layout row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = tier.price,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = tier.marketPrice,
                            color = TextMuted,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            style = TextStyle(textDecoration = TextDecoration.LineThrough),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                        Text(
                            text = "Offer Price",
                            color = OrangeSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = tier.tagline, color = TextLightGray, fontSize = 11.sp, lineHeight = 16.sp)
                    
                    HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 12.dp))
                    
                    Text(
                        text = "Services Included:",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(text = tier.features, color = TextMuted, fontSize = 12.sp, lineHeight = 20.sp)
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Booking Amount callout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OrangePrimary.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .border(1.dp, OrangePrimary.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Booking info",
                            tint = OrangePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "To Start Your Project, Pay Only ₹500 Booking Amount",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val context = LocalContext.current
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subscribe_${tier.name.lowercase().replace(" ", "_")}"),
                        colors = ButtonDefaults.buttonColors(containerColor = if (tier.name == "PLAN B") OrangePrimary else PremiumGray),
                        shape = RoundedCornerShape(10.dp),
                        onClick = {
                            viewModel.initiateMockPayment(
                                amount = 500.0,
                                service = "${tier.name} Booking Deposit (Our Price: ${tier.price})"
                            )
                            viewModel.currentScreen = "portal"
                        }
                    ) {
                        Text(
                            text = "Pay ₹500 Booking Amount",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.4f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366)),
                        onClick = {
                            val msg = "Hello Editoz Agency, I am interested in ${tier.name} (${tier.validity}) package for ${tier.price}. Please share more details."
                            val encodedMsg = java.net.URLEncoder.encode(msg, "UTF-8")
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://wa.me/919006822583?text=$encodedMsg")
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Enquire via WhatsApp",
                                color = Color(0xFF25D366),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Custom Pricing Simulator Slider UI
        item {
            Box(
                modifier = Modifier
                    .fillModifierWithGradient()
                    .padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Editoz Custom SLA Builder",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Build a completely independent Service Level Agreement. Price computes dynamically.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    InteractiveDeliverableSlider(
                        title = "Expected Professional Reels",
                        value = expectedReels,
                        range = 2f..40f,
                        onValueChange = { expectedReels = it }
                    )
                    InteractiveDeliverableSlider(
                        title = "Graphic Poster Designs / Assets",
                        value = expectedPosters,
                        range = 2f..50f,
                        onValueChange = { expectedPosters = it }
                    )
                    InteractiveDeliverableSlider(
                        title = "Ad Funnels Config / Audits",
                        value = expectedAdsCampaigns,
                        range = 0f..10f,
                        onValueChange = { expectedAdsCampaigns = it }
                    )

                    Divider(color = BorderGray, modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Custom Estimate:", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$$customMonthlyCost / mo",
                            color = OrangePrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.testTag("custom_price_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth().testTag("custom_quote_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        onClick = {
                            viewModel.initiateMockPayment(
                                amount = customMonthlyCost.toDouble(),
                                service = "Custom SLA Plan ($expectedReels Reels, $expectedPosters Posters)"
                            )
                            viewModel.currentScreen = "portal"
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Secure This Custom Retainer", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Custom Quote Request Sub-Form
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth().testTag("quote_form_section")) {
                Text(
                    text = "Request Custom Quote Call",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Need specific enterprise conditions? Submit this prompt and our management team will coordinate a custom proposal deck.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = quoteName,
                    onValueChange = { quoteName = it },
                    label = { Text("Contact Person") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = BorderGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = quoteBusiness,
                    onValueChange = { quoteBusiness = it },
                    label = { Text("Brand / Legal Entity") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = BorderGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = quoteContact,
                    onValueChange = { quoteContact = it },
                    label = { Text("Direct Telephone / Mobile") },
                    placeholder = { Text("e.g. +1 555-0321") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = BorderGray
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("quote_phone")
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Execution Urgency Target:", color = TextLightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Immediate", "15 Days", "Exploratory").forEach { time ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (quoteTimeline == time) OrangePrimary.copy(alpha = 0.2f) else PremiumGray)
                                .border(1.dp, if (quoteTimeline == time) OrangePrimary else BorderGray, RoundedCornerShape(8.dp))
                                .clickable { quoteTimeline = time }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = time, color = if (quoteTimeline == time) OrangePrimary else TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val buildDetails = "Custom Slider Setup: $expectedReels Reels, $expectedPosters Posters, $expectedAdsCampaigns Ads. Desired timelines: $quoteTimeline"
                        viewModel.submitLeadForm(
                            name = quoteName,
                            email = viewModel.userEmail,
                            phone = quoteContact,
                            businessName = quoteBusiness,
                            serviceType = "Custom Quote Plan",
                            websiteUrl = "",
                            description = buildDetails,
                            inquiryType = "Consultation"
                        )
                        quoteContact = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("submit_quote")
                ) {
                    Text("Secure Custom Proposal Call", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

data class TierItem(val name: String, val price: String, val marketPrice: String, val validity: String, val tagline: String, val features: String, val icon: ImageVector)

// Extension layout helper
fun Modifier.fillModifierWithGradient(): Modifier = this
    .fillMaxWidth()
    .background(PremiumCharcoal, RoundedCornerShape(16.dp))
    .border(1.dp, BorderGray, RoundedCornerShape(16.dp))

// ----------------------------------------------------
// PORTFOLIO VIEW (WITH REELS PLAYERS & POSTERS)
// ----------------------------------------------------
@Composable
fun PortfolioView(viewModel: AgencyViewModel) {
    var activePortfolioTab by remember { mutableStateOf("reels") } // reels, posters, cases

    // Movie details simulation
    var movieIsPlaying by remember { mutableStateOf(false) }
    val simulatedHeartCount = remember { mutableStateOf(1420) }
    val isLiked = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Case Studies & Creative Showcase",
            color = OrangePrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Text(
            text = "Editoz Master Works",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Grid selection pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(PremiumCharcoal)
                .padding(4.dp)
        ) {
            listOf("reels" to "🎥 Active Reels", "posters" to "🎨 Designs", "cases" to "📈 Cases").forEach { tab ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activePortfolioTab == tab.first) OrangePrimary else Color.Transparent)
                        .clickable { activePortfolioTab = tab.first }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = tab.second, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (activePortfolioTab) {
                "reels" -> {
                    // Simulated Vertical Mobile Reel Player Card
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
                    ) {
                        // Ambient looping canvas simulation background
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(OrangePrimary.copy(alpha = 0.2f), RichBlack, Color.DarkGray.copy(alpha = 0.15f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (movieIsPlaying) Icons.Default.MovieFilter else Icons.Default.PlayCircleFilled,
                                    contentDescription = "Playback state indicator",
                                    tint = OrangePrimary.copy(alpha = 0.82f),
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clickable { movieIsPlaying = !movieIsPlaying }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (movieIsPlaying) "VIDEO PLAYBACK ACTIVE" else "TAP MATRIX TO PLAY REELS DEMO",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Right hand controls overlay
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 80.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Love control
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = {
                                        isLiked.value = !isLiked.value
                                        if (isLiked.value) simulatedHeartCount.value++ else simulatedHeartCount.value--
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Like campaign link",
                                        tint = if (isLiked.value) OrangePrimary else Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Text(text = "${simulatedHeartCount.value}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Share control
                            IconButton(onClick = { viewModel.initiateMockPayment(0.0, "Share Campaign") }) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                        }

                        // Bottom caption details of the selected reel
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LiveIndicator(text = "DEMO EDITING GRID")
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "@editoz.marketing", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "High retention vertical format graded for FitLife Athletic. The 0-3 second visual hook boosts conversion rate by 45%. ✨",
                                color = TextLightGray,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                "posters" -> {
                    // Graphic Designs grid showoff
                    LazyVerticalGridDesign(
                        onSelectFile = { filename ->
                            viewModel.initiateMockPayment(0.00, "View Graphic: $filename")
                        }
                    )
                }

                "cases" -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val cases = listOf(
                            CaseStudyItem("Apex Tech Launch", "Built premium dark funnel driving $45,000 corporate lead acquisitions in 30 days.", "Meta CBO • Video Hook Ads", "+312% Growth"),
                            CaseStudyItem("FitLife Workout Loops", "Seeded 15 vertical hooks matching premium sound triggers. Generated 1.2M viral impressions organically.", "Reel pacing • Hook grids", "+480% Views"),
                            CaseStudyItem("Derm Glow Branding", "Defined sleek aesthetic typography logos, product labels, and standard email templates for a cosmetics startup.", "Minimal Layouts • Brand Stylebook", "+120% conversion")
                        )
                        items(cases) { c ->
                            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(text = c.serviceStack, color = OrangePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(text = c.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    }
                                    Text(
                                        text = c.growthMetric,
                                        color = Color.Green,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = c.description, color = TextLightGray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LazyVerticalGridDesign(onSelectFile: (String) -> Unit) {
    val items = listOf("lux_watch_ad.png", "cosmetic_minimal.png", "startup_dashboard.png", "gym_fitconcept.png")
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PremiumCharcoal)
                    .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                    .clickable { onSelectFile(item) }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = item, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Curated vector design asset • Click to unpack", color = TextMuted, fontSize = 11.sp)
                }
            }
        }
    }
}

data class CaseStudyItem(val title: String, val description: String, val serviceStack: String, val growthMetric: String)

// ----------------------------------------------------
// CLIENT PORTAL (WITH PROGRESS & SIGN SIGNATURE)
// ----------------------------------------------------
@Composable
fun ClientPortalView(viewModel: AgencyViewModel) {
    var activePortalTab by remember { mutableStateOf("progress") } // progress, templates, invoice
    val projectsList by viewModel.projectsState.collectAsState()
    val paymentsList by viewModel.paymentsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Master Campaign Portal",
            color = OrangePrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Text(
            text = "Campaign Central",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Navigation tab selections
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(PremiumCharcoal)
                .padding(4.dp)
        ) {
            listOf("progress" to "🛰️ Progress", "templates" to "📝 Agreements", "invoice" to "💳 Billing").forEach { tab ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activePortalTab == tab.first) OrangePrimary else Color.Transparent)
                        .clickable { activePortalTab = tab.first }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = tab.second, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (activePortalTab) {
                "progress" -> {
                    // List of campaigns
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(projectsList) { p ->
                            ProjectSectionCard(
                                project = p, 
                                onSubmitRevision = { rev -> viewModel.submitRevisionRequest(p, rev) }
                            )
                        }
                    }
                }

                "templates" -> {
                    // Agreements & Canvas drawing signature block
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Legal Agreements & Signing Certified Documents",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Certificate and seal agreement documents directly inside the partner portal. Please complete your custom signature on the secure electronic canvas block below.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )
                        }

                        items(projectsList) { p ->
                            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = p.documentUrl ?: "Standard Service Agreement", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "SLA Package: ${p.category}", color = TextMuted, fontSize = 11.sp)
                                    }
                                    Icon(
                                        imageVector = if (p.signatureSaved) Icons.Default.VerifiedUser else Icons.Default.PendingActions,
                                        contentDescription = null,
                                        tint = if (p.signatureSaved) Color.Green else OrangeSecondary
                                    )
                                }

                                if (!p.signatureSaved) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Electronic Secure Signature:", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    // CANVAS COMPOSITE FOR SIGNATURE DRAWING
                                    SignatureCanvasInputBlock(
                                        onSigned = {
                                            viewModel.signAgreement(p)
                                        }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("✓ Digitally Sealed and Legally Binding", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                "invoice" -> {
                    // Billing history list + Quick UPI payment demo
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                                Text("Outstanding Standard Retainer", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("$1,250.00", color = OrangePrimary, fontSize = 28.sp, fontWeight = FontWeight.Black)
                                Text("Premium Social Retainer due June 24, 2026", color = TextMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    modifier = Modifier.fillMaxWidth().testTag("razorpay_pay_now"),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                    onClick = { viewModel.initiateMockPayment(1250.00, "Outstanding Monthly Retainer (Standard)") }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Wallet, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Complete Pay via Razorpay / UPI", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Payment Ledger History", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(paymentsList) { payment ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PremiumCharcoal, RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = payment.serviceName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "${payment.transactionId} • ${payment.dateCreated}", color = TextMuted, fontSize = 10.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "$${"%,.2f".format(payment.amount)}", color = OrangeSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            text = "Download PDF Invoice",
                                            color = OrangePrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .clickable { viewModel.initiateMockPayment(0.00, "Invoice Download triggered: ${payment.invoiceUrl}") }
                                                .padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectSectionCard(project: Project, onSubmitRevision: (String) -> Unit) {
    var revisionInput by remember { mutableStateOf("") }
    var activeProgressScale by remember { mutableStateOf(0f) }

    LaunchedEffect(project.progress) {
        animate(
            initialValue = 0f,
            targetValue = project.progress.toFloat() / 100f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        ) { valValue, _ -> activeProgressScale = valValue }
    }

    GlassmorphicCard(modifier = Modifier.fillMaxWidth().testTag("project_card_${project.id}")) {
        // Label header space
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = project.category.uppercase(), color = OrangePrimary, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text(text = project.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            Text(
                text = "${project.progress}%",
                color = OrangePrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar vector simulation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(PremiumGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(activeProgressScale)
                    .background(Brush.horizontalGradient(listOf(OrangePrimary, OrangeSecondary)))
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Production Milestones & Tasks Done:", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        // Milestones
        val taskSplits = project.taskListJson.split(",")
        taskSplits.forEach { task ->
            if (task.contains(":")) {
                val name = task.substringBefore(":")
                val status = task.substringAfter(":")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = name, color = TextLightGray, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (status == "Done") Icons.Default.CheckCircle else Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = if (status == "Done") Color.Green else OrangeAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = status, color = if (status == "Done") Color.Green else OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Divider(color = BorderGray, modifier = Modifier.padding(vertical = 12.dp))

        if (project.revisionRequest != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OrangePrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(text = "Active Revision Request: \"${project.revisionRequest}\" (Production Team review in progress)", color = OrangeSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Revision requests box
        Text(text = "Submit Premium Revision Directive:", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = revisionInput,
                onValueChange = { revisionInput = it },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = OrangePrimary,
                    unfocusedBorderColor = BorderGray
                ),
                placeholder = { Text("e.g. Please crop middle sequence...", fontSize = 11.sp) },
                modifier = Modifier.weight(1f).testTag("revision_input_field")
            )
            Button(
                onClick = {
                    if (revisionInput.isNotEmpty()) {
                        onSubmitRevision(revisionInput)
                        revisionInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("submit_revision_button")
            ) {
                Text("Send", color = Color.White)
            }
        }
    }
}

@Composable
fun SignatureCanvasInputBlock(
    onSigned: () -> Unit
) {
    val points = remember { mutableStateListOf<Offset>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PremiumGray, RoundedCornerShape(12.dp))
            .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color.Black, RoundedCornerShape(8.dp))
                .border(0.5.dp, BorderGray, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        points.add(change.position)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (points.size > 1) {
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = OrangePrimary,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            if (points.isEmpty()) {
                Text(
                    text = "Sign with finger dynamically on this canvas",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = { points.clear() }
            ) {
                Text("Clear Drawing", color = TextMuted, fontSize = 12.sp)
            }
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(6.dp),
                onClick = {
                    if (points.isNotEmpty()) {
                        onSigned()
                        points.clear()
                    }
                },
                modifier = Modifier.testTag("submit_signature")
            ) {
                Text("Certify Custom Sign", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

// ----------------------------------------------------
// SUPPORT TICKETS / CONSULTATION BOOKING
// ----------------------------------------------------
@Composable
fun AIConsultationView(viewModel: AgencyViewModel) {
    val ticketsList by viewModel.ticketsState.collectAsState()
    
    var ticketSubject by remember { mutableStateOf("") }
    var ticketDescription by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Reel Editing") }

    // Appointment Calendars selection mockup
    var consultDate by remember { mutableStateOf("June 18, 2026") }
    var consultTime by remember { mutableStateOf("03:30 PM (EST)") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text(
                text = "Secure Consultations & Tickets",
                color = OrangePrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Client Support",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Direct Whatsapp Link Booking
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Official Agency Contacts",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Connect directly with our executive team. All links open instant direct channels.",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Contact Info Rows
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val context = LocalContext.current
                    
                    // Phone / WhatsApp info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:+919006822583")
                                }
                                context.startActivity(intent)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone icon",
                            tint = OrangePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Official Phone Line", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("+91 9006822583", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    // Email row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:editozagency@gmail.com")
                                }
                                context.startActivity(intent)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email icon",
                            tint = OrangePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Official Email Handle", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("editozagency@gmail.com", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val context = LocalContext.current
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            val defaultMessage = "Hello Editoz Agency, I want to know more about your digital marketing services."
                            val encodedMsg = java.net.URLEncoder.encode(defaultMessage, "UTF-8")
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://wa.me/919006822583?text=$encodedMsg")
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Launch WhatsApp Chat", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGray),
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:+919006822583")
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Secure Dial Hotline", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            // Appointment Booking Scheduler Segment
            Box(
                modifier = Modifier
                    .fillModifierWithGradient()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Schedule Creative Consultation",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lock in a dedicated consultation session with our senior strategy leads to map out your channel expansion.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text("Curated Available Booking Dates:", color = TextLightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("June 18, 2026", "June 22, 2026", "June 25, 2026").forEach { d ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (consultDate == d) OrangePrimary.copy(alpha = 0.2f) else Color.Black)
                                    .border(1.dp, if (consultDate == d) OrangePrimary else BorderGray, RoundedCornerShape(6.dp))
                                    .clickable { consultDate = d }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = d, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Curated Work-hour Slots:", color = TextLightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("10:00 AM", "01:30 PM", "03:30 PM (EST)").forEach { t ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (consultTime == t) OrangePrimary.copy(alpha = 0.2f) else Color.Black)
                                    .border(1.dp, if (consultTime == t) OrangePrimary else BorderGray, RoundedCornerShape(6.dp))
                                    .clickable { consultTime = t }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = t, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val context = LocalContext.current
                    Button(
                        modifier = Modifier.fillMaxWidth().testTag("book_consultation"),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            val msg = "Hello Editoz Agency, I want to book a free consultation for $consultDate at $consultTime."
                            val encodedMsg = java.net.URLEncoder.encode(msg, "UTF-8")
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://wa.me/919006822583?text=$encodedMsg")
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Book Free Consultation", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Open Campaign Tickets Hub", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Active ticket list from Room Flow
        items(ticketsList) { t ->
            Box(
                modifier = Modifier
                    .fillModifierWithGradient()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = t.subject, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = t.status.uppercase(),
                            color = if (t.status == "Open") OrangeSecondary else Color.Green,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(if (t.status == "Open") OrangePrimary.copy(alpha = 0.15f) else Color.Green.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(text = "Stack: ${t.category} • Urgency: ${t.priority}", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(vertical = 4.dp))
                    Text(text = t.lastMessage, color = TextLightGray, fontSize = 12.sp)
                }
            }
        }

        // Raise Ticket form
        item {
            Spacer(modifier = Modifier.height(12.dp))
            GlassmorphicCard(modifier = Modifier.fillMaxWidth().testTag("raise_ticket_section")) {
                Text(text = "Raise Support Ticket", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = ticketSubject,
                    onValueChange = { ticketSubject = it },
                    label = { Text("What is the main topic / issue?") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = BorderGray
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("ticket_subject")
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text("Channel category:", color = TextLightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Reel Editing", "Meta Ads", "Branding Assets", "Web Systems").forEach { cat ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedCategory == cat) OrangePrimary.copy(alpha = 0.2f) else PremiumGray)
                                .border(1.dp, if (selectedCategory == cat) OrangePrimary else BorderGray, RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = cat, color = if (selectedCategory == cat) OrangePrimary else TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = ticketDescription,
                    onValueChange = { ticketDescription = it },
                    label = { Text("Unpack descriptive details of the issue") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = BorderGray
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("ticket_desc")
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (ticketSubject.isNotEmpty() && ticketDescription.isNotEmpty()) {
                            viewModel.createSupportTicket(
                                subject = ticketSubject,
                                category = selectedCategory,
                                priority = "High",
                                description = ticketDescription
                            )
                            ticketSubject = ""
                            ticketDescription = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("ticket_submit")
                ) {
                    Text("Secure Dispatch Ticket", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ----------------------------------------------------
// AI TOOLS VIEW (CAPTION, HASHTAGS etc.)
// ----------------------------------------------------
@Composable
fun AIToolsView(viewModel: AgencyViewModel) {
    var toolSelectedTab by remember { mutableStateOf("Caption") } // Caption, Hashtag, Reel Script, Content Idea
    var userPrompt by remember { mutableStateOf("Organic healthy cold-press brand launching key winter detox bundle") }
    val logsList by viewModel.aiLogsState.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Copywriter & Strategy Generator Suite",
            color = OrangePrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Text(
            text = "Creative Copy AI",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Tool Mode selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .clip(RoundedCornerShape(8.dp))
                .background(PremiumCharcoal)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Caption" to "✍️ Caption", "Hashtag" to "🏷️ Hashtag", "Reel Script" to "🎬 Reel Script", "Content Idea" to "💡 Strategy Ideas").forEach { t ->
                val selected = toolSelectedTab == t.first
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selected) OrangePrimary else Color.Transparent)
                        .clickable { toolSelectedTab = t.first }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = t.second, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth().testTag("ai_tool_card")) {
                    Text(
                        text = "Secure Gemini Flash Campaign Architect",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Inject core product details or brand context below. Editoz Copy AI yields conversion-optimized output structures instantly.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = userPrompt,
                        onValueChange = { userPrompt = it },
                        label = { Text("Core Value Proposition / Topic Details") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = BorderGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("ai_prompt_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (viewModel.isGeneratingAI) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(12.dp)
                        ) {
                            CircularProgressIndicator(color = OrangePrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Gemini 3.5-Flash formulating strategic assets...", color = OrangePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            modifier = Modifier.fillMaxWidth().testTag("trigger_ai"),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            shape = RoundedCornerShape(8.dp),
                            onClick = {
                                viewModel.generateAICreativeContent(type = toolSelectedTab, prompt = userPrompt)
                            }
                        ) {
                            Text("Generate Optimized SLA Copy", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (viewModel.aiResultOutput.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillModifierWithGradient()
                            .testTag("ai_result_box")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Generated Strategic Structure (${viewModel.aiGeneratorType})",
                                    color = OrangeSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PremiumGray),
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(viewModel.aiResultOutput))
                                    }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy Assets", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = viewModel.aiResultOutput,
                                color = TextWhite,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            item {
                Text(text = "Agency Generation Logs History", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(logsList) { log ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PremiumCharcoal, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = log.toolType.uppercase(), color = OrangePrimary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text(text = log.dateCreated, color = TextMuted, fontSize = 10.sp)
                        }
                        Text(text = "Value: \"${log.prompt}\"", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
                        Text(text = log.result, color = TextLightGray, fontSize = 11.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// ADMIN DASHBOARD (ANALYTICS, LEADS etc.)
// ----------------------------------------------------
@Composable
fun AdminView(viewModel: AgencyViewModel) {
    var activeAdminTab by remember { mutableStateOf("leads") } // leads, analytics, work
    val leadsList by viewModel.leadsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Editoz Agency Headquarters",
                    color = OrangePrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Control Desk",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Role Badge switcher
            Text(
                text = "ADMIN VIEW",
                color = RichBlack,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .background(OrangePrimary, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .clickable { viewModel.switchRole() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Role restriction gate info banner
        if (viewModel.currentUserRole != "Admin") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, OrangePrimary, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = OrangePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulated Restriction Active", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "The Admin Console tracks received brand inquiries, lead conversions, revenue analytics, and team allocations. Click below to bypass role restrictions during testing.",
                        color = TextLightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth().testTag("toggle_admin_role"),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        onClick = { viewModel.currentUserRole = "Admin" }
                    ) {
                        Text("Bypass Role Protection", color = Color.White)
                    }
                }
            }
        } else {
            // Authorized Admin Interface
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PremiumCharcoal)
                    .padding(4.dp)
            ) {
                listOf("leads" to "📥 Inbound Leads", "analytics" to "📊 Revenue", "work" to "👥 Team Assignment").forEach { tab ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeAdminTab == tab.first) OrangePrimary else Color.Transparent)
                            .clickable { activeAdminTab = tab.first }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = tab.second, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (activeAdminTab) {
                    "leads" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (leadsList.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No active inquiries received yet. Submit the lead form in 'Services' or 'Pricing' to populate.",
                                            color = TextMuted,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            items(leadsList) { lead ->
                                Box(
                                    modifier = Modifier
                                        .fillModifierWithGradient()
                                        .testTag("inbound_lead_${lead.id}")
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = lead.inquiryType.uppercase(), color = OrangePrimary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                            IconButton(
                                                modifier = Modifier.size(16.dp),
                                                onClick = { viewModel.deleteLead(lead.id) }
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove Lead", tint = TextMuted)
                                            }
                                        }
                                        Text(text = "${lead.name} (${lead.businessName})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "Service: ${lead.serviceType} • Contact: ${lead.email} ${lead.phone}", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(vertical = 4.dp))
                                        Text(text = "Details: \"${lead.description}\"", color = TextLightGray, fontSize = 12.sp)
                                        lead.websiteUrl?.let { url ->
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(text = "Target Account: $url", color = OrangeSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "analytics" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("Unified Monthly Recurring Revenue", color = TextLightGray, fontSize = 12.sp)
                                    Text("$48,600.00", color = OrangePrimary, fontSize = 32.sp, fontWeight = FontWeight.Black)
                                    Text("MRR target scale (June Target: $50,000.00)", color = TextMuted, fontSize = 11.sp)
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Agency Scaling Run Rate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    
                                    // Render custom line chart
                                    MetricLineChart(
                                        data = listOf(
                                            "Jan" to 14000f,
                                            "Feb" to 19500f,
                                            "Mar" to 22000f,
                                            "Apr" to 31000f,
                                            "May" to 42000f,
                                            "June" to 48600f
                                        )
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("Performance Benchmarks (Overall)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("• Active SLA Calendars: 14 Brand partners\n• Reels Editing Production Speed: 6.4h mean turnaround\n• Over-the-beat Video Hook retention: 84.5% avg density\n• Meta Ads CAC efficiency: -24% over industrial parity", color = TextLightGray, fontSize = 12.sp, lineHeight = 20.sp)
                                }
                            }
                        }
                    }

                    "work" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val members = listOf(
                                Triple("Marcus Vance", "Lead Video Editor", "FitLife Gym Campaigns • 2 Active Reels"),
                                Triple("Elena Rostova", "Head Social Manager", "Google Placements • Brand Overhauls"),
                                Triple("Ashhar Khan", "Meta Advertising Architect", "Glam Beauty funnels • CBO Configurations")
                            )
                            items(members) { m ->
                                Box(
                                    modifier = Modifier
                                        .fillModifierWithGradient()
                                        .padding(bottom = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(OrangePrimary.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.Groups, contentDescription = null, tint = OrangePrimary)
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(text = m.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(text = m.second, color = OrangeSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(text = m.third, color = TextMuted, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
