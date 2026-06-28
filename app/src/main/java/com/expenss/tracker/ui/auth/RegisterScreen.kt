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
fun RegisterScreen(
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(context) as T
        }
    })
    val registerState by vm.registerState.collectAsState()

    var username        by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreedToTerms   by remember { mutableStateOf(false) }

    var passwordVisible        by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // touched flags for inline validation
    var touchedUsername        by remember { mutableStateOf(false) }
    var touchedEmail           by remember { mutableStateOf(false) }
    var touchedPassword        by remember { mutableStateOf(false) }
    var touchedConfirmPassword by remember { mutableStateOf(false) }
    var touchedTerms           by remember { mutableStateOf(false) }

    var errorMsg by remember { mutableStateOf("") }

    val isLoading = registerState is RegisterState.Loading
    val isSuccess = registerState is RegisterState.Success

    LaunchedEffect(registerState) {
        if (registerState is RegisterState.Error) {
            errorMsg = (registerState as RegisterState.Error).message
        }
    }

    // Validation
    val usernameError = when {
        username.isEmpty() -> t("signup.usernameRequired")
        username.length < 3 -> t("signup.usernameMinLength")
        username.contains(' ') -> t("signup.usernameNoSpaces")
        else -> null
    }
    val emailError = when {
        email.isEmpty() -> t("signup.emailRequired")
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> t("signup.emailInvalid")
        else -> null
    }
    val passwordError = when {
        password.isEmpty() -> t("signup.passwordRequired")
        password.length < 8 -> t("signup.passwordMinLength")
        !password.any { it.isUpperCase() } || !password.any { it.isDigit() } ->
            t("signup.passwordWeak")
        else -> null
    }
    val confirmError = when {
        confirmPassword.isEmpty() -> t("signup.confirmPasswordRequired")
        confirmPassword != password -> t("signup.passwordMismatch")
        else -> null
    }

    fun passwordStrength(): String {
        if (password.length < 8) return "weak"
        val hasUpper = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        return if (hasUpper && hasDigit) "strong" else "medium"
    }

    fun isFormValid() = usernameError == null && emailError == null &&
            passwordError == null && confirmError == null && agreedToTerms

    fun submit() {
        touchedUsername = true; touchedEmail = true
        touchedPassword = true; touchedConfirmPassword = true; touchedTerms = true
        errorMsg = ""
        if (!isFormValid()) return
        vm.register(username.trim(), email.trim(), password)
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
            // Email-sent success state
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
                Text(t("signup.checkEmailTitle"), fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                    color = text, letterSpacing = (-0.5).sp)
                Text(t("signup.checkEmailDesc"),
                    fontSize = 14.sp, color = text2, lineHeight = 22.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text(t("signup.checkEmailHint"),
                    fontSize = 12.sp, color = text3,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onNavigateToLogin) {
                    Text(t("signup.backToLogin"), fontSize = 14.sp, color = Accent,
                        fontWeight = FontWeight.Medium)
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
                Text(t("signup.title"), fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                    color = text, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(6.dp))
                Text(t("signup.subtitle"), fontSize = 14.sp,
                    color = text2, fontWeight = FontWeight.Light)
                Spacer(Modifier.height(24.dp))

                // Error banner
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

                // Username
                FieldLabel(t("signup.usernameLabel"), text2)
                Spacer(Modifier.height(6.dp))
                AuthField(
                    value = username,
                    onValueChange = { username = it; touchedUsername = true },
                    placeholder = t("signup.usernamePlaceholder"),
                    isError = touchedUsername && usernameError != null,
                    text = text, text3 = text3, bg2 = bg2, border2 = border2
                )
                if (touchedUsername && usernameError != null) {
                    FieldErrorText(usernameError, errorRed)
                }
                Spacer(Modifier.height(16.dp))

                // Email
                FieldLabel(t("signup.emailLabel"), text2)
                Spacer(Modifier.height(6.dp))
                AuthField(
                    value = email,
                    onValueChange = { email = it; touchedEmail = true },
                    placeholder = t("signup.emailPlaceholder"),
                    keyboardType = KeyboardType.Email,
                    isError = touchedEmail && emailError != null,
                    text = text, text3 = text3, bg2 = bg2, border2 = border2
                )
                if (touchedEmail && emailError != null) {
                    FieldErrorText(emailError, errorRed)
                }
                Spacer(Modifier.height(16.dp))

                // Password
                FieldLabel(t("signup.passwordLabel"), text2)
                Spacer(Modifier.height(6.dp))
                AuthField(
                    value = password,
                    onValueChange = { password = it; touchedPassword = true },
                    placeholder = t("signup.passwordPlaceholder"),
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible },
                    isError = touchedPassword && passwordError != null,
                    text = text, text3 = text3, bg2 = bg2, border2 = border2
                )
                // Strength indicator
                if (password.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    val strength = passwordStrength()
                    val (strengthColor, strengthLabel) = when (strength) {
                        "strong" -> Color(0xFF4ADE80) to t("signup.strengthStrong")
                        "medium" -> Color(0xFFFBBF24) to t("signup.strengthMedium")
                        else     -> errorRed to t("signup.strengthWeak")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(3) { i ->
                                val active = (strength == "strong") ||
                                        (strength == "medium" && i < 2) ||
                                        (strength == "weak" && i < 1)
                                Box(modifier = Modifier.width(32.dp).height(4.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (active) strengthColor else Color(0x1AFFFFFF)))
                            }
                        }
                        Text(strengthLabel, fontSize = 11.sp, color = strengthColor,
                            fontWeight = FontWeight.Medium)
                    }
                }
                if (touchedPassword && passwordError != null) {
                    FieldErrorText(passwordError, errorRed)
                }
                Spacer(Modifier.height(16.dp))

                // Confirm password
                FieldLabel(t("signup.confirmPasswordLabel"), text2)
                Spacer(Modifier.height(6.dp))
                AuthField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; touchedConfirmPassword = true },
                    placeholder = t("signup.confirmPasswordPlaceholder"),
                    isPassword = true,
                    passwordVisible = confirmPasswordVisible,
                    onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible },
                    isError = touchedConfirmPassword && confirmError != null,
                    text = text, text3 = text3, bg2 = bg2, border2 = border2
                )
                if (touchedConfirmPassword && confirmError != null) {
                    FieldErrorText(confirmError, errorRed)
                }
                Spacer(Modifier.height(20.dp))

                // Terms checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Checkbox(
                        checked = agreedToTerms,
                        onCheckedChange = { agreedToTerms = it; touchedTerms = true },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Accent,
                            uncheckedColor = if (touchedTerms && !agreedToTerms) errorRed else text3,
                            checkmarkColor = Color.White
                        )
                    )
                    Text(
                        "${t("signup.termsAgree")} ${t("signup.termsOfService")} ${t("signup.and")} ${t("signup.privacyPolicy")}",
                        fontSize = 13.sp, color = text2, lineHeight = 19.sp)
                }
                if (touchedTerms && !agreedToTerms) {
                    FieldErrorText(t("signup.termsRequired"), errorRed)
                }
                Spacer(Modifier.height(20.dp))

                // Submit
                Button(
                    onClick = { submit() },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp),
                            color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(t("signup.createAccount"), fontSize = 14.sp,
                            fontWeight = FontWeight.Medium, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${t("signup.hasAccount")} ", fontSize = 13.sp, color = text3)
                TextButton(onClick = onNavigateToLogin, contentPadding = PaddingValues(0.dp)) {
                    Text(t("signup.signIn"), fontSize = 13.sp, color = Accent, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(label: String, color: Color) {
    Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = color)
}

@Composable
private fun FieldErrorText(msg: String, color: Color) {
    Spacer(Modifier.height(4.dp))
    Text(msg, fontSize = 12.sp, color = color)
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    text: Color,
    text3: Color,
    bg2: Color,
    border2: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = text3, fontSize = 14.sp) },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        isError = isError,
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType
        ),
        trailingIcon = if (isPassword && onTogglePassword != null) ({
            TextButton(onClick = onTogglePassword,
                contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(if (passwordVisible) "Hide" else "Show", fontSize = 12.sp, color = text3)
            }
        }) else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Accent,
            unfocusedBorderColor = border2,
            errorBorderColor = Color(0xFFE24B4A),
            focusedContainerColor = bg2,
            unfocusedContainerColor = bg2,
            errorContainerColor = bg2,
            focusedTextColor = text,
            unfocusedTextColor = text,
            errorTextColor = text,
        )
    )
}
