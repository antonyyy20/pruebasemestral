package com.example.jhdkasjhd.ui.marketplace

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.jhdkasjhd.ui.theme.CoinbaseAccentYellow
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticUp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal data class CategoryPalette(
    val top: Color,
    val bottom: Color,
    val accent: Color
)

private val categoryPalettes = listOf(
    CategoryPalette(Color(0xFF4DA3FF), Color(0xFF0052FF), Color(0xFF003ECC)),
    CategoryPalette(Color(0xFFFFB347), Color(0xFFE86A17), Color(0xFFB84E00)),
    CategoryPalette(Color(0xFF6EE7A8), Color(0xFF05B169), Color(0xFF038A52)),
    CategoryPalette(Color(0xFFFF8A8A), Color(0xFFCF202F), Color(0xFF9E1824)),
    CategoryPalette(Color(0xFFF5A8FF), Color(0xFF9B59FF), Color(0xFF6B35CC)),
    CategoryPalette(Color(0xFF7CB9FF), Color(0xFF2F5BFF), Color(0xFF1E3EB8)),
    CategoryPalette(Color(0xFFFFD56B), CoinbaseAccentYellow, Color(0xFFB88600)),
    CategoryPalette(Color(0xFF7AE7FF), Color(0xFF00A3B4), Color(0xFF007A88))
)

internal fun categoryPalette(name: String): CategoryPalette {
    val index = kotlin.math.abs(name.hashCode()) % categoryPalettes.size
    return categoryPalettes[index]
}

internal fun categoryGradient(name: String): Brush {
    val palette = categoryPalette(name)
    return Brush.verticalGradient(listOf(palette.top, palette.bottom))
}

internal fun formatEventDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "Próximamente"
    return try {
        val instant = Instant.parse(isoDate)
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale("es", "PA"))
        localDate.format(formatter)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

internal fun isUpcoming(isoDate: String?): Boolean {
    if (isoDate.isNullOrBlank()) return true
    return try {
        val instant = Instant.parse(isoDate)
        val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        !date.isBefore(LocalDate.now())
    } catch (_: Exception) {
        true
    }
}

internal fun categoryEmoji(category: String): String = when {
    category.contains("tech", ignoreCase = true) ||
        category.contains("tecnolog", ignoreCase = true) -> "💻"
    category.contains("music", ignoreCase = true) ||
        category.contains("música", ignoreCase = true) ||
        category.contains("concierto", ignoreCase = true) -> "🎵"
    category.contains("sport", ignoreCase = true) ||
        category.contains("deporte", ignoreCase = true) -> "⚽"
    category.contains("food", ignoreCase = true) ||
        category.contains("gastronom", ignoreCase = true) -> "🍽️"
    category.contains("meet", ignoreCase = true) ||
        category.contains("conferenc", ignoreCase = true) -> "🎤"
    category.contains("wedding", ignoreCase = true) ||
        category.contains("boda", ignoreCase = true) -> "💍"
    category.contains("party", ignoreCase = true) ||
        category.contains("fiesta", ignoreCase = true) -> "🎉"
    else -> "✦"
}

internal val CoinbaseNavAccent = CoinbasePrimary

private val unsplashPool = listOf(
    "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=800&q=80",
    "https://images.unsplash.com/photo-1540575467063-fe6469fd6382?auto=format&fit=crop&w=800&q=80",
    "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80",
    "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?auto=format&fit=crop&w=800&q=80",
    "https://images.unsplash.com/photo-1574629810360-7aecae4b6c98?auto=format&fit=crop&w=800&q=80",
    "https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=800&q=80",
    "https://images.unsplash.com/photo-1530103862676-de8c9debad1d?auto=format&fit=crop&w=800&q=80",
    "https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=800&q=80"
)

private val categoryImageMap = mapOf(
    "tecnolog" to "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80",
    "tech" to "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80",
    "conferenc" to "https://images.unsplash.com/photo-1540575467063-fe6469fd6382?auto=format&fit=crop&w=800&q=80",
    "meet" to "https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=800&q=80",
    "música" to "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?auto=format&fit=crop&w=800&q=80",
    "music" to "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?auto=format&fit=crop&w=800&q=80",
    "concierto" to "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?auto=format&fit=crop&w=800&q=80",
    "deporte" to "https://images.unsplash.com/photo-1574629810360-7aecae4b6c98?auto=format&fit=crop&w=800&q=80",
    "sport" to "https://images.unsplash.com/photo-1574629810360-7aecae4b6c98?auto=format&fit=crop&w=800&q=80",
    "turf" to "https://images.unsplash.com/photo-1529900748604-07564a03e7a9?auto=format&fit=crop&w=800&q=80",
    "boda" to "https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=800&q=80",
    "wedding" to "https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=800&q=80",
    "fiesta" to "https://images.unsplash.com/photo-1530103862676-de8c9debad1d?auto=format&fit=crop&w=800&q=80",
    "party" to "https://images.unsplash.com/photo-1530103862676-de8c9debad1d?auto=format&fit=crop&w=800&q=80",
    "birthday" to "https://images.unsplash.com/photo-1464349095432-e22fbcd73f64?auto=format&fit=crop&w=800&q=80",
    "gastronom" to "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?auto=format&fit=crop&w=800&q=80",
    "food" to "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?auto=format&fit=crop&w=800&q=80",
    "arte" to "https://images.unsplash.com/photo-1460661419201-fd4cecdf7841?auto=format&fit=crop&w=800&q=80",
    "network" to "https://images.unsplash.com/photo-1521737711867-e3b97375f902?auto=format&fit=crop&w=800&q=80"
)

internal fun categoryImageUrl(name: String): String {
    val normalized = name.lowercase()
    categoryImageMap.forEach { (keyword, url) ->
        if (normalized.contains(keyword)) return url
    }
    val index = kotlin.math.abs(name.hashCode()) % unsplashPool.size
    return unsplashPool[index]
}

internal fun sanitizeBannerUrl(raw: String?): String? {
    val trimmed = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val cleaned = trimmed.replace(Regex("""\$\d+$"""), "")
    return cleaned.takeIf { it.startsWith("http", ignoreCase = true) }
}
