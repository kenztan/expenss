package com.expenss.tracker.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenss.tracker.i18n.t
import com.expenss.tracker.ui.dashboard.*
import com.expenss.tracker.ui.theme.IcBarChart

@Composable
fun AnalyticsScreen(
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val vm: AnalyticsViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(context) as T
        }
    })

    val username by vm.username.collectAsState()
    val currency by vm.currency.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val monthlyTotals by vm.monthlyTotals.collectAsState()
    val categoryTotals by vm.categoryTotals.collectAsState()
    val comparisonMonths by vm.comparisonMonths.collectAsState()
    val totalCurrentPeriod by vm.totalCurrentPeriod.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(DBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            SimpleTopBar(
                title = t("dashboard.nav.analytics"),
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
                // Hero
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DSurface)
                        .border(1.dp, DBorder, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                        .background(DAccentBg), contentAlignment = Alignment.Center) {
                        Icon(IcBarChart, null, tint = DAccent, modifier = Modifier.size(22.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(t("analytics.heroTitle"), fontSize = 18.sp,
                            fontWeight = FontWeight.Bold, color = DText, letterSpacing = (-0.4).sp)
                        Text(t("analytics.heroSubtitle"),
                            fontSize = 13.5.sp, color = DText2, lineHeight = 19.sp)
                    }
                }

                // Spending Over Time
                PreviewCard(title = t("analytics.spendingOverTime"), period = t("analytics.last6Months")) {
                    when {
                        isLoading -> ChartSkeleton(130.dp)
                        monthlyTotals.isEmpty() -> NoDataRow()
                        else -> BarChart(monthlyTotals, currency)
                    }
                }

                // Category Breakdown
                PreviewCard(title = t("analytics.categoryBreakdown")) {
                    when {
                        isLoading -> ChartSkeleton(110.dp)
                        categoryTotals.isEmpty() -> NoDataRow()
                        else -> DonutChart(categoryTotals, formatShort(currency, totalCurrentPeriod))
                    }
                }

                // Top Categories
                PreviewCard(title = t("analytics.topCategories")) {
                    when {
                        isLoading -> ChartSkeleton(80.dp)
                        categoryTotals.isEmpty() -> NoDataRow()
                        else -> TopCategoriesList(categoryTotals, currency)
                    }
                }

                // Monthly Comparison
                PreviewCard(title = t("analytics.monthlyComparison")) {
                    when {
                        isLoading -> ChartSkeleton(80.dp)
                        comparisonMonths.isEmpty() -> NoDataRow()
                        else -> ComparisonRows(comparisonMonths, currency)
                    }
                }
            }

            AppBottomNav(currentRoute = "analytics", onNavigate = onNavigate)
        }
    }
}

@Composable
private fun PreviewCard(
    title: String,
    period: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DSurface)
            .border(1.dp, DBorder, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                color = DText3, letterSpacing = 0.08.sp)
            if (period != null) {
                Text(period, fontSize = 10.5.sp, color = DText3, fontWeight = FontWeight.Medium)
            }
        }
        content()
    }
}

@Composable
private fun ChartSkeleton(height: androidx.compose.ui.unit.Dp) {
    SkeletonBox(Modifier.fillMaxWidth().height(height), RoundedCornerShape(8.dp))
}

@Composable
private fun NoDataRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(IcBarChart, null, tint = DText3, modifier = Modifier.size(18.dp))
        Text(t("analytics.noData"), fontSize = 13.sp, color = DText3)
    }
}

@Composable
private fun BarChart(totals: List<PeriodTotal>, currency: String) {
    Row(
        modifier = Modifier.fillMaxWidth().height(130.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        totals.forEach { m ->
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                val amountLabel = formatShort(currency, m.amount)
                Text(
                    amountLabel.ifEmpty { " " },
                    fontSize = 9.sp, color = DText2, fontWeight = FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Clip
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height((130 * (m.pct / 100f)).dp.coerceAtLeast(2.dp))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(if (m.isCurrent) Color(0x8C3B82F6) else Color(0x383B82F6))
                )
                Spacer(Modifier.height(6.dp))
                Text(m.label, fontSize = 10.sp, color = DText3, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun DonutChart(categories: List<CatTotal>, totalLabel: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(88.dp)) {
                var startAngle = -90f
                categories.forEach { c ->
                    val sweep = (c.pct / 100f) * 360f
                    drawArc(color = c.color.copy(alpha = 0.45f), startAngle = startAngle, sweepAngle = sweep, useCenter = true)
                    startAngle += sweep
                }
                val remainder = 270f - startAngle
                if (remainder > 0.5f) {
                    drawArc(color = Color(0x0FFFFFFF), startAngle = startAngle, sweepAngle = remainder, useCenter = true)
                }
            }
            Box(
                modifier = Modifier.size(54.dp).clip(CircleShape).background(DSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(totalLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DText,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 12.sp)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.take(5).forEach { c ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(c.color))
                    Text(t("dashboard.cat.${c.key}"), fontSize = 12.sp, color = DText2,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 90.dp))
                    Text("${c.pct}%", fontSize = 11.sp, color = DText3, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TopCategoriesList(categories: List<CatTotal>, currency: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.forEach { c ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(c.color))
                Text(t("dashboard.cat.${c.key}"), fontSize = 12.5.sp, color = DText2,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(90.dp))
                Box(
                    modifier = Modifier.weight(1f).height(5.dp)
                        .clip(RoundedCornerShape(999.dp)).background(Color(0x0DFFFFFF))
                ) {
                    Box(modifier = Modifier.fillMaxWidth(c.pct / 100f).height(5.dp)
                        .clip(RoundedCornerShape(999.dp)).background(c.color.copy(alpha = 0.55f)))
                }
                Text("${c.pct}%", fontSize = 11.5.sp, color = DText3, modifier = Modifier.width(34.dp))
                Text(formatShort(currency, c.amount), fontSize = 11.5.sp, color = DText2,
                    fontWeight = FontWeight.Medium, modifier = Modifier.width(48.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
        }
    }
}

@Composable
private fun ComparisonRows(months: List<PeriodTotal>, currency: String) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        months.forEach { m ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(m.label, fontSize = 12.5.sp,
                    color = if (m.isCurrent) DAccent else DText2,
                    fontWeight = if (m.isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.width(64.dp))
                Box(
                    modifier = Modifier.weight(1f).height(8.dp)
                        .clip(RoundedCornerShape(999.dp)).background(Color(0x0DFFFFFF))
                ) {
                    Box(modifier = Modifier.fillMaxWidth(m.pct / 100f).height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (m.isCurrent) Color(0x8C3B82F6) else Color(0x14FFFFFF)))
                }
                Text(formatAmount(currency, m.amount), fontSize = 12.sp, color = DText2,
                    fontWeight = FontWeight.Medium, modifier = Modifier.width(72.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
        }
    }
}
