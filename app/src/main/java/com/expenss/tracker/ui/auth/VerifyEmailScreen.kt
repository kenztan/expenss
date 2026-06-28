package com.expenss.tracker.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenss.tracker.i18n.t
import com.expenss.tracker.ui.theme.*

@Composable
fun VerifyEmailScreen(
    token: String?,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    val context = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(context) as T
        }
    })
    val state by vm.verifyEmailState.collectAsState()

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) vm.verifyEmail(token)
    }

    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) BgDark else BgLight
    val text = if (isDark) TextDark else TextLight
    val text2 = if (isDark) Text2Dark else Text2Light
    val border = if (isDark) Color(0x14FFFFFF) else Color(0x14000000)

    Box(modifier = Modifier.fillMaxSize().background(bg), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bg)
                .border(0.5.dp, border, RoundedCornerShape(14.dp))
                .padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                token.isNullOrBlank() -> VerifyResult(
                    icon = "✕", iconBg = Color(0x1AE24B4A), iconColor = Color(0xFFE24B4A),
                    title = t("verifyEmail.failedTitle"), desc = t("verifyEmail.noToken"),
                    text = text, text2 = text2,
                    buttonLabel = t("verifyEmail.backToSignup"), onButtonClick = onNavigateToSignup
                )
                state is VerifyEmailState.Loading || state is VerifyEmailState.Idle -> {
                    CircularProgressIndicator(color = Accent, strokeWidth = 2.5.dp, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(20.dp))
                    Text(t("verifyEmail.verifying"), fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                        color = text, letterSpacing = (-0.5).sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text(t("verifyEmail.verifyingDesc"), fontSize = 14.sp, color = text2,
                        textAlign = TextAlign.Center, lineHeight = 21.sp)
                }
                state is VerifyEmailState.Success -> VerifyResult(
                    icon = "✓", iconBg = Color(0x1F22C55E), iconColor = Color(0xFF4ADE80),
                    title = t("verifyEmail.successTitle"), desc = t("verifyEmail.successDesc"),
                    text = text, text2 = text2,
                    buttonLabel = t("verifyEmail.goToLogin"), onButtonClick = onNavigateToLogin
                )
                else -> VerifyResult(
                    icon = "✕", iconBg = Color(0x1AE24B4A), iconColor = Color(0xFFE24B4A),
                    title = t("verifyEmail.failedTitle"),
                    desc = (state as? VerifyEmailState.Error)?.message ?: t("errors.unexpected"),
                    text = text, text2 = text2,
                    buttonLabel = t("verifyEmail.backToSignup"), onButtonClick = onNavigateToSignup
                )
            }
        }
    }
}

@Composable
private fun VerifyResult(
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
