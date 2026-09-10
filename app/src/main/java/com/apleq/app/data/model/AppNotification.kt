package com.apleq.app.data.model

import com.apleq.app.data.local.MemberEntity
import com.apleq.app.data.local.SubscriptionEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class NotificationStatus { UPCOMING, TODAY, OVERDUE }
enum class NotificationType { ALARM, OVERDUE, PENDING }

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val subscriptionId: String,
    val subscriptionName: String,
    val subscriptionColorHex: String,
    val memberId: String,
    val memberName: String,
    val sharingPlatform: String,
    val amount: Double,
    val currencySymbol: String,
    val nextPaymentDate: String,
    val dueDateText: String,
    val daysRemaining: Int,
    val status: NotificationStatus,
    val isRead: Boolean,
    val alarmConfigText: String? = null,
    val debtSinceDate: String = "",
    val unpaidCycles: Int = 0
) {
    val debtText: String?
        get() {
            if (unpaidCycles < 1 || debtSinceDate.isBlank()) return null
            val formattedDate = try {
                val inputFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val outputFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                LocalDate.parse(debtSinceDate.take(10), inputFmt).format(outputFmt)
            } catch (e: Exception) {
                debtSinceDate
            }
            return if (unpaidCycles == 1) {
                "Debe 1 cuota desde el $formattedDate"
            } else {
                "Debe $unpaidCycles cuotas desde el $formattedDate"
            }
        }
}

object NotificationGenerator {

    /** Convierte la antelación configurada a días enteros. */
    fun alarmLeadTimeDays(value: Int, unit: String): Int {
        if (unit == "same_day" || value == 0) return 0
        val v = maxOf(0, value)
        return when (unit) {
            "hours" -> 0
            "weeks" -> v * 7
            "months" -> v * 30
            else -> v
        }
    }

    private fun parseIsoDate(raw: String): LocalDate? = try {
        if (raw.isBlank()) null
        else LocalDate.parse(raw.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) { null }

    /**
     * Genera la lista de avisos activos a partir de las suscripciones del gestor.
     * Replica la lógica de `generateNotificationsFromSubscriptions` de la web.
     */
    fun generate(
        subscriptions: List<SubscriptionEntity>,
        membersBySubscription: Map<String, List<MemberEntity>>,
        readIds: Set<String>,
        today: LocalDate = LocalDate.now()
    ): List<AppNotification> {
        val result = mutableListOf<AppNotification>()

        subscriptions.forEach { sub ->
            val subId = sub.id.toString()
            val subName = sub.platformName.ifBlank { "Suscripción" }
            val subColor = sub.iconColorHex.ifBlank { "#1285FA" }
            val members = membersBySubscription[subId] ?: emptyList()

            // Pre-calcular platform pricing para calcular correctamente el importe del miembro si contributionAmount == 0.0
            val platformPrices = com.apleq.app.data.model.PlatformPricingHelper.parse(sub.platformPricing)

            members.forEach { m ->
                // Las plazas pendientes de eliminar o aún sin reclamar no generan avisos de pago.
                if (m.isPendingRemoval) return@forEach

                val paymentDate = parseIsoDate(m.nextPaymentDate)
                val isPaid = m.isPaidThisMonth && !m.isPendingPayment
                val hasAlarm = m.enableAlarm

                // No se corta por estar pagado: si el próximo cobro está a 3 días o menos,
                // el gestor quiere verlo igualmente.

                var daysRemaining = 999
                var alarmTriggered = false

                if (paymentDate != null) {
                    daysRemaining = (paymentDate.toEpochDay() - today.toEpochDay()).toInt()
                    val lead = alarmLeadTimeDays(m.alarmValue, m.alarmUnit)
                    if (hasAlarm && daysRemaining <= lead && !isPaid) alarmTriggered = true
                }

                val isPending = m.isPendingPayment
                val hasDebt = m.unpaidCycles >= 1

                // Avisa si: el miembro tiene cuotas pendientes (deuda), o salta su alarma
                // configurada, o quedan 3 días o menos para el próximo cobro.
                // La deuda manda: como la fecha de pago siempre apunta al futuro, sin esta
                // condición un moroso nunca generaría aviso.
                val shouldNotify = hasDebt ||
                    alarmTriggered ||
                    (daysRemaining in 0..3)
                if (!shouldNotify) return@forEach

                val status = when {
                    hasDebt -> NotificationStatus.OVERDUE
                    daysRemaining < 0 -> NotificationStatus.OVERDUE
                    daysRemaining == 0 -> NotificationStatus.TODAY
                    else -> NotificationStatus.UPCOMING
                }

                val dueDateText = when {
                    hasDebt && m.unpaidCycles == 1 -> "Pago pendiente"
                    hasDebt -> "${m.unpaidCycles} pagos pendientes"
                    daysRemaining < 0 -> {
                        val d = -daysRemaining
                        if (d == 1) "Vencido ayer" else "Vencido hace $d días"
                    }
                    daysRemaining == 0 -> "¡Vence hoy!"
                    daysRemaining == 1 -> "Vence mañana"
                    else -> "Vence en $daysRemaining días"
                }

                val unitLabel = when (m.alarmUnit) {
                    "weeks" -> if (m.alarmValue == 1) "semana" else "semanas"
                    "months" -> if (m.alarmValue == 1) "mes" else "meses"
                    else -> if (m.alarmValue == 1) "día" else "días"
                }
                val alarmConfigText = when {
                    !hasAlarm -> null
                    m.alarmUnit == "same_day" || m.alarmValue == 0 -> "Alarma configurada: El mismo día"
                    else -> "Alarma configurada: ${m.alarmValue} $unitLabel antes"
                }

                val type = when {
                    status == NotificationStatus.OVERDUE -> NotificationType.OVERDUE
                    alarmTriggered -> NotificationType.ALARM
                    else -> NotificationType.PENDING
                }

                // Para deudas, el id se ancla a la fecha de inicio de la deuda MÁS el número
                // de cuotas: así, descartar un aviso lo silencia solo mientras la deuda no
                // empeore; si sube de 1 a 2 cuotas, se genera un aviso nuevo que vuelve a saltar.
                val notifId = if (hasDebt && m.debtSinceDate.isNotBlank()) {
                    "notif_${subId}_${m.id}_debt_${m.debtSinceDate}_${m.unpaidCycles}"
                } else {
                    "notif_${subId}_${m.id}_${m.nextPaymentDate.ifBlank { "nopdate" }}"
                }

                val matchedPricing = platformPrices.find { it.platformName.equals(m.sharingPlatform, ignoreCase = true) }
                val effectiveAmount = when {
                    m.contributionAmount > 0.0 -> m.contributionAmount
                    matchedPricing != null && matchedPricing.pricePerUser > 0.0 -> matchedPricing.pricePerUser
                    platformPrices.isNotEmpty() && platformPrices.first().pricePerUser > 0.0 -> platformPrices.first().pricePerUser
                    sub.defaultContributionPerUser > 0.0 -> sub.defaultContributionPerUser
                    else -> 0.0
                }

                val currencySymbol = matchedPricing?.currencyItem?.symbol
                    ?: sub.currency.ifBlank { "€" }

                result.add(
                    AppNotification(
                        id = notifId,
                        type = type,
                        subscriptionId = subId,
                        subscriptionName = subName,
                        subscriptionColorHex = subColor,
                        memberId = m.id.toString(),
                        memberName = m.memberName.ifBlank { "Co-suscriptor" },
                        sharingPlatform = m.sharingPlatform,
                        amount = effectiveAmount,
                        currencySymbol = currencySymbol,
                        nextPaymentDate = m.nextPaymentDate,
                        dueDateText = dueDateText,
                        daysRemaining = daysRemaining,
                        status = status,
                        isRead = readIds.contains(notifId),
                        alarmConfigText = alarmConfigText,
                        debtSinceDate = m.debtSinceDate,
                        unpaidCycles = m.unpaidCycles
                    )
                )
            }
        }

        // Primero los que tienen deuda (más cuotas primero), luego por proximidad del cobro.
        return result.sortedWith(
            compareByDescending<AppNotification> { it.unpaidCycles }
                .thenBy { it.daysRemaining }
        )
    }
}
