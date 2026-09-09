package com.apleq.app.data.util

import java.util.Calendar

/**
 * Avanza una fecha al siguiente ciclo de cobro, repitiendo periodos hasta
 * superar la fecha de referencia (por defecto, hoy).
 */
fun calculateNextCycleDate(
    fromMillis: Long,
    freqValue: Int,
    freqUnit: String,
    referenceMillis: Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = fromMillis
    val value = if (freqValue > 0) freqValue else 1

    fun addOnePeriod() {
        when (freqUnit.lowercase()) {
            "days", "dias", "día", "días" -> cal.add(Calendar.DAY_OF_MONTH, value)
            "weeks", "semanas", "semana" -> cal.add(Calendar.WEEK_OF_YEAR, value)
            "years", "anos", "años", "año", "ano" -> cal.add(Calendar.YEAR, value)
            else -> cal.add(Calendar.MONTH, value)
        }
    }

    addOnePeriod()
    var safety = 0
    while (cal.timeInMillis <= referenceMillis && safety < 1200) {
        safety++
        addOnePeriod()
    }
    return cal.timeInMillis
}
