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
import com.expenss.tracker.ui.theme.IcClose
import com.expenss.tracker.ui.theme.IcOther
import com.expenss.tracker.ui.theme.IcTrash
import com.expenss.tracker.ui.theme.IcWallet
import com.expenss.tracker.ui.theme.categoryIcon

private val BUDGET_CATEGORIES = listOf(
    "food", "housing", "transport", "shopping",
    "entertainment", "taxes", "investment", "savings", "other"
)

@Composable
fun ManageBudgetSheet(
    currencySymbol: String,
    monthBudget: Double,
    categoryBudgets: Map<String, Double>,
    onDismiss: () -> Unit,
    onSaveMonthly: (Double) -> Unit,
    onDeleteMonthly: () -> Unit,
    onSaveCategory: (String, Double) -> Unit,
    onRemoveCategory: (String) -> Unit
) {
    var monthlyInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("") }

    val totalCat = categoryBudgets.values.sum()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 680.dp)
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
                Text("Manage Budget", fontSize = 17.sp, fontWeight = FontWeight.Bold,
                    color = DText, letterSpacing = (-0.3).sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    Icon(IcClose, "Close", tint = DText2, modifier = Modifier.size(16.dp))
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                // ── Section 1: Monthly Budget ──
                SectionHeader(
                    iconTint = Color(0xFF60A5FA), iconBg = Color(0x263B82F6),
                    title = "Budget", sub = "Total spending limit for this period",
                    trailing = if (monthBudget > 0) money(currencySymbol, monthBudget) else null
                )

                // Allocation breakdown
                if (monthBudget > 0 && totalCat > 0) {
                    val unallocated = monthBudget - totalCat
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DSurface2)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AllocRow("Allocated to categories", money(currencySymbol, totalCat), DText2)
                        AllocRow(
                            "Unallocated", money(currencySymbol, unallocated),
                            if (unallocated >= 0) Color(0xFF4ADE80) else Color(0xFFF87171)
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth().height(6.dp)
                                .clip(RoundedCornerShape(999.dp)).background(Color(0x14FFFFFF))
                        ) {
                            val frac = (totalCat / monthBudget).coerceIn(0.0, 1.0).toFloat()
                            Box(
                                modifier = Modifier.fillMaxWidth(frac).height(6.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (totalCat > monthBudget) DRed else DAccent)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                FieldLabel("Amount")
                StyledField(
                    value = monthlyInput,
                    onValueChange = { monthlyInput = it.filter { c -> c.isDigit() || c == '.' } },
                    placeholder = if (monthBudget > 0) money(currencySymbol, monthBudget) else "0",
                    isError = false,
                    keyboardType = KeyboardType.Decimal,
                    prefix = currencySymbol
                )

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val amt = monthlyInput.toDoubleOrNull()
                            if (amt != null && amt >= 0) { onSaveMonthly(amt); monthlyInput = "" }
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DAccent)
                    ) {
                        Text(if (monthBudget > 0) "Update Budget" else "Set Budget",
                            color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (monthBudget > 0) {
                        Button(
                            onClick = onDeleteMonthly,
                            modifier = Modifier.height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AEF4444))
                        ) {
                            Icon(IcTrash, "Delete budget", tint = DRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Warning
                if (monthBudget > 0 && totalCat > monthBudget) {
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x1AEF4444))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(IcOther, null, tint = Color(0xFFF87171), modifier = Modifier.size(15.dp))
                        Text(
                            "Category budgets (${money(currencySymbol, totalCat)}) exceed your monthly budget (${money(currencySymbol, monthBudget)}).",
                            fontSize = 11.5.sp, color = Color(0xFFFCA5A5), lineHeight = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                HorizontalDivider(color = DBorder)
                Spacer(Modifier.height(18.dp))

                // ── Section 2: Category Budget ──
                SectionHeader(
                    iconTint = Color(0xFFA78BFA), iconBg = Color(0x24A78BFA),
                    title = "Category Budget", sub = "Limit spending per category",
                    trailing = null
                )

                // Existing category budgets
                if (totalCat > 0) {
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BUDGET_CATEGORIES.filter { (categoryBudgets[it] ?: 0.0) > 0 }.forEach { cat ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DSurface2)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat.replaceFirstChar { it.uppercase() },
                                    fontSize = 13.sp, color = DText, modifier = Modifier.weight(1f))
                                Text(money(currencySymbol, categoryBudgets[cat] ?: 0.0),
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DText)
                                Spacer(Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(7.dp))
                                        .background(Color(0x1AEF4444)).clickable { onRemoveCategory(cat) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(IcTrash, "Remove", tint = DRed, modifier = Modifier.size(13.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                FieldLabel("Category")
                BUDGET_CATEGORIES.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { cat ->
                            BudgetCatButton(
                                cat = cat,
                                selected = selectedCategory == cat,
                                current = categoryBudgets[cat] ?: 0.0,
                                currencySymbol = currencySymbol,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedCategory = cat; categoryInput = "" }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                if (selectedCategory.isNotBlank()) {
                    FieldLabel("Amount")
                    StyledField(
                        value = categoryInput,
                        onValueChange = { categoryInput = it.filter { c -> c.isDigit() || c == '.' } },
                        placeholder = (categoryBudgets[selectedCategory]?.takeIf { it > 0 }
                            ?.let { money(currencySymbol, it) }) ?: "0",
                        isError = false,
                        keyboardType = KeyboardType.Decimal,
                        prefix = currencySymbol
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val amt = categoryInput.toDoubleOrNull()
                            if (amt != null && amt >= 0) { onSaveCategory(selectedCategory, amt); categoryInput = "" }
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DAccent)
                    ) {
                        Text(
                            if ((categoryBudgets[selectedCategory] ?: 0.0) > 0) "Update Budget" else "Set Category Budget",
                            color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Button(
                        onClick = {}, enabled = false,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = DSurface2
                        )
                    ) {
                        Text("Set Category Budget", color = DText3, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(
    iconTint: Color, iconBg: Color, title: String, sub: String, trailing: String?
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(if (title == "Budget") IcWallet else IcOther, null,
                tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DText)
            Text(sub, fontSize = 11.5.sp, color = DText3)
        }
        if (trailing != null) {
            Text(trailing, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DText)
        }
    }
}

@Composable
private fun AllocRow(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = DText2)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun BudgetCatButton(
    cat: String, selected: Boolean, current: Double, currencySymbol: String,
    modifier: Modifier, onClick: () -> Unit
) {
    val (_, color) = categoryColors(cat)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) DAccentBg else DSurface2)
            .border(1.dp, if (selected) DAccent else DBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(categoryIcon(cat), null,
            tint = if (selected) DAccent else color, modifier = Modifier.size(16.dp))
        Text(cat.replaceFirstChar { it.uppercase() }, fontSize = 10.sp,
            color = if (selected) DAccent else DText2, fontWeight = FontWeight.Medium)
        if (current > 0) {
            Text(money(currencySymbol, current), fontSize = 9.sp, color = DAccent,
                fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun money(symbol: String, amount: Double): String = symbol + "%,.0f".format(amount)
