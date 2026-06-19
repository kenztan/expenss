package com.expenss.tracker.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expenss.tracker.data.network.Expense
import com.expenss.tracker.ui.theme.categoryIcon

val CATEGORY_KEYS = listOf(
    "food", "housing", "transport", "shopping",
    "entertainment", "taxes", "investment", "savings", "other"
)

fun categoryLabel(c: String): String = when (c) {
    "food"          -> "Food & Dining"
    "housing"       -> "Housing"
    "transport"     -> "Transport"
    "shopping"      -> "Shopping"
    "entertainment" -> "Entertainment"
    "taxes"         -> "Taxes"
    "investment"    -> "Investment"
    "savings"       -> "Savings"
    else            -> "Other"
}

private fun chipBg(c: String) = when (c) {
    "food" -> Color(0xFF7C3200); "housing" -> Color(0xFF1E3A70); "transport" -> Color(0xFF3B1F6E)
    "shopping" -> Color(0xFF6B1546); "entertainment" -> Color(0xFF6B4100); "taxes" -> Color(0xFF0D4A42)
    "investment" -> Color(0xFF1A3D2B); "savings" -> Color(0xFF2D1F5E); else -> Color(0xFF243040)
}
private fun chipIconColor(c: String) = when (c) {
    "food" -> Color(0xFFFB923C); "housing" -> Color(0xFF93C5FD); "transport" -> Color(0xFFC4B5FD)
    "shopping" -> Color(0xFFF9A8D4); "entertainment" -> Color(0xFFFCD34D); "taxes" -> Color(0xFF5EEAD4)
    "investment" -> Color(0xFF4ADE80); "savings" -> Color(0xFFA78BFA); else -> Color(0xFF94A3B8)
}
private fun chipBar(c: String) = when (c) {
    "food" -> Color(0xFFF97316); "housing" -> Color(0xFF3B82F6); "transport" -> Color(0xFF8B5CF6)
    "shopping" -> Color(0xFFEC4899); "entertainment" -> Color(0xFFF59E0B); "taxes" -> Color(0xFF14B8A6)
    "investment" -> Color(0xFF4ADE80); "savings" -> Color(0xFFA78BFA); else -> Color(0xFF64748B)
}

private data class CatStat(
    val spent: Double, val count: Int, val pct: Int,
    val budget: Double, val remaining: Double, val budgetPct: Int
)

private fun catStat(cat: String, expenses: List<Expense>, budgets: Map<String, Double>, totalSpent: Double): CatStat {
    val items = expenses.filter { it.category == cat }
    val spent = items.sumOf { it.amount }
    val budget = budgets[cat] ?: 0.0
    return CatStat(
        spent = spent,
        count = items.size,
        pct = if (totalSpent > 0) Math.round(spent / totalSpent * 100).toInt() else 0,
        budget = budget,
        remaining = if (budget > 0) budget - spent else 0.0,
        budgetPct = if (budget > 0) minOf(Math.round(spent / budget * 100).toInt(), 100) else 0
    )
}

@Composable
fun CategoryDeck(
    expenses: List<Expense>,
    categoryBudgets: Map<String, Double>,
    totalSpent: Double,
    expanded: String?,
    format: (Double) -> String,
    onToggle: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 2.dp)
    ) {
        items(CATEGORY_KEYS) { cat ->
            CatChip(
                cat = cat,
                stat = catStat(cat, expenses, categoryBudgets, totalSpent),
                flipped = expanded == cat,
                format = format,
                onClick = { onToggle(cat) }
            )
        }
    }
}

@Composable
private fun CatChip(cat: String, stat: CatStat, flipped: Boolean, format: (Double) -> String, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(600),
        label = "flip"
    )
    Box(
        modifier = Modifier
            .width(220.dp)
            .height(180.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(14.dp))
            .background(chipBg(cat))
            .border(
                1.dp,
                if (flipped) Color(0x26FFFFFF) else Color.Transparent,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
    ) {
        if (rotation <= 90f) {
            // FRONT
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(categoryIcon(cat), null, tint = chipIconColor(cat), modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(10.dp))
                Text(categoryLabel(cat), color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            // BACK (counter-rotated so text is upright)
            Column(modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) {
                Box(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 6.dp)) {
                    Text(categoryLabel(cat), color = Color.White.copy(alpha = 0.7f),
                        fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x59000000))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        BStat("Budget", if (stat.budget > 0) format(stat.budget) else "—",
                            color = Color.White.copy(alpha = 0.55f))
                        BStat("Spent", format(stat.spent), color = Color.White.copy(alpha = 0.9f))
                        BStat("Remaining",
                            if (stat.budget > 0) format(stat.remaining) else "—",
                            color = when {
                                stat.budget == 0.0 -> Color.White.copy(alpha = 0.55f)
                                stat.remaining >= 0 -> Color(0xFF4ADE80)
                                else -> Color(0xFFF87171)
                            })
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(4.dp)
                                .clip(RoundedCornerShape(999.dp)).background(Color(0x14FFFFFF))
                        ) {
                            val frac = ((if (stat.budget > 0) stat.budgetPct else stat.pct) / 100f)
                                .coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier.fillMaxWidth(frac).height(4.dp)
                                    .clip(RoundedCornerShape(999.dp)).background(chipBar(cat))
                            )
                        }
                        Text("${stat.pct}% of total spending · ${stat.count} txn",
                            color = Color.White.copy(alpha = 0.6f), fontSize = 9.5.sp, lineHeight = 13.sp)
                        if (stat.budget > 0) {
                            Text("${stat.budgetPct}% used · ${stat.count} txn",
                                color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, lineHeight = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BStat(label: String, value: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(label, color = Color.White.copy(alpha = 0.45f), fontSize = 9.sp, fontWeight = FontWeight.Medium)
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
