package com.expenss.tracker.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Lucide icons (24x24, stroke-based) ported directly from the web app's SVG path data.
 * Color is a placeholder — the actual color is applied at the call site via Icon(tint = ...).
 */
private fun lucide(strokes: List<String>, fills: List<String> = emptyList()): ImageVector {
    val b = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    )
    strokes.forEach { d ->
        b.addPath(
            pathData = PathParser().parsePathString(d).toNodes(),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }
    fills.forEach { d ->
        b.addPath(
            pathData = PathParser().parsePathString(d).toNodes(),
            fill = SolidColor(Color.Black)
        )
    }
    return b.build()
}

// SVG primitive -> path-data helpers (arithmetic done here to avoid hand-conversion mistakes)
private fun circle(cx: Float, cy: Float, r: Float) =
    "M${cx - r} $cy a$r $r 0 1 0 ${2 * r} 0 a$r $r 0 1 0 ${-2 * r} 0 z"

private fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
    "M$x1 $y1 L$x2 $y2"

private fun rrect(x: Float, y: Float, w: Float, h: Float, rx: Float) =
    "M${x + rx} $y h${w - 2 * rx} a$rx $rx 0 0 1 $rx $rx v${h - 2 * rx} " +
    "a$rx $rx 0 0 1 ${-rx} $rx h${-(w - 2 * rx)} a$rx $rx 0 0 1 ${-rx} ${-rx} " +
    "v${-(h - 2 * rx)} a$rx $rx 0 0 1 $rx ${-rx} z"

// ---- Bottom navigation ----
val IcDashboard = lucide(listOf(
    rrect(3f, 3f, 7f, 9f, 1f), rrect(14f, 3f, 7f, 5f, 1f),
    rrect(14f, 12f, 7f, 9f, 1f), rrect(3f, 16f, 7f, 5f, 1f)
))
val IcTarget = lucide(listOf(circle(12f, 12f, 10f), circle(12f, 12f, 6f), circle(12f, 12f, 2f)))
val IcWallet = lucide(listOf(
    "M20 12V8H6a2 2 0 0 1-2-2c0-1.1.9-2 2-2h12v4",
    "M4 6v12c0 1.1.9 2 2 2h14v-4",
    "M18 12a2 2 0 0 0 0 4h4v-4z"
))
val IcBarChart = lucide(listOf(line(18f, 20f, 18f, 10f), line(12f, 20f, 12f, 4f), line(6f, 20f, 6f, 14f)))
val IcClock = lucide(listOf(circle(12f, 12f, 10f), "M12 6 L12 12 L16 14"))

// ---- Category icons ----
val IcFood = lucide(listOf(
    "M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2",
    "M7 2v20",
    "M21 15V2a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3zm0 0v7"
))
val IcHousing = lucide(listOf(
    "M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z",
    "M9 22 L9 12 L15 12 L15 22"
))
val IcTransport = lucide(
    strokes = listOf(rrect(4f, 3f, 16f, 16f, 2f), "M9 19l-2 3M15 19l2 3M9 12h6M12 3v4"),
    fills = listOf(circle(9f, 14f, 1f), circle(15f, 14f, 1f))
)
val IcShopping = lucide(listOf(
    "M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z",
    "M3 6 L21 6",
    "M16 10a4 4 0 0 1-8 0"
))
val IcEntertainment = lucide(listOf("M5 3 L19 12 L5 21 L5 3 Z"))
val IcTaxes = lucide(listOf(line(19f, 5f, 5f, 19f), circle(6.5f, 6.5f, 2.5f), circle(17.5f, 17.5f, 2.5f)))
val IcInvestment = lucide(listOf("M22 7 L13.5 15.5 L8.5 10.5 L2 17", "M16 7 L22 7 L22 13"))
val IcOther = lucide(listOf(circle(12f, 12f, 10f), line(12f, 8f, 12f, 12f), line(12f, 16f, 12.01f, 16f)))

// ---- UI icons ----
val IcChevronLeft = lucide(listOf("M15 18l-6-6 6-6"))
val IcChevronRight = lucide(listOf("M9 18l6-6-6-6"))
val IcChevronDown = lucide(listOf("M6 9l6 6 6-6"))
val IcPlus = lucide(listOf("M12 5v14M5 12h14"))
val IcTrash = lucide(listOf(
    "M3 6 L5 6 L21 6",
    "M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6",
    "M10 11v6M14 11v6",
    "M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"
))
val IcEdit = lucide(listOf(
    "M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7",
    "M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"
))
val IcClose = lucide(listOf(line(18f, 6f, 6f, 18f), line(6f, 6f, 18f, 18f)))
val IcLogout = lucide(listOf(
    "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4",
    "M16 17 L21 12 L16 7",
    "M21 12 L9 12"
))
val IcReceipt = lucide(listOf(
    "M4 2v20l2-1 2 1 2-1 2 1 2-1 2 1 2-1 2 1V2l-2 1-2-1-2 1-2-1-2 1-2-1-2 1Z",
    "M16 8H8M16 12H8M12 16H8"
))
val IcCheck = lucide(listOf("M20 6L9 17L4 12"))
val IcTriangleAlert = lucide(listOf(
    "M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z",
    line(12f, 9f, 12f, 13f),
    line(12f, 17f, 12.01f, 17f)
))
val IcTrophy = lucide(listOf(
    "M6 9H4.5a2.5 2.5 0 0 1 0-5H6",
    "M18 9h1.5a2.5 2.5 0 0 0 0-5H18",
    "M4 22h16",
    "M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22",
    "M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22",
    "M18 2H6v7a6 6 0 0 0 12 0V2z"
))

fun categoryIcon(category: String): ImageVector = when (category) {
    "food"          -> IcFood
    "housing"       -> IcHousing
    "transport"     -> IcTransport
    "shopping"      -> IcShopping
    "entertainment" -> IcEntertainment
    "taxes"         -> IcTaxes
    "investment"    -> IcInvestment
    "savings"       -> IcWallet
    else            -> IcOther
}
