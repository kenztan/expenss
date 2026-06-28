package com.expenss.tracker.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenss.tracker.i18n.t
import com.expenss.tracker.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNeedsOnboarding: () -> Unit = {},
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(context) as T
        }
    })
    val loginState by viewModel.loginState.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) BgDark else BgLight
    val bg2 = if (isDark) Bg2Dark else Bg2Light
    val text = if (isDark) TextDark else TextLight
    val text2 = if (isDark) Text2Dark else Text2Light
    val text3 = if (isDark) Text3Dark else Text3Light
    val border = if (isDark) Color(0x14FFFFFF) else Color(0x14000000)
    val border2 = if (isDark) Color(0x20FFFFFF) else Color(0x20000000)
    val errorRed = Color(0xFFE24B4A)
    val isLoading = loginState is LoginState.Loading

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> onLoginSuccess()
            is LoginState.NeedsOnboarding -> onNeedsOnboarding()
            is LoginState.Error -> errorMsg = (loginState as LoginState.Error).message
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(bg).imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(bg)
                    .border(0.5.dp, border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 28.dp, vertical = 32.dp)
            ) {
                Text(t("login.title"), fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                    color = text, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(6.dp))
                Text(t("login.subtitle"), fontSize = 14.sp,
                    color = text2, fontWeight = FontWeight.Light)
                Spacer(Modifier.height(24.dp))

                // Error alert
                AnimatedVisibility(visible = errorMsg.isNotEmpty()) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x14E24B4A))
                                .border(1.dp, Color(0x33E24B4A), RoundedCornerShape(10.dp))
                                .padding(12.dp, 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("⚠", fontSize = 14.sp, color = errorRed)
                            Text(errorMsg, fontSize = 13.sp, color = errorRed,
                                fontWeight = FontWeight.Medium, lineHeight = 18.sp)
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }

                // Username field
                Text(t("login.usernameLabel"), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = text2)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(t("login.usernamePlaceholder"), color = text3, fontSize = 14.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = border2,
                        focusedContainerColor = bg2,
                        unfocusedContainerColor = bg2,
                        focusedTextColor = text,
                        unfocusedTextColor = text,
                    )
                )
                Spacer(Modifier.height(16.dp))

                // Password field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(t("login.passwordLabel"), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = text2)
                    TextButton(onClick = onNavigateToForgotPassword, contentPadding = PaddingValues(0.dp)) {
                        Text(t("login.forgotPassword"), fontSize = 12.sp, color = Accent)
                    }
                }
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("••••••••", color = text3, fontSize = 14.sp) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible },
                            contentPadding = PaddingValues(horizontal = 8.dp)) {
                            Text(if (passwordVisible) "Hide" else "Show",
                                fontSize = 12.sp, color = text3)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = border2,
                        focusedContainerColor = bg2,
                        unfocusedContainerColor = bg2,
                        focusedTextColor = text,
                        unfocusedTextColor = text,
                    )
                )
                Spacer(Modifier.height(20.dp))

                // Sign in button
                Button(
                    onClick = { viewModel.login(username, password) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(t("login.signIn"), fontSize = 14.sp, fontWeight = FontWeight.Medium,
                            color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${t("login.noAccount")} ", fontSize = 13.sp, color = text3)
                TextButton(onClick = onNavigateToRegister, contentPadding = PaddingValues(0.dp)) {
                    Text(t("login.signUp"), fontSize = 13.sp, color = Accent, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
