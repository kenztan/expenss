package com.expenss.tracker.ui.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenss.tracker.data.network.Saving
import com.expenss.tracker.ui.dashboard.*
import com.expenss.tracker.ui.dashboard.SkeletonSavingsRows
import com.expenss.tracker.ui.dashboard.SkeletonSavingsStats
import com.expenss.tracker.ui.theme.IcClose
import com.expenss.tracker.ui.theme.IcEdit
import com.expenss.tracker.ui.theme.IcTrash
import com.expenss.tracker.ui.theme.IcWallet
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SavingsScreen(
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val vm: SavingsViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SavingsViewModel(context) as T
        }
    })

    val records          by vm.records.collectAsState()
    val totalSavings     by vm.totalSavings.collectAsState()
    val monthlyRemaining by vm.monthlyRemaining.collectAsState()
    val monthlyCommitment by vm.monthlyCommitment.collectAsState()
    val avgMonthly       by vm.avgMonthlySavings.collectAsState()
    val username         by vm.username.collectAsState()
    val currency         by vm.currency.collectAsState()
    val isLoading        by vm.isLoading.collectAsState()

    var amountInput by remember { mutableStateOf("") }
    var dateInput   by remember { mutableStateOf(todaySavingsIso()) }
    var noteInput   by remember { mutableStateOf("") }

    var showCommitmentModal by remember { mutableStateOf(false) }
    var commitmentInput     by remember { mutableStateOf("") }
    var recordToDelete      by remember { mutableStateOf<Saving?>(null) }
    var showDatePicker      by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(DBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            SimpleTopBar(
                title = "Savings",
                username = username,
                currencyCode = currency,
                onSetCurrency = { vm.setCurrency(it) },
                onLogout = {
                    com.expenss.tracker.util.TokenManager(context).clearToken()
                    onLogout()
                }
            )

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                // Stats
                if (isLoading) {
                    item { SkeletonSavingsStats() }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SavingsStatCard(
                                modifier = Modifier.weight(1f),
                                label = "Total Savings",
                                value = formatAmount(currency, totalSavings + monthlyRemaining),
                                sub = "Includes remaining",
                                valueColor = Color(0xFF4ADE80)
                            )
                            SavingsStatCard(
                                modifier = Modifier.weight(1f),
                                label = "This Month Remaining",
                                value = formatAmount(currency, monthlyRemaining),
                                sub = "Auto from budget",
                                valueColor = Color(0xFF60A5FA)
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(DSurface)
                                .border(1.dp, DBorder, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Monthly Commitment", fontSize = 11.sp, color = DText3)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(formatAmount(currency, monthlyCommitment), fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold, color = DText,
                                            letterSpacing = (-0.5).sp, modifier = Modifier.weight(1f))
                                        IconButton(
                                            onClick = { commitmentInput = monthlyCommitment.toLong().toString(); showCommitmentModal = true },
                                            modifier = Modifier.size(28.dp)
                                        ) { Icon(IcEdit, null, tint = DAccent, modifier = Modifier.size(14.dp)) }
                                    }
                                    Text("Savings target per month", fontSize = 11.sp, color = DText3)
                                }
                            }
                            SavingsStatCard(
                                modifier = Modifier.weight(1f),
                                label = "Avg Monthly Savings",
                                value = formatAmount(currency, avgMonthly),
                                sub = "Historical average",
                                valueColor = DText
                            )
                        }
                    }
                }

                // Log savings form
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DSurface)
                            .border(1.dp, DBorder, RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Log Savings", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                color = DText, letterSpacing = (-0.2).sp)

                            FieldLabel("Amount")
                            StyledField(
                                value = amountInput,
                                onValueChange = { amountInput = it.filter { c -> c.isDigit() || c == '.' } },
                                placeholder = "0", isError = false,
                                keyboardType = KeyboardType.Decimal,
                                prefix = currencySymbol(currency)
                            )

                            FieldLabel("Date")
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DSurface2)
                                    .border(1.dp, DBorder2, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(prettySavingsDate(dateInput), fontSize = 14.sp, color = DText,
                                    modifier = Modifier.weight(1f))
                                TextButton(onClick = { showDatePicker = true }) {
                                    Text("Change", fontSize = 12.sp, color = DAccent)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FieldLabel("Note")
                                Spacer(Modifier.width(6.dp))
                                Text("optional", fontSize = 11.sp, color = DText3)
                            }
                            StyledField(
                                value = noteInput, onValueChange = { noteInput = it },
                                placeholder = "Add a note", isError = false
                            )

                            Button(
                                onClick = {
                                    val amt = amountInput.toDoubleOrNull()
                                    if (amt != null && amt >= 1) {
                                        vm.createSaving(amt, dateInput, noteInput.ifBlank { null }) {
                                            amountInput = ""; noteInput = ""
                                            dateInput = todaySavingsIso()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DAccent)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White,
                                        strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                } else {
                                    Text("+ Add Record", fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // History header
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("HISTORY", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            color = DText3, letterSpacing = 0.1.sp)
                        if (records.isNotEmpty()) {
                            Box(modifier = Modifier.clip(RoundedCornerShape(999.dp))
                                .background(DAccentBg).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text("${records.size}", fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold, color = DAccent)
                            }
                        }
                    }
                }

                if (isLoading) {
                    item { SkeletonSavingsRows(4) }
                } else if (records.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DSurface)
                                .border(1.dp, DBorder, RoundedCornerShape(16.dp))
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape)
                                .background(Color(0x0AFFFFFF)), contentAlignment = Alignment.Center) {
                                Icon(IcWallet, null, tint = DText3, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("No savings records yet", fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold, color = DText)
                            Text("Log your first savings to get started",
                                fontSize = 13.sp, color = DText3)
                        }
                    }
                } else {
                    items(records) { record ->
                        SavingsRecordRow(
                            record = record,
                            amount = formatAmount(currency, record.amount),
                            onDelete = { recordToDelete = record }
                        )
                    }
                }
            }

            AppBottomNav(currentRoute = "savings", onNavigate = onNavigate)
        }
    }

    // Date picker
    if (showDatePicker) {
        val dpState = rememberDatePickerState(
            initialSelectedDateMillis = savingsIsoToMillis(dateInput)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { dateInput = savingsMillisToIso(it) }
                    showDatePicker = false
                }) { Text("OK", color = DAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = DText2) }
            }
        ) { DatePicker(state = dpState) }
    }

    // Commitment modal
    if (showCommitmentModal) {
        Dialog(onDismissRequest = { showCommitmentModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Column(
                modifier = Modifier.fillMaxWidth(0.88f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DSurface)
                    .border(1.dp, DBorder2, RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Set Monthly Commitment", fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, color = DText)
                    IconButton(onClick = { showCommitmentModal = false }, modifier = Modifier.size(30.dp)) {
                        Icon(IcClose, null, tint = DText2, modifier = Modifier.size(14.dp))
                    }
                }
                Text("Set how much you commit to save each month.",
                    fontSize = 13.sp, color = DText3, lineHeight = 19.sp)
                FieldLabel("Monthly amount")
                StyledField(
                    value = commitmentInput,
                    onValueChange = { commitmentInput = it.filter { c -> c.isDigit() || c == '.' } },
                    placeholder = "0", isError = false,
                    keyboardType = KeyboardType.Decimal,
                    prefix = currencySymbol(currency)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { showCommitmentModal = false },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DSurface2)
                    ) { Text("Cancel", color = DText2, fontSize = 14.sp) }
                    Button(
                        onClick = {
                            val amt = commitmentInput.toDoubleOrNull()
                            if (amt != null) {
                                vm.setCommitment(amt) { showCommitmentModal = false }
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DAccent)
                    ) { Text("Save", color = Color.White, fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }

    // Delete confirm
    recordToDelete?.let { rec ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            containerColor = DSurface, shape = RoundedCornerShape(18.dp),
            title = { Text("Delete record?", color = DText, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Delete savings record of ${formatAmount(currency, rec.amount)}?",
                color = DText2, fontSize = 13.5.sp) },
            confirmButton = {
                TextButton(onClick = { vm.deleteSaving(rec.id); recordToDelete = null }) {
                    Text("Delete", color = DRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) { Text("Cancel", color = DText2) }
            }
        )
    }
}

@Composable
private fun SavingsStatCard(modifier: Modifier, label: String, value: String,
                             sub: String, valueColor: Color) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(14.dp))
            .background(DSurface).border(1.dp, DBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, fontSize = 11.sp, color = DText3)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor,
            letterSpacing = (-0.5).sp)
        Text(sub, fontSize = 11.sp, color = DText3)
    }
}

@Composable
private fun SavingsRecordRow(record: Saving, amount: String, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DSurface)
            .border(1.dp, DBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
            .background(Color(0x24A78BFA)), contentAlignment = Alignment.Center) {
            Icon(IcWallet, null, tint = Color(0xFFA78BFA), modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(record.note ?: "Savings Deposit", fontSize = 14.sp,
                fontWeight = FontWeight.Medium, color = DText)
            Text(formatSavingsDate(record.date), fontSize = 11.sp, color = DText3)
        }
        Text("+$amount", fontSize = 14.sp, fontWeight = FontWeight.Bold,
            color = Color(0xFF4ADE80), letterSpacing = (-0.4).sp)
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(IcTrash, null, tint = DRed, modifier = Modifier.size(16.dp))
        }
    }
}

private fun todaySavingsIso(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

private fun prettySavingsDate(iso: String): String = try {
    val in2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val out = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    out.format(in2.parse(iso)!!)
} catch (_: Exception) { iso }

private fun formatSavingsDate(dateStr: String): String = try {
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        .format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)!!)
} catch (_: Exception) { dateStr }

private fun savingsIsoToMillis(iso: String): Long = try {
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        .parse(iso)?.time ?: System.currentTimeMillis()
} catch (_: Exception) { System.currentTimeMillis() }

private fun savingsMillisToIso(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        .format(Date(millis))
