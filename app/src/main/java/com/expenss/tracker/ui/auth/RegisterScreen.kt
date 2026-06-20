package com.expenss.tracker.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
        username.isEmpty() -> "Username is required"
        username.length < 3 -> "At least 3 characters"
        username.contains(' ') -> "No spaces allowed"
        else -> null
    }
    val emailError = when {
        email.isEmpty() -> "Email is required"
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email"
        else -> null
    }
    val passwordError = when {
        password.isEmpty() -> "Password is required"
        password.length < 8 -> "At least 8 characters"
        !password.any { it.isUpperCase() } || !password.any { it.isDigit() } ->
            "Must include uppercase letter and number"
        else -> null
    }
    val confirmError = when {
        confirmPassword.isEmpty() -> "Please confirm your password"
        confirmPassword != password -> "Passwords do not match"
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

    val bg     = Color(0xFF0B0D14)
    val bg2    = Color(0xFF161C2E)
    val text   = Color(0xFFDDE3F5)
    val text2  = Color(0xFF7880A0)
    val text3  = Color(0xFF404A68)
    val border = Color(0x14FFFFFF)
    val border2 = Color(0x20FFFFFF)
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
                Text("Check your email", fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                    color = text, letterSpacing = (-0.5).sp)
                Text("We sent a verification link to $email.\nClick it to activate your account.",
                    fontSize = 14.sp, color = text2, lineHeight = 22.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text("Check your spam folder if you don't see it.",
                    fontSize = 12.sp, color = text3,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onNavigateToLogin) {
                    Text("Back to Sign in", fontSize = 14.sp, color = Accent,
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
                Text("Create account", fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                    color = text, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(6.dp))
                Text("Sign up for Expenss", fontSize = 14.sp,
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
                FieldLabel("Username", text2)
                Spacer(Modifier.height(6.dp))
                AuthField(
                    value = username,
                    onValueChange = { username = it; touchedUsername = true },
                    placeholder = "username",
                    isError = touchedUsername && usernameError != null,
                    text = text, text3 = text3, bg2 = bg2, border2 = border2
                )
                if (touchedUsername && usernameError != null) {
                    FieldErrorText(usernameError, errorRed)
                }
                Spacer(Modifier.height(16.dp))

                // Email
                FieldLabel("Email", text2)
                Spacer(Modifier.height(6.dp))
                AuthField(
                    value = email,
                    onValueChange = { email = it; touchedEmail = true },
                    placeholder = "you@example.com",
                    keyboardType = KeyboardType.Email,
                    isError = touchedEmail && emailError != null,
                    text = text, text3 = text3, bg2 = bg2, border2 = border2
                )
                if (touchedEmail && emailError != null) {
                    FieldErrorText(emailError, errorRed)
                }
                Spacer(Modifier.height(16.dp))

                // Password
                FieldLabel("Password", text2)
                Spacer(Modifier.height(6.dp))
                AuthField(
                    value = password,
                    onValueChange = { password = it; touchedPassword = true },
                    placeholder = "••••••••",
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
                        "strong" -> Color(0xFF4ADE80) to "Strong"
                        "medium" -> Color(0xFFFBBF24) to "Medium"
                        else     -> errorRed to "Weak"
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
                FieldLabel("Confirm password", text2)
                Spacer(Modifier.height(6.dp))
                AuthField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; touchedConfirmPassword = true },
                    placeholder = "••••••••",
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
                    Text("I agree to the Terms of Service and Privacy Policy",
                        fontSize = 13.sp, color = text2, lineHeight = 19.sp)
                }
                if (touchedTerms && !agreedToTerms) {
                    FieldErrorText("You must agree to continue", errorRed)
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
                        Text("Create account", fontSize = 14.sp,
                            fontWeight = FontWeight.Medium, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", fontSize = 13.sp, color = text3)
                TextButton(onClick = onNavigateToLogin, contentPadding = PaddingValues(0.dp)) {
                    Text("Sign in", fontSize = 13.sp, color = Accent, fontWeight = FontWeight.Medium)
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
