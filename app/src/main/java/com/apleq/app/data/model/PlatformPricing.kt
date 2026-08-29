package com.apleq.app.data.model

import java.util.Locale

data class PlatformPriceItem(
    val platformName: String,
    val pricePerUser: Double,
    val currency: String = "EUR",
    val billingPeriod: String = "MONTHLY",
    val defaultPaymentMethod: String = ""
) {
    val currencyItem: CurrencyItem
        get() = CurrencyManager.findCurrency(currency)

    val billingPeriodObj: BillingPeriod
        get() = BillingPeriod.fromKey(billingPeriod)

    fun formattedPrice(): String {
        return "${String.format(Locale.getDefault(), "%.2f", pricePerUser)} ${currencyItem.symbol}${billingPeriodObj.suffix}"
    }

    val monthlyEurAmount: Double
        get() {
            val eurPrice = CurrencyManager.convertToEur(pricePerUser, currency)
            return billingPeriodObj.toMonthlyCost(eurPrice)
        }
}

object PlatformPricingHelper {
    val defaultPlatforms = listOf(
        "Sharesub",
        "Together Price",
        "Spliiit",
        "Sharingful",
        "GamsGo",
        "Directo / Familia"
    )

    fun parse(raw: String): List<PlatformPriceItem> {
        if (raw.isBlank()) return emptyList()
        return raw.split("|").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size >= 2) {
                val name = parts[0].trim()
                val price = parts[1].trim().replace(',', '.').toDoubleOrNull() ?: 0.0
                val rawCurr = if (parts.size >= 3 && parts[2].isNotBlank()) parts[2].trim() else "EUR"
                val currency = CurrencyManager.findCurrency(rawCurr).code
                val billingPeriod = if (parts.size >= 4 && parts[3].isNotBlank()) parts[3].trim().uppercase() else "MONTHLY"
                val method = if (parts.size >= 5 && parts[4].isNotBlank()) parts[4].trim() else ""
                if (name.isNotEmpty()) PlatformPriceItem(name, price, currency, billingPeriod, method) else null
            } else null
        }
    }

    fun parseAny(raw: Any?): List<PlatformPriceItem> {
        if (raw == null) return emptyList()
        if (raw is String) return parse(raw)
        if (raw is List<*>) {
            return raw.mapNotNull { item ->
                when (item) {
                    is Map<*, *> -> {
                        val name = (item["platformName"] ?: item["platform_name"] ?: item["platform"] ?: item["name"])?.toString()?.trim().orEmpty()
                        val priceRaw = item["pricePerUser"] ?: item["price_per_user"] ?: item["price"] ?: item["cost"] ?: item["amount"]
                        val price = when (priceRaw) {
                            is Number -> priceRaw.toDouble()
                            is String -> priceRaw.replace(',', '.').toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                        val currRaw = (item["currency"] ?: item["currency_code"] ?: item["moneda"] ?: item["currencyCode"])?.toString()?.trim().orEmpty()
                        val currency = if (currRaw.isNotBlank()) CurrencyManager.findCurrency(currRaw).code else "EUR"
                        val periodRaw = (item["billingPeriod"] ?: item["billing_period"] ?: item["period"])?.toString()?.trim().orEmpty()
                        val billingPeriod = if (periodRaw.isNotBlank()) periodRaw.uppercase() else "MONTHLY"
                        val methodRaw = (item["defaultPaymentMethod"] ?: item["default_payment_method"] ?: item["paymentMethod"] ?: item["method"])?.toString()?.trim().orEmpty()
                        if (name.isNotBlank()) PlatformPriceItem(name, price, currency, billingPeriod, methodRaw) else null
                    }
                    is String -> {
                        parse(item).firstOrNull()
                    }
                    else -> null
                }
            }
        }
        if (raw is Map<*, *>) {
            return raw.entries.mapNotNull { (key, value) ->
                val name = key?.toString()?.trim().orEmpty()
                if (name.isBlank()) return@mapNotNull null
                when (value) {
                    is Map<*, *> -> {
                        val priceRaw = value["pricePerUser"] ?: value["price_per_user"] ?: value["price"] ?: value["cost"] ?: value["amount"]
                        val price = when (priceRaw) {
                            is Number -> priceRaw.toDouble()
                            is String -> priceRaw.replace(',', '.').toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                        val currRaw = (value["currency"] ?: value["currency_code"] ?: value["moneda"])?.toString()?.trim().orEmpty()
                        val currency = if (currRaw.isNotBlank()) CurrencyManager.findCurrency(currRaw).code else "EUR"
                        val periodRaw = (value["billingPeriod"] ?: value["billing_period"] ?: value["period"])?.toString()?.trim().orEmpty()
                        val billingPeriod = if (periodRaw.isNotBlank()) periodRaw.uppercase() else "MONTHLY"
                        val methodRaw = (value["defaultPaymentMethod"] ?: value["default_payment_method"] ?: value["paymentMethod"] ?: value["method"])?.toString()?.trim().orEmpty()
                        PlatformPriceItem(name, price, currency, billingPeriod, methodRaw)
                    }
                    is Number -> PlatformPriceItem(name, value.toDouble(), "EUR", "MONTHLY")
                    is String -> {
                        val parsed = parse("$name:$value").firstOrNull()
                        parsed ?: PlatformPriceItem(name, value.replace(',', '.').toDoubleOrNull() ?: 0.0, "EUR", "MONTHLY")
                    }
                    else -> null
                }
            }
        }
        return emptyList()
    }

    fun serialize(items: List<PlatformPriceItem>): String {
        return items
            .filter { it.platformName.isNotBlank() }
            .joinToString("|") { item ->
                val method = item.defaultPaymentMethod.replace(":", "").replace("|", "").trim()
                "${item.platformName.trim()}:${item.pricePerUser}:${item.currency.trim().uppercase()}:${item.billingPeriod.trim().uppercase()}:${method}"
            }
    }

    fun getPriceItemFor(raw: String, platformName: String): PlatformPriceItem? {
        return parse(raw).find { it.platformName.equals(platformName.trim(), ignoreCase = true) }
    }

    fun getPriceFor(raw: String, platformName: String): Double? {
        return getPriceItemFor(raw, platformName)?.pricePerUser
    }
}

