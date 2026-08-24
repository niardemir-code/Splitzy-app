package com.apleq.app.data.model

import java.util.Locale

data class CurrencyItem(
    val code: String,       // e.g. "EUR", "USD", "GBP"
    val symbol: String,     // e.g. "€", "$", "£"
    val name: String,       // e.g. "Euro", "Dólar estadounidense"
    val flag: String,       // e.g. "🇪🇺", "🇺🇸", "🇬🇧"
    val defaultRateToEur: Double // 1 unit of this currency = X EUR
)

object CurrencyManager {
    val currencies = listOf(
        CurrencyItem("GHS", "GH₵", "Cedi ghanés (GHS)", "🇬🇭", 0.063),
        CurrencyItem("NOK", "kr", "Corona noruega (NOK)", "🇳🇴", 0.085),
        CurrencyItem("SEK", "kr", "Corona sueca (SEK)", "🇸🇪", 0.088),
        CurrencyItem("AUD", "AU$", "Dólar australiano (AUD)", "🇦🇺", 0.60),
        CurrencyItem("CAD", "CA$", "Dólar canadiense (CAD)", "🇨🇦", 0.67),
        CurrencyItem("USD", "$", "Dólar estadounidense (USD)", "🇺🇸", 0.92),
        CurrencyItem("EUR", "€", "Euro (EUR)", "🇪🇺", 1.0),
        CurrencyItem("CHF", "CHF", "Franco suizo (CHF)", "🇨🇭", 1.04),
        CurrencyItem("GBP", "£", "Libra esterlina (GBP)", "🇬🇧", 1.17),
        CurrencyItem("TRY", "₺", "Lira turca (TRY)", "🇹🇷", 0.027),
        CurrencyItem("ARS", "AR$", "Peso argentino (ARS)", "🇦🇷", 0.00095),
        CurrencyItem("CLP", "CLP$", "Peso chileno (CLP)", "🇨🇱", 0.00097),
        CurrencyItem("COP", "COL$", "Peso colombiano (COP)", "🇨🇴", 0.00023),
        CurrencyItem("MXN", "MX$", "Peso mexicano (MXN)", "🇲🇽", 0.051),
        CurrencyItem("BRL", "R$", "Real brasileño (BRL)", "🇧🇷", 0.17),
        CurrencyItem("INR", "₹", "Rupia india (INR)", "🇮🇳", 0.011),
        CurrencyItem("JPY", "¥", "Yen japonés (JPY)", "🇯🇵", 0.0062),
        CurrencyItem("CNY", "¥", "Yuan chino (CNY)", "🇨🇳", 0.127),
        CurrencyItem("PLN", "zł", "Zloty polaco (PLN)", "🇵🇱", 0.233)
    ).sortedBy { it.name }

    private val liveRates = mutableMapOf<String, Double>()

    init {
        currencies.forEach { item ->
            liveRates[item.code] = item.defaultRateToEur
        }
    }

    fun updateRates(newRatesFromEur: Map<String, Double>) {
        // newRatesFromEur maps "USD" -> 1.087, so 1 USD = 1 / 1.087 EUR
        newRatesFromEur.forEach { (code, rate) ->
            if (rate > 0.0) {
                if (code.equals("EUR", ignoreCase = true)) {
                    liveRates["EUR"] = 1.0
                } else {
                    liveRates[code.uppercase()] = 1.0 / rate
                }
            }
        }
    }

    fun findCurrency(codeOrSymbol: String?): CurrencyItem {
        val defaultEur = currencies.find { it.code == "EUR" } ?: currencies.first()
        if (codeOrSymbol.isNullOrBlank()) return defaultEur
        val normalized = codeOrSymbol.trim().uppercase()
        return currencies.find {
            it.code.equals(normalized, ignoreCase = true) ||
            it.symbol.equals(normalized, ignoreCase = true) ||
            it.symbol.equals(codeOrSymbol.trim(), ignoreCase = true)
        } ?: defaultEur
    }

    fun convertToEur(amount: Double, currencyCodeOrSymbol: String?): Double {
        val curr = findCurrency(currencyCodeOrSymbol)
        val rate = liveRates[curr.code] ?: curr.defaultRateToEur
        return amount * rate
    }

    fun getRateToEur(currencyCodeOrSymbol: String?): Double {
        val curr = findCurrency(currencyCodeOrSymbol)
        return liveRates[curr.code] ?: curr.defaultRateToEur
    }

    fun formatInEur(amountInEur: Double): String {
        return "${String.format(Locale.getDefault(), "%.2f", amountInEur)} €"
    }
}
