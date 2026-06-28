package com.expenss.tracker.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenss.tracker.i18n.t
import com.expenss.tracker.ui.theme.*

@Composable
fun ResetPasswordScreen(
    token: String?,
    onNavigateToLogin: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val context = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(context) as T
        }
    })
    val resetState by vm.resetPasswordState.collectAsState()

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var touchedPassword by remember { mutableStateOf(false) }
    var touchedConfirm by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    val isLoading = resetState is ResetPasswordState.Loading
    val isSuccess = resetState is ResetPasswordState.Success
    val noToken = token.isNullOrBlank()

    LaunchedEffect(resetState) {
        if (resetState is ResetPasswordState.Error) {
            errorMsg = (resetState as ResetPasswordState.Error).message
        }
    }

    val passwordError = when {
        password.isEmpty() -> t("resetPassword.passwordRequired")
        password.length < 8 -> t("resetPassword.passwordMinLength")
        !password.any { it.isUpperCase() } || !password.any { it.isDigit() } -> t("resetPassword.passwordWeak")
        else -> null
    }
    val confirmError = when {
        confirmPassword.isNotEmpty() && confirmPassword != password -> t("resetPassword.passwordMismatch")
        else -> null
    }

    fun passwordStrength(): String {
        if (password.length < 8) return "weak"
        val hasUpper = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        return if (hasUpper && hasDigit) "strong" else "medium"
    }

    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) BgDark else BgLight
    val bg2 = if (isDark) Bg2Dark else Bg2Light
    val text = if (isDark) TextDark else TextLight
    val text2 = if (isDark) Text2Dark else Text2Light
    val text3 = if (isDark) Text3Dark else Text3Light
    val border = if (isDark) Color(0x14FFFFFF) else Color(0x14000000)
    val border2 = if (isDark) Color(0x20FFFFFF) else Color(0x20000000)
    val errorRed = Color(0xFFE24B4A)

    Box(modifier = Modifier.fillMaxSize().background(bg).imePadding(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(bg)
                    .border(0.5.dp, border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    noToken -> ResultState(
                        icon = "✕", iconBg = Color(0x1AE24B4A), iconColor = errorRed,
                        title = t("resetPassword.invalidTitle"),
                        desc = t("resetPassword.noToken"),
                        text = text, text2 = text2,
                        buttonLabel = t("resetPassword.requestNew"),
                        onButtonClick = onNavigateToForgotPassword
                    )
                    isSuccess -> ResultState(
                        icon = "✓", iconBg = Color(0x1F22C55E), iconColor = Color(0xFF4ADE80),
                        title = t("resetPassword.successTitle"),
                        desc = t("resetPassword.successDesc"),
                        text = text, text2 = text2,
                        buttonLabel = t("resetPassword.goToLogin"),
                        onButtonClick = onNavigateToLogin
                    )
                    else -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x143B82F6)),
                                contentAlignment = Alignment.Center
                            ) { Icon(IcLock, null, tint = Accent, modifier = Modifier.size(20.dp)) }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(t("resetPassword.title"), fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                            color = text, letterSpacing = (-0.5).sp, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        Text(t("resetPassword.subtitle"), fontSize = 14.sp, color = text2,
                            fontWeight = FontWeight.Light, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(24.dp))

                        AnimatedVisibility(visible = errorMsg.isNotEmpty()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x14E24B4A))
                                        .border(1.dp, Color(0x33E24B4A), RoundedCornerShape(10.dp))
                                        .padding(12.dp, 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("!", fontSize = 14.sp, color = errorRed, fontWeight = FontWeight.Bold)
                                    Text(errorMsg, fontSize = 13.sp, color = errorRed,
                                        fontWeight = FontWeight.Medium, lineHeight = 18.sp)
                                }
                                Spacer(Modifier.height(20.dp))
                            }
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(t("resetPassword.newPasswordLabel"), fontSize = 13.sp,
                                fontWeight = FontWeight.Medium, color = text2)
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it; touchedPassword = true },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("••••••••", color = text3, fontSize = 14.sp) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                isError = touchedPassword && passwordError != null,
                                shape = RoundedCornerShape(10.dp),
                                trailingIcon = {
                                    TextButton(onClick = { passwordVisible = !passwordVisible },
                                        contentPadding = PaddingValues(horizontal = 8.dp)) {
                                        Text(if (passwordVisible) "Hide" else "Show", fontSize = 12.sp, color = text3)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Accent, unfocusedBorderColor = border2,
                                    errorBorderColor = errorRed,
                                    focusedContainerColor = bg2, unfocusedContainerColor = bg2, errorContainerColor = bg2,
                                    focusedTextColor = text, unfocusedTextColor = text, errorTextColor = text,
                                )
                            )
                            if (password.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                val strength = passwordStrength()
                                val strengthColor = when (strength) {
                                    "strong" -> Color(0xFF4ADE80); "medium" -> Color(0xFFFBBF24); else -> errorRed
                                }
                                Box(modifier = Modifier.fillMaxWidth().height(3.dp)
                                    .clip(RoundedCornerShape(2.dp)).background(if (isDark) Color(0x1AFFFFFF) else Color(0x1A000000))) {
                                    val frac = when (strength) { "strong" -> 1f; "medium" -> 0.66f; else -> 0.33f }
                                    Box(modifier = Modifier.fillMaxWidth(frac).height(3.dp)
                                        .clip(RoundedCornerShape(2.dp)).background(strengthColor))
                                }
                            }
                            if (touchedPassword && passwordError != null) {
                                Spacer(Modifier.height(5.dp))
                                Text(passwordError, fontSize = 11.sp, color = errorRed)
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(t("resetPassword.confirmPasswordLabel"), fontSize = 13.sp,
                                fontWeight = FontWeight.Medium, color = text2)
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it; touchedConfirm = true },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("••••••••", color = text3, fontSize = 14.sp) },
                                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                isError = touchedConfirm && confirmError != null,
                                shape = RoundedCornerShape(10.dp),
                                trailingIcon = {
                                    TextButton(onClick = { confirmVisible = !confirmVisible },
                                        contentPadding = PaddingValues(horizontal = 8.dp)) {
                                        Text(if (confirmVisible) "Hide" else "Show", fontSize = 12.sp, color = text3)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Accent, unfocusedBorderColor = border2,
                                    errorBorderColor = errorRed,
                                    focusedContainerColor = bg2, unfocusedContainerColor = bg2, errorContainerColor = bg2,
                                    focusedTextColor = text, unfocusedTextColor = text, errorTextColor = text,
                                )
                            )
                            if (touchedConfirm && confirmError != null) {
                                Spacer(Modifier.height(5.dp))
                                Text(confirmError, fontSize = 11.sp, color = errorRed)
                            }
                        }
                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = {
                                touchedPassword = true; touchedConfirm = true
                                errorMsg = ""
                                if (passwordError == null && confirmError == null && confirmPassword.isNotEmpty() && token != null) {
                                    vm.resetPassword(token, password)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Accent)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(t("resetPassword.submit"), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultState(
    icon: String, iconBg: Color, iconColor: Color,
    title: String, desc: String, text: Color, text2: Color,
    buttonLabel: String, onButtonClick: () -> Unit
) {
    Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)).background(iconBg),
        contentAlignment = Alignment.Center) {
        Text(icon, fontSize = 24.sp, color = iconColor, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(16.dp))
    Text(title, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = text,
        letterSpacing = (-0.5).sp, textAlign = TextAlign.Center)
    Spacer(Modifier.height(6.dp))
    Text(desc, fontSize = 14.sp, color = text2, lineHeight = 21.sp, textAlign = TextAlign.Center)
    Spacer(Modifier.height(20.dp))
    Button(
        onClick = onButtonClick,
        modifier = Modifier.fillMaxWidth().height(44.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Accent)
    ) {
        Text(buttonLabel, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}
