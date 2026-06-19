package com.expenss.tracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expenss.tracker.ui.theme.IcBarChart
import com.expenss.tracker.ui.theme.IcDashboard
import com.expenss.tracker.ui.theme.IcReceipt
import com.expenss.tracker.ui.theme.IcTarget

@Composable
fun SimpleTopBar(
    title: String,
    username: String,
    currencyCode: String,
    onSetCurrency: (String) -> Unit,
    onLogout: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xB30B0D14))
            .border(width = 0.5.dp, color = DBorder, shape = RoundedCornerShape(0.dp))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
            color = DText, letterSpacing = (-0.3).sp)
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
                currencyCode = currencyCode,
                onDismiss = { showMenu = false },
                onSetCurrency = onSetCurrency,
                onLogout = { showMenu = false; onLogout() }
            )
        }
    }
}

@Composable
fun UserMenu(
    expanded: Boolean,
    username: String,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSetCurrency: (String) -> Unit,
    onLogout: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(Color(0xFF1A1F30))
            .border(1.dp, DBorder2, RoundedCornerShape(12.dp))
    ) {
        Text(username, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = DText, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp))
        HorizontalDivider(color = DBorder)
        Text("CURRENCY", fontSize = 10.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 0.08.sp, color = DText3,
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 7.dp))
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("USD", "JPY", "IDR").forEach { code ->
                val active = currencyCode == code
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) DAccent else Color(0x0FFFFFFF))
                        .border(1.dp, if (active) DAccent else DBorder2, RoundedCornerShape(8.dp))
                        .clickable { onSetCurrency(code) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(code, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = if (active) Color.White else DText2)
                }
            }
        }
        HorizontalDivider(color = DBorder)
        DropdownMenuItem(
            text = { Text("Logout", fontSize = 13.5.sp, color = DRed) },
            onClick = onLogout
        )
    }
}

@Composable
fun AppBottomNav(currentRoute: String, onNavigate: (String) -> Unit) {
    val items = listOf(
        Triple("dashboard", "Dashboard", IcDashboard),
        Triple("goals",     "Goals",     IcReceipt),
        Triple("savings",   "Savings",   IcTarget),
        Triple("analytics", "Analytics", IcBarChart)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DSidebar)
            .border(width = 0.5.dp, color = DBorder, shape = RoundedCornerShape(0.dp))
            .navigationBarsPadding()
            .padding(horizontal = 8.dp)
            .height(64.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (route, label, icon) ->
            val isActive = route == currentRoute
            val bgMod = if (isActive)
                Modifier.clip(RoundedCornerShape(12.dp)).background(DAccentBg)
            else Modifier

            Column(
                modifier = bgMod
                    .clickable { if (!isActive) onNavigate(route) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(icon, label, tint = if (isActive) DAccent else DText2,
                    modifier = Modifier.size(20.dp))
                Text(label, fontSize = 10.sp, letterSpacing = 0.01.sp,
                    color = if (isActive) DAccent else DText2,
                    fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal)
            }
        }
    }
}
