package com.example.data.model

import java.text.DateFormatSymbols
import java.util.Locale

data class MonthItem(
    val number: Int, // 1 to 12
    val name: String,
    val shortName: String
)

enum class BillingPeriod(
    val key: String,
    val label: String,
    val months: Int,
    val suffix: String
) {
    MONTHLY("MONTHLY", "Mensual", 1, "/mes"),
    QUARTERLY("QUARTERLY", "Trimestral", 3, "/trimestre"),
    SEMI_ANNUAL("SEMI_ANNUAL", "Semestral", 6, "/semestre"),
    YEARLY("YEARLY", "Anual", 12, "/año");

    val localizedLabel: String
        get() {
            val lang = Locale.getDefault().language.lowercase()
            return when {
                lang.startsWith("es") -> label
                lang.startsWith("ca") -> when (this) {
                    MONTHLY -> "Mensual"
                    QUARTERLY -> "Trimestral"
                    SEMI_ANNUAL -> "Semestral"
                    YEARLY -> "Anual"
                }
                else -> when (this) {
                    MONTHLY -> "Monthly"
                    QUARTERLY -> "Quarterly"
                    SEMI_ANNUAL -> "Semi-annual"
                    YEARLY -> "Annual"
                }
            }
        }

    val localizedSuffix: String
        get() {
            val lang = Locale.getDefault().language.lowercase()
            return when {
                lang.startsWith("es") || lang.startsWith("ca") -> suffix
                else -> when (this) {
                    MONTHLY -> "/mo"
                    QUARTERLY -> "/quarter"
                    SEMI_ANNUAL -> "/semester"
                    YEARLY -> "/yr"
                }
            }
        }

    fun toMonthlyCost(cost: Double): Double {
        return if (months > 0) cost / months else cost
    }

    val requiresMonthSelection: Boolean
        get() = this != MONTHLY

    companion object {
        val list = listOf(MONTHLY, QUARTERLY, SEMI_ANNUAL, YEARLY)

        val months: List<MonthItem>
            get() {
                val locale = Locale.getDefault()
                val symbols = DateFormatSymbols(locale)
                val fullMonths = symbols.months
                val shortMonths = symbols.shortMonths
                return (1..12).map { i ->
                    val fullName = fullMonths.getOrNull(i - 1)?.takeIf { it.isNotBlank() }
                        ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
                        ?: "Month $i"
                    val shortName = shortMonths.getOrNull(i - 1)?.takeIf { it.isNotBlank() }
                        ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
                        ?: fullName.take(3)
                    MonthItem(i, fullName, shortName)
                }
            }

        fun getMonthName(monthNumber: Int): String {
            val clamped = monthNumber.coerceIn(1, 12)
            val locale = Locale.getDefault()
            val symbols = DateFormatSymbols(locale)
            val name = symbols.months.getOrNull(clamped - 1)
            return if (!name.isNullOrBlank()) {
                name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
            } else {
                "Month $clamped"
            }
        }

        fun getMonthShort(monthNumber: Int): String {
            val clamped = monthNumber.coerceIn(1, 12)
            val locale = Locale.getDefault()
            val symbols = DateFormatSymbols(locale)
            val name = symbols.shortMonths.getOrNull(clamped - 1)
            return if (!name.isNullOrBlank()) {
                name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
            } else {
                getMonthName(clamped).take(3)
            }
        }

        fun fromKey(key: String?): BillingPeriod {
            return when (key?.uppercase()?.trim()) {
                "QUARTERLY", "TRIMESTRAL" -> QUARTERLY
                "SEMI_ANNUAL", "SEMIANNUAL", "SEMESTRAL" -> SEMI_ANNUAL
                "YEARLY", "ANUAL", "ANNUAL" -> YEARLY
                else -> MONTHLY
            }
        }

        fun formatSchedule(day: Int, month: Int, period: BillingPeriod): String {
            val validDay = day.coerceIn(1, 31)
            val monthName = getMonthName(month)
            val lang = Locale.getDefault().language.lowercase()
            val isSpanish = lang.startsWith("es")
            val isCatalan = lang.startsWith("ca")

            return when (period) {
                MONTHLY -> when {
                    isSpanish -> "Día $validDay de cada mes"
                    isCatalan -> "Dia $validDay de cada mes"
                    else -> "Day $validDay of each month"
                }
                YEARLY -> when {
                    isSpanish -> "Día $validDay de $monthName (${period.localizedLabel})"
                    isCatalan -> "Dia $validDay de $monthName (${period.localizedLabel})"
                    else -> "$monthName $validDay (${period.localizedLabel})"
                }
                SEMI_ANNUAL -> {
                    val secondMonth = ((month - 1 + 6) % 12) + 1
                    val secondMonthName = getMonthName(secondMonth)
                    when {
                        isSpanish -> "Día $validDay de $monthName y $secondMonthName (${period.localizedLabel})"
                        isCatalan -> "Dia $validDay de $monthName i $secondMonthName (${period.localizedLabel})"
                        else -> "$monthName & $secondMonthName $validDay (${period.localizedLabel})"
                    }
                }
                QUARTERLY -> when {
                    isSpanish -> "Día $validDay de $monthName (Ciclo cada 3 meses)"
                    isCatalan -> "Dia $validDay de $monthName (Cicle cada 3 mesos)"
                    else -> "$monthName $validDay (Cycle every 3 months)"
                }
            }
        }

        fun formatShortSchedule(day: Int, month: Int, period: BillingPeriod): String {
            val validDay = day.coerceIn(1, 31)
            val lang = Locale.getDefault().language.lowercase()
            val isSpanish = lang.startsWith("es")
            val isCatalan = lang.startsWith("ca")

            return when (period) {
                MONTHLY -> if (isSpanish) "Día $validDay" else if (isCatalan) "Dia $validDay" else "Day $validDay"
                YEARLY -> if (isSpanish) "Día $validDay de ${getMonthShort(month)} • ${period.localizedLabel}" else "$validDay ${getMonthShort(month)} • ${period.localizedLabel}"
                SEMI_ANNUAL -> if (isSpanish) "Día $validDay de ${getMonthShort(month)} • ${period.localizedLabel}" else "$validDay ${getMonthShort(month)} • ${period.localizedLabel}"
                QUARTERLY -> if (isSpanish) "Día $validDay de ${getMonthShort(month)} • ${period.localizedLabel}" else "$validDay ${getMonthShort(month)} • ${period.localizedLabel}"
            }
        }

        fun getNextRenewalTimestamp(day: Int, month: Int, period: BillingPeriod): Long {
            val now = System.currentTimeMillis()
            val cal = java.util.Calendar.getInstance().apply {
                timeInMillis = now
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val currentDay = cal.get(java.util.Calendar.DAY_OF_MONTH)
            val currentMonth = cal.get(java.util.Calendar.MONTH) + 1
            val validDay = day.coerceIn(1, 31)

            when (period) {
                MONTHLY -> {
                    cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                    if (validDay < currentDay) {
                        cal.add(java.util.Calendar.MONTH, 1)
                    }
                    val maxDays = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                    cal.set(java.util.Calendar.DAY_OF_MONTH, validDay.coerceAtMost(maxDays))
                    return cal.timeInMillis
                }
                YEARLY -> {
                    val validMonth = month.coerceIn(1, 12)
                    cal.set(java.util.Calendar.MONTH, validMonth - 1)
                    cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                    val maxDays = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                    cal.set(java.util.Calendar.DAY_OF_MONTH, validDay.coerceAtMost(maxDays))
                    if (cal.timeInMillis < now - 86400000L) {
                        cal.add(java.util.Calendar.YEAR, 1)
                    }
                    return cal.timeInMillis
                }
                SEMI_ANNUAL -> {
                    val validMonth1 = month.coerceIn(1, 12)
                    val validMonth2 = ((validMonth1 - 1 + 6) % 12) + 1
                    val targetMonths = listOf(validMonth1, validMonth2)
                    val candidates = targetMonths.map { m ->
                        val c = java.util.Calendar.getInstance().apply {
                            timeInMillis = now
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                            set(java.util.Calendar.MONTH, m - 1)
                            set(java.util.Calendar.DAY_OF_MONTH, 1)
                            val maxDays = getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                            set(java.util.Calendar.DAY_OF_MONTH, validDay.coerceAtMost(maxDays))
                            if (timeInMillis < now - 86400000L) {
                                add(java.util.Calendar.YEAR, 1)
                            }
                        }
                        c.timeInMillis
                    }
                    return candidates.minOrNull() ?: cal.timeInMillis
                }
                QUARTERLY -> {
                    val validMonth1 = month.coerceIn(1, 12)
                    val targetMonths = (0..3).map { ((validMonth1 - 1 + it * 3) % 12) + 1 }.distinct()
                    val candidates = targetMonths.map { m ->
                        val c = java.util.Calendar.getInstance().apply {
                            timeInMillis = now
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                            set(java.util.Calendar.MONTH, m - 1)
                            set(java.util.Calendar.DAY_OF_MONTH, 1)
                            val maxDays = getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                            set(java.util.Calendar.DAY_OF_MONTH, validDay.coerceAtMost(maxDays))
                            if (timeInMillis < now - 86400000L) {
                                add(java.util.Calendar.YEAR, 1)
                            }
                        }
                        c.timeInMillis
                    }
                    return candidates.minOrNull() ?: cal.timeInMillis
                }
            }
        }
    }
}

