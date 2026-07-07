package com.paytrack.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand / Primary ──────────────────────────────────────────────────────────
val IndigoPrimary      = Color(0xFF5B4CFC)
val IndigoDark         = Color(0xFF4F46E5)
val IndigoLight        = Color(0xFFE7E4FF)
val IndigoContainer    = Color(0xFFF1EEFF)

val VioletAccent       = Color(0xFF8B5CF6)
val VioletLight        = Color(0xFFF1ECFF)

// ── Semantic Colors ───────────────────────────────────────────────────────────
val IncomeGreen        = Color(0xFF10B981)
val IncomeGreenAlt     = Color(0xFF0FA968)
val IncomeGreenBg      = Color(0xFFE6F7EF)
val IncomeGreenDark    = Color(0xFF059669)

val ExpenseRed         = Color(0xFFEF4444)
val ExpenseRedBg       = Color(0xFFFFE4E6)
val ExpenseRedDark     = Color(0xFFE11D48)

val SavingsPurple      = Color(0xFF8B5CF6)
val SavingsPurpleBg    = Color(0xFFEDE9FE)

val WarningAmber       = Color(0xFFE0A11C)
val WarningAmberBg     = Color(0xFFFCF3DE)

// ── Background & Surface ─────────────────────────────────────────────────────
val AppBackground      = Color(0xFFF6F6FB)
val AppSurface         = Color(0xFFFFFFFF)
val AppSurfaceMuted    = Color(0xFFF1F3FF)
val AppSurfaceVariant  = Color(0xFFE8EAFF)

// ── Text ─────────────────────────────────────────────────────────────────────
val AppTextPrimary     = Color(0xFF14141F)
val AppTextSecondary   = Color(0xFF8A8A9A)
val AppTextHint        = Color(0xFFB0B0C0)
val AppTextOnPrimary   = Color(0xFFFFFFFF)

// ── Borders / Dividers ───────────────────────────────────────────────────────
val AppBorder          = Color(0xFFF2F2F8)
val AppDivider         = Color(0x0F14141F)

// ── Dark Theme ───────────────────────────────────────────────────────────────
val AppBackgroundDark     = Color(0xFF0D0F1A)
val AppSurfaceDark        = Color(0xFF161825)
val AppSurfaceMutedDark   = Color(0xFF1E2135)
val AppSurfaceVariantDark = Color(0xFF252840)
val AppTextPrimaryDark    = Color(0xFFF8F8FF)
val AppTextSecondaryDark  = Color(0xFF94A3B8)
val AppBorderDark         = Color(0xFF252840)

// ── Legacy aliases (kept for backward compat) ─────────────────────────────────
val EmeraldPrimary     = IndigoPrimary
val EmeraldDark        = IndigoDark
val EmeraldLight       = IndigoLight
val EmeraldContainer   = IndigoContainer
val BrandBlue          = Color(0xFF5B4CFC)
val BrandBlueDark      = Color(0xFF2563EB)
val BrandBlueLight     = Color(0xFFEFF6FF)
val AppPrimary         = IndigoPrimary
val AppPrimaryDark     = IndigoDark
val AppBlue            = BrandBlue
val AppCoral           = ExpenseRed
val AppPink            = ExpenseRedBg
val AppGrayChip        = Color(0xFF475569)
val AppProgressTrack   = AppSurfaceMuted
val AppSurface_        = AppSurface
val WarningOrange      = WarningAmber
val WarningOrangeBg    = WarningAmberBg

// ── Chart Colors ─────────────────────────────────────────────────────────────
val ChartColors = listOf(
    Color(0xFF6D5DFB), // Food
    Color(0xFF0FA968), // Shopping
    Color(0xFF8B5CF6), // Entertainment
    Color(0xFFE0A11C), // Salary
    Color(0xFFF43F5E),
    Color(0xFF06B6D4),
    Color(0xFFEC4899),
    Color(0xFF84CC16)
)

val CategoryFoodBg = Color(0xFFEEECFF)
val CategoryShoppingBg = Color(0xFFE6F7EF)
val CategoryEntertainmentBg = Color(0xFFF1ECFF)
val CategorySalaryBg = Color(0xFFFCF3DE)

// ── Gradient Definitions ──────────────────────────────────────────────────────
val GradientHero       = listOf(Color(0xFF5B4CFC), Color(0xFF7C6BFF), Color(0xFF9C8CFF))
val GradientHeroDark   = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
val GradientIncome     = listOf(Color(0xFF10B981), Color(0xFF059669))
val GradientExpense    = listOf(Color(0xFFEF4444), Color(0xFFE11D48))
val GradientChart      = listOf(Color(0xFF5B4CFC), Color(0xFF5B4CFC).copy(alpha = 0f))
val GradientVault      = listOf(Color(0xFF0FA968), Color(0xFF17C989), Color(0xFF3EE0A8))
