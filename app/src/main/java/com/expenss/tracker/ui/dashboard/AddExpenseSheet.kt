package com.expenss.tracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.expenss.tracker.data.network.Expense
import com.expenss.tracker.ui.theme.IcClose
import com.expenss.tracker.ui.theme.categoryIcon
import java.text.SimpleDateFormat
import java.util.*

private val CATEGORIES = listOf(
    "food", "housing", "transport", "shopping",
    "entertainment", "taxes", "investment", "savings", "other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseSheet(
    title: String,
    currencySymbol: String,
    initial: Expense? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double, category: String, date: String, note: String?) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var amount by remember { mutableStateOf(initial?.amount?.let { amt ->
        if (amt == amt.toLong().toDouble()) amt.toLong().toString() else amt.toString()
    } ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "") }
    var date by remember { mutableStateOf(initial?.date?.take(10) ?: todayIso()) }
    var note by remember { mutableStateOf(initial?.note ?: "") }

    var submitting by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val amountValue = amount.toDoubleOrNull()
    val nameError = name.isBlank()
    val amountError = amountValue == null || amountValue < 1
    val categoryError = category.isBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 640.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(DSurface)
                .border(1.dp, DBorder2, RoundedCornerShape(20.dp))
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 14.dp, top = 18.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                    color = DText, letterSpacing = (-0.3).sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    Icon(IcClose, "Close", tint = DText2, modifier = Modifier.size(16.dp))
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // Name
                FieldLabel("Name")
                StyledField(
                    value = name, onValueChange = { name = it },
                    placeholder = "e.g. Lunch", isError = showErrors && nameError
                )
                if (showErrors && nameError) FieldError("Name is required")

                Spacer(Modifier.height(14.dp))

                // Amount
                FieldLabel("Amount")
                StyledField(
                    value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    placeholder = "0", isError = showErrors && amountError,
                    keyboardType = KeyboardType.Decimal,
                    prefix = currencySymbol
                )
                if (showErrors && amountError) FieldError("Enter a valid amount")

                Spacer(Modifier.height(14.dp))

                // Category grid (3 x 3)
                FieldLabel("Category")
                CATEGORIES.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { cat ->
                            CatButton(
                                cat = cat,
                                selected = category == cat,
                                modifier = Modifier.weight(1f),
                                onClick = { category = cat }
                            )
                        }
                    }
                }
                if (showErrors && categoryError) FieldError("Pick a category")

                Spacer(Modifier.height(14.dp))

                // Date
                FieldLabel("Date")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DSurface2)
                        .border(1.dp, DBorder2, RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(prettyDate(date), fontSize = 14.sp, color = DText)
                }

                Spacer(Modifier.height(14.dp))

                // Note (optional)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FieldLabel("Note")
                    Spacer(Modifier.width(6.dp))
                    Text("optional", fontSize = 11.sp, color = DText3)
                }
                StyledField(
                    value = note, onValueChange = { note = it },
                    placeholder = "Add a note", isError = false
                )

                Spacer(Modifier.height(20.dp))
            }

            // Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DSurface2)
                ) { Text("Cancel", color = DText2, fontSize = 14.sp, fontWeight = FontWeight.Medium) }

                Button(
                    onClick = {
                        showErrors = true
                        if (!nameError && !amountError && !categoryError && amountValue != null) {
                            submitting = true
                            onSave(name.trim(), amountValue, category, date, note.trim().ifBlank { null })
                        }
                    },
                    enabled = !submitting,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DAccent)
                ) {
                    if (submitting) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp))
                    } else {
                        Text("Save", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = isoToMillis(date))
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { date = millisToIso(it) }
                    showDatePicker = false
                }) { Text("OK", color = DAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = DText2) }
            }
        ) {
            DatePicker(state = dpState)
        }
    }
}

@Composable
private fun CatButton(cat: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val (_, color) = categoryColors(cat)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) DAccentBg else DSurface2)
            .border(1.dp, if (selected) DAccent else DBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(categoryIcon(cat), null,
            tint = if (selected) DAccent else color, modifier = Modifier.size(16.dp))
        Text(cat.replaceFirstChar { it.uppercase() }, fontSize = 10.5.sp,
            color = if (selected) DAccent else DText2, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FieldLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = DText2,
        modifier = Modifier.padding(bottom = 7.dp))
}

@Composable
fun FieldError(text: String) {
    Text(text, fontSize = 11.sp, color = DRed, modifier = Modifier.padding(top = 5.dp))
}

@Composable
fun StyledField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    prefix: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = DText3, fontSize = 14.sp) },
        singleLine = true,
        isError = isError,
        prefix = if (prefix != null) ({ Text(prefix, color = DText2, fontSize = 14.sp) }) else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = DText,
            unfocusedTextColor = DText,
            focusedContainerColor = DSurface2,
            unfocusedContainerColor = DSurface2,
            errorContainerColor = DSurface2,
            cursorColor = DAccent,
            focusedBorderColor = DAccent,
            unfocusedBorderColor = DBorder2,
            errorBorderColor = DRed
        )
    )
}

private fun todayIso(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

private fun prettyDate(iso: String): String = try {
    val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outFmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    outFmt.format(inFmt.parse(iso)!!)
} catch (e: Exception) { iso }

private fun isoToMillis(iso: String): Long = try {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
    fmt.parse(iso)?.time ?: System.currentTimeMillis()
} catch (e: Exception) { System.currentTimeMillis() }

private fun millisToIso(millis: Long): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
    return fmt.format(Date(millis))
}
