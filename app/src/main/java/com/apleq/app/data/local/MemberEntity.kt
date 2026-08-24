package com.apleq.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "members",
    foreignKeys = [
        ForeignKey(
            entity = SubscriptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["subscriptionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subscriptionId"])]
)
data class MemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subscriptionId: Long,
    val memberName: String,
    val sharingPlatform: String = "Sharesub", // Sharesub, Together Price, Spliiit, Sharingful
    val memberContact: String = "", // Email o teléfono para avisos / recordatorios
    val joinedDate: Long = System.currentTimeMillis(), // Timestamp en milisegundos
    val joinedDateStr: String = "", // Formato YYYY-MM-DD
    val nextPaymentDate: String = "", // Próxima fecha calculada/fijada YYYY-MM-DD
    val paymentFrequencyValue: Int = 1, // Periodicidad numérica (1, 2, 3, 6, 12...)
    val paymentFrequencyUnit: String = "months", // "days", "weeks", "months", "years"
    val autoRepeatPayment: Boolean = true, // Avance automático de ciclo
    val paymentMethod: String = "Bizum", // Método de pago habitual ("Bizum", "Transferencia", "PayPal", etc.)
    val lastPaymentDate: String = "", // Fecha del último pago YYYY-MM-DD
    val enableAlarm: Boolean = false, // Recordatorio / alarma previa
    val alarmValue: Int = 3, // Antelación numérica
    val alarmUnit: String = "days", // Unidad de antelación ("days", "same_day", "hours", "weeks", "months")
    val alarmDaysBefore: Int = 3, // Antelación convertida a días enteros (retrocompatibilidad)
    val contributionAmount: Double = 0.0, // Dinero que aporta este usuario
    val currency: String = "EUR", // Moneda del aporte individual
    val isPaidThisMonth: Boolean = true, // Al día / Pagado (Verde)
    val isPendingPayment: Boolean = false, // Alerta: Pendiente de pago (Amarillo)
    val isPendingRemoval: Boolean = false, // Alerta: Pendiente de eliminar / baja (Rojo)
    val isPendingRegistration: Boolean = false, // Alerta: Pendiente de dar de alta (Azul)
    val paymentStatus: String = "paid", // "paid", "pending", "overdue"
    val notes: String = ""
)

