package com.example.data.local

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.model.BillingPeriod
import com.example.data.model.CurrencyManager

data class SubscriptionWithMembers(
    @Embedded val subscription: SubscriptionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "subscriptionId"
    )
    val members: List<MemberEntity>
) {
    val billingPeriodObj: BillingPeriod
        get() = BillingPeriod.fromKey(subscription.billingPeriod)

    val totalMembers: Int
        get() = members.size

    val platformPrices: List<com.example.data.model.PlatformPriceItem>
        get() = com.example.data.model.PlatformPricingHelper.parse(subscription.platformPricing)

    val nextRenewalTimestamp: Long
        get() = BillingPeriod.getNextRenewalTimestamp(
            subscription.billingDay,
            subscription.billingMonth,
            billingPeriodObj
        )

    val totalContributed: Double
        get() = members.sumOf { member ->
            val pPricing = platformPrices.find { it.platformName.equals(member.sharingPlatform, ignoreCase = true) }
            val amount = if (member.contributionAmount > 0.0) {
                member.contributionAmount
            } else if (pPricing != null && pPricing.pricePerUser > 0) {
                pPricing.pricePerUser
            } else {
                0.0
            }
            val curr = if (member.currency.isNotBlank()) member.currency else (pPricing?.currency ?: "EUR")
            val eurAmount = CurrencyManager.convertToEur(amount, curr)
            if (member.paymentFrequencyUnit.isNotBlank()) {
                val freqValue = if (member.paymentFrequencyValue > 0) member.paymentFrequencyValue else 1
                when (member.paymentFrequencyUnit) {
                    "months" -> eurAmount / freqValue
                    "years" -> eurAmount / (freqValue * 12.0)
                    "weeks" -> eurAmount * (52.0 / 12.0) / freqValue
                    "days" -> eurAmount * (365.25 / 12.0) / freqValue
                    else -> pPricing?.billingPeriodObj?.toMonthlyCost(eurAmount) ?: eurAmount
                }
            } else if (pPricing != null) {
                pPricing.billingPeriodObj.toMonthlyCost(eurAmount)
            } else {
                eurAmount
            }
        }

    // Coste total convertido a Euros
    val myCostEur: Double
        get() = CurrencyManager.convertToEur(subscription.cost, subscription.currency)

    // Coste mensualizado equivalente en Euros
    val myCostMonthly: Double
        get() = billingPeriodObj.toMonthlyCost(myCostEur)

    val myCost: Double
        get() = myCostMonthly

    // Dinero neto mensual: Total que aportan los usuarios al mes menos mi coste mensual (en Euros)
    val netBalance: Double
        get() = totalContributed - myCostMonthly

    // Si es positivo, estoy ganando dinero neto (superávit)
    val isNetProfit: Boolean
        get() = netBalance > 0.001

    // Si es cero o casi cero, coste 100% cubierto
    val isBreakEven: Boolean
        get() = kotlin.math.abs(netBalance) < 0.01

    // Porcentaje del coste propio que está siendo cubierto por los aportes
    val coveragePercentage: Float
        get() = if (myCostMonthly > 0.0) {
            ((totalContributed / myCostMonthly) * 100).toFloat().coerceAtLeast(0f)
        } else {
            100f
        }

    // Cuánto costaría por persona si se dividiera equitativamente entre titular + miembros
    val equalSplitPerPerson: Double
        get() {
            val totalPeople = totalMembers + 1 // Titular + miembros
            return if (totalPeople > 0) myCostMonthly / totalPeople else myCostMonthly
        }

    // Cuántos miembros tienen su pago al día (no pendiente de pago)
    val paidMembersCount: Int
        get() = members.count { it.isPaidThisMonth && !it.isPendingPayment }

    val pendingMembersCount: Int
        get() = members.count { !it.isPaidThisMonth || it.isPendingPayment }

    val pendingRemovalCount: Int
        get() = members.count { it.isPendingRemoval }

    val pendingRegistrationCount: Int
        get() = members.count { it.isPendingRegistration }
}
