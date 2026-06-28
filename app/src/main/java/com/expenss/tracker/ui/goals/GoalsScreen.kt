package com.expenss.tracker.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenss.tracker.ui.dashboard.AppBottomNav
import com.expenss.tracker.ui.dashboard.DBg
import com.expenss.tracker.ui.dashboard.DBorder
import com.expenss.tracker.ui.dashboard.DBorder2
import com.expenss.tracker.ui.dashboard.DAccent
import com.expenss.tracker.ui.dashboard.DAccentBg
import com.expenss.tracker.ui.dashboard.DRed
import com.expenss.tracker.ui.dashboard.DSurface
import com.expenss.tracker.ui.dashboard.DSurface2
import com.expenss.tracker.ui.dashboard.DText
import com.expenss.tracker.ui.dashboard.DText2
import com.expenss.tracker.ui.dashboard.DText3
import com.expenss.tracker.ui.dashboard.FieldError
import com.expenss.tracker.ui.dashboard.FieldLabel
import com.expenss.tracker.ui.dashboard.SkeletonGoals
import com.expenss.tracker.ui.dashboard.currencySymbol
import com.expenss.tracker.ui.dashboard.formatAmount
import com.expenss.tracker.ui.dashboard.SimpleTopBar
import com.expenss.tracker.ui.dashboard.StyledField
import com.expenss.tracker.i18n.t
import com.expenss.tracker.ui.theme.IcCheck
import com.expenss.tracker.ui.theme.IcClock
import com.expenss.tracker.ui.theme.IcClose
import com.expenss.tracker.ui.theme.IcEdit
import com.expenss.tracker.ui.theme.IcTarget
import com.expenss.tracker.ui.theme.IcTrash
import com.expenss.tracker.ui.theme.IcWallet

@Composable
fun GoalsScreen(
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val vm: GoalsViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return GoalsViewModel(context) as T
        }
    })

    val dream        by vm.dream.collectAsState()
    val totalSavings by vm.totalSavings.collectAsState()
    val avgMonthly   by vm.avgMonthlySavings.collectAsState()
    val username     by vm.username.collectAsState()
    val currency     by vm.currency.collectAsState()
    val isLoading    by vm.isLoading.collectAsState()

    var showModal       by remember { mutableStateOf(false) }
    var isEditing       by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var nameInput   by remember { mutableStateOf("") }
    var targetInput by remember { mutableStateOf("") }
    var nameError   by remember { mutableStateOf(false) }
    var targetError by remember { mutableStateOf(false) }

    val progress = if (dream != null && dream!!.target_amount > 0)
        minOf((totalSavings / dream!!.target_amount * 100).toInt(), 100) else 0
    val funded = dream != null && totalSavings >= dream!!.target_amount

    fun completionEstimate(): String {
        if (dream == null || avgMonthly <= 0) return "—"
        val monthsLeft = (dream!!.target_amount - totalSavings) / avgMonthly
        if (monthsLeft <= 0) return "—"
        val years = (monthsLeft / 12).toInt()
        val months = (monthsLeft % 12).toInt()
        return when {
            years == 0 -> "$months month${if (months != 1) "s" else ""}"
            months == 0 -> "$years year${if (years != 1) "s" else ""}"
            else -> "$years year${if (years != 1) "s" else ""} and $months month${if (months != 1) "s" else ""}"
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            SimpleTopBar(
                title = t("dashboard.nav.goals"),
                username = username,
                currencyCode = currency,
                onSetCurrency = { vm.setCurrency(it) },
                onLogout = {
                    com.expenss.tracker.util.TokenManager(context).clearToken()
                    onLogout()
                },
                onChangePassword = { onNavigate("forgot-password") },
                onContact = { onNavigate("contact") }
            )

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading) {
                    SkeletonGoals()
                } else if (dream == null) {
                    // Empty state
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(modifier = Modifier.size(80.dp).clip(CircleShape)
                            .background(DSurface2), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { 0f }, modifier = Modifier.size(80.dp),
                                color = DAccent, trackColor = Color(0x1A3B82F6), strokeWidth = 4.dp
                            )
                            Icon(IcTarget, null, tint = DText3, modifier = Modifier.size(40.dp))
                        }
                        Text(t("goals.emptyTitle"), fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = DText, letterSpacing = (-0.4).sp)
                        Text(t("goals.emptyDesc"),
                            fontSize = 13.sp, color = DText3)
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = {
                                isEditing = false
                                nameInput = ""; targetInput = ""
                                nameError = false; targetError = false
                                showModal = true
                            },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DAccent)
                        ) {
                            Text("+ ${t("goals.addGoal")}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                color = Color.White)
                        }
                    }
                } else {
                    // Savings pool strip
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DSurface2)
                            .border(1.dp, DBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(IcWallet, null, tint = DText2, modifier = Modifier.size(14.dp))
                        Text(t("goals.savingsPool"), fontSize = 12.sp, color = DText2,
                            modifier = Modifier.weight(1f))
                        Text(formatAmount(currency, totalSavings), fontSize = 14.sp,
                            fontWeight = FontWeight.Bold, color = DText)
                    }

                    // Dream card
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(DSurface)
                            .border(1.dp, if (funded) Color(0x334ADE80) else DBorder2,
                                RoundedCornerShape(18.dp))
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            // Ring + info
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Box(modifier = Modifier.size(100.dp),
                                    contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = { progress / 100f },
                                        modifier = Modifier.size(100.dp),
                                        color = if (funded) Color(0xFF4ADE80) else DAccent,
                                        trackColor = Color(0x12FFFFFF),
                                        strokeWidth = 7.dp,
                                        strokeCap = StrokeCap.Round
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$progress%", fontSize = 19.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (funded) Color(0xFF4ADE80) else DText,
                                            letterSpacing = (-0.5).sp)
                                        Text(if (funded) t("goals.funded") else t("goals.ringSubLabel"),
                                            fontSize = 8.5.sp, color = DText3)
                                    }
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (funded) {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(Color(0x1A4ADE80))
                                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(IcCheck, null, tint = Color(0xFF4ADE80), modifier = Modifier.size(11.dp))
                                            Text(t("goals.funded"), fontSize = 11.sp,
                                                color = Color(0xFF4ADE80), fontWeight = FontWeight.SemiBold)
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(DAccentBg)
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(t("goals.dreamItemLabel"), fontSize = 11.sp,
                                                color = DAccent, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    Text(dream!!.name, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                        color = DText, letterSpacing = (-0.4).sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        SmallActionBtn(t("goals.editGoal"), DAccentBg, DAccent, icon = IcEdit) {
                                            isEditing = true
                                            nameInput = dream!!.name
                                            targetInput = dream!!.target_amount.toLong().toString()
                                            nameError = false; targetError = false
                                            showModal = true
                                        }
                                        SmallActionBtn(t("goals.deleteTitle"), Color(0x1AEF4444), DRed, icon = IcTrash) {
                                            showDeleteConfirm = true
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = DBorder)

                            // Stats row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                GoalStat(t("goals.saved"), formatAmount(currency, totalSavings),
                                    if (funded) Color(0xFF4ADE80) else DText)
                                Box(modifier = Modifier.width(1.dp).height(36.dp).background(DBorder))
                                GoalStat(t("goals.remaining"),
                                    if (funded) "—" else formatAmount(currency, dream!!.target_amount - totalSavings),
                                    DText)
                                Box(modifier = Modifier.width(1.dp).height(36.dp).background(DBorder))
                                GoalStat(t("goals.target"), formatAmount(currency, dream!!.target_amount), DText2)
                            }

                            // Estimation banner
                            if (!funded) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DSurface2)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(IcClock, null, tint = DText2, modifier = Modifier.size(14.dp))
                                    Column {
                                        Text(t("goals.estTimeLabel"),
                                            fontSize = 11.sp, color = DText3)
                                        Text(completionEstimate(), fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold, color = DText)
                                    }
                                }
                                Text(t("goals.estTimeHint"),
                                    fontSize = 11.sp, color = DText3)
                            }
                        }
                    }
                }
            }

            AppBottomNav(currentRoute = "goals", onNavigate = onNavigate)
        }
    }

    // Add/Edit modal
    if (showModal) {
        Dialog(
            onDismissRequest = { showModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DSurface)
                    .border(1.dp, DBorder2, RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isEditing) t("goals.editGoal") else t("goals.addGoal"),
                        fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DText)
                    IconButton(onClick = { showModal = false }, modifier = Modifier.size(30.dp)) {
                        Icon(IcClose, "Close", tint = DText2, modifier = Modifier.size(14.dp))
                    }
                }
                FieldLabel(t("goals.goalName"))
                StyledField(
                    value = nameInput, onValueChange = { nameInput = it; nameError = false },
                    placeholder = t("goals.namePlaceholder"), isError = nameError
                )
                if (nameError) FieldError(t("goals.nameRequired"))

                FieldLabel(t("goals.targetAmount"))
                StyledField(
                    value = targetInput,
                    onValueChange = { targetInput = it.filter { c -> c.isDigit() || c == '.' }; targetError = false },
                    placeholder = "0", isError = targetError,
                    keyboardType = KeyboardType.Decimal,
                    prefix = currencySymbol(currency)
                )
                if (targetError) FieldError(t("goals.amountRequired"))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { showModal = false },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DSurface2)
                    ) { Text(t("dashboard.form.cancel"), color = DText2, fontSize = 14.sp) }
                    Button(
                        onClick = {
                            nameError = nameInput.isBlank()
                            val amt = targetInput.toDoubleOrNull()
                            targetError = amt == null || amt < 1
                            if (!nameError && !targetError && amt != null) {
                                if (isEditing && dream != null) {
                                    vm.editDream(dream!!.id, nameInput.trim(), amt) { showModal = false }
                                } else {
                                    vm.createDream(nameInput.trim(), amt) { showModal = false }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DAccent)
                    ) { Text(if (isEditing) t("goals.updateGoal") else t("goals.saveGoal"), color = Color.White, fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }

    // Delete confirm
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = DSurface, shape = RoundedCornerShape(18.dp),
            title = { Text(t("goals.deleteTitle"), color = DText, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("${t("goals.deleteDesc")} \"${dream?.name}\"?", color = DText2,
                fontSize = 13.5.sp, lineHeight = 20.sp) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteDream(); showDeleteConfirm = false
                }) { Text(t("dashboard.delete"), color = DRed, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(t("dashboard.cancel"), color = DText2) }
            }
        )
    }
}

@Composable
private fun GoalStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, fontSize = 11.sp, color = DText3, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color,
            letterSpacing = (-0.4).sp)
    }
}

@Composable
private fun SmallActionBtn(
    label: String, bg: Color, textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(bg)
            .clickable { onClick() }.padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (icon != null) Icon(icon, null, tint = textColor, modifier = Modifier.size(11.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}
