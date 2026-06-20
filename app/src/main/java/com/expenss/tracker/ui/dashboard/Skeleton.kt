package com.expenss.tracker.ui.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonBox(modifier: Modifier, shape: Shape = RoundedCornerShape(8.dp)) {
    val t = rememberInfiniteTransition(label = "sk")
    val a by t.animateFloat(
        initialValue = 1f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "a"
    )
    Box(modifier.clip(shape).background(Color(0xFF252D4A).copy(alpha = a)))
}

// ── Dashboard: 4 skeleton expense rows ──────────────────────────────────────

@Composable
fun SkeletonExpenseRows(count: Int = 4) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(count) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DSurface)
                    .border(1.dp, DBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonBox(Modifier.size(36.dp), RoundedCornerShape(10.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SkeletonBox(Modifier.fillMaxWidth(0.55f).height(13.dp))
                    SkeletonBox(Modifier.width(60.dp).height(10.dp), RoundedCornerShape(999.dp))
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    SkeletonBox(Modifier.width(56.dp).height(13.dp))
                    SkeletonBox(Modifier.width(36.dp).height(10.dp))
                }
                SkeletonBox(Modifier.size(28.dp), RoundedCornerShape(7.dp))
                SkeletonBox(Modifier.size(28.dp), RoundedCornerShape(7.dp))
            }
        }
    }
}

// ── Dashboard: overview card skeleton ────────────────────────────────────────

@Composable
fun SkeletonDashboardOverview() {
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
                // Ring placeholder
                SkeletonBox(Modifier.size(110.dp), CircleShape)
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    repeat(3) {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            SkeletonBox(Modifier.width(48.dp).height(9.dp))
                            SkeletonBox(Modifier.width(90.dp).height(18.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
                    .border(0.5.dp, DBorder, RoundedCornerShape(0.dp))
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonBox(Modifier.width(80.dp).height(12.dp))
                SkeletonBox(Modifier.width(100.dp).height(12.dp))
            }
        }
    }
}

// ── Dashboard: quick stats 3-card skeleton ────────────────────────────────────

@Composable
fun SkeletonQuickStats() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DSurface)
                    .border(1.dp, DBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                SkeletonBox(Modifier.size(32.dp), RoundedCornerShape(9.dp))
                SkeletonBox(Modifier.fillMaxWidth(0.8f).height(18.dp))
                SkeletonBox(Modifier.width(52.dp).height(10.dp))
            }
        }
    }
}

// ── Dashboard: category deck skeleton ────────────────────────────────────────

@Composable
fun SkeletonCategoryDeck() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DSurface)
            .border(1.dp, DBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SkeletonBox(Modifier.size(32.dp), RoundedCornerShape(8.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    SkeletonBox(Modifier.fillMaxWidth(0.4f).height(11.dp))
                    SkeletonBox(Modifier.fillMaxWidth().height(4.dp), RoundedCornerShape(999.dp))
                }
                SkeletonBox(Modifier.width(52.dp).height(13.dp))
            }
        }
    }
}

// ── Goals: skeleton matching the dream card ──────────────────────────────────

@Composable
fun SkeletonGoals() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
            SkeletonBox(Modifier.size(14.dp), CircleShape)
            SkeletonBox(Modifier.width(90.dp).height(12.dp))
            Spacer(Modifier.weight(1f))
            SkeletonBox(Modifier.width(72.dp).height(14.dp))
        }

        // Dream card
        Column(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DSurface)
                .border(1.dp, DBorder, RoundedCornerShape(18.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SkeletonBox(Modifier.size(100.dp), CircleShape)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SkeletonBox(Modifier.width(64.dp).height(20.dp), RoundedCornerShape(999.dp))
                    SkeletonBox(Modifier.fillMaxWidth(0.75f).height(22.dp))
                    SkeletonBox(Modifier.fillMaxWidth(0.55f).height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonBox(Modifier.width(64.dp).height(28.dp), RoundedCornerShape(8.dp))
                        SkeletonBox(Modifier.width(64.dp).height(28.dp), RoundedCornerShape(8.dp))
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DBorder))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                repeat(3) { i ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SkeletonBox(Modifier.width(40.dp).height(11.dp))
                        SkeletonBox(Modifier.width(60.dp).height(15.dp))
                    }
                    if (i < 2) Box(Modifier.width(1.dp).height(36.dp).background(DBorder))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DSurface2)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SkeletonBox(Modifier.size(14.dp), CircleShape)
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    SkeletonBox(Modifier.width(120.dp).height(11.dp))
                    SkeletonBox(Modifier.width(80.dp).height(14.dp))
                }
            }
        }
    }
}

// ── Savings: skeleton for stat cards ─────────────────────────────────────────

@Composable
fun SkeletonSavingsStats() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(2) {
                Column(
                    modifier = Modifier.weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DSurface)
                        .border(1.dp, DBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SkeletonBox(Modifier.width(80.dp).height(11.dp))
                    SkeletonBox(Modifier.fillMaxWidth(0.7f).height(22.dp))
                    SkeletonBox(Modifier.width(90.dp).height(10.dp))
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(2) {
                Column(
                    modifier = Modifier.weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DSurface)
                        .border(1.dp, DBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SkeletonBox(Modifier.width(80.dp).height(11.dp))
                    SkeletonBox(Modifier.fillMaxWidth(0.7f).height(22.dp))
                    SkeletonBox(Modifier.width(90.dp).height(10.dp))
                }
            }
        }
    }
}

@Composable
fun SkeletonSavingsRows(count: Int = 4) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(count) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DSurface)
                    .border(1.dp, DBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonBox(Modifier.size(36.dp), RoundedCornerShape(10.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    SkeletonBox(Modifier.fillMaxWidth(0.5f).height(13.dp))
                    SkeletonBox(Modifier.width(60.dp).height(10.dp))
                }
                SkeletonBox(Modifier.width(56.dp).height(14.dp))
                SkeletonBox(Modifier.size(28.dp), CircleShape)
            }
        }
    }
}
