package com.expenss.tracker.ui.analytics

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenss.tracker.ui.dashboard.*
import com.expenss.tracker.ui.theme.IcBarChart
import com.expenss.tracker.ui.theme.IcTriangleAlert

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

    Box(modifier = Modifier.fillMaxSize().background(DBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            SimpleTopBar(
                title = "Analytics",
                username = username,
                currencyCode = currency,
                onSetCurrency = { vm.setCurrency(it) },
                onLogout = {
                    com.expenss.tracker.util.TokenManager(context).clearToken()
                    onLogout()
                }
            )

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Under construction banner
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1AF59E0B))
                        .border(1.dp, Color(0x26F59E0B), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(IcTriangleAlert, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                    Text("Under Construction — Charts coming soon!",
                        fontSize = 13.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.Medium)
                }

                // Hero
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DSurface)
                        .border(1.dp, DBorder, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.size(52.dp).clip(CircleShape)
                        .background(DAccentBg), contentAlignment = Alignment.Center) {
                        Icon(IcBarChart, null, tint = DAccent, modifier = Modifier.size(22.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Spending Analytics", fontSize = 17.sp,
                            fontWeight = FontWeight.Bold, color = DText, letterSpacing = (-0.4).sp)
                        Text("Visualize your spending trends over time.",
                            fontSize = 13.sp, color = DText3, lineHeight = 18.sp)
                    }
                }

                // Preview card: Spending over time
                PreviewCard(title = "Spending Over Time") {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(55, 72, 48, 88, 63, 40).forEachIndexed { i, h ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom) {
                                Box(modifier = Modifier.width(22.dp)
                                    .fillMaxHeight(h / 100f)
                                    .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                    .background(if (i == 5) DAccent else Color(0x3A3B82F6)))
                                Spacer(Modifier.height(4.dp))
                                Text(listOf("Jan","Feb","Mar","Apr","May","Jun")[i],
                                    fontSize = 9.sp, color = DText3)
                            }
                        }
                    }
                }

                // Preview cards row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PreviewCard(title = "Category Breakdown", modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.size(70.dp).clip(CircleShape)
                            .background(Color(0x1A3B82F6)).align(Alignment.CenterHorizontally)) {
                            Box(modifier = Modifier.size(50.dp).clip(CircleShape)
                                .background(DSurface2).align(Alignment.Center)) {
                                Text("—", fontSize = 14.sp, color = DText3,
                                    modifier = Modifier.align(Alignment.Center))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        listOf("Food" to Color(0xFFA78BFA), "Housing" to Color(0xFF4ADE80),
                            "Transport" to Color(0xFF60A5FA)).forEach { (name, color) ->
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(color))
                                Text(name, fontSize = 10.sp, color = DText3)
                            }
                        }
                    }

                    PreviewCard(title = "Top Categories", modifier = Modifier.weight(1f)) {
                        listOf(
                            "Food" to 0.72f,
                            "Housing" to 0.55f,
                            "Transport" to 0.38f,
                        ).forEach { (name, pct) ->
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(name, fontSize = 10.sp, color = DText2)
                                    Text("${(pct * 100).toInt()}%", fontSize = 10.sp, color = DText3)
                                }
                                Box(modifier = Modifier.fillMaxWidth().height(4.dp)
                                    .clip(RoundedCornerShape(999.dp)).background(Color(0x14FFFFFF))) {
                                    Box(modifier = Modifier.fillMaxWidth(pct).height(4.dp)
                                        .clip(RoundedCornerShape(999.dp)).background(DAccent))
                                }
                            }
                        }
                    }
                }

                // Monthly comparison
                PreviewCard(title = "Monthly Comparison") {
                    listOf("Jun 2026" to 0.80f, "May 2026" to 0.65f, "Apr 2026" to 0.72f).forEach { (month, pct) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(month, fontSize = 11.sp, color = DText2,
                                modifier = Modifier.width(64.dp))
                            Box(modifier = Modifier.weight(1f).height(6.dp)
                                .clip(RoundedCornerShape(999.dp)).background(Color(0x14FFFFFF))) {
                                Box(modifier = Modifier.fillMaxWidth(pct).height(6.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (month.startsWith("Jun")) DAccent else Color(0x3A3B82F6)))
                            }
                            Text("—", fontSize = 11.sp, color = DText3)
                        }
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
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DSurface)
            .border(1.dp, DBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = DText, letterSpacing = (-0.2).sp)
            Box(modifier = Modifier.clip(RoundedCornerShape(999.dp))
                .background(Color(0x14F59E0B)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                Text("Soon", fontSize = 10.sp, color = Color(0xFFFBBF24),
                    fontWeight = FontWeight.SemiBold)
            }
        }
        content()
    }
}
