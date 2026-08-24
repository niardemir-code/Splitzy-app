package com.apleq.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val platformName: String,
    val customPlanName: String = "",
    val mainUserName: String, // Titular / Administrador principal
    val mainUserContact: String = "",
    val cost: Double, // Dinero que me cuesta a mí la suscripción completa
    val billingPeriod: String = "MONTHLY", // MONTHLY, QUARTERLY, SEMI_ANNUAL, YEARLY
    val billingDay: Int = 1, // Día del mes en que se factura (1-31)
    val billingMonth: Int = 1, // Mes de cobro/inicio del ciclo (1-12) para periodos no mensuales
    val currency: String = "€",
    val defaultContributionPerUser: Double = 0.0, // Aporte sugerido / esperado por usuario
    val platformPricing: String = "", // Serializado de hasta 3 plataformas con precio: "Sharesub:3.50|Spliiit:4.00"
    val category: String = "Streaming",
    val maxSlots: Int = 4, // Capacidad total de miembros / perfiles disponibles
    val notes: String = "",
    val iconType: String = "PRESET", // "PRESET", "VECTOR", "CUSTOM_IMAGE"
    val iconKey: String = "Netflix", // Preset platform name or Vector key from IconLibrary
    val customImageUri: String = "", // Local file URI or content path of uploaded gallery image
    val iconColorHex: String = "#6366F1", // Custom color hex for vector icons
    val enableAlarm: Boolean = false, // Alarma de aviso para cobro de la suscripción
    val alarmValue: Int = 3, // Antelación numérica
    val alarmUnit: String = "days", // Unidad de antelación ("days", "same_day", "hours", "weeks", "months")
    val alarmDaysBefore: Int = 3, // Antelación convertida a días enteros
    val createdAt: Long = System.currentTimeMillis()
)
