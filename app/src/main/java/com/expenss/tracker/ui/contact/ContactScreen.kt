package com.expenss.tracker.ui.contact

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenss.tracker.i18n.t
import com.expenss.tracker.ui.theme.*

@Composable
fun ContactScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val vm: ContactViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(context) as T
        }
    })
    val state by vm.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var touchedEmail by remember { mutableStateOf(false) }
    var touchedMessage by remember { mutableStateOf(false) }

    val isLoading = state is ContactState.Loading
    val errorMsg = (state as? ContactState.Error)?.message ?: ""
    val successMsg = (state as? ContactState.Success)?.message ?: ""

    val emailError = when {
        email.isEmpty() -> t("legal.emailRequired")
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> t("legal.emailInvalid")
        else -> null
    }
    val messageError = when {
        message.isEmpty() -> t("legal.messageRequired")
        message.length > 1000 -> t("legal.messageTooLong")
        else -> null
    }

    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) BgDark else BgLight
    val bg2 = if (isDark) Bg2Dark else Bg2Light
    val text = if (isDark) TextDark else TextLight
    val text2 = if (isDark) Text2Dark else Text2Light
    val text3 = if (isDark) Text3Dark else Text3Light
    val border2 = if (isDark) Color(0x20FFFFFF) else Color(0x20000000)
    val errorRed = Color(0xFFEF4444)
    val greenText = Color(0xFF15803D)
    val greenBg = if (isDark) Color(0xFF0D2818) else Color(0xFFF0FDF4)

    Box(modifier = Modifier.fillMaxSize().background(bg).imePadding()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                    Icon(IcChevronLeft, "Back", tint = text2, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(t("legal.contactTitle"), fontSize = 26.sp, fontWeight = FontWeight.Bold,
                color = text, letterSpacing = (-0.5).sp)
            Spacer(Modifier.height(8.dp))
            Text(t("legal.contactIntro"), fontSize = 14.sp, color = text2,
                fontWeight = FontWeight.Light, lineHeight = 21.sp)
            Spacer(Modifier.height(28.dp))

            Text(t("legal.yourEmail"), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = text2)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; touchedEmail = true },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(t("legal.emailPlaceholder"), color = text3, fontSize = 14.sp) },
                singleLine = true,
                isError = touchedEmail && emailError != null,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent, unfocusedBorderColor = border2, errorBorderColor = errorRed,
                    focusedContainerColor = bg2, unfocusedContainerColor = bg2, errorContainerColor = bg2,
                    focusedTextColor = text, unfocusedTextColor = text, errorTextColor = text,
                )
            )
            if (touchedEmail && emailError != null) {
                Spacer(Modifier.height(4.dp))
                Text(emailError, fontSize = 12.sp, color = errorRed)
            }
            Spacer(Modifier.height(16.dp))

            Text(t("legal.messageLabel"), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = text2)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { message = it; touchedMessage = true },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                placeholder = { Text(t("legal.messagePlaceholder"), color = text3, fontSize = 14.sp) },
                isError = touchedMessage && messageError != null,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent, unfocusedBorderColor = border2, errorBorderColor = errorRed,
                    focusedContainerColor = bg2, unfocusedContainerColor = bg2, errorContainerColor = bg2,
                    focusedTextColor = text, unfocusedTextColor = text, errorTextColor = text,
                )
            )
            if (touchedMessage && messageError != null) {
                Spacer(Modifier.height(4.dp))
                Text(messageError, fontSize = 12.sp, color = errorRed)
            }
            Spacer(Modifier.height(18.dp))

            AnimatedVisibility(visible = errorMsg.isNotEmpty()) {
                Column {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x14EF4444))
                            .border(1.dp, Color(0x33EF4444), RoundedCornerShape(10.dp))
                            .padding(12.dp, 10.dp)
                    ) { Text(errorMsg, fontSize = 13.sp, color = errorRed) }
                    Spacer(Modifier.height(14.dp))
                }
            }
            AnimatedVisibility(visible = successMsg.isNotEmpty()) {
                Column {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(greenBg)
                            .border(1.dp, Color(0x3322C55E), RoundedCornerShape(10.dp))
                            .padding(12.dp, 10.dp)
                    ) { Text(successMsg, fontSize = 13.sp, color = greenText) }
                    Spacer(Modifier.height(14.dp))
                }
            }

            Button(
                onClick = {
                    touchedEmail = true; touchedMessage = true
                    if (emailError == null && messageError == null) {
                        vm.send(email.trim(), message.trim())
                    }
                },
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(10.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(t("legal.sendMessage"), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
