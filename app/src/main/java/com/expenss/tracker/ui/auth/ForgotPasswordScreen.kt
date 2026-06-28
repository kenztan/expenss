package com.expenss.tracker.ui.auth

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenss.tracker.i18n.t
import com.expenss.tracker.ui.theme.*

@Composable
fun ForgotPasswordScreen(onNavigateToLogin: () -> Unit) {
    val context = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(context) as T
        }
    })
    val forgotState by vm.forgotState.collectAsState()

    var email       by remember { mutableStateOf("") }
    var touched     by remember { mutableStateOf(false) }
    var errorMsg    by remember { mutableStateOf("") }

    val isLoading = forgotState is ForgotState.Loading
    val isSuccess = forgotState is ForgotState.Success

    LaunchedEffect(forgotState) {
        if (forgotState is ForgotState.Error) {
            errorMsg = (forgotState as ForgotState.Error).message
        }
    }

    val emailError = when {
        email.isEmpty() -> t("forgotPassword.emailRequired")
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> t("forgotPassword.emailInvalid")
        else -> null
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

        if (isSuccess) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(18.dp))
                        .background(Color(0x1F3B82F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✉", fontSize = 28.sp)
                }
                Text(t("forgotPassword.checkEmailTitle"), fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                    color = text, letterSpacing = (-0.5).sp)
                Text(
                    t("forgotPassword.checkEmailDesc"),
                    fontSize = 14.sp, color = text2, lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text(t("forgotPassword.backToLogin"), fontSize = 14.sp, fontWeight = FontWeight.Medium,
                        color = Color.White)
                }
            }
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(bg)
                    .border(0.5.dp, border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 28.dp, vertical = 32.dp)
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color(0x143B82F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✉", fontSize = 20.sp)
                }
                Spacer(Modifier.height(16.dp))
                Text(t("forgotPassword.title"), fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                    color = text, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(6.dp))
                Text(t("forgotPassword.subtitle"),
                    fontSize = 14.sp, color = text2, fontWeight = FontWeight.Light)
                Spacer(Modifier.height(24.dp))

                AnimatedVisibility(visible = errorMsg.isNotEmpty()) {
                    Column {
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

                Text(t("forgotPassword.emailLabel"), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = text2)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; touched = true; errorMsg = "" },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("you@email.com", color = text3, fontSize = 14.sp) },
                    singleLine = true,
                    isError = touched && emailError != null,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = border2,
                        errorBorderColor = errorRed,
                        focusedContainerColor = bg2,
                        unfocusedContainerColor = bg2,
                        errorContainerColor = bg2,
                        focusedTextColor = text,
                        unfocusedTextColor = text,
                        errorTextColor = text,
                    )
                )
                if (touched && emailError != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(emailError, fontSize = 12.sp, color = errorRed)
                }
                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        touched = true
                        errorMsg = ""
                        if (emailError != null) return@Button
                        vm.forgotPassword(email.trim())
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp),
                            color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(t("forgotPassword.sendLink"), fontSize = 14.sp,
                            fontWeight = FontWeight.Medium, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${t("forgotPassword.rememberPassword")} ", fontSize = 13.sp, color = text3)
                TextButton(onClick = onNavigateToLogin, contentPadding = PaddingValues(0.dp)) {
                    Text(t("forgotPassword.backToLogin"), fontSize = 13.sp, color = Accent, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
