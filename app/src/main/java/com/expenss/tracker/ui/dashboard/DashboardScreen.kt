package com.expenss.tracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenss.tracker.data.network.Expense
import com.expenss.tracker.ui.theme.IcBarChart
import com.expenss.tracker.ui.theme.IcClose
import com.expenss.tracker.ui.theme.IcEdit
import com.expenss.tracker.ui.theme.IcInvestment
import com.expenss.tracker.ui.theme.IcOther
import com.expenss.tracker.ui.theme.IcReceipt
import com.expenss.tracker.ui.theme.categoryIcon
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val vm: DashboardViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(context) as T
        }
    })

    val expenses         by vm.expenses.collectAsState()
    val budget           by vm.budget.collectAsState()
    val categoryBudgets  by vm.categoryBudgets.collectAsState()
    val username         by vm.username.collectAsState()
    val currency         by vm.currency.collectAsState()
    val isLoading        by vm.isLoading.collectAsState()
    val periodLabel      by vm.periodLabel.collectAsState()

    val totalSpent   = expenses.sumOf { it.amount }
    val remaining    = budget - totalSpent
    val spentPercent = if (budget > 0) minOf((totalSpent / budget * 100).toInt(), 100) else 0

    var showAddExpense   by remember { mutableStateOf(false) }
    var showBudget       by remember { mutableStateOf(false) }
    var expenseToEdit    by remember { mutableStateOf<Expense?>(null) }
    var expenseToDelete  by remember { mutableStateOf<Expense?>(null) }
    var expandedCategory by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val filteredExpenses = if (selectedCategory != null)
        expenses.filter { it.category == selectedCategory }
    else expenses

    Box(modifier = Modifier.fillMaxSize().background(DBg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar with month nav pill ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xB30B0D14))
                    .border(0.5.dp, DBorder, RoundedCornerShape(0.dp))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Dashboard", fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                        color = DText, letterSpacing = (-0.3).sp)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0x0AFFFFFF))
                            .border(1.dp, DBorder, RoundedCornerShape(999.dp))
                            .padding(horizontal = 3.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { vm.prevMonth() }, modifier = Modifier.size(26.dp)) {
                            Text("‹", fontSize = 16.sp, color = DText2, fontWeight = FontWeight.Bold)
                        }
                        Text(periodLabel, fontSize = 11.5.sp, fontWeight = FontWeight.Medium,
                            color = DText, modifier = Modifier.padding(horizontal = 6.dp),
                            letterSpacing = (-0.2).sp)
                        IconButton(onClick = { vm.nextMonth() }, modifier = Modifier.size(26.dp)) {
                            Text("›", fontSize = 16.sp, color = DText2, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                run {
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Box(
                                modifier = Modifier.size(26.dp).clip(CircleShape).background(DAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        UserMenu(
                            expanded = showMenu,
                            username = username,
                            currencyCode = currency,
                            onDismiss = { showMenu = false },
                            onSetCurrency = { vm.setCurrency(it) },
                            onLogout = {
                                showMenu = false
                                com.expenss.tracker.util.TokenManager(context).clearToken()
                                onLogout()
                            }
                        )
                    }
                }
            }

            // ── Scrollable content ──
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                // Overview card
                item {
                    if (isLoading) { SkeletonDashboardOverview(); return@item }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DSurface)
                            .border(1.dp, DBorder, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                                    CircularProgressIndicator(
                                        progress = { spentPercent / 100f },
                                        modifier = Modifier.size(110.dp),
                                        color = when {
                                            spentPercent >= 90 -> Color(0xFFF87171)
                                            spentPercent >= 75 -> Color(0xFFFBBF24)
                                            else -> Color.White.copy(alpha = 0.85f)
                                        },
                                        trackColor = Color(0x12FFFFFF),
                                        strokeWidth = 7.dp,
                                        strokeCap = StrokeCap.Round
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$spentPercent%", fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold, color = DText,
                                            letterSpacing = (-0.8).sp)
                                        Text("USED", fontSize = 8.sp, fontWeight = FontWeight.SemiBold,
                                            color = DText3, letterSpacing = 0.1.sp)
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    OvStat("BUDGET", formatAmount(currency, budget), isMuted = true)
                                    OvStat("SPENT", formatAmount(currency, totalSpent))
                                    OvStat("REMAINING", formatAmount(currency, remaining),
                                        valueColor = if (remaining >= 0) Color(0xFF4ADE80) else Color(0xFFF87171))
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .border(0.5.dp, DBorder, RoundedCornerShape(0.dp))
                                    .padding(vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OvMeta("${vm.getDaysLeft()} days left")
                                OvMeta("Daily avg ${formatAmount(currency, vm.getDailyAvg(totalSpent))}")
                            }
                        }
                    }
                }

                // Category deck
                item {
                    if (isLoading) { SkeletonCategoryDeck(); return@item }
                    CategoryDeck(
                        expenses = expenses,
                        categoryBudgets = categoryBudgets,
                        totalSpent = totalSpent,
                        expanded = expandedCategory,
                        format = { formatAmount(currency, it) },
                        onToggle = { cat ->
                            expandedCategory = if (expandedCategory == cat) null else cat
                            selectedCategory = expandedCategory
                        }
                    )
                }

                // Quick stats
                item {
                    if (isLoading) { SkeletonQuickStats(); return@item }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QsCard(
                            modifier = Modifier.weight(1f),
                            icon = IcReceipt,
                            iconBg = Color(0x263B82F6), iconColor = Color(0xFF60A5FA),
                            value = "${expenses.size}", label = "Transactions"
                        )
                        QsCard(
                            modifier = Modifier.weight(1f),
                            icon = IcInvestment,
                            iconBg = Color(0x1F22C55E), iconColor = Color(0xFF4ADE80),
                            value = formatAmount(currency, vm.getDailyAvg(totalSpent)),
                            label = "Per day"
                        )
                        QsCard(
                            modifier = Modifier.weight(1f),
                            icon = IcBarChart,
                            iconBg = Color(0x1F3B82F6), iconColor = DAccent,
                            value = expenses.groupBy { it.category }
                                .maxByOrNull { it.value.sumOf { e -> e.amount } }
                                ?.key?.replaceFirstChar { it.uppercase() } ?: "—",
                            label = "Top category",
                            smallValue = true
                        )
                    }
                }

                // Action buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showAddExpense = true },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DAccent)
                        ) {
                            Text("+ Add Expense", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                                color = Color.White)
                        }
                        Button(
                            onClick = { showBudget = true },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DSurface2)
                        ) {
                            Text("Manage Budget", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                                color = DText)
                        }
                    }
                }

                // Expenses header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("EXPENSES", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            color = DText3, letterSpacing = 0.1.sp)
                        if (filteredExpenses.isNotEmpty()) {
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(999.dp))
                                    .background(DAccentBg).padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("${filteredExpenses.size}", fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold, color = DAccent)
                            }
                        }
                        if (selectedCategory != null) {
                            Spacer(Modifier.weight(1f))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(DAccentBg)
                                    .padding(start = 10.dp, end = 6.dp, top = 3.dp, bottom = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(selectedCategory!!.replaceFirstChar { it.uppercase() },
                                    fontSize = 11.sp, color = DAccent, fontWeight = FontWeight.Medium)
                                IconButton(
                                    onClick = { selectedCategory = null; expandedCategory = null },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(IcClose, "Clear filter", tint = DText3, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }
                }

                if (isLoading) {
                    item { SkeletonExpenseRows(4) }
                } else if (filteredExpenses.isEmpty()) {
                    item { EmptyState() }
                } else {
                    items(filteredExpenses) { expense ->
                        ExpenseRow(
                            expense = expense,
                            amount = formatAmount(currency, expense.amount),
                            onEdit = { expenseToEdit = expense },
                            onDelete = { expenseToDelete = expense }
                        )
                    }
                }
            }

            AppBottomNav(currentRoute = "dashboard", onNavigate = onNavigate)
        }
    }

    // ── Add Expense dialog ──
    if (showAddExpense) {
        ExpenseSheet(
            title = "Add Expense",
            currencySymbol = currencySymbol(currency),
            onDismiss = { showAddExpense = false },
            onSave = { name, amount, category, date, note ->
                vm.createExpense(name, amount, category, date, note) { showAddExpense = false }
            }
        )
    }

    // ── Edit Expense dialog ──
    expenseToEdit?.let { exp ->
        ExpenseSheet(
            title = "Edit Expense",
            currencySymbol = currencySymbol(currency),
            initial = exp,
            onDismiss = { expenseToEdit = null },
            onSave = { name, amount, category, date, note ->
                vm.editExpense(exp.id, name, amount, category, date, note) { expenseToEdit = null }
            }
        )
    }

    // ── Manage Budget dialog ──
    if (showBudget) {
        ManageBudgetSheet(
            currencySymbol = currencySymbol(currency),
            monthBudget = budget,
            categoryBudgets = categoryBudgets,
            onDismiss = { showBudget = false },
            onSaveMonthly = { amt -> vm.setMonthlyBudget(amt) {} },
            onDeleteMonthly = { vm.deleteMonthlyBudget {} },
            onSaveCategory = { cat, amt -> vm.setCategoryBudget(cat, amt) {} },
            onRemoveCategory = { cat -> vm.deleteCategoryBudget(cat) }
        )
    }

    // ── Delete confirm ──
    expenseToDelete?.let { exp ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            containerColor = DSurface,
            shape = RoundedCornerShape(18.dp),
            title = { Text("Delete expense?", color = DText, fontWeight = FontWeight.Bold,
                fontSize = 16.sp, letterSpacing = (-0.3).sp) },
            text = { Text("Delete \"${exp.name}\" (${formatAmount(currency, exp.amount)})?",
                color = DText2, fontSize = 13.5.sp, lineHeight = 20.sp) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteExpense(exp.id)
                    expenseToDelete = null
                }) { Text("Delete", color = DRed, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) { Text("Cancel", color = DText2) }
            }
        )
    }
}

@Composable
fun OvStat(label: String, value: String, isMuted: Boolean = false, valueColor: Color = DText) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = DText3,
            letterSpacing = 0.1.sp)
        Text(value,
            fontSize = if (isMuted) 16.sp else 19.sp,
            fontWeight = FontWeight.Bold,
            color = if (isMuted) DText2 else valueColor,
            letterSpacing = (-0.8).sp)
    }
}

@Composable
fun OvMeta(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = DText3)
}

@Composable
fun QsCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector,
           iconBg: Color, iconColor: Color, value: String,
           label: String, smallValue: Boolean = false) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DSurface)
            .border(1.dp, DBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(iconBg),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(14.dp))
        }
        Text(value, fontSize = if (smallValue) 13.5.sp else 18.sp,
            fontWeight = FontWeight.Bold, color = DText, letterSpacing = (-0.5).sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, fontSize = 11.sp, color = DText3, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(DSurface).border(1.dp, DBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 52.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Color(0x0AFFFFFF)),
            contentAlignment = Alignment.Center) {
            Icon(IcReceipt, null, tint = DText3, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text("No expenses yet", fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
            color = DText, letterSpacing = (-0.2).sp)
        Text("Add your first expense to get started", fontSize = 13.sp, color = DText3,
            lineHeight = 20.sp)
    }
}

@Composable
fun ExpenseRow(expense: Expense, amount: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    val (catBg, catColor) = categoryColors(expense.category)
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(DSurface).border(1.dp, DBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(catBg),
            contentAlignment = Alignment.Center) {
            Icon(categoryIcon(expense.category), null, tint = catColor, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(expense.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DText,
                maxLines = 1, overflow = TextOverflow.Ellipsis, letterSpacing = (-0.2).sp)
            Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(catBg)
                .padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text(expense.category.replaceFirstChar { it.uppercase() },
                    fontSize = 10.5.sp, fontWeight = FontWeight.Medium, color = catColor)
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("-$amount", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                color = DText, letterSpacing = (-0.4).sp)
            Text(formatExpenseDate(expense.date), fontSize = 11.sp, color = DText3)
        }
        // Edit button
        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(7.dp))
            .background(Color(0x1A3B82F6)).clickable { onEdit() },
            contentAlignment = Alignment.Center) {
            Icon(IcEdit, "Edit", tint = DAccent, modifier = Modifier.size(11.dp))
        }
        // Delete button
        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(7.dp))
            .background(Color(0x1AEF4444)).clickable { onDelete() },
            contentAlignment = Alignment.Center) {
            Icon(IcClose, "Delete", tint = DRed, modifier = Modifier.size(11.dp))
        }
    }
}

fun categoryColors(category: String): Pair<Color, Color> = when (category) {
    "food"          -> Color(0x24F97316) to Color(0xFFFB923C)
    "housing"       -> Color(0x243B82F6) to Color(0xFF60A5FA)
    "transport"     -> Color(0x248B5CF6) to Color(0xFFA78BFA)
    "shopping"      -> Color(0x24EC4899) to Color(0xFFF472B6)
    "entertainment" -> Color(0x24F59E0B) to Color(0xFFFBBF24)
    "taxes"         -> Color(0x2414B8A6) to Color(0xFF2DD4BF)
    "investment"    -> Color(0x244ADE80) to Color(0xFF4ADE80)
    "savings"       -> Color(0x24A78BFA) to Color(0xFFA78BFA)
    else            -> Color(0x2464748B) to Color(0xFF94A3B8)
}


fun formatExpenseDate(dateStr: String): String = try {
    val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val output = SimpleDateFormat("MMM d", Locale.getDefault())
    output.format(input.parse(dateStr)!!)
} catch (e: Exception) {
    try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("MMM d", Locale.getDefault())
        output.format(input.parse(dateStr)!!)
    } catch (e2: Exception) { dateStr }
}
