package com.expenss.tracker.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
fun OnboardingScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(context) as T
        }
    })
    val onboardingState by vm.onboardingState.collectAsState()

    var step by remember { mutableIntStateOf(1) }
    var selectedCurrency by remember { mutableStateOf<String?>(null) }
    var selectedMode    by remember { mutableStateOf<String?>(null) }
    var cycleStartDay   by remember { mutableStateOf("") }
    var errorMsg        by remember { mutableStateOf("") }

    val isLoading = onboardingState is OnboardingState.Loading

    LaunchedEffect(onboardingState) {
        when (onboardingState) {
            is OnboardingState.Success -> onDone()
            is OnboardingState.Error -> errorMsg = (onboardingState as OnboardingState.Error).message
            else -> {}
        }
    }

    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) BgDark else BgLight
    val bg2 = if (isDark) Bg2Dark else Bg2Light
    val text = if (isDark) TextDark else TextLight
    val text2 = if (isDark) Text2Dark else Text2Light
    val text3 = if (isDark) Text3Dark else Text3Light
    val borderNormal = if (isDark) Color(0x20FFFFFF) else Color(0x20000000)
    val errorRed = Color(0xFFE24B4A)

    Box(modifier = Modifier.fillMaxSize().background(bg), contentAlignment = Alignment.Center) {
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
                    .border(0.5.dp, if (isDark) Color(0x14FFFFFF) else Color(0x14000000), RoundedCornerShape(14.dp))
                    .padding(horizontal = 28.dp, vertical = 36.dp)
            ) {

                // ── Step 1: Currency ─────────────────────────────────────
                if (step == 1) {
                    StepIcon(icon = IcClock)
                    Spacer(Modifier.height(16.dp))
                    Text(t("onboarding.step0Title"), fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold, color = text,
                        letterSpacing = (-0.5).sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Text(t("onboarding.step0Subtitle"),
                        fontSize = 14.sp, color = text2, fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(
                            Triple("JPY", "¥", "Japanese Yen"),
                            Triple("USD", "$", "US Dollar"),
                            Triple("IDR", "Rp", "Indonesian Rupiah")
                        ).forEach { (code, symbol, label) ->
                            val selected = selectedCurrency == code
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) Color(0x143B82F6) else bg)
                                    .border(
                                        1.5.dp,
                                        if (selected) Accent else borderNormal,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedCurrency = code; errorMsg = "" }
                                    .padding(vertical = 20.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.End)
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Accent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✓", fontSize = 9.sp, color = Color.White,
                                            fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Spacer(Modifier.size(18.dp))
                                }
                                Text(symbol, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                                    color = text)
                                Text(code, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    color = text)
                                Text(label, fontSize = 11.sp, color = text2,
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // ── Step 2: Tracking mode ────────────────────────────────
                if (step == 2) {
                    StepIcon(icon = IcBarChart)
                    Spacer(Modifier.height(16.dp))
                    Text(t("onboarding.step1Title"), fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold, color = text,
                        letterSpacing = (-0.5).sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Text(t("onboarding.step1Subtitle"),
                        fontSize = 14.sp, color = text2, fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(
                            Triple("monthly", IcCalendar, Triple(
                                t("onboarding.monthly"), t("onboarding.monthlyDesc"), t("onboarding.monthlyExample")
                            )),
                            Triple("paycycle", IcRefreshCcw, Triple(
                                t("onboarding.payday"), t("onboarding.paydayDesc"), t("onboarding.paydayExample")
                            ))
                        ).forEach { (mode, icon, labels) ->
                            val (name, desc, example) = labels
                            val selected = selectedMode == mode
                            val iconBg = if (mode == "monthly") Color(0x1F22C55E) else Color(0x143B82F6)
                            val iconTint = if (mode == "monthly") Color(0xFF4ADE80) else Accent
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) Color(0x143B82F6) else bg)
                                    .border(
                                        1.5.dp,
                                        if (selected) Accent else borderNormal,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedMode = mode; errorMsg = "" }
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier.size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(iconBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, null, tint = iconTint,
                                            modifier = Modifier.size(18.dp))
                                    }
                                    if (selected) {
                                        Box(
                                            modifier = Modifier.size(18.dp)
                                                .clip(CircleShape)
                                                .background(Accent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("✓", fontSize = 9.sp, color = Color.White,
                                                fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                                    color = text)
                                Text(desc, fontSize = 12.sp, color = text2, lineHeight = 17.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isDark) Color(0x0AFFFFFF) else Color(0x0A000000))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(example, fontSize = 11.sp, color = text3)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // ── Step 3: Cycle start day ──────────────────────────────
                if (step == 3) {
                    TextButton(
                        onClick = { step = 2; errorMsg = "" },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 24.dp)
                    ) {
                        Text("← ${t("onboarding.back")}", fontSize = 13.sp, color = text3)
                    }

                    StepIcon(icon = IcCalendar)
                    Spacer(Modifier.height(16.dp))
                    Text(t("onboarding.step2Title"), fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold, color = text,
                        letterSpacing = (-0.5).sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Text(t("onboarding.step2Subtitle"),
                        fontSize = 14.sp, color = text2, fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(28.dp))

                    Text(t("onboarding.dayLabel"), fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = text2)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = cycleStartDay,
                            onValueChange = { v ->
                                if (v.length <= 2 && v.all { it.isDigit() }) cycleStartDay = v
                            },
                            modifier = Modifier.width(90.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 22.sp, fontWeight = FontWeight.Bold,
                                color = text, textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            placeholder = { Text("—", color = text3, fontSize = 16.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Accent,
                                unfocusedBorderColor = borderNormal,
                                focusedContainerColor = bg2,
                                unfocusedContainerColor = bg2,
                                focusedTextColor = text,
                                unfocusedTextColor = text,
                            )
                        )
                        Text(t("onboarding.ofEveryMonth"), fontSize = 14.sp, color = text2)
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // ── Error banner ─────────────────────────────────────────
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

                // ── Continue / Get started button ────────────────────────
                val btnEnabled = when (step) {
                    1 -> selectedCurrency != null
                    2 -> selectedMode != null
                    3 -> cycleStartDay.isNotEmpty() &&
                            cycleStartDay.toIntOrNull()?.let { it in 1..31 } == true
                    else -> false
                } && !isLoading

                Button(
                    onClick = {
                        errorMsg = ""
                        when (step) {
                            1 -> step = 2
                            2 -> if (selectedMode == "paycycle") step = 3
                                 else vm.setTrackingMode(selectedCurrency!!, selectedMode!!, null)
                            3 -> vm.setTrackingMode(
                                selectedCurrency!!,
                                selectedMode!!,
                                cycleStartDay.toIntOrNull()
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    enabled = btnEnabled,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp),
                            color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(if (step == 3) t("onboarding.getStarted") else t("onboarding.continue"),
                            fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.StepIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x143B82F6))
            .align(Alignment.CenterHorizontally),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(24.dp))
    }
}
