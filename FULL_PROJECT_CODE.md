# SPLITZY - CÓDIGO FUENTE COMPLETO DEL PROYECTO ANDROID

Este documento contiene todos los archivos fuente de la aplicación Android Splitzy para replicarla en la versión Web.



## ARCHIVO: `app/src/main/java/com/example/MainActivity.kt`

```kotlin
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.remote.AuthState
import com.example.data.util.AppThemeMode
import com.example.ui.screens.AuthGateScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SubscriptionViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SubscriptionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDark = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            MyApplicationTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val authState by viewModel.authState.collectAsStateWithLifecycle()
                    val showSettings by viewModel.showSettingsScreen.collectAsStateWithLifecycle()

                    Crossfade(
                        targetState = authState is AuthState.Authenticated,
                        label = "auth_crossfade"
                    ) { isAuthenticated ->
                        if (isAuthenticated) {
                            if (showSettings) {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { viewModel.closeSettingsScreen() }
                                )
                            } else {
                                HomeScreen(viewModel = viewModel)
                            }
                        } else {
                            AuthGateScreen(
                                viewModel = viewModel,
                                authState = authState
                            )
                        }
                    }
                }
            }
        }
    }
}



```


## ARCHIVO: `app/src/main/java/com/example/MyApplication.kt`

```kotlin
package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val initialized = try { FirebaseApp.initializeApp(this) } catch (_: Throwable) { null }
                if (initialized == null) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:345750792662:android:7123300d38bec1521f1c77")
                        .setApiKey("AIzaSyD5S5DI2FSp-LHWmdEhId-5zGETcrqsm78")
                        .setProjectId("splitzy-8ceb1")
                        .setStorageBucket("splitzy-8ceb1.firebasestorage.app")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                }
            }
        } catch (t: Throwable) {
            Log.w("MyApplication", "FirebaseApp init handled gracefully: ${t.message}")
        }
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/data/local/AppDatabase.kt`

```kotlin
package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SubscriptionEntity::class, MemberEntity::class, SharingPlatformEntity::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shared_subscriptions.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/data/local/MemberEntity.kt`

```kotlin
package com.example.data.local

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
    val memberContact: String = "", // Email o teléfono para recordatorios
    val joinedDate: Long = System.currentTimeMillis(), // Fecha en que se unió
    val contributionAmount: Double = 0.0, // Dinero que aporta este usuario
    val isPaidThisMonth: Boolean = true, // Si ya ha pagado la cuota corriente
    val isPendingPayment: Boolean = false, // Interruptor: Pendiente de pago (resalta en amarillo)
    val isPendingRemoval: Boolean = false, // Interruptor: Pendiente eliminar (resalta en rojo)
    val isPendingRegistration: Boolean = false, // Interruptor: Pendiente dar de alta (resalta en azul)
    val notes: String = ""
)

```


## ARCHIVO: `app/src/main/java/com/example/data/local/SharingPlatformEntity.kt`

```kotlin
package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sharing_platforms")
data class SharingPlatformEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val displayOrder: Int = 0
)

```


## ARCHIVO: `app/src/main/java/com/example/data/local/SubscriptionDao.kt`

```kotlin
package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    @Transaction
    @Query("SELECT * FROM subscriptions ORDER BY id DESC")
    fun getAllSubscriptionsWithMembers(): Flow<List<SubscriptionWithMembers>>

    @Transaction
    @Query("SELECT * FROM subscriptions WHERE id = :subscriptionId")
    fun getSubscriptionWithMembersById(subscriptionId: Long): Flow<SubscriptionWithMembers?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity): Long

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE id = :subscriptionId")
    suspend fun deleteSubscriptionById(subscriptionId: Long)

    // Members operations
    @Query("SELECT * FROM members WHERE subscriptionId = :subscriptionId ORDER BY joinedDate ASC")
    fun getMembersForSubscription(subscriptionId: Long): Flow<List<MemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    @Query("DELETE FROM members WHERE id = :memberId")
    suspend fun deleteMemberById(memberId: Long)

    @Query("UPDATE members SET isPaidThisMonth = :isPaid, isPendingPayment = :isPendingPayment WHERE id = :memberId")
    suspend fun updateMemberPaymentStatus(memberId: Long, isPaid: Boolean, isPendingPayment: Boolean)

    @Query("UPDATE members SET isPendingPayment = :isPending, isPaidThisMonth = :isPaid WHERE id = :memberId")
    suspend fun updateMemberPendingPayment(memberId: Long, isPending: Boolean, isPaid: Boolean)

    @Query("UPDATE members SET isPendingRemoval = :isPending WHERE id = :memberId")
    suspend fun updateMemberPendingRemoval(memberId: Long, isPending: Boolean)

    @Query("UPDATE members SET isPendingRegistration = :isPending WHERE id = :memberId")
    suspend fun updateMemberPendingRegistration(memberId: Long, isPending: Boolean)

    @Query("SELECT COUNT(*) FROM subscriptions")
    suspend fun getSubscriptionCount(): Int

    @Query("SELECT * FROM subscriptions")
    suspend fun getAllSubscriptionsDirect(): List<SubscriptionEntity>

    @Query("SELECT * FROM members")
    suspend fun getAllMembersDirect(): List<MemberEntity>

    @Query("DELETE FROM subscriptions")
    suspend fun deleteAllSubscriptions()

    @Query("DELETE FROM members")
    suspend fun deleteAllMembers()

    @Query("DELETE FROM members WHERE subscriptionId = :subscriptionId")
    suspend fun deleteMembersBySubscriptionId(subscriptionId: Long)

    @Transaction
    suspend fun replaceAllSubscriptionsAndMembers(items: List<Pair<SubscriptionEntity, List<MemberEntity>>>) {
        deleteAllMembers()
        deleteAllSubscriptions()
        for ((sub, members) in items) {
            val subId = insertSubscription(sub)
            for (m in members) {
                insertMember(m.copy(subscriptionId = subId))
            }
        }
    }

    // Sharing Platforms operations
    @Query("SELECT * FROM sharing_platforms ORDER BY displayOrder ASC, id ASC")
    fun getAllSharingPlatforms(): Flow<List<SharingPlatformEntity>>

    @Query("SELECT * FROM sharing_platforms ORDER BY displayOrder ASC, id ASC")
    suspend fun getAllSharingPlatformsDirect(): List<SharingPlatformEntity>

    @Query("SELECT * FROM sharing_platforms WHERE id = :id")
    suspend fun getSharingPlatformById(id: Long): SharingPlatformEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharingPlatform(platform: SharingPlatformEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharingPlatforms(platforms: List<SharingPlatformEntity>)

    @Update
    suspend fun updateSharingPlatform(platform: SharingPlatformEntity)

    @Delete
    suspend fun deleteSharingPlatform(platform: SharingPlatformEntity)

    @Query("DELETE FROM sharing_platforms WHERE id = :id")
    suspend fun deleteSharingPlatformById(id: Long)

    @Query("SELECT COUNT(*) FROM sharing_platforms")
    suspend fun getSharingPlatformCount(): Int
}


```


## ARCHIVO: `app/src/main/java/com/example/data/local/SubscriptionEntity.kt`

```kotlin
package com.example.data.local

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
    val notes: String = "",
    val iconType: String = "PRESET", // "PRESET", "VECTOR", "CUSTOM_IMAGE"
    val iconKey: String = "Netflix", // Preset platform name or Vector key from IconLibrary
    val customImageUri: String = "", // Local file URI or content path of uploaded gallery image
    val iconColorHex: String = "#6366F1", // Custom color hex for vector icons
    val createdAt: Long = System.currentTimeMillis()
)

```


## ARCHIVO: `app/src/main/java/com/example/data/local/SubscriptionWithMembers.kt`

```kotlin
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
        get() = members.sumOf { it.contributionAmount }

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

```


## ARCHIVO: `app/src/main/java/com/example/data/model/BillingPeriod.kt`

```kotlin
package com.example.data.model

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

    fun toMonthlyCost(cost: Double): Double {
        return if (months > 0) cost / months else cost
    }

    val requiresMonthSelection: Boolean
        get() = this != MONTHLY

    companion object {
        val list = listOf(MONTHLY, QUARTERLY, SEMI_ANNUAL, YEARLY)

        val months = listOf(
            MonthItem(1, "Enero", "Ene"),
            MonthItem(2, "Febrero", "Feb"),
            MonthItem(3, "Marzo", "Mar"),
            MonthItem(4, "Abril", "Abr"),
            MonthItem(5, "Mayo", "May"),
            MonthItem(6, "Junio", "Jun"),
            MonthItem(7, "Julio", "Jul"),
            MonthItem(8, "Agosto", "Ago"),
            MonthItem(9, "Septiembre", "Sep"),
            MonthItem(10, "Octubre", "Oct"),
            MonthItem(11, "Noviembre", "Nov"),
            MonthItem(12, "Diciembre", "Dic")
        )

        fun getMonthName(monthNumber: Int): String {
            val clamped = monthNumber.coerceIn(1, 12)
            return months[clamped - 1].name
        }

        fun getMonthShort(monthNumber: Int): String {
            val clamped = monthNumber.coerceIn(1, 12)
            return months[clamped - 1].shortName
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
            return when (period) {
                MONTHLY -> "Día $validDay de cada mes"
                YEARLY -> "Día $validDay de $monthName (${period.label})"
                SEMI_ANNUAL -> {
                    val secondMonth = ((month - 1 + 6) % 12) + 1
                    val secondMonthName = getMonthName(secondMonth)
                    "Día $validDay de $monthName y $secondMonthName (${period.label})"
                }
                QUARTERLY -> {
                    "Día $validDay de $monthName (Ciclo cada 3 meses)"
                }
            }
        }

        fun formatShortSchedule(day: Int, month: Int, period: BillingPeriod): String {
            val validDay = day.coerceIn(1, 31)
            return when (period) {
                MONTHLY -> "Día $validDay"
                YEARLY -> "Día $validDay de ${getMonthShort(month)} • Anual"
                SEMI_ANNUAL -> "Día $validDay de ${getMonthShort(month)} • Semestral"
                QUARTERLY -> "Día $validDay de ${getMonthShort(month)} • Trimestral"
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


```


## ARCHIVO: `app/src/main/java/com/example/data/model/CurrencyItem.kt`

```kotlin
package com.example.data.model

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

```


## ARCHIVO: `app/src/main/java/com/example/data/model/IconLibrary.kt`

```kotlin
package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CustomIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val category: String
)

data class ColorOption(
    val hex: String,
    val color: Color,
    val label: String
)

object IconLibrary {
    val availableColors = listOf(
        ColorOption("#6366F1", Color(0xFF6366F1), "Índigo"),
        ColorOption("#4F46E5", Color(0xFF4F46E5), "Violeta"),
        ColorOption("#E50914", Color(0xFFE50914), "Rojo"),
        ColorOption("#1DB954", Color(0xFF1DB954), "Verde"),
        ColorOption("#113CCF", Color(0xFF113CCF), "Azul"),
        ColorOption("#00A8E1", Color(0xFF00A8E1), "Cian"),
        ColorOption("#10A37F", Color(0xFF10A37F), "Esmeralda"),
        ColorOption("#F59E0B", Color(0xFFF59E0B), "Ámbar"),
        ColorOption("#EC4899", Color(0xFFEC4899), "Rosa"),
        ColorOption("#8B5CF6", Color(0xFF8B5CF6), "Púrpura"),
        ColorOption("#06B6D4", Color(0xFF06B6D4), "Turquesa"),
        ColorOption("#F97316", Color(0xFFF97316), "Naranja"),
        ColorOption("#64748B", Color(0xFF64748B), "Pizarra"),
        ColorOption("#1E293B", Color(0xFF1E293B), "Oscuro")
    )

    val availableIcons = listOf(
        CustomIconOption("movie", "Películas", Icons.Default.Movie, "Streaming"),
        CustomIconOption("tv", "Series / TV", Icons.Default.Tv, "Streaming"),
        CustomIconOption("videocam", "Video", Icons.Default.Videocam, "Streaming"),
        CustomIconOption("headphones", "Música", Icons.Default.Headphones, "Música"),
        CustomIconOption("mic", "Podcast / Audio", Icons.Default.Mic, "Música"),
        CustomIconOption("radio", "Radio", Icons.Default.Radio, "Música"),
        CustomIconOption("chat", "Chat / IA", Icons.Default.Chat, "Productividad"),
        CustomIconOption("auto_awesome", "IA / Gemini", Icons.Default.AutoAwesome, "Productividad"),
        CustomIconOption("code", "Desarrollo", Icons.Default.Code, "Productividad"),
        CustomIconOption("cloud", "Nube / Almacenamiento", Icons.Default.Cloud, "Productividad"),
        CustomIconOption("work", "Trabajo", Icons.Default.Work, "Productividad"),
        CustomIconOption("laptop", "Software", Icons.Default.Laptop, "Productividad"),
        CustomIconOption("games", "Gaming", Icons.Default.Games, "Gaming"),
        CustomIconOption("sports_esports", "Consola", Icons.Default.SportsEsports, "Gaming"),
        CustomIconOption("school", "Educación", Icons.Default.School, "Educación"),
        CustomIconOption("book", "Libros / Lectura", Icons.Default.Book, "Educación"),
        CustomIconOption("fitness", "Gimnasio / Deporte", Icons.Default.FitnessCenter, "Salud"),
        CustomIconOption("soccer", "Deportes", Icons.Default.SportsSoccer, "Salud"),
        CustomIconOption("shopping", "Compras / Tienda", Icons.Default.ShoppingBag, "Estilo de vida"),
        CustomIconOption("palette", "Diseño", Icons.Default.Palette, "Estilo de vida"),
        CustomIconOption("newspaper", "Noticias / Prensa", Icons.Default.Newspaper, "Estilo de vida"),
        CustomIconOption("lock", "Seguridad / VPN", Icons.Default.Lock, "Seguridad"),
        CustomIconOption("key", "Contraseñas / Vault", Icons.Default.VpnKey, "Seguridad"),
        CustomIconOption("euro", "Finanzas / Banco", Icons.Default.Euro, "Finanzas"),
        CustomIconOption("star", "Favorito / Premium", Icons.Default.Star, "General"),
        CustomIconOption("heart", "Salud / Bienestar", Icons.Default.Favorite, "General"),
        CustomIconOption("subscriptions", "Suscripción", Icons.Default.Subscriptions, "General"),
        CustomIconOption("widgets", "Servicios", Icons.Default.Widgets, "General")
    )

    fun getIconByKey(key: String): ImageVector {
        return availableIcons.find { it.key.equals(key, ignoreCase = true) }?.icon
            ?: PlatformPresets.list.find { it.name.equals(key, ignoreCase = true) }?.icon
            ?: Icons.Default.Subscriptions
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/data/model/PlatformPreset.kt`

```kotlin
package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class PlatformPreset(
    val name: String,
    val defaultPlan: String,
    val defaultCost: Double,
    val defaultSplitSuggested: Double,
    val maxMembersSuggested: Int,
    val primaryColor: Color,
    val accentColor: Color,
    val category: String,
    val icon: ImageVector
)

object PlatformPresets {
    val list = listOf(
        PlatformPreset(
            name = "Netflix",
            defaultPlan = "Plan Premium 4K (4 pantallas)",
            defaultCost = 17.99,
            defaultSplitSuggested = 4.50,
            maxMembersSuggested = 4,
            primaryColor = Color(0xFFE50914),
            accentColor = Color(0xFFFF4B55),
            category = "Streaming",
            icon = Icons.Default.Movie
        ),
        PlatformPreset(
            name = "Spotify",
            defaultPlan = "Plan Familiar (6 cuentas)",
            defaultCost = 17.99,
            defaultSplitSuggested = 3.00,
            maxMembersSuggested = 5,
            primaryColor = Color(0xFF1DB954),
            accentColor = Color(0xFF1ED760),
            category = "Música",
            icon = Icons.Default.Headphones
        ),
        PlatformPreset(
            name = "Disney+",
            defaultPlan = "Plan Premium 4K",
            defaultCost = 11.99,
            defaultSplitSuggested = 3.00,
            maxMembersSuggested = 4,
            primaryColor = Color(0xFF113CCF),
            accentColor = Color(0xFF3861FB),
            category = "Streaming",
            icon = Icons.Default.Tv
        ),
        PlatformPreset(
            name = "Max (HBO)",
            defaultPlan = "Plan Platino 4K",
            defaultCost = 13.99,
            defaultSplitSuggested = 3.50,
            maxMembersSuggested = 4,
            primaryColor = Color(0xFF5822B4),
            accentColor = Color(0xFF7E42E6),
            category = "Streaming",
            icon = Icons.Default.Movie
        ),
        PlatformPreset(
            name = "YouTube Premium",
            defaultPlan = "Plan Familiar (5 miembros)",
            defaultCost = 17.99,
            defaultSplitSuggested = 3.60,
            maxMembersSuggested = 5,
            primaryColor = Color(0xFFFF0000),
            accentColor = Color(0xFFFF4D4D),
            category = "Streaming",
            icon = Icons.Default.Videocam
        ),
        PlatformPreset(
            name = "ChatGPT Plus / Team",
            defaultPlan = "Suscripción Team / Compartida",
            defaultCost = 25.00,
            defaultSplitSuggested = 12.50,
            maxMembersSuggested = 2,
            primaryColor = Color(0xFF10A37F),
            accentColor = Color(0xFF1FAF88),
            category = "Productividad",
            icon = Icons.Default.Chat
        ),
        PlatformPreset(
            name = "Prime Video / Amazon",
            defaultPlan = "Amazon Prime Anual / Mensual",
            defaultCost = 4.99,
            defaultSplitSuggested = 2.50,
            maxMembersSuggested = 2,
            primaryColor = Color(0xFF00A8E1),
            accentColor = Color(0xFF33C2F3),
            category = "Streaming",
            icon = Icons.Default.ShoppingBag
        ),
        PlatformPreset(
            name = "Apple One",
            defaultPlan = "Apple One Familiar",
            defaultCost = 25.95,
            defaultSplitSuggested = 5.20,
            maxMembersSuggested = 5,
            primaryColor = Color(0xFFFB233B),
            accentColor = Color(0xFFFF5266),
            category = "Música / Servicios",
            icon = Icons.Default.Widgets
        ),
        PlatformPreset(
            name = "Xbox Game Pass",
            defaultPlan = "Ultimate",
            defaultCost = 14.99,
            defaultSplitSuggested = 7.50,
            maxMembersSuggested = 2,
            primaryColor = Color(0xFF107C10),
            accentColor = Color(0xFF139813),
            category = "Gaming",
            icon = Icons.Default.Games
        ),
        PlatformPreset(
            name = "Nintendo Switch Online",
            defaultPlan = "Suscripción Familiar + Paquete",
            defaultCost = 5.83, // 69.99/12
            defaultSplitSuggested = 1.00,
            maxMembersSuggested = 7,
            primaryColor = Color(0xFFE60012),
            accentColor = Color(0xFFFF3344),
            category = "Gaming",
            icon = Icons.Default.Games
        ),
        PlatformPreset(
            name = "Duolingo Super",
            defaultPlan = "Plan Familiar (6 cuentas)",
            defaultCost = 10.25, // 122.99/12
            defaultSplitSuggested = 2.00,
            maxMembersSuggested = 5,
            primaryColor = Color(0xFF58CC02),
            accentColor = Color(0xFF76E817),
            category = "Educación",
            icon = Icons.Default.School
        ),
        PlatformPreset(
            name = "Canva Pro",
            defaultPlan = "Canva Equipos",
            defaultCost = 14.00,
            defaultSplitSuggested = 3.50,
            maxMembersSuggested = 4,
            primaryColor = Color(0xFF00C4CC),
            accentColor = Color(0xFF2CE5ED),
            category = "Diseño",
            icon = Icons.Default.Palette
        ),
        PlatformPreset(
            name = "Gimnasio / Fitness",
            defaultPlan = "Pase Duo / Familiar",
            defaultCost = 45.00,
            defaultSplitSuggested = 22.50,
            maxMembersSuggested = 2,
            primaryColor = Color(0xFFF97316),
            accentColor = Color(0xFFFB923C),
            category = "Salud",
            icon = Icons.Default.FitnessCenter
        ),
        PlatformPreset(
            name = "Otra Plataforma",
            defaultPlan = "Suscripción Compartida",
            defaultCost = 10.00,
            defaultSplitSuggested = 5.00,
            maxMembersSuggested = 2,
            primaryColor = Color(0xFF6366F1),
            accentColor = Color(0xFF818CF8),
            category = "General",
            icon = Icons.Default.Subscriptions
        )
    )

    fun getPreset(name: String): PlatformPreset {
        return list.find { it.name.equals(name, ignoreCase = true) }
            ?: list.last()
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/data/model/PlatformPricing.kt`

```kotlin
package com.example.data.model

data class PlatformPriceItem(
    val platformName: String,
    val pricePerUser: Double
)

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
            if (parts.size == 2) {
                val name = parts[0].trim()
                val price = parts[1].trim().replace(',', '.').toDoubleOrNull() ?: 0.0
                if (name.isNotEmpty()) PlatformPriceItem(name, price) else null
            } else null
        }
    }

    fun serialize(items: List<PlatformPriceItem>): String {
        return items
            .filter { it.platformName.isNotBlank() }
            .take(3)
            .joinToString("|") { "${it.platformName.trim()}:${it.pricePerUser}" }
    }

    fun getPriceFor(raw: String, platformName: String): Double? {
        return parse(raw).find { it.platformName.equals(platformName.trim(), ignoreCase = true) }?.pricePerUser
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/data/model/SharingPlatformInfo.kt`

```kotlin
package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.data.local.SharingPlatformEntity

data class SharingPlatformInfo(
    val name: String,
    val baseColor: Color,
    val badgeBgColor: Color,
    val badgeTextColor: Color
)

object SharingPlatforms {

    val defaultList = listOf(
        SharingPlatformEntity(id = 1, name = "Together Price", colorHex = "#6D28D9", displayOrder = 0),
        SharingPlatformEntity(id = 2, name = "Sharingful", colorHex = "#DB2777", displayOrder = 1),
        SharingPlatformEntity(id = 3, name = "Spliiit", colorHex = "#059669", displayOrder = 2),
        SharingPlatformEntity(id = 4, name = "GamsGo", colorHex = "#D97706", displayOrder = 3),
        SharingPlatformEntity(id = 5, name = "Sharesub", colorHex = "#0284C7", displayOrder = 4),
        SharingPlatformEntity(id = 6, name = "Directo / Familia", colorHex = "#C2410C", displayOrder = 5)
    )

    fun isPlatformMatch(candidate: String, target: String): Boolean {
        val c = candidate.trim()
        val t = target.trim()
        if (c.equals(t, ignoreCase = true)) return true

        // Together Price / Price Together aliases
        val isCandidateTogetherPrice = c.equals("Together Price", ignoreCase = true) || c.equals("Price Together", ignoreCase = true)
        val isTargetTogetherPrice = t.equals("Together Price", ignoreCase = true) || t.equals("Price Together", ignoreCase = true)
        if (isCandidateTogetherPrice && isTargetTogetherPrice) return true

        // Directo / Familia aliases
        val isCandidateDirecto = c.startsWith("Directo", ignoreCase = true) || c.startsWith("Familia", ignoreCase = true)
        val isTargetDirecto = t.startsWith("Directo", ignoreCase = true) || t.startsWith("Familia", ignoreCase = true)
        if (isCandidateDirecto && isTargetDirecto) return true

        return false
    }

    fun parseColor(hex: String, defaultColor: Color = Color(0xFF6366F1)): Color {
        return try {
            val cleanHex = hex.trim().removePrefix("#")
            val colorLong = when (cleanHex.length) {
                6 -> ("FF$cleanHex").toLong(16)
                8 -> cleanHex.toLong(16)
                else -> return defaultColor
            }
            Color(colorLong)
        } catch (e: Exception) {
            defaultColor
        }
    }

    /**
     * Calculates high contrast text color for any background color.
     * Returns White for dark backgrounds, Black for light backgrounds.
     */
    fun getContrastTextColor(backgroundColor: Color): Color {
        // Standard perceived luminance: 0.299*R + 0.587*G + 0.114*B
        val luminance = 0.299f * backgroundColor.red + 0.587f * backgroundColor.green + 0.114f * backgroundColor.blue
        return if (luminance < 0.55f) Color.White else Color(0xFF111827)
    }

    fun fromEntity(entity: SharingPlatformEntity): SharingPlatformInfo {
        val baseColor = parseColor(entity.colorHex)
        val textColor = getContrastTextColor(baseColor)
        return SharingPlatformInfo(
            name = entity.name,
            baseColor = baseColor,
            badgeBgColor = baseColor,
            badgeTextColor = textColor
        )
    }

    fun getInfo(platform: String, customPlatforms: List<SharingPlatformEntity>? = null): SharingPlatformInfo {
        val trimmed = platform.trim()
        if (trimmed.isBlank()) {
            return SharingPlatformInfo("General", Color(0xFF64748B), Color(0xFF64748B), Color.White)
        }

        // 1. Check custom active platforms from database/configuration first
        if (!customPlatforms.isNullOrEmpty()) {
            val match = customPlatforms.find { isPlatformMatch(it.name, trimmed) }
            if (match != null) {
                val baseColor = parseColor(match.colorHex)
                val textColor = getContrastTextColor(baseColor)
                return SharingPlatformInfo(
                    name = trimmed,
                    baseColor = baseColor,
                    badgeBgColor = baseColor,
                    badgeTextColor = textColor
                )
            }
        }

        // 2. Check defaults
        val defaultMatch = defaultList.find { isPlatformMatch(it.name, trimmed) }
        if (defaultMatch != null) {
            val baseColor = parseColor(defaultMatch.colorHex)
            val textColor = getContrastTextColor(baseColor)
            return SharingPlatformInfo(
                name = trimmed,
                baseColor = baseColor,
                badgeBgColor = baseColor,
                badgeTextColor = textColor
            )
        }

        // 3. Fallback generic palette derived deterministically from name hash
        val colors = listOf(
            Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899),
            Color(0xFF059669), Color(0xFFD97706), Color(0xFF0284C7),
            Color(0xFF06B6D4), Color(0xFF84CC16), Color(0xFFDC2626)
        )
        val colorIndex = Math.abs(trimmed.hashCode()) % colors.size
        val baseColor = colors[colorIndex]
        val textColor = getContrastTextColor(baseColor)
        return SharingPlatformInfo(
            name = trimmed,
            baseColor = baseColor,
            badgeBgColor = baseColor,
            badgeTextColor = textColor
        )
    }

    val list: List<SharingPlatformInfo>
        get() = defaultList.map { fromEntity(it) }
}


```


## ARCHIVO: `app/src/main/java/com/example/data/remote/FirebaseAuthService.kt`

```kotlin
package com.example.data.remote

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.local.MemberEntity
import com.example.data.local.SubscriptionDao
import com.example.data.local.SubscriptionEntity
import com.example.data.local.SubscriptionWithMembers
import com.example.ui.util.ImageStorageHelper
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class FirebaseAuthService(
    private val context: Context,
    private val dao: SubscriptionDao,
    private val scope: CoroutineScope
) {
    private val auth: FirebaseAuth? by lazy {
        try {
            ensureFirebaseInitialized(context)
            FirebaseAuth.getInstance()
        } catch (t: Throwable) {
            Log.w("FirebaseAuthService", "Could not initialize FirebaseAuth: ${t.message}")
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            ensureFirebaseInitialized(context)
            FirebaseFirestore.getInstance()
        } catch (t: Throwable) {
            Log.w("FirebaseAuthService", "Could not initialize Firestore: ${t.message}")
            null
        }
    }

    private val credentialManager: CredentialManager? by lazy {
        try {
            CredentialManager.create(context)
        } catch (t: Throwable) {
            Log.w("FirebaseAuthService", "CredentialManager create: ${t.message}")
            null
        }
    }

    private val _authState = MutableStateFlow<AuthState>(
        try {
            auth?.currentUser?.let { AuthState.Authenticated(it) } ?: AuthState.Idle
        } catch (_: Throwable) {
            AuthState.Idle
        }
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val activeListeners = mutableListOf<ListenerRegistration>()

    private fun removeActiveListeners() {
        activeListeners.forEach { try { it.remove() } catch (_: Throwable) {} }
        activeListeners.clear()
    }

    init {
        try {
            auth?.addAuthStateListener { firebaseAuth ->
                try {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        _authState.value = AuthState.Authenticated(user)
                        setupFirestoreRealtimeSync(user.uid)
                        // Sincronización automática de datos locales a la nube al autenticar
                        scope.launch(Dispatchers.IO) {
                            try {
                                syncToCloud()
                            } catch (e: Exception) {
                                Log.w("FirebaseAuthService", "Auto-sync error on auth state change: ${e.message}")
                            }
                        }
                    } else {
                        _authState.value = AuthState.Idle
                        removeActiveListeners()
                    }
                } catch (t: Throwable) {
                    Log.w("FirebaseAuthService", "Auth state change error: ${t.message}")
                }
            }
        } catch (t: Throwable) {
            Log.w("FirebaseAuthService", "Auth state listener init: ${t.message}")
        }
    }

    val currentUser: FirebaseUser? get() = try { auth?.currentUser } catch (_: Throwable) { null }

    private fun ensureFirebaseInitialized(ctx: Context) {
        try {
            if (com.google.firebase.FirebaseApp.getApps(ctx).isEmpty()) {
                val init = try { com.google.firebase.FirebaseApp.initializeApp(ctx) } catch (_: Throwable) { null }
                if (init == null) {
                    val options = com.google.firebase.FirebaseOptions.Builder()
                        .setApplicationId("1:345750792662:android:7123300d38bec1521f1c77")
                        .setApiKey("AIzaSyD5S5DI2FSp-LHWmdEhId-5zGETcrqsm78")
                        .setProjectId("splitzy-8ceb1")
                        .setStorageBucket("splitzy-8ceb1.firebasestorage.app")
                        .build()
                    com.google.firebase.FirebaseApp.initializeApp(ctx, options)
                }
            }
        } catch (t: Throwable) {
            Log.w("FirebaseAuthService", "ensureFirebaseInitialized: ${t.message}")
        }
    }

    /**
     * Iniciar sesión con Google usando Credential Manager
     */
    suspend fun signInWithGoogle(): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading
        try {
            val serverClientId = getWebClientId() ?: "345750792662-6bel6qbkrkmgcodbq5qvrpaiughmv66f.apps.googleusercontent.com"

            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .addCredentialOption(googleIdOption)
                .build()

            val cm = credentialManager ?: throw IllegalStateException("El gestor de credenciales no está disponible en este dispositivo")
            val response: GetCredentialResponse = cm.getCredential(
                request = request,
                context = context
            )

            when (val credential = response.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                        val authInst = auth ?: throw IllegalStateException("Servicio de autenticación no inicializado")
                        val authResult = authInst.signInWithCredential(authCredential).await()
                        val user = authResult.user ?: throw IllegalStateException("Usuario no disponible")
                        _authState.value = AuthState.Authenticated(user)
                        
                        // Sincronizar datos de la nube
                        syncFromCloud()
                        Result.success(user)
                    } else {
                        val err = "Tipo de credencial no soportado: ${credential.type}"
                        _authState.value = AuthState.Error(err)
                        Result.failure(Exception(err))
                    }
                }
                else -> {
                    val err = "Tipo de credencial desconocido"
                    _authState.value = AuthState.Error(err)
                    Result.failure(Exception(err))
                }
            }
        } catch (e: GetCredentialCancellationException) {
            _authState.value = auth?.currentUser?.let { AuthState.Authenticated(it) } ?: AuthState.Idle
            Result.failure(e)
        } catch (e: GetCredentialException) {
            val rawMsg = e.localizedMessage ?: ""
            val userFriendlyMsg = when {
                rawMsg.contains("No credentials available", ignoreCase = true) || 
                rawMsg.contains("No credential", ignoreCase = true) -> 
                    "No se encontró una sesión de Google vinculada automáticamente. Puedes registrarte o iniciar sesión escribiendo tu Correo y Contraseña aquí abajo."
                else -> "Error de Google: $rawMsg"
            }
            _authState.value = AuthState.Error(userFriendlyMsg)
            Result.failure(e)
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Error de autenticación"
            _authState.value = AuthState.Error(msg)
            Result.failure(e)
        }
    }

    /**
     * Registro con correo y contraseña
     */
    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading
        try {
            val authInst = auth ?: throw IllegalStateException("Servicio de autenticación no inicializado")
            val authResult = authInst.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw IllegalStateException("Usuario no disponible")
            _authState.value = AuthState.Authenticated(user)
            
            // Subir datos locales a la nueva cuenta
            syncToCloud()
            Result.success(user)
        } catch (e: Exception) {
            val msg = parseAuthErrorMessage(e)
            _authState.value = AuthState.Error(msg)
            Result.failure(Exception(msg))
        }
    }

    /**
     * Inicio de sesión con correo y contraseña
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading
        try {
            val authInst = auth ?: throw IllegalStateException("Servicio de autenticación no inicializado")
            val authResult = authInst.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw IllegalStateException("Usuario no disponible")
            _authState.value = AuthState.Authenticated(user)
            
            // Sincronizar datos al iniciar sesión
            syncFromCloud()
            Result.success(user)
        } catch (e: Exception) {
            val msg = parseAuthErrorMessage(e)
            _authState.value = AuthState.Error(msg)
            Result.failure(Exception(msg))
        }
    }

    /**
     * Cierre de sesión (limpia los datos locales para proteger la privacidad)
     */
    suspend fun signOut(clearLocalData: Boolean = true) = withContext(Dispatchers.IO) {
        try {
            removeActiveListeners()
            if (clearLocalData) {
                dao.deleteAllMembers()
                dao.deleteAllSubscriptions()
            }
            auth?.signOut()
            try {
                credentialManager?.clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {}
            _authState.value = AuthState.Idle
            _syncStatus.value = null
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Error signing out", e)
        }
    }

    fun clearError() {
        _authState.value = auth?.currentUser?.let { AuthState.Authenticated(it) } ?: AuthState.Idle
    }

    fun clearSyncStatus() {
        _syncStatus.value = null
    }

    /**
     * Sube todos los datos locales a Firestore (Multidispositivo y Web - Multi-ruta)
     */
    suspend fun syncToCloud(): Boolean = withContext(Dispatchers.IO) {
        val user = auth?.currentUser ?: return@withContext false
        val db = firestore ?: return@withContext false
        _isSyncing.value = true
        try {
            val subs = dao.getAllSubscriptionsDirect()
            val members = dao.getAllMembersDirect()

            val membersBySubId = members.groupBy { it.subscriptionId }

            val batch = db.batch()
            val userSubsCollection = db.collection("users").document(user.uid).collection("subscriptions")
            val rootSubsCollection = db.collection("subscriptions")
            val now = System.currentTimeMillis()

            for (sub in subs) {
                val imageBase64 = if (sub.iconType == "CUSTOM_IMAGE" && sub.customImageUri.isNotBlank()) {
                    ImageStorageHelper.imageToBase64(context, sub.customImageUri) ?: ""
                } else {
                    ""
                }

                val memberList = (membersBySubId[sub.id] ?: emptyList()).map { m ->
                    FirebaseMemberDto(
                        id = m.id,
                        memberName = m.memberName,
                        sharingPlatform = m.sharingPlatform,
                        memberContact = m.memberContact,
                        joinedDate = m.joinedDate,
                        contributionAmount = m.contributionAmount,
                        isPaidThisMonth = m.isPaidThisMonth,
                        isPendingPayment = m.isPendingPayment,
                        isPendingRemoval = m.isPendingRemoval,
                        isPendingRegistration = m.isPendingRegistration,
                        notes = m.notes
                    )
                }

                val subDto = FirebaseSubscriptionDto(
                    id = sub.id,
                    platformName = sub.platformName,
                    customPlanName = sub.customPlanName,
                    mainUserName = sub.mainUserName,
                    mainUserContact = sub.mainUserContact,
                    cost = sub.cost,
                    billingPeriod = sub.billingPeriod,
                    billingDay = sub.billingDay,
                    billingMonth = sub.billingMonth,
                    currency = sub.currency,
                    defaultContributionPerUser = sub.defaultContributionPerUser,
                    platformPricing = sub.platformPricing,
                    category = sub.category,
                    notes = sub.notes,
                    iconType = sub.iconType,
                    iconKey = sub.iconKey,
                    customImageUri = sub.customImageUri,
                    customImageBase64 = imageBase64,
                    iconColorHex = sub.iconColorHex,
                    createdAt = sub.createdAt,
                    updatedAt = now,
                    userId = user.uid,
                    members = memberList
                )

                // Escribir en users/{uid}/subscriptions/{sub.id}
                val userDocRef = userSubsCollection.document(sub.id.toString())
                batch.set(userDocRef, subDto, SetOptions.merge())

                // Escribir en subscriptions/{sub.id} (multi-ruta)
                val rootDocRef = rootSubsCollection.document(sub.id.toString())
                val rootData = mutableMapOf<String, Any>(
                    "id" to sub.id,
                    "platformName" to sub.platformName,
                    "customPlanName" to sub.customPlanName,
                    "mainUserName" to sub.mainUserName,
                    "mainUserContact" to sub.mainUserContact,
                    "cost" to sub.cost,
                    "billingPeriod" to sub.billingPeriod,
                    "billingDay" to sub.billingDay,
                    "billingMonth" to sub.billingMonth,
                    "currency" to sub.currency,
                    "defaultContributionPerUser" to sub.defaultContributionPerUser,
                    "platformPricing" to sub.platformPricing,
                    "category" to sub.category,
                    "notes" to sub.notes,
                    "iconType" to sub.iconType,
                    "iconKey" to sub.iconKey,
                    "customImageUri" to sub.customImageUri,
                    "customImageBase64" to imageBase64,
                    "iconColorHex" to sub.iconColorHex,
                    "createdAt" to sub.createdAt,
                    "updatedAt" to now,
                    "updated_at" to now,
                    "updatedAtMs" to now,
                    "userId" to user.uid,
                    "user_id" to user.uid,
                    "members" to memberList
                )
                batch.set(rootDocRef, rootData, SetOptions.merge())

                // Escribir también cada miembro en la subcolección 'members'
                for (m in (membersBySubId[sub.id] ?: emptyList())) {
                    val mData = mapOf(
                        "id" to m.id,
                        "subscriptionId" to sub.id,
                        "memberName" to m.memberName,
                        "name" to m.memberName,
                        "sharingPlatform" to m.sharingPlatform,
                        "platform" to m.sharingPlatform,
                        "memberContact" to m.memberContact,
                        "contact" to m.memberContact,
                        "joinedDate" to m.joinedDate,
                        "contributionAmount" to m.contributionAmount,
                        "amount" to m.contributionAmount,
                        "isPaidThisMonth" to m.isPaidThisMonth,
                        "paidThisMonth" to m.isPaidThisMonth,
                        "paid" to m.isPaidThisMonth,
                        "isPendingPayment" to m.isPendingPayment,
                        "pendingPayment" to m.isPendingPayment,
                        "isPendingRemoval" to m.isPendingRemoval,
                        "pendingRemoval" to m.isPendingRemoval,
                        "isPendingRegistration" to m.isPendingRegistration,
                        "pendingRegistration" to m.isPendingRegistration,
                        "notes" to m.notes
                    )
                    val uMemRef = userSubsCollection.document(sub.id.toString()).collection("members").document(m.id.toString())
                    val rMemRef = rootSubsCollection.document(sub.id.toString()).collection("members").document(m.id.toString())
                    batch.set(uMemRef, mData, SetOptions.merge())
                    batch.set(rMemRef, mData, SetOptions.merge())
                }
            }

            batch.commit().await()
            _syncStatus.value = "✅ Datos sincronizados con la nube"
            _isSyncing.value = false
            true
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Error syncing to cloud", e)
            val msg = e.localizedMessage ?: "Error desconocido"
            val userFriendly = if (msg.contains("PERMISSION_DENIED", ignoreCase = true) || msg.contains("insufficient permissions", ignoreCase = true)) {
                "⚠️ Permiso denegado en Firestore: Configura las Reglas (Rules) en tu consola de Firebase."
            } else {
                "⚠️ Error al sincronizar con la nube: $msg"
            }
            _syncStatus.value = userFriendly
            _isSyncing.value = false
            false
        }
    }

    /**
     * Elimina una suscripción de Firestore en todas las rutas cuando el usuario la borra en local
     */
    suspend fun deleteSubscriptionFromCloud(subscriptionId: Long) = withContext(Dispatchers.IO) {
        val user = auth?.currentUser ?: return@withContext
        val db = firestore ?: return@withContext
        try {
            db.collection("users").document(user.uid)
                .collection("subscriptions").document(subscriptionId.toString())
                .delete().await()
        } catch (e: Exception) {
            Log.w("FirebaseAuthService", "Error deleting sub from user collection: ${e.message}")
        }
        try {
            db.collection("subscriptions").document(subscriptionId.toString())
                .delete().await()
        } catch (e: Exception) {
            Log.w("FirebaseAuthService", "Error deleting sub from root collection: ${e.message}")
        }
    }

    /**
     * Conversor seguro a booleano tolerando booleanos nativos, números enteros (0/1) y cadenas de texto
     */
    private fun parseSafeBoolean(value: Any?): Boolean {
        return when (value) {
            is Boolean -> value
            is Number -> value.toInt() != 0
            is String -> {
                val clean = value.trim().lowercase()
                clean == "true" || clean == "1" || clean == "yes" || clean == "si" || clean == "sí" || clean == "t"
            }
            else -> false
        }
    }

    /**
     * Extrae el timestamp de actualización más reciente disponible en el documento
     */
    private fun extractUpdatedAtTimestamp(data: Map<String, Any?>, dto: FirebaseSubscriptionDto?): Long {
        val candidates = listOfNotNull(
            (data["updatedAt"] as? Number)?.toLong(),
            (data["updated_at"] as? Number)?.toLong(),
            (data["updatedAtMs"] as? Number)?.toLong(),
            (data["lastModified"] as? Number)?.toLong(),
            dto?.updatedAt,
            (data["createdAt"] as? Number)?.toLong(),
            dto?.createdAt
        )
        return candidates.maxOrNull() ?: System.currentTimeMillis()
    }

    /**
     * Parsea un DocumentSnapshot de forma robusta tolerando diferentes convenciones de nombres de la web (JS/TS)
     * y consultando tanto el array 'members' como la subcolección 'members'
     */
    private suspend fun parseSubscriptionFromSnapshot(doc: com.google.firebase.firestore.DocumentSnapshot): Triple<SubscriptionEntity, List<MemberEntity>, Long>? {
        val data = doc.data ?: return null
        val dto = doc.toObject(FirebaseSubscriptionDto::class.java)

        val docIdAsLong = doc.id.toLongOrNull()
        val rawId = (data["id"] as? Number)?.toLong()
            ?: (data["id"] as? String)?.toLongOrNull()
            ?: dto?.id ?: 0L
        val effectiveId = when {
            rawId > 0 -> rawId
            docIdAsLong != null && docIdAsLong > 0 -> docIdAsLong
            else -> Math.abs(doc.id.hashCode().toLong().let { if (it == 0L) 1L else it })
        }

        val platformName = (data["platformName"] as? String)?.trim()
            ?: (data["name"] as? String)?.trim()
            ?: dto?.platformName.orEmpty()
        val customPlanName = (data["customPlanName"] as? String)?.trim()
            ?: (data["planName"] as? String)?.trim()
            ?: (data["plan"] as? String)?.trim()
            ?: dto?.customPlanName.orEmpty()
        val mainUserName = (data["mainUserName"] as? String)?.trim()
            ?: (data["ownerName"] as? String)?.trim()
            ?: (data["mainUser"] as? String)?.trim()
            ?: dto?.mainUserName.orEmpty()
        val mainUserContact = (data["mainUserContact"] as? String)?.trim()
            ?: (data["ownerContact"] as? String)?.trim()
            ?: dto?.mainUserContact.orEmpty()
        val cost = (data["cost"] as? Number)?.toDouble()
            ?: (data["price"] as? Number)?.toDouble()
            ?: (data["cost"] as? String)?.toDoubleOrNull()
            ?: dto?.cost ?: 0.0
        val billingPeriod = (data["billingPeriod"] as? String)?.trim()
            ?: (data["period"] as? String)?.trim()
            ?: dto?.billingPeriod ?: "MONTHLY"
        val billingDay = (data["billingDay"] as? Number)?.toInt()
            ?: (data["day"] as? Number)?.toInt()
            ?: dto?.billingDay ?: 1
        val billingMonth = (data["billingMonth"] as? Number)?.toInt()
            ?: (data["month"] as? Number)?.toInt()
            ?: dto?.billingMonth ?: 1
        val currency = (data["currency"] as? String)?.trim() ?: dto?.currency ?: "€"
        val defaultContributionPerUser = (data["defaultContributionPerUser"] as? Number)?.toDouble()
            ?: (data["defaultContribution"] as? Number)?.toDouble()
            ?: dto?.defaultContributionPerUser ?: 0.0
        val platformPricing = (data["platformPricing"] as? String)?.trim() ?: dto?.platformPricing.orEmpty()
        val category = (data["category"] as? String)?.trim() ?: dto?.category ?: "Streaming"
        val notes = (data["notes"] as? String)?.trim()
            ?: (data["note"] as? String)?.trim()
            ?: dto?.notes.orEmpty()
        val iconType = (data["iconType"] as? String)?.trim() ?: dto?.iconType ?: "PRESET"
        val iconKey = (data["iconKey"] as? String)?.trim() ?: dto?.iconKey ?: "Netflix"
        val customImageUri = (data["customImageUri"] as? String)?.trim() ?: dto?.customImageUri.orEmpty()
        val customImageBase64 = (data["customImageBase64"] as? String)?.trim() ?: dto?.customImageBase64.orEmpty()
        val iconColorHex = (data["iconColorHex"] as? String)?.trim() ?: dto?.iconColorHex ?: "#6366F1"
        val createdAt = (data["createdAt"] as? Number)?.toLong()
            ?: (data["created_at"] as? Number)?.toLong()
            ?: dto?.createdAt ?: System.currentTimeMillis()
        val updatedAt = extractUpdatedAtTimestamp(data, dto)

        var localCustomImageUri = customImageUri
        if (customImageBase64.isNotBlank()) {
            val restoredPath = ImageStorageHelper.saveBase64Image(context, customImageBase64, "cloud_$effectiveId")
            if (restoredPath != null) {
                localCustomImageUri = restoredPath
            }
        }

        val subEntity = SubscriptionEntity(
            id = effectiveId,
            platformName = platformName,
            customPlanName = customPlanName,
            mainUserName = mainUserName,
            mainUserContact = mainUserContact,
            cost = cost,
            billingPeriod = billingPeriod,
            billingDay = billingDay,
            billingMonth = billingMonth,
            currency = currency,
            defaultContributionPerUser = defaultContributionPerUser,
            platformPricing = platformPricing,
            category = category,
            notes = notes,
            iconType = iconType,
            iconKey = iconKey,
            customImageUri = localCustomImageUri,
            iconColorHex = iconColorHex,
            createdAt = if (createdAt > 0) createdAt else System.currentTimeMillis()
        )

        val memberEntitiesMap = mutableMapOf<String, MemberEntity>()

        fun addMemberFromMap(rawItem: Map<*, *>, index: Int) {
            val mName = (rawItem["memberName"] as? String)?.trim()
                ?: (rawItem["name"] as? String)?.trim()
                ?: (rawItem["userName"] as? String)?.trim()
                ?: (rawItem["username"] as? String)?.trim()
                ?: (rawItem["user"] as? String)?.trim()
                ?: (rawItem["member_name"] as? String)?.trim()
                ?: (rawItem["displayName"] as? String)?.trim()
                ?: ""

            if (mName.isBlank()) return

            val mId = (rawItem["id"] as? Number)?.toLong()
                ?: (rawItem["id"] as? String)?.toLongOrNull()
                ?: (rawItem["memberId"] as? Number)?.toLong()
                ?: (rawItem["memberId"] as? String)?.toLongOrNull()
                ?: (rawItem["member_id"] as? Number)?.toLong()
                ?: (rawItem["member_id"] as? String)?.toLongOrNull()
                ?: (effectiveId * 1000L + index + 1)

            val mSharing = (rawItem["sharingPlatform"] as? String)?.trim()
                ?: (rawItem["platform"] as? String)?.trim()
                ?: (rawItem["sharing_platform"] as? String)?.trim()
                ?: (rawItem["service"] as? String)?.trim()
                ?: "Sharesub"

            val mContact = (rawItem["memberContact"] as? String)?.trim()
                ?: (rawItem["contact"] as? String)?.trim()
                ?: (rawItem["email"] as? String)?.trim()
                ?: (rawItem["phone"] as? String)?.trim()
                ?: (rawItem["member_contact"] as? String)?.trim().orEmpty()

            val mJoinedDate = (rawItem["joinedDate"] as? Number)?.toLong()
                ?: (rawItem["joined_date"] as? Number)?.toLong()
                ?: (rawItem["date"] as? Number)?.toLong()
                ?: (rawItem["createdAt"] as? Number)?.toLong()
                ?: (rawItem["created_at"] as? Number)?.toLong()
                ?: System.currentTimeMillis()

            val mContribution = (rawItem["contributionAmount"] as? Number)?.toDouble()
                ?: (rawItem["amount"] as? Number)?.toDouble()
                ?: (rawItem["contribution"] as? Number)?.toDouble()
                ?: (rawItem["price"] as? Number)?.toDouble()
                ?: (rawItem["cost"] as? Number)?.toDouble()
                ?: (rawItem["contributionAmount"] as? String)?.toDoubleOrNull()
                ?: (rawItem["amount"] as? String)?.toDoubleOrNull()
                ?: 0.0

            val statusStr = (rawItem["status"] as? String)?.lowercase()?.trim() ?: ""
            val isPendingPayment = parseSafeBoolean(rawItem["isPendingPayment"]) ||
                    parseSafeBoolean(rawItem["pendingPayment"]) ||
                    parseSafeBoolean(rawItem["pending_payment"]) ||
                    statusStr == "pending_payment" || statusStr == "pending" || statusStr == "pendiente_pago" || statusStr == "pendiente"

            val isPendingRemoval = parseSafeBoolean(rawItem["isPendingRemoval"]) ||
                    parseSafeBoolean(rawItem["pendingRemoval"]) ||
                    parseSafeBoolean(rawItem["pending_removal"]) ||
                    parseSafeBoolean(rawItem["isPendingDelete"]) ||
                    parseSafeBoolean(rawItem["pendingDelete"]) ||
                    statusStr == "pending_removal" || statusStr == "removal" || statusStr == "baja" || statusStr == "pendiente_baja" || statusStr == "pendiente_eliminar"

            val isPendingRegistration = parseSafeBoolean(rawItem["isPendingRegistration"]) ||
                    parseSafeBoolean(rawItem["pendingRegistration"]) ||
                    parseSafeBoolean(rawItem["pending_registration"]) ||
                    parseSafeBoolean(rawItem["isPendingAdd"]) ||
                    parseSafeBoolean(rawItem["pendingAdd"]) ||
                    statusStr == "pending_registration" || statusStr == "registration" || statusStr == "alta" || statusStr == "pendiente_alta" || statusStr == "pendiente_dar_de_alta"

            val rawPaid = if (rawItem.containsKey("isPaidThisMonth")) {
                parseSafeBoolean(rawItem["isPaidThisMonth"])
            } else if (rawItem.containsKey("paidThisMonth")) {
                parseSafeBoolean(rawItem["paidThisMonth"])
            } else if (rawItem.containsKey("paid_this_month")) {
                parseSafeBoolean(rawItem["paid_this_month"])
            } else if (rawItem.containsKey("paid")) {
                parseSafeBoolean(rawItem["paid"])
            } else if (rawItem.containsKey("is_paid")) {
                parseSafeBoolean(rawItem["is_paid"])
            } else {
                statusStr == "paid" || statusStr == "al_dia" || statusStr == "active" || (!isPendingPayment && !isPendingRemoval && !isPendingRegistration)
            }

            val mNotes = (rawItem["notes"] as? String)?.trim() ?: (rawItem["note"] as? String)?.trim().orEmpty()

            val memberObj = MemberEntity(
                id = if (mId > 0) mId else (effectiveId * 1000L + index + 1),
                subscriptionId = effectiveId,
                memberName = mName,
                sharingPlatform = mSharing,
                memberContact = mContact,
                joinedDate = mJoinedDate,
                contributionAmount = mContribution,
                isPaidThisMonth = rawPaid && !isPendingPayment,
                isPendingPayment = isPendingPayment,
                isPendingRemoval = isPendingRemoval,
                isPendingRegistration = isPendingRegistration,
                notes = mNotes
            )

            val memberKey = mName.lowercase().trim()
            memberEntitiesMap[memberKey] = memberObj
        }

        // 1. Parsear del array 'members'
        val rawMembersList = (data["members"] as? List<*>)
        if (rawMembersList != null && rawMembersList.isNotEmpty()) {
            rawMembersList.forEachIndexed { index, rawItem ->
                if (rawItem is Map<*, *>) {
                    addMemberFromMap(rawItem, index)
                }
            }
        } else if (dto != null && dto.members.isNotEmpty()) {
            dto.members.forEachIndexed { index, mDto ->
                if (mDto.memberName.isNotBlank()) {
                    val memberId = if (mDto.id > 0) mDto.id else (effectiveId * 1000L + index + 1)
                    val memberObj = MemberEntity(
                        id = memberId,
                        subscriptionId = effectiveId,
                        memberName = mDto.memberName,
                        sharingPlatform = mDto.sharingPlatform,
                        memberContact = mDto.memberContact,
                        joinedDate = mDto.joinedDate,
                        contributionAmount = mDto.contributionAmount,
                        isPaidThisMonth = mDto.isPaidThisMonth && !mDto.isPendingPayment,
                        isPendingPayment = mDto.isPendingPayment,
                        isPendingRemoval = mDto.isPendingRemoval,
                        isPendingRegistration = mDto.isPendingRegistration,
                        notes = mDto.notes
                    )
                    memberEntitiesMap[mDto.memberName.lowercase().trim()] = memberObj
                }
            }
        }

        // 2. Consultar también la subcolección 'members' si existe para no perder ningún miembro
        try {
            val subColSnap = doc.reference.collection("members").get().await()
            if (subColSnap != null && !subColSnap.isEmpty) {
                subColSnap.documents.forEachIndexed { index, memDoc ->
                    val memData = memDoc.data
                    if (memData != null) {
                        addMemberFromMap(memData, index + 100)
                    }
                }
            }
        } catch (_: Exception) {}

        return Triple(subEntity, memberEntitiesMap.values.toList(), updatedAt)
    }

    /**
     * Procesa los documentos de Firestore e inserta/reemplaza de forma atómica en Room.
     * Fusiona miembros de todas las rutas para garantizar que nunca se pierda un usuario como Renato.
     */
    private suspend fun processSnapshotAndSyncToRoom(documents: List<com.google.firebase.firestore.DocumentSnapshot>) {
        val parsedTriples = mutableListOf<Triple<SubscriptionEntity, List<MemberEntity>, Long>>()

        for (doc in documents) {
            val item = parseSubscriptionFromSnapshot(doc) ?: continue
            parsedTriples.add(item)
        }

        if (parsedTriples.isEmpty()) return

        // Mapear por clave de suscripción (ID o plataforma+titular) y FUSIONAR miembros
        val mergedMap = mutableMapOf<String, Triple<SubscriptionEntity, MutableMap<String, MemberEntity>, Long>>()

        for (item in parsedTriples) {
            val sub = item.first
            val members = item.second
            val timestamp = item.third

            val subKey = if (sub.platformName.isNotBlank()) {
                "${sub.platformName.trim().lowercase()}_${sub.mainUserName.trim().lowercase()}"
            } else {
                sub.id.toString()
            }

            val existing = mergedMap[subKey]
            if (existing == null) {
                val memberMap = mutableMapOf<String, MemberEntity>()
                members.forEach { m -> memberMap[m.memberName.trim().lowercase()] = m }
                mergedMap[subKey] = Triple(sub, memberMap, timestamp)
            } else {
                val finalSub = if (timestamp >= existing.third) sub else existing.first
                val finalTimestamp = maxOf(timestamp, existing.third)
                val memberMap = existing.second

                // Fusionar miembros: incorporar todos y tomar la versión más reciente
                members.forEach { m ->
                    val mKey = m.memberName.trim().lowercase()
                    if (!memberMap.containsKey(mKey) || timestamp >= existing.third) {
                        memberMap[mKey] = m
                    }
                }

                mergedMap[subKey] = Triple(finalSub, memberMap, finalTimestamp)
            }
        }

        val finalItemsToSync = mergedMap.values.map { triple ->
            val sub = triple.first
            val membersList = triple.second.values.mapIndexed { idx, m ->
                m.copy(subscriptionId = sub.id, id = if (m.id > 0) m.id else (sub.id * 1000L + idx + 1))
            }
            sub to membersList
        }

        dao.replaceAllSubscriptionsAndMembers(finalItemsToSync)
    }

    /**
     * Descarga y fusiona las suscripciones de Firestore a la base de datos local consultando ambas rutas
     */
    suspend fun syncFromCloud(): Boolean = withContext(Dispatchers.IO) {
        val user = auth?.currentUser ?: return@withContext false
        val db = firestore ?: return@withContext false
        _isSyncing.value = true
        try {
            val allDocs = mutableListOf<com.google.firebase.firestore.DocumentSnapshot>()

            // 1. Ruta users/{uid}/subscriptions
            try {
                val userSnap = db.collection("users").document(user.uid).collection("subscriptions").get().await()
                allDocs.addAll(userSnap.documents)
            } catch (e: Exception) {
                Log.w("FirebaseAuthService", "Error reading user subs: ${e.message}")
            }

            // 2. Ruta subscriptions (userId == uid)
            try {
                val rootSnap = db.collection("subscriptions").whereEqualTo("userId", user.uid).get().await()
                allDocs.addAll(rootSnap.documents)
            } catch (_: Exception) {}

            // 3. Ruta subscriptions (user_id == uid)
            try {
                val rootSnapSnake = db.collection("subscriptions").whereEqualTo("user_id", user.uid).get().await()
                allDocs.addAll(rootSnapSnake.documents)
            } catch (_: Exception) {}

            if (allDocs.isNotEmpty()) {
                processSnapshotAndSyncToRoom(allDocs)
                _syncStatus.value = "✅ Suscripciones actualizadas desde la nube"
            }
            _isSyncing.value = false
            true
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Error syncing from cloud", e)
            val msg = e.localizedMessage ?: "Error desconocido"
            val userFriendly = if (msg.contains("PERMISSION_DENIED", ignoreCase = true) || msg.contains("insufficient permissions", ignoreCase = true)) {
                "⚠️ Permiso denegado en Firestore: Revisa las Reglas en Firebase Console."
            } else {
                "⚠️ Error al descargar de la nube: $msg"
            }
            _syncStatus.value = userFriendly
            _isSyncing.value = false
            false
        }
    }

    /**
     * Escucha en tiempo real cambios desde ambas rutas (users/{uid}/subscriptions y subscriptions)
     */
    private fun setupFirestoreRealtimeSync(uid: String) {
        val db = firestore ?: return
        removeActiveListeners()

        // 1. Listener en users/{uid}/subscriptions
        val l1 = db.collection("users").document(uid).collection("subscriptions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirebaseAuthService", "User subs listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty && !snapshot.metadata.hasPendingWrites()) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            processSnapshotAndSyncToRoom(snapshot.documents)
                        } catch (e: Exception) {
                            Log.e("FirebaseAuthService", "Error processing user subs snapshot", e)
                        }
                    }
                }
            }
        activeListeners.add(l1)

        // 2. Listener en subscriptions con userId
        val l2 = db.collection("subscriptions").whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirebaseAuthService", "Root subs (userId) listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty && !snapshot.metadata.hasPendingWrites()) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            processSnapshotAndSyncToRoom(snapshot.documents)
                        } catch (e: Exception) {
                            Log.e("FirebaseAuthService", "Error processing root subs snapshot", e)
                        }
                    }
                }
            }
        activeListeners.add(l2)

        // 3. Listener en subscriptions con user_id
        val l3 = db.collection("subscriptions").whereEqualTo("user_id", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirebaseAuthService", "Root subs (user_id) listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty && !snapshot.metadata.hasPendingWrites()) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            processSnapshotAndSyncToRoom(snapshot.documents)
                        } catch (e: Exception) {
                            Log.e("FirebaseAuthService", "Error processing root snake subs snapshot", e)
                        }
                    }
                }
            }
        activeListeners.add(l3)
    }

    private fun getWebClientId(): String? {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else "345750792662-6bel6qbkrkmgcodbq5qvrpaiughmv66f.apps.googleusercontent.com"
        } catch (_: Exception) {
            "345750792662-6bel6qbkrkmgcodbq5qvrpaiughmv66f.apps.googleusercontent.com"
        }
    }

    private fun parseAuthErrorMessage(e: Exception): String {
        val msg = e.localizedMessage ?: ""
        return when {
            msg.contains("The email address is already in use", ignoreCase = true) -> "Este correo electrónico ya está registrado. Inicia sesión directamente."
            msg.contains("The email address is badly formatted", ignoreCase = true) -> "El formato del correo electrónico no es válido."
            msg.contains("The password is invalid or the user does not have a password", ignoreCase = true) ||
            msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            msg.contains("wrong-password", ignoreCase = true) ||
            msg.contains("user-not-found", ignoreCase = true) -> "Correo o contraseña incorrectos."
            msg.contains("Password should be at least 6 characters", ignoreCase = true) -> "La contraseña debe tener al menos 6 caracteres."
            msg.contains("A network error", ignoreCase = true) -> "Error de red. Comprueba tu conexión a Internet."
            else -> msg.ifBlank { "Ha ocurrido un error en la autenticación." }
        }
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/data/remote/FirebaseSubscriptionDto.kt`

```kotlin
package com.example.data.remote

data class FirebaseSubscriptionDto(
    val id: Long = 0L,
    val platformName: String = "",
    val customPlanName: String = "",
    val mainUserName: String = "",
    val mainUserContact: String = "",
    val cost: Double = 0.0,
    val billingPeriod: String = "MONTHLY",
    val billingDay: Int = 1,
    val billingMonth: Int = 1,
    val currency: String = "€",
    val defaultContributionPerUser: Double = 0.0,
    val platformPricing: String = "",
    val category: String = "Streaming",
    val notes: String = "",
    val iconType: String = "PRESET",
    val iconKey: String = "Netflix",
    val customImageUri: String = "",
    val customImageBase64: String = "",
    val iconColorHex: String = "#6366F1",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val userId: String = "",
    val members: List<FirebaseMemberDto> = emptyList()
)

data class FirebaseMemberDto(
    val id: Long = 0L,
    val memberName: String = "",
    val sharingPlatform: String = "Sharesub",
    val memberContact: String = "",
    val joinedDate: Long = System.currentTimeMillis(),
    val contributionAmount: Double = 0.0,
    val isPaidThisMonth: Boolean = true,
    val isPendingPayment: Boolean = false,
    val isPendingRemoval: Boolean = false,
    val isPendingRegistration: Boolean = false,
    val notes: String = ""
)

```


## ARCHIVO: `app/src/main/java/com/example/data/repository/SubscriptionRepository.kt`

```kotlin
package com.example.data.repository

import com.example.data.local.MemberEntity
import com.example.data.local.SharingPlatformEntity
import com.example.data.local.SubscriptionDao
import com.example.data.local.SubscriptionEntity
import com.example.data.local.SubscriptionWithMembers
import com.example.data.model.SharingPlatforms
import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(private val dao: SubscriptionDao) {

    val allSubscriptions: Flow<List<SubscriptionWithMembers>> = dao.getAllSubscriptionsWithMembers()

    fun getSubscriptionById(id: Long): Flow<SubscriptionWithMembers?> = dao.getSubscriptionWithMembersById(id)

    suspend fun insertSubscription(subscription: SubscriptionEntity): Long = dao.insertSubscription(subscription)

    suspend fun updateSubscription(subscription: SubscriptionEntity) = dao.updateSubscription(subscription)

    suspend fun deleteSubscription(subscription: SubscriptionEntity) = dao.deleteSubscription(subscription)

    suspend fun deleteSubscriptionById(id: Long) = dao.deleteSubscriptionById(id)

    suspend fun insertMember(member: MemberEntity): Long = dao.insertMember(member)

    suspend fun updateMember(member: MemberEntity) = dao.updateMember(member)

    suspend fun deleteMember(member: MemberEntity) = dao.deleteMember(member)

    suspend fun deleteMemberById(id: Long) = dao.deleteMemberById(id)

    suspend fun toggleMemberPayment(memberId: Long, isPaid: Boolean) {
        dao.updateMemberPaymentStatus(memberId = memberId, isPaid = isPaid, isPendingPayment = !isPaid)
    }

    suspend fun toggleMemberPendingPayment(memberId: Long, isPending: Boolean) {
        dao.updateMemberPendingPayment(memberId = memberId, isPending = isPending, isPaid = !isPending)
    }

    suspend fun toggleMemberPendingRemoval(memberId: Long, isPending: Boolean) {
        dao.updateMemberPendingRemoval(memberId = memberId, isPending = isPending)
    }

    suspend fun toggleMemberPendingRegistration(memberId: Long, isPending: Boolean) {
        dao.updateMemberPendingRegistration(memberId = memberId, isPending = isPending)
    }

    suspend fun getAllSubscriptionsDirect(): List<SubscriptionEntity> = dao.getAllSubscriptionsDirect()

    suspend fun getAllMembersDirect(): List<MemberEntity> = dao.getAllMembersDirect()

    // Sharing Platforms Repository methods
    val allSharingPlatforms: Flow<List<SharingPlatformEntity>> = dao.getAllSharingPlatforms()

    suspend fun ensureDefaultPlatformsSeeded() {
        val count = dao.getSharingPlatformCount()
        if (count == 0) {
            val initialList = SharingPlatforms.defaultList.map {
                it.copy(id = 0)
            }
            dao.insertSharingPlatforms(initialList)
        }
    }

    suspend fun insertSharingPlatform(platform: SharingPlatformEntity): Long = dao.insertSharingPlatform(platform)

    suspend fun updateSharingPlatform(platform: SharingPlatformEntity) = dao.updateSharingPlatform(platform)

    suspend fun deleteSharingPlatform(platform: SharingPlatformEntity) = dao.deleteSharingPlatform(platform)

    suspend fun deleteSharingPlatformById(id: Long) = dao.deleteSharingPlatformById(id)

    suspend fun getAllSharingPlatformsDirect(): List<SharingPlatformEntity> = dao.getAllSharingPlatformsDirect()

    val rawDao: SubscriptionDao get() = dao
}


```


## ARCHIVO: `app/src/main/java/com/example/data/util/BackupManager.kt`

```kotlin
package com.example.data.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.MemberEntity
import com.example.data.local.SubscriptionDao
import com.example.data.local.SubscriptionEntity
import com.example.ui.util.ImageStorageHelper
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupPreview(
    val exportDate: String,
    val subscriptionsCount: Int,
    val membersCount: Int,
    val sampleSubscriptions: List<String>,
    val isValid: Boolean,
    val errorMessage: String? = null
)

data class RestoreResult(
    val success: Boolean,
    val subscriptionsRestored: Int = 0,
    val membersRestored: Int = 0,
    val errorMessage: String? = null
)

object BackupManager {

    fun generateBackupJson(
        context: Context? = null,
        subscriptions: List<SubscriptionEntity>,
        members: List<MemberEntity>
    ): String {
        val root = JSONObject()
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        root.put("version", 2)
        root.put("appName", "GestorSuscripciones")
        root.put("timestamp", now)
        root.put("exportDate", dateFormat.format(Date(now)))
        root.put("subscriptionsCount", subscriptions.size)
        root.put("membersCount", members.size)

        val subsArray = JSONArray()
        for (sub in subscriptions) {
            val subObj = JSONObject().apply {
                put("id", sub.id)
                put("platformName", sub.platformName)
                put("customPlanName", sub.customPlanName)
                put("mainUserName", sub.mainUserName)
                put("mainUserContact", sub.mainUserContact)
                put("cost", sub.cost)
                put("billingPeriod", sub.billingPeriod)
                put("billingDay", sub.billingDay)
                put("billingMonth", sub.billingMonth)
                put("currency", sub.currency)
                put("defaultContributionPerUser", sub.defaultContributionPerUser)
                put("platformPricing", sub.platformPricing)
                put("category", sub.category)
                put("notes", sub.notes)
                put("iconType", sub.iconType)
                put("iconKey", sub.iconKey)
                put("customImageUri", sub.customImageUri)

                // Si tiene un icono personalizado subido por el usuario, incluirlo en Base64 optimizado
                if (sub.iconType == "CUSTOM_IMAGE" && sub.customImageUri.isNotBlank() && context != null) {
                    val base64 = ImageStorageHelper.imageToBase64(context, sub.customImageUri)
                    if (!base64.isNullOrBlank()) {
                        put("customImageBase64", base64)
                    }
                }

                put("iconColorHex", sub.iconColorHex)
                put("createdAt", sub.createdAt)
            }
            subsArray.put(subObj)
        }
        root.put("subscriptions", subsArray)

        val membersArray = JSONArray()
        for (member in members) {
            val memObj = JSONObject().apply {
                put("id", member.id)
                put("subscriptionId", member.subscriptionId)
                put("memberName", member.memberName)
                put("sharingPlatform", member.sharingPlatform)
                put("memberContact", member.memberContact)
                put("joinedDate", member.joinedDate)
                put("contributionAmount", member.contributionAmount)
                put("isPaidThisMonth", member.isPaidThisMonth)
                put("isPendingPayment", member.isPendingPayment)
                put("isPendingRemoval", member.isPendingRemoval)
                put("isPendingRegistration", member.isPendingRegistration)
                put("notes", member.notes)
            }
            membersArray.put(memObj)
        }
        root.put("members", membersArray)

        return root.toString(2)
    }

    fun parseBackupPreview(jsonString: String): BackupPreview {
        return try {
            val root = JSONObject(jsonString)
            val exportDate = root.optString("exportDate", "Fecha desconocida")
            val subsArray = root.optJSONArray("subscriptions") ?: JSONArray()
            val membersArray = root.optJSONArray("members") ?: JSONArray()

            val namesList = mutableListOf<String>()
            for (i in 0 until minOf(5, subsArray.length())) {
                val item = subsArray.getJSONObject(i)
                val name = item.optString("platformName", "Suscripción")
                namesList.add(name)
            }

            BackupPreview(
                exportDate = exportDate,
                subscriptionsCount = subsArray.length(),
                membersCount = membersArray.length(),
                sampleSubscriptions = namesList,
                isValid = true
            )
        } catch (e: Exception) {
            BackupPreview(
                exportDate = "",
                subscriptionsCount = 0,
                membersCount = 0,
                sampleSubscriptions = emptyList(),
                isValid = false,
                errorMessage = e.localizedMessage ?: "Archivo de copia de seguridad no válido o dañado"
            )
        }
    }

    suspend fun restoreFromJson(
        context: Context? = null,
        jsonString: String,
        dao: SubscriptionDao,
        replaceExisting: Boolean
    ): RestoreResult {
        return try {
            val root = JSONObject(jsonString)
            val subsArray = root.optJSONArray("subscriptions")
                ?: return RestoreResult(false, errorMessage = "Formato de archivo inválido (sin suscripciones)")
            val membersArray = root.optJSONArray("members") ?: JSONArray()

            if (replaceExisting) {
                dao.deleteAllMembers()
                dao.deleteAllSubscriptions()
            }

            val oldToNewSubId = mutableMapOf<Long, Long>()
            var subsRestored = 0
            var membersRestored = 0

            // 1. Insert Subscriptions
            for (i in 0 until subsArray.length()) {
                val obj = subsArray.getJSONObject(i)
                val oldId = obj.optLong("id", 0L)
                val rawImageUri = obj.optString("customImageUri", "")
                val base64Image = obj.optString("customImageBase64", "")

                // Si viene la imagen en Base64, restaurarla en el almacenamiento interno de la app
                var finalCustomImageUri = rawImageUri
                if (base64Image.isNotBlank() && context != null) {
                    val restoredPath = ImageStorageHelper.saveBase64Image(context, base64Image, "restored_${oldId}")
                    if (restoredPath != null) {
                        finalCustomImageUri = restoredPath
                    }
                }

                val subEntity = SubscriptionEntity(
                    id = 0, // Auto-generate new primary key
                    platformName = obj.optString("platformName", "Suscripción"),
                    customPlanName = obj.optString("customPlanName", ""),
                    mainUserName = obj.optString("mainUserName", "Titular"),
                    mainUserContact = obj.optString("mainUserContact", ""),
                    cost = obj.optDouble("cost", 0.0),
                    billingPeriod = obj.optString("billingPeriod", "MONTHLY"),
                    billingDay = obj.optInt("billingDay", 1),
                    billingMonth = obj.optInt("billingMonth", 1),
                    currency = obj.optString("currency", "EUR"),
                    defaultContributionPerUser = obj.optDouble("defaultContributionPerUser", 0.0),
                    platformPricing = obj.optString("platformPricing", ""),
                    category = obj.optString("category", "Streaming"),
                    notes = obj.optString("notes", ""),
                    iconType = obj.optString("iconType", "PRESET"),
                    iconKey = obj.optString("iconKey", "Netflix"),
                    customImageUri = finalCustomImageUri,
                    iconColorHex = obj.optString("iconColorHex", "#6366F1"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )

                val newSubId = dao.insertSubscription(subEntity)
                oldToNewSubId[oldId] = newSubId
                subsRestored++
            }

            // 2. Insert Members
            for (i in 0 until membersArray.length()) {
                val obj = membersArray.getJSONObject(i)
                val oldSubId = obj.optLong("subscriptionId", 0L)
                val newSubId = oldToNewSubId[oldSubId]

                if (newSubId != null) {
                    val memberEntity = MemberEntity(
                        id = 0, // Auto-generate new primary key
                        subscriptionId = newSubId,
                        memberName = obj.optString("memberName", "Usuario"),
                        sharingPlatform = obj.optString("sharingPlatform", "Sharesub"),
                        memberContact = obj.optString("memberContact", ""),
                        joinedDate = obj.optLong("joinedDate", System.currentTimeMillis()),
                        contributionAmount = obj.optDouble("contributionAmount", 0.0),
                        isPaidThisMonth = obj.optBoolean("isPaidThisMonth", true),
                        isPendingPayment = obj.optBoolean("isPendingPayment", false),
                        isPendingRemoval = obj.optBoolean("isPendingRemoval", false),
                        isPendingRegistration = obj.optBoolean("isPendingRegistration", false),
                        notes = obj.optString("notes", "")
                    )
                    dao.insertMember(memberEntity)
                    membersRestored++
                }
            }

            RestoreResult(
                success = true,
                subscriptionsRestored = subsRestored,
                membersRestored = membersRestored
            )
        } catch (e: Exception) {
            RestoreResult(
                success = false,
                errorMessage = e.localizedMessage ?: "Error al procesar la copia de seguridad"
            )
        }
    }

    fun writeBackupToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(content)
                    writer.flush()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun readBackupFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createShareableFile(context: Context, content: String): Uri? {
        return try {
            val backupDir = File(context.cacheDir, "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(backupDir, "backup_suscripciones_$timeStamp.json")
            file.writeText(content)

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/data/util/CurrencyRateService.kt`

```kotlin
package com.example.data.util

import android.util.Log
import com.example.data.model.CurrencyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object CurrencyRateService {

    private const val TAG = "CurrencyRateService"
    private const val API_URL = "https://open.er-api.com/v6/latest/EUR"

    suspend fun fetchLatestRates(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val json = JSONObject(response.toString())
                if (json.has("rates")) {
                    val ratesObj = json.getJSONObject("rates")
                    val ratesMap = mutableMapOf<String, Double>()
                    val keys = ratesObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val rateValue = ratesObj.optDouble(key, 0.0)
                        if (rateValue > 0.0) {
                            ratesMap[key] = rateValue
                        }
                    }
                    CurrencyManager.updateRates(ratesMap)
                    Log.d(TAG, "Currency exchange rates successfully updated with ${ratesMap.size} currencies.")
                    return@withContext true
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not fetch live currency rates (using fallback rates): ${e.message}")
        }
        return@withContext false
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/data/util/ThemePreferences.kt`

```kotlin
package com.example.data.util

import android.content.Context
import android.content.SharedPreferences

enum class AppThemeMode(val title: String, val description: String) {
    SYSTEM("Usar configuración del sistema", "Sigue automáticamente el tema claro u oscuro de Android"),
    LIGHT("Claro", "Tema luminoso de alto contraste"),
    DARK("Oscuro", "Tema oscuro que reduce el brillo y ahorra batería")
}

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME_MODE = "key_selected_theme_mode"
    }

    fun getThemeMode(): AppThemeMode {
        val savedName = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        return try {
            AppThemeMode.valueOf(savedName ?: AppThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/components/AddEditMemberDialog.kt`

```kotlin
package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MemberEntity
import com.example.data.local.SharingPlatformEntity
import com.example.data.local.SubscriptionWithMembers
import com.example.data.model.CurrencyManager
import com.example.data.model.SharingPlatforms
import com.example.ui.theme.ProfitGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMemberDialog(
    targetSubscription: SubscriptionWithMembers,
    memberToEdit: MemberEntity?,
    onDismiss: () -> Unit,
    onSave: (MemberEntity) -> Unit,
    availablePlatforms: List<SharingPlatformEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val isEditing = memberToEdit != null
    val sub = targetSubscription.subscription
    val platformPrices = targetSubscription.platformPrices
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var memberName by remember {
        mutableStateOf(memberToEdit?.memberName ?: "")
    }
    var sharingPlatform by remember {
        mutableStateOf(
            memberToEdit?.sharingPlatform ?: (platformPrices.firstOrNull()?.platformName ?: "Sharesub")
        )
    }
    var contributionText by remember {
        mutableStateOf(
            if (memberToEdit != null) {
                String.format(Locale.US, "%.2f", memberToEdit.contributionAmount)
            } else {
                val matchingPrice = platformPrices.find { it.platformName.equals(sharingPlatform, ignoreCase = true) }?.pricePerUser
                if (matchingPrice != null && matchingPrice > 0.0) {
                    String.format(Locale.US, "%.2f", matchingPrice)
                } else if (sub.defaultContributionPerUser > 0.0) {
                    String.format(Locale.US, "%.2f", sub.defaultContributionPerUser)
                } else {
                    val split = targetSubscription.equalSplitPerPerson
                    String.format(Locale.US, "%.2f", split)
                }
            }
        )
    }
    var joinedDateTimestamp by remember {
        mutableLongStateOf(memberToEdit?.joinedDate ?: System.currentTimeMillis())
    }
    var memberContact by remember {
        mutableStateOf(memberToEdit?.memberContact ?: "")
    }

    // 3 Status Switches
    var isPendingPayment by remember {
        mutableStateOf(memberToEdit?.isPendingPayment ?: (if (memberToEdit != null) !memberToEdit.isPaidThisMonth else false))
    }
    var isPendingRemoval by remember {
        mutableStateOf(memberToEdit?.isPendingRemoval ?: false)
    }
    var isPendingRegistration by remember {
        mutableStateOf(memberToEdit?.isPendingRegistration ?: false)
    }

    var notes by remember {
        mutableStateOf(memberToEdit?.notes ?: "")
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var platformDropdownExpanded by remember { mutableStateOf(false) }
    var showCustomPlatformInput by remember { mutableStateOf(false) }
    var customPlatformInputText by remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = joinedDateTimestamp
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isEditing) "Editar Usuario" else "Añadir Usuario",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                // Subscription reference and Balance summary in 3 independent lines
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Balance de la suscripción: ${sub.platformName}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 0.8.dp
                        )

                        val curr = CurrencyManager.findCurrency(sub.currency)
                        val period = targetSubscription.billingPeriodObj

                        // Línea 1: Coste propio
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Coste propio",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.2f", sub.cost)} ${curr.symbol}${period.suffix}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (curr.code != "EUR") {
                                    val eurConvertedText = if (period != com.example.data.model.BillingPeriod.MONTHLY) {
                                        "≈ ${String.format(Locale.getDefault(), "%.2f", targetSubscription.myCostEur)} € (${String.format(Locale.getDefault(), "%.2f", targetSubscription.myCostMonthly)} €/mes)"
                                    } else {
                                        "≈ ${String.format(Locale.getDefault(), "%.2f", targetSubscription.myCostMonthly)} €/mes"
                                    }
                                    Text(
                                        text = eurConvertedText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else if (period != com.example.data.model.BillingPeriod.MONTHLY) {
                                    Text(
                                        text = "(${String.format(Locale.getDefault(), "%.2f", targetSubscription.myCostMonthly)} €/mes)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 0.8.dp
                        )

                        // Línea 2: Total que aportan por usuario / global
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total que aportan",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "+${String.format(Locale.getDefault(), "%.2f", targetSubscription.totalContributed)} €/mes",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = ProfitGreen
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 0.8.dp
                        )

                        // Línea 3: Número de miembros activos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Miembros activos",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${targetSubscription.members.size} miembros",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Member Name
                OutlinedTextField(
                    value = memberName,
                    onValueChange = {
                        memberName = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Nombre del usuario *") },
                    placeholder = { Text("Ej: Lucía García") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("El nombre es obligatorio") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_member_name"),
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Sharing Platform Selector (Desplegable)
                Text(
                    text = "Plataforma de compartición *",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = platformDropdownExpanded,
                    onExpandedChange = { platformDropdownExpanded = !platformDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentPlatformInfo = SharingPlatforms.getInfo(sharingPlatform, availablePlatforms)
                    OutlinedTextField(
                        value = sharingPlatform.ifBlank { "Seleccionar plataforma..." },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Plataforma") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(currentPlatformInfo.baseColor)
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformDropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("input_member_platform_dropdown"),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = platformDropdownExpanded,
                        onDismissRequest = { platformDropdownExpanded = false }
                    ) {
                        // 1. First show subscription's configured platforms if any
                        if (platformPrices.isNotEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "TARIFAS DE ESTA SUSCRIPCIÓN",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {},
                                enabled = false
                            )
                            platformPrices.forEach { pItem ->
                                val pInfo = SharingPlatforms.getInfo(pItem.platformName, availablePlatforms)
                                val isSelected = sharingPlatform.equals(pItem.platformName, ignoreCase = true)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = pItem.platformName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (pItem.pricePerUser > 0) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        text = "${String.format(Locale.US, "%.2f", pItem.pricePerUser)} €",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(pInfo.baseColor)
                                        )
                                    },
                                    trailingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        sharingPlatform = pItem.platformName
                                        if (pItem.pricePerUser > 0) {
                                            contributionText = String.format(Locale.US, "%.2f", pItem.pricePerUser)
                                        }
                                        platformDropdownExpanded = false
                                        showCustomPlatformInput = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "TODAS LAS PLATAFORMAS",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {},
                                enabled = false
                            )
                        }

                        // 2. Sharing platforms from settings or default list
                        val platformsToDisplay = if (availablePlatforms.isNotEmpty()) {
                            availablePlatforms.map { SharingPlatforms.fromEntity(it) }
                        } else {
                            SharingPlatforms.list
                        }

                        platformsToDisplay.forEach { platformInfo ->
                            val matchingConfiguredPrice = platformPrices.find { it.platformName.equals(platformInfo.name, ignoreCase = true) }?.pricePerUser
                            val isSelected = sharingPlatform.equals(platformInfo.name, ignoreCase = true)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = platformInfo.name,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (matchingConfiguredPrice != null && matchingConfiguredPrice > 0) {
                                            Text(
                                                text = "${String.format(Locale.US, "%.2f", matchingConfiguredPrice)} €",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(platformInfo.baseColor)
                                    )
                                },
                                trailingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                onClick = {
                                    sharingPlatform = platformInfo.name
                                    if (matchingConfiguredPrice != null && matchingConfiguredPrice > 0) {
                                        contributionText = String.format(Locale.US, "%.2f", matchingConfiguredPrice)
                                    }
                                    platformDropdownExpanded = false
                                    showCustomPlatformInput = false
                                }
                            )
                        }


                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("+ Otra plataforma personalizada...") },
                            onClick = {
                                platformDropdownExpanded = false
                                showCustomPlatformInput = true
                            }
                        )
                    }
                }

                if (showCustomPlatformInput) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customPlatformInputText,
                            onValueChange = { customPlatformInputText = it },
                            placeholder = { Text("Escribe el nombre de la plataforma...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (customPlatformInputText.isNotBlank()) {
                                    sharingPlatform = customPlatformInputText.trim()
                                    showCustomPlatformInput = false
                                    customPlatformInputText = ""
                                }
                            },
                            enabled = customPlatformInputText.isNotBlank(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Fijar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Money contributed by this user (dinero que aporta este usuario)
                OutlinedTextField(
                    value = contributionText,
                    onValueChange = {
                        contributionText = it
                        amountError = it.replace(',', '.').toDoubleOrNull() == null
                    },
                    label = { Text("Dinero que aporta (€/mes) *") },
                    placeholder = { Text("5.00") },
                    leadingIcon = { Icon(Icons.Default.Euro, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text("Introduce un importe numérico válido") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_member_contribution"),
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Split helpers in a clean horizontal Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (sub.defaultContributionPerUser > 0) {
                        OutlinedButton(
                            onClick = {
                                contributionText = String.format(Locale.US, "%.2f", sub.defaultContributionPerUser)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Cuota sugerida (${String.format(Locale.getDefault(), "%.2f", sub.defaultContributionPerUser)}€)",
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val split = targetSubscription.equalSplitPerPerson
                            contributionText = String.format(Locale.US, "%.2f", split)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Dividir a partes iguales",
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Joined Date Selector
                Text(
                    text = "Fecha de unión",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = dateFormat.format(Date(joinedDateTimestamp)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha de unión") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    trailingIcon = {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("Cambiar")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_member_joined_date"),
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick date shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = { joinedDateTimestamp = System.currentTimeMillis() },
                        label = { Text("Hoy", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            val c = Calendar.getInstance()
                            c.add(Calendar.MONTH, -1)
                            joinedDateTimestamp = c.timeInMillis
                        },
                        label = { Text("Hace 1 mes", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            val c = Calendar.getInstance()
                            c.add(Calendar.MONTH, -3)
                            joinedDateTimestamp = c.timeInMillis
                        },
                        label = { Text("Hace 3 meses", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Contact
                OutlinedTextField(
                    value = memberContact,
                    onValueChange = { memberContact = it },
                    label = { Text("Teléfono o Email (opcional)") },
                    placeholder = { Text("Contacto para avisos") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_member_contact"),
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION: 3 Status Switches with colored highlights
                Text(
                    text = "Estados y alertas del usuario",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 1. Switch: Pendiente de pago (Amarillo)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPendingPayment) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = if (isPendingPayment) BorderStroke(1.5.dp, Color(0xFFF59E0B)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = if (isPendingPayment) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Pendiente de pago",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isPendingPayment) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isPendingPayment) "Resaltado en amarillo (Pago pendiente)" else "El usuario está al día con la cuota",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPendingPayment) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isPendingPayment,
                            onCheckedChange = { isPendingPayment = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFF59E0B),
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("switch_pending_payment")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Switch: Pendiente eliminar (Rojo)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPendingRemoval) Color(0xFFFFE4E6) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = if (isPendingRemoval) BorderStroke(1.5.dp, Color(0xFFEF4444)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = if (isPendingRemoval) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Pendiente eliminar",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isPendingRemoval) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isPendingRemoval) "Resaltado en rojo (Para dar de baja)" else "Usuario activo en la suscripción",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPendingRemoval) Color(0xFFBE123C) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isPendingRemoval,
                            onCheckedChange = { isPendingRemoval = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFEF4444),
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("switch_pending_removal")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Switch: Pendiente dar de alta (Azul)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPendingRegistration) Color(0xFFDBEAFE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = if (isPendingRegistration) BorderStroke(1.5.dp, Color(0xFF3B82F6)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = if (isPendingRegistration) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Pendiente dar de alta",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isPendingRegistration) Color(0xFF1E40AF) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isPendingRegistration) "Resaltado en azul (En proceso de alta)" else "Alta ya tramitada y confirmada",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPendingRegistration) Color(0xFF1D4ED8) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isPendingRegistration,
                            onCheckedChange = { isPendingRegistration = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3B82F6),
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("switch_pending_registration")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    placeholder = { Text("Ej: Paga por Bizum...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_member_notes"),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_member_btn")
                ) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val amount = contributionText.replace(',', '.').toDoubleOrNull()
                        if (memberName.isBlank()) {
                            nameError = true
                            return@Button
                        }
                        if (amount == null || amount < 0.0) {
                            amountError = true
                            return@Button
                        }

                        val entity = MemberEntity(
                            id = memberToEdit?.id ?: 0L,
                            subscriptionId = sub.id,
                            memberName = memberName.trim(),
                            sharingPlatform = sharingPlatform.trim().ifBlank { "Sharesub" },
                            memberContact = memberContact.trim(),
                            joinedDate = joinedDateTimestamp,
                            contributionAmount = amount,
                            isPaidThisMonth = !isPendingPayment,
                            isPendingPayment = isPendingPayment,
                            isPendingRemoval = isPendingRemoval,
                            isPendingRegistration = isPendingRegistration,
                            notes = notes.trim()
                        )
                        onSave(entity)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("save_member_btn")
                ) {
                    Text(
                        text = if (isEditing) "Guardar Cambios" else "Añadir Usuario",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        dismissButton = null,
        modifier = modifier
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            joinedDateTimestamp = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/components/AddEditPlatformDialog.kt`

```kotlin
package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.SharingPlatformEntity
import com.example.data.model.SharingPlatforms

// Curated modern color palette for sharing platforms
val PRESET_PLATFORM_COLORS = listOf(
    "#6D28D9", // Violet / Purple (Together Price)
    "#DB2777", // Pink / Fuchsia (Sharingful)
    "#059669", // Emerald / Green (Spliiit)
    "#D97706", // Amber / Warm Gold (GamsGo)
    "#0284C7", // Sky Blue (Sharesub)
    "#C2410C", // Vibrant Orange (Directo/Familia)
    "#4F46E5", // Indigo (Splitzy Classic)
    "#2563EB", // Royal Blue
    "#0891B2", // Cyan
    "#0D9488", // Teal
    "#16A34A", // Forest Green
    "#65A30D", // Lime Green
    "#CA8A04", // Mustard Yellow
    "#EA580C", // Coral Orange
    "#DC2626", // Crimson Red
    "#E11D48", // Rose Red
    "#9333EA", // Deep Purple
    "#475569"  // Slate Grey
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditPlatformDialog(
    platformToEdit: SharingPlatformEntity?,
    onDismiss: () -> Unit,
    onSave: (SharingPlatformEntity) -> Unit
) {
    var name by remember { mutableStateOf(platformToEdit?.name ?: "") }
    var selectedColorHex by remember { mutableStateOf(platformToEdit?.colorHex ?: "#6D28D9") }
    var customHexInput by remember { mutableStateOf(selectedColorHex.removePrefix("#")) }
    var nameError by remember { mutableStateOf(false) }

    val currentColor = SharingPlatforms.parseColor(selectedColorHex)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("dialog_add_edit_platform")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(currentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = currentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (platformToEdit == null) "Nueva plataforma" else "Editar plataforma",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Personaliza el nombre y color de la plataforma",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Field: Platform Name
                Text(
                    text = "Nombre de la plataforma *",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    placeholder = { Text("Ej. Price Together, Sharingful...") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Introduce un nombre para la plataforma") }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_platform_name")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Field: Color Selection
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Color identificativo",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // Color circle preview
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(currentColor)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedColorHex.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Palette grid
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PRESET_PLATFORM_COLORS.forEach { hex ->
                        val color = SharingPlatforms.parseColor(hex)
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedColorHex = hex
                                    customHexInput = hex.removePrefix("#")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Hex input
                OutlinedTextField(
                    value = customHexInput,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isLetterOrDigit() }.take(6)
                        customHexInput = filtered
                        if (filtered.length == 6) {
                            selectedColorHex = "#$filtered"
                        }
                    },
                    label = { Text("Código de color Hex personalizado") },
                    prefix = { Text("# ", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = currentColor)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_platform_hex")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Live Preview Card ("Vista previa")
                Text(
                    text = "VISTA PREVIA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Así se verá en la lista de usuarios y filtros:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // 1. Capsule Badge Preview
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = currentColor.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(currentColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = name.ifBlank { "Nombre plataforma" },
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = currentColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_cancel_platform")
                    ) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            val cleanHex = if (selectedColorHex.startsWith("#")) selectedColorHex else "#$selectedColorHex"
                            val entity = platformToEdit?.copy(
                                name = name.trim(),
                                colorHex = cleanHex
                            ) ?: SharingPlatformEntity(
                                name = name.trim(),
                                colorHex = cleanHex,
                                displayOrder = 0
                            )
                            onSave(entity)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_save_platform")
                    ) {
                        Text(if (platformToEdit == null) "Añadir plataforma" else "Guardar cambios")
                    }
                }
            }
        }
    }
}

@Composable
fun DeletePlatformConfirmDialog(
    platform: SharingPlatformEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "¿Eliminar plataforma?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas eliminar la plataforma \"${platform.name}\"? Desaparecerá de los selectores y listas de la aplicación."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_delete_platform")
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancelar")
            }
        }
    )
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/components/AddEditSubscriptionDialog.kt`

```kotlin
package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SharingPlatformEntity
import com.example.data.local.SubscriptionEntity
import com.example.data.model.BillingPeriod
import com.example.data.model.CurrencyManager
import com.example.data.model.IconLibrary
import com.example.data.model.PlatformPriceItem
import com.example.data.model.PlatformPricingHelper
import com.example.data.model.SharingPlatforms
import com.example.ui.util.ImageStorageHelper
import kotlinx.coroutines.delay
import java.util.Locale

val availableCategories = listOf(
    "Streaming",
    "Música",
    "Productividad",
    "Gaming",
    "Educación",
    "Salud",
    "Estilo de vida",
    "Seguridad",
    "Finanzas",
    "General"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionDialog(
    subscriptionToEdit: SubscriptionEntity?,
    onDismiss: () -> Unit,
    onSave: (SubscriptionEntity) -> Unit,
    availablePlatforms: List<SharingPlatformEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEditing = subscriptionToEdit != null

    // Campos completamente vacíos para nueva suscripción
    var platformName by remember {
        mutableStateOf(subscriptionToEdit?.platformName ?: "")
    }
    var mainUserName by remember {
        mutableStateOf(subscriptionToEdit?.mainUserName ?: "")
    }
    var mainUserContact by remember {
        mutableStateOf(subscriptionToEdit?.mainUserContact ?: "")
    }
    var costText by remember {
        mutableStateOf(
            if (subscriptionToEdit != null) {
                String.format(Locale.US, "%.2f", subscriptionToEdit.cost)
            } else {
                ""
            }
        )
    }

    // Hasta 3 plataformas de compartición con precios independientes
    var configuredPlatforms by remember {
        mutableStateOf<List<Pair<String, String>>>(
            if (subscriptionToEdit != null) {
                val parsed = PlatformPricingHelper.parse(subscriptionToEdit.platformPricing)
                if (parsed.isNotEmpty()) {
                    parsed.map { it.platformName to (if (it.pricePerUser > 0) String.format(Locale.US, "%.2f", it.pricePerUser) else "") }
                } else if (subscriptionToEdit.defaultContributionPerUser > 0) {
                    listOf("Sharesub" to String.format(Locale.US, "%.2f", subscriptionToEdit.defaultContributionPerUser))
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        )
    }
    val platformPriceFocusRequesters = remember { List(3) { FocusRequester() } }
    var pendingFocusPlatformIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(pendingFocusPlatformIndex) {
        val idx = pendingFocusPlatformIndex
        if (idx != null && idx in 0 until configuredPlatforms.size && idx in platformPriceFocusRequesters.indices) {
            delay(120)
            try {
                platformPriceFocusRequesters[idx].requestFocus()
            } catch (e: Exception) {
                // Ignore if component not yet attached
            }
            pendingFocusPlatformIndex = null
        }
    }

    var showCustomPlatformInput by remember { mutableStateOf(false) }
    var customPlatformNameText by remember { mutableStateOf("") }
    var platformSelectDropdownExpanded by remember { mutableStateOf(false) }

    var billingDayText by remember {
        mutableStateOf(
            if (subscriptionToEdit != null) {
                subscriptionToEdit.billingDay.toString()
            } else {
                ""
            }
        )
    }
    var category by remember {
        mutableStateOf(subscriptionToEdit?.category ?: "Streaming")
    }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var billingPeriod by remember {
        mutableStateOf(BillingPeriod.fromKey(subscriptionToEdit?.billingPeriod))
    }
    var billingPeriodDropdownExpanded by remember { mutableStateOf(false) }

    var billingMonth by remember {
        mutableStateOf<Int?>(subscriptionToEdit?.billingMonth)
    }
    var monthDropdownExpanded by remember { mutableStateOf(false) }

    var selectedCurrency by remember {
        mutableStateOf(CurrencyManager.findCurrency(subscriptionToEdit?.currency ?: "EUR"))
    }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }

    var notes by remember {
        mutableStateOf(subscriptionToEdit?.notes ?: "")
    }

    // Icon Selection States
    var iconType by remember {
        mutableStateOf(subscriptionToEdit?.iconType ?: "VECTOR")
    }
    var iconKey by remember {
        mutableStateOf(subscriptionToEdit?.iconKey ?: "subscriptions")
    }
    var customImageUri by remember {
        mutableStateOf(subscriptionToEdit?.customImageUri ?: "")
    }
    var iconColorHex by remember {
        mutableStateOf(subscriptionToEdit?.iconColorHex ?: "#6366F1")
    }

    // Modal / Sub-diálogo para personalizar el icono cuando se pulsa sobre él
    var showIconPickerModal by remember { mutableStateOf(false) }

    var costError by remember { mutableStateOf(false) }
    var mainUserError by remember { mutableStateOf(false) }
    var platformNameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Subscriptions,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Editar Suscripción" else "Nueva Suscripción",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                // Header: Icono genérico interactivo + Nombre de la suscripción
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showIconPickerModal = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            PlatformIconBadge(
                                platformName = platformName.ifBlank { "Suscripción" },
                                iconType = iconType,
                                iconKey = iconKey,
                                customImageUri = customImageUri,
                                iconColorHex = iconColorHex,
                                size = 56.dp,
                                iconSize = 30.dp
                            )
                            // Pequeño indicador de edición sobre el icono
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Cambiar icono",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = platformName.ifBlank { "Nombre del servicio / suscripción" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (platformName.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Toca aquí para personalizar icono o imagen",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Nombre del servicio / suscripción
                OutlinedTextField(
                    value = platformName,
                    onValueChange = {
                        platformName = it
                        platformNameError = it.isBlank()
                    },
                    label = { Text("Nombre del servicio / suscripción *") },
                    placeholder = { Text("Ej: Netflix, Spotify, ChatGPT, Gimnasio, Canal+...") },
                    isError = platformNameError,
                    supportingText = if (platformNameError) {
                        { Text("El nombre del servicio es obligatorio") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_sub_platform_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Desplegable para elegir la categoría de la suscripción
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría de la suscripción *") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("input_sub_category_dropdown"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        availableCategories.forEach { catOption ->
                            DropdownMenuItem(
                                text = { Text(catOption) },
                                onClick = {
                                    category = catOption
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Titular / Usuario Principal (vacío por defecto)
                OutlinedTextField(
                    value = mainUserName,
                    onValueChange = {
                        mainUserName = it
                        mainUserError = it.isBlank()
                    },
                    label = { Text("Titular / Usuario Principal *") },
                    placeholder = { Text("Ej: Carlos (Yo)") },
                    isError = mainUserError,
                    supportingText = if (mainUserError) {
                        { Text("El titular es obligatorio") }
                    } else null,
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_main_user_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Dinero que me cuesta a mí (con selector de divisas y conversión a euros)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    OutlinedTextField(
                        value = costText,
                        onValueChange = {
                            costText = it
                            costError = it.replace(',', '.').toDoubleOrNull() == null
                        },
                        label = { Text("Precio (${selectedCurrency.symbol}${billingPeriod.suffix}) *") },
                        placeholder = { Text(if (billingPeriod == BillingPeriod.YEARLY) "119.99" else "17.99") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = costError,
                        supportingText = if (costError) {
                            { Text("Introduce un importe numérico") }
                        } else null,
                        modifier = Modifier
                            .weight(0.58f)
                            .testTag("input_sub_cost"),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Selector de moneda
                    ExposedDropdownMenuBox(
                        expanded = currencyDropdownExpanded,
                        onExpandedChange = { currencyDropdownExpanded = !currencyDropdownExpanded },
                        modifier = Modifier.weight(0.42f)
                    ) {
                        OutlinedTextField(
                            value = "${selectedCurrency.flag} ${selectedCurrency.code}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Moneda") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyDropdownExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("dropdown_currency_selector"),
                            singleLine = true,
                            maxLines = 1,
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = currencyDropdownExpanded,
                            onDismissRequest = { currencyDropdownExpanded = false }
                        ) {
                            CurrencyManager.currencies.forEach { curr ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${curr.flag} ${curr.code} (${curr.symbol})",
                                            fontWeight = if (curr.code == selectedCurrency.code) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        selectedCurrency = curr
                                        currencyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Conversión actualizada en todo momento a Euros (€)
                val parsedCost = costText.replace(',', '.').toDoubleOrNull()
                if (parsedCost != null && parsedCost > 0.0) {
                    val costInEur = CurrencyManager.convertToEur(parsedCost, selectedCurrency.code)
                    val monthlyCostInEur = billingPeriod.toMonthlyCost(costInEur)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedCurrency.code != "EUR") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            if (selectedCurrency.code != "EUR") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyExchange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Conversión a Euros: ≈ ${CurrencyManager.formatInEur(costInEur)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(
                                    text = "Tipo de cambio: 1 ${selectedCurrency.code} ≈ ${String.format(Locale.getDefault(), "%.4f", CurrencyManager.getRateToEur(selectedCurrency.code))} €",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(start = 24.dp)
                                )
                            }
                            if (billingPeriod != BillingPeriod.MONTHLY) {
                                if (selectedCurrency.code != "EUR") Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Equivalente mensual: ≈ ${CurrencyManager.formatInEur(monthlyCostInEur)} / mes",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Frecuencia / Periodo de cobro (Desplegable: Mensual, Trimestral, Semestral, Anual)
                ExposedDropdownMenuBox(
                    expanded = billingPeriodDropdownExpanded,
                    onExpandedChange = { billingPeriodDropdownExpanded = !billingPeriodDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = billingPeriod.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frecuencia / Periodo de cobro *") },
                        leadingIcon = { Icon(Icons.Default.Timelapse, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = billingPeriodDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("dropdown_billing_period"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = billingPeriodDropdownExpanded,
                        onDismissRequest = { billingPeriodDropdownExpanded = false }
                    ) {
                        BillingPeriod.list.forEach { periodOption ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = periodOption.label,
                                            fontWeight = if (billingPeriod == periodOption) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (billingPeriod == periodOption) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    billingPeriod = periodOption
                                    billingPeriodDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 6. Día y mes de cobro / ciclo de facturación
                if (billingPeriod == BillingPeriod.MONTHLY) {
                    OutlinedTextField(
                        value = billingDayText,
                        onValueChange = {
                            billingDayText = it.filter { ch -> ch.isDigit() }.take(2)
                        },
                        label = { Text("Día de cobro (1-31)") },
                        placeholder = { Text("Día (1-31)") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_billing_day"),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = billingDayText,
                            onValueChange = {
                                billingDayText = it.filter { ch -> ch.isDigit() }.take(2)
                            },
                            label = { Text("Día (1-31)") },
                            placeholder = { Text("Día") },
                            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(0.40f)
                                .testTag("input_billing_day"),
                            singleLine = true,
                            maxLines = 1,
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = monthDropdownExpanded,
                            onExpandedChange = { monthDropdownExpanded = !monthDropdownExpanded },
                            modifier = Modifier.weight(0.60f)
                        ) {
                            OutlinedTextField(
                                value = if (billingMonth != null) BillingPeriod.getMonthName(billingMonth!!) else "",
                                onValueChange = {},
                                readOnly = true,
                                label = {
                                    Text(
                                        when (billingPeriod) {
                                            BillingPeriod.YEARLY -> "Mes renovación"
                                            BillingPeriod.SEMI_ANNUAL -> "Mes inicio"
                                            BillingPeriod.QUARTERLY -> "Mes inicio"
                                            else -> "Mes cobro"
                                        }
                                    )
                                },
                                placeholder = { Text("Seleccionar mes") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthDropdownExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("dropdown_billing_month"),
                                singleLine = true,
                                maxLines = 1,
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = monthDropdownExpanded,
                                onDismissRequest = { monthDropdownExpanded = false }
                            ) {
                                BillingPeriod.months.forEach { monthItem ->
                                    DropdownMenuItem(
                                        text = { Text("${monthItem.number}. ${monthItem.name}") },
                                        onClick = {
                                            billingMonth = monthItem.number
                                            monthDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (billingMonth != null || billingDayText.isNotBlank()) {
                        val currentDayVal = billingDayText.toIntOrNull()?.coerceIn(1, 31) ?: 1
                        val currentMonthVal = billingMonth ?: 1
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "📅 ${BillingPeriod.formatSchedule(currentDayVal, currentMonthVal, billingPeriod)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 7. Plataformas de compartición y precio por usuario independiente (Hasta 3)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Plataformas de compartición",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Elige hasta 3 plataformas y su precio por usuario",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (configuredPlatforms.size in 1..3) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(
                                    text = "${configuredPlatforms.size}/3",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (configuredPlatforms.size in 1..3) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Desplegable para seleccionar y añadir plataformas
                        val availableQuickPlatforms = if (availablePlatforms.isNotEmpty()) {
                            availablePlatforms.map { it.name }
                        } else {
                            listOf("Together Price", "Sharingful", "Spliiit", "GamsGo", "Sharesub", "Directo / Familia")
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { platformSelectDropdownExpanded = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_select_platform_dropdown"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Añadir / Elegir plataforma (Desplegable)", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }

                            DropdownMenu(
                                expanded = platformSelectDropdownExpanded,
                                onDismissRequest = { platformSelectDropdownExpanded = false }
                            ) {
                                availableQuickPlatforms.forEach { pName ->
                                    val isAlreadyAdded = configuredPlatforms.any { it.first.equals(pName, ignoreCase = true) }
                                    val pInfo = SharingPlatforms.getInfo(pName, availablePlatforms)
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = pName,
                                                    fontWeight = if (isAlreadyAdded) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (isAlreadyAdded) {
                                                    Text(
                                                        text = "Añadida",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(start = 8.dp)
                                                    )
                                                }
                                            }
                                        },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(pInfo.baseColor)
                                            )
                                        },
                                        trailingIcon = if (isAlreadyAdded) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Añadida",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else null,
                                        onClick = {
                                            if (isAlreadyAdded) {
                                                configuredPlatforms = configuredPlatforms.filterNot { it.first.equals(pName, ignoreCase = true) }
                                            } else if (configuredPlatforms.size < 3) {
                                                val newIndex = configuredPlatforms.size
                                                configuredPlatforms = configuredPlatforms + (pName to "")
                                                pendingFocusPlatformIndex = newIndex
                                            }
                                            platformSelectDropdownExpanded = false
                                        }
                                    )
                                }


                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("+ Otra plataforma personalizada...") },
                                    onClick = {
                                        platformSelectDropdownExpanded = false
                                        showCustomPlatformInput = true
                                    }
                                )
                            }
                        }

                        // Añadir plataforma con nombre personalizado
                        if (showCustomPlatformInput) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customPlatformNameText,
                                    onValueChange = { customPlatformNameText = it },
                                    placeholder = { Text("Nombre de la plataforma...") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (customPlatformNameText.isNotBlank() && configuredPlatforms.size < 3) {
                                            val newIndex = configuredPlatforms.size
                                            configuredPlatforms = configuredPlatforms + (customPlatformNameText.trim() to "")
                                            customPlatformNameText = ""
                                            showCustomPlatformInput = false
                                            pendingFocusPlatformIndex = newIndex
                                        }
                                    },
                                    enabled = customPlatformNameText.isNotBlank() && configuredPlatforms.size < 3,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Añadir")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Lista de tarjetas para configurar el precio por usuario de cada plataforma seleccionada
                        if (configuredPlatforms.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Selecciona al menos 1 plataforma arriba para definir el precio que pagará cada usuario.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            configuredPlatforms.forEachIndexed { index, (pName, pPrice) ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 1.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = pName,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Precio individual",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        OutlinedTextField(
                                            value = pPrice,
                                            onValueChange = { newPrice ->
                                                configuredPlatforms = configuredPlatforms.toMutableList().also { list ->
                                                    list[index] = pName to newPrice
                                                }
                                            },
                                            placeholder = { Text("0.00") },
                                            trailingIcon = { Text("€/mes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp)) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .width(140.dp)
                                                .focusRequester(platformPriceFocusRequesters.getOrElse(index) { remember { FocusRequester() } })
                                                .testTag("input_price_platform_$index")
                                        )

                                        IconButton(
                                            onClick = {
                                                configuredPlatforms = configuredPlatforms.filterIndexed { i, _ -> i != index }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar plataforma",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 8. Notas adicionales
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas adicionales (opcional)") },
                    placeholder = { Text("Ej: Acceso de administrador, Bizum...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_sub_notes"),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = costText.replace(',', '.').toDoubleOrNull()
                    if (platformName.isBlank()) {
                        platformNameError = true
                        return@Button
                    }
                    if (mainUserName.isBlank()) {
                        mainUserError = true
                        return@Button
                    }
                    if (cost == null || cost <= 0.0) {
                        costError = true
                        return@Button
                    }

                    val finalPlatformItems = configuredPlatforms.mapNotNull { (name, priceStr) ->
                        val p = priceStr.replace(',', '.').toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank()) PlatformPriceItem(name.trim(), p) else null
                    }
                    val platformPricingString = PlatformPricingHelper.serialize(finalPlatformItems)
                    val defaultContribution = finalPlatformItems.firstOrNull()?.pricePerUser ?: 0.0
                    val billingDay = billingDayText.toIntOrNull()?.coerceIn(1, 31) ?: 1

                    val finalIconType = if (iconType == "CUSTOM_IMAGE" && customImageUri.isNotBlank()) {
                        "CUSTOM_IMAGE"
                    } else {
                        "VECTOR"
                    }

                    val entity = SubscriptionEntity(
                        id = subscriptionToEdit?.id ?: 0L,
                        platformName = platformName.trim(),
                        customPlanName = "",
                        mainUserName = mainUserName.trim(),
                        mainUserContact = mainUserContact.trim(),
                        cost = cost,
                        billingPeriod = billingPeriod.key,
                        billingDay = billingDay,
                        billingMonth = billingMonth ?: 1,
                        currency = selectedCurrency.code,
                        defaultContributionPerUser = defaultContribution,
                        platformPricing = platformPricingString,
                        category = category,
                        notes = notes.trim(),
                        iconType = finalIconType,
                        iconKey = iconKey,
                        customImageUri = customImageUri,
                        iconColorHex = iconColorHex,
                        createdAt = subscriptionToEdit?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(entity)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_sub_btn")
            ) {
                Text(if (isEditing) "Guardar Cambios" else "Crear Suscripción")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_sub_btn")
            ) {
                Text("Cancelar")
            }
        },
        modifier = modifier
    )

    // Diálogo emergente al hacer clic en el icono para elegir Biblioteca de Iconos o Subir Imagen
    if (showIconPickerModal) {
        IconPickerDialog(
            initialIconType = iconType,
            initialIconKey = iconKey,
            initialCustomImageUri = customImageUri,
            initialColorHex = iconColorHex,
            platformName = platformName,
            onDismiss = { showIconPickerModal = false },
            onIconSelected = { selectedType, selectedKey, selectedUri, selectedColor ->
                iconType = selectedType
                iconKey = selectedKey
                customImageUri = selectedUri
                iconColorHex = selectedColor
                showIconPickerModal = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconPickerDialog(
    initialIconType: String,
    initialIconKey: String,
    initialCustomImageUri: String,
    initialColorHex: String,
    platformName: String,
    onDismiss: () -> Unit,
    onIconSelected: (type: String, key: String, uri: String, colorHex: String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember {
        mutableIntStateOf(if (initialIconType == "CUSTOM_IMAGE" && initialCustomImageUri.isNotBlank()) 1 else 0)
    }
    var tempIconType by remember { mutableStateOf(initialIconType) }
    var tempIconKey by remember { mutableStateOf(initialIconKey) }
    var tempCustomImageUri by remember { mutableStateOf(initialCustomImageUri) }
    var tempColorHex by remember { mutableStateOf(initialColorHex) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImageFromUri(context, uri)
            if (savedPath != null) {
                tempCustomImageUri = savedPath
                tempIconType = "CUSTOM_IMAGE"
                selectedTab = 1
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Elegir Icono para la Suscripción",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Live preview
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlatformIconBadge(
                            platformName = platformName.ifBlank { "Icono" },
                            iconType = tempIconType,
                            iconKey = tempIconKey,
                            customImageUri = tempCustomImageUri,
                            iconColorHex = tempColorHex,
                            size = 50.dp,
                            iconSize = 26.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Vista Previa",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = when (tempIconType) {
                                    "CUSTOM_IMAGE" -> "Foto de la galería"
                                    else -> "Símbolo: $tempIconKey"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            tempIconType = "VECTOR"
                        },
                        text = { Text("Biblioteca de Iconos", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            if (tempCustomImageUri.isNotBlank()) {
                                tempIconType = "CUSTOM_IMAGE"
                            }
                        },
                        text = { Text("Subir de la Galería", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // TAB 0: Biblioteca de Iconos y Colores
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Color de fondo",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconLibrary.availableColors.forEach { colorOption ->
                                val isColorSelected = tempColorHex.equals(colorOption.hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(colorOption.color)
                                        .clickable {
                                            tempColorHex = colorOption.hex
                                            tempIconType = "VECTOR"
                                        }
                                        .then(
                                            if (isColorSelected) {
                                                Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isColorSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Seleccionado",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Seleccionar símbolo",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconLibrary.availableIcons.forEach { iconOption ->
                                val isIconSelected = tempIconType == "VECTOR" && tempIconKey.equals(iconOption.key, ignoreCase = true)
                                FilterChip(
                                    selected = isIconSelected,
                                    onClick = {
                                        tempIconKey = iconOption.key
                                        tempIconType = "VECTOR"
                                    },
                                    label = { Text(iconOption.label, fontSize = 11.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = iconOption.icon,
                                            contentDescription = iconOption.label,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                } else {
                    // TAB 1: Subir imagen de la galería
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (tempCustomImageUri.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PlatformIconBadge(
                                        platformName = platformName.ifBlank { "Icono" },
                                        iconType = "CUSTOM_IMAGE",
                                        customImageUri = tempCustomImageUri,
                                        size = 64.dp,
                                        cornerRadius = 16.dp
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Button(
                                            onClick = { galleryLauncher.launch("image/*") },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.testTag("change_gallery_image_modal_btn")
                                        ) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Cambiar")
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        TextButton(
                                            onClick = {
                                                tempCustomImageUri = ""
                                                tempIconType = "VECTOR"
                                                selectedTab = 0
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Quitar imagen", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Elige una foto o logo desde tu dispositivo",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("upload_gallery_image_modal_btn")
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Seleccionar de la galería")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalType = if (tempIconType == "CUSTOM_IMAGE" && tempCustomImageUri.isNotBlank()) "CUSTOM_IMAGE" else "VECTOR"
                    onIconSelected(finalType, tempIconKey, tempCustomImageUri, tempColorHex)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Aplicar Icono")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/components/AppMenuSheet.kt`

```kotlin
package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMenuSheet(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBackupRestore: () -> Unit,
    onOpenCloudSync: () -> Unit,
    userEmail: String? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("app_menu_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Drag indicator
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // App Brand Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                SplitzyLogo(size = 48.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Splitzy",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "v1.2",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (!userEmail.isNullOrBlank()) userEmail else "Gestión de suscripciones compartidas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Highlight Action: CONFIGURACIÓN (Settings)
            Card(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_menu_settings")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Configuración",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Tema, apariencia y plataformas de compartición",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Other Quick Actions
            AppMenuItemRow(
                icon = Icons.Default.Backup,
                title = "Copia de Seguridad y Restauración",
                subtitle = "Exportar o importar tus suscripciones en formato JSON",
                iconTint = MaterialTheme.colorScheme.secondary,
                iconBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                onClick = onOpenBackupRestore,
                testTag = "btn_menu_backup_restore"
            )

            Spacer(modifier = Modifier.height(8.dp))

            AppMenuItemRow(
                icon = Icons.Default.CloudSync,
                title = "Sincronización en la Nube",
                subtitle = if (!userEmail.isNullOrBlank()) "Conectado como $userEmail" else "Inicia sesión con Google para sincronizar",
                iconTint = MaterialTheme.colorScheme.tertiary,
                iconBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                onClick = onOpenCloudSync,
                testTag = "btn_menu_cloud_sync"
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Footer note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Splitzy • Privacidad local y sincronización segura",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AppMenuItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/components/AuthAccountDialog.kt`

```kotlin
package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.remote.AuthState
import com.google.firebase.auth.FirebaseUser

@Composable
fun AuthAccountDialog(
    authState: AuthState,
    isSyncing: Boolean,
    onDismissRequest: () -> Unit,
    onSignInWithGoogle: () -> Unit,
    onSignInWithEmail: (email: String, pass: String) -> Unit,
    onRegisterWithEmail: (email: String, pass: String) -> Unit,
    onSignOut: () -> Unit,
    onSyncToCloud: () -> Unit,
    onSyncFromCloud: () -> Unit,
    onClearError: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.testTag("auth_account_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (authState is AuthState.Authenticated) Icons.Default.CloudDone else Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = if (authState is AuthState.Authenticated) "Mi Cuenta & Sincronización" else if (isRegisterMode) "Crear cuenta" else "Acceso Multidispositivo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (authState is AuthState.Authenticated) "Sincronizado con la nube (Firestore)" else "Usa tu cuenta en Android y Web",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (authState) {
                    is AuthState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Conectando con la nube...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is AuthState.Authenticated -> {
                        val user = authState.user
                        AuthenticatedUserView(
                            user = user,
                            isSyncing = isSyncing,
                            onSyncToCloud = onSyncToCloud,
                            onSyncFromCloud = onSyncFromCloud,
                            onSignOut = onSignOut
                        )
                    }

                    is AuthState.Error -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚠️ " + authState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(
                                    onClick = onClearError,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Reintentar", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        LoginForm(
                            isRegisterMode = isRegisterMode,
                            email = email,
                            onEmailChange = { email = it },
                            password = password,
                            onPasswordChange = { password = it },
                            passwordVisible = passwordVisible,
                            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                            onToggleMode = { isRegisterMode = !isRegisterMode },
                            onSignInWithGoogle = onSignInWithGoogle,
                            onSubmitEmail = {
                                if (isRegisterMode) {
                                    onRegisterWithEmail(email, password)
                                } else {
                                    onSignInWithEmail(email, password)
                                }
                            }
                        )
                    }

                    is AuthState.Idle -> {
                        LoginForm(
                            isRegisterMode = isRegisterMode,
                            email = email,
                            onEmailChange = { email = it },
                            password = password,
                            onPasswordChange = { password = it },
                            passwordVisible = passwordVisible,
                            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                            onToggleMode = { isRegisterMode = !isRegisterMode },
                            onSignInWithGoogle = onSignInWithGoogle,
                            onSubmitEmail = {
                                if (isRegisterMode) {
                                    onRegisterWithEmail(email, password)
                                } else {
                                    onSignInWithEmail(email, password)
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun AuthenticatedUserView(
    user: FirebaseUser,
    isSyncing: Boolean,
    onSyncToCloud: () -> Unit,
    onSyncFromCloud: () -> Unit,
    onSignOut: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (user.photoUrl != null) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (user.displayName?.firstOrNull() ?: user.email?.firstOrNull() ?: 'U').uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName ?: "Usuario registrado",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user.email ?: user.uid,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Sincronización en la nube",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
    )

    Text(
        text = "Tus datos se guardan de forma segura y se sincronizan en tiempo real para que los consultes desde cualquier dispositivo o desde la futura versión web.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSyncToCloud,
            enabled = !isSyncing,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text("Subir a la nube", fontSize = 13.sp)
        }

        OutlinedButton(
            onClick = onSyncFromCloud,
            enabled = !isSyncing,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Descargar", fontSize = 13.sp)
        }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

    OutlinedButton(
        onClick = onSignOut,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Cerrar sesión")
    }
}

@Composable
private fun LoginForm(
    isRegisterMode: Boolean,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onToggleMode: () -> Unit,
    onSignInWithGoogle: () -> Unit,
    onSubmitEmail: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tab selector for Iniciar Sesión vs Crear Cuenta
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                onClick = { if (isRegisterMode) onToggleMode() },
                shape = RoundedCornerShape(10.dp),
                color = if (!isRegisterMode) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (!isRegisterMode) 2.dp else 0.dp,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Iniciar sesión",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (!isRegisterMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isRegisterMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Surface(
                onClick = { if (!isRegisterMode) onToggleMode() },
                shape = RoundedCornerShape(10.dp),
                color = if (isRegisterMode) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (isRegisterMode) 2.dp else 0.dp,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Crear cuenta",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isRegisterMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (isRegisterMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Tu Correo electrónico") },
            placeholder = { Text("ejemplo@gmail.com") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(if (isRegisterMode) "Crear contraseña (mínimo 6 caracteres)" else "Tu Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Ver contraseña"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSubmitEmail,
            enabled = email.isNotBlank() && password.length >= 6,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = if (isRegisterMode) "Registrarme con Correo" else "Iniciar sesión",
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = " o también ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        // Google Sign-In button
        OutlinedButton(
            onClick = onSignInWithGoogle,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Continuar con Google",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/components/BackupRestoreDialog.kt`

```kotlin
package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.util.BackupManager
import com.example.data.util.BackupPreview
import com.example.ui.theme.ProfitGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRestoreDialog(
    onDismissRequest: () -> Unit,
    onGetBackupJson: suspend () -> String,
    onPreviewBackup: (String) -> Unit,
    pendingPreview: BackupPreview?,
    onConfirmRestore: (replaceExisting: Boolean) -> Unit,
    onDismissPreview: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    // Launcher for saving backup file (SAF CreateDocument)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isProcessing = true
                val json = onGetBackupJson()
                val success = BackupManager.writeBackupToUri(context, uri, json)
                isProcessing = false
                if (success) {
                    Toast.makeText(context, "✅ Copia de seguridad guardada correctamente", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "❌ Error al guardar el archivo", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Launcher for selecting backup file to restore (SAF OpenDocument)
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isProcessing = true
                val content = BackupManager.readBackupFromUri(context, uri)
                isProcessing = false
                if (content != null) {
                    onPreviewBackup(content)
                } else {
                    Toast.makeText(context, "❌ No se pudo leer el archivo seleccionado", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (pendingPreview != null) {
        RestoreConfirmationDialog(
            preview = pendingPreview,
            onConfirm = { replaceExisting ->
                onConfirmRestore(replaceExisting)
            },
            onDismiss = onDismissPreview
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.testTag("backup_restore_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Copia y Restauración",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Local • Google Drive • OneDrive",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Informative Cloud Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Puedes guardar y restaurar tus copias en tu almacenamiento local, Google Drive o Microsoft OneDrive mediante el selector del sistema o compartirlas a la nube.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Text(
                    text = "Crear copia de seguridad",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Option 1: Save Backup File (Local, Google Drive, OneDrive)
                Card(
                    onClick = {
                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                        createDocumentLauncher.launch("backup_suscripciones_$timeStamp.json")
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Guardar archivo de copia",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Elige guardar en Local, Google Drive o OneDrive",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Option 2: Share / Upload to Cloud Directly
                Card(
                    onClick = {
                        scope.launch {
                            val json = onGetBackupJson()
                            val uri = BackupManager.createShareableFile(context, json)
                            if (uri != null) {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "Copia de Seguridad - Gestor de Suscripciones")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                val chooser = Intent.createChooser(sendIntent, "Guardar en Google Drive, OneDrive o Enviar...")
                                context.startActivity(chooser)
                            } else {
                                Toast.makeText(context, "Error al preparar el archivo para compartir", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ProfitGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = ProfitGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enviar o Subir a la nube",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Sube a Google Drive, OneDrive, Gmail, etc.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = ProfitGreen
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Restauración de datos",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Option 3: Restore Backup
                Card(
                    onClick = {
                        openDocumentLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Restaurar desde archivo",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Selecciona desde Local, Google Drive o OneDrive",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun RestoreConfirmationDialog(
    preview: BackupPreview,
    onConfirm: (replaceExisting: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var replaceExisting by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        icon = {
            Icon(
                imageVector = if (preview.isValid) Icons.Default.CloudDownload else Icons.Default.Warning,
                contentDescription = null,
                tint = if (preview.isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = if (preview.isValid) "Restaurar copia de seguridad" else "Copia no válida",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (preview.isValid) {
                    Text(
                        text = "Se ha leído correctamente el archivo de copia:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "📅 Fecha: ${preview.exportDate}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "💳 Suscripciones: ${preview.subscriptionsCount}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "👥 Miembros/Usuarios: ${preview.membersCount}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (preview.sampleSubscriptions.isNotEmpty()) {
                                Text(
                                    text = "Incluye: ${preview.sampleSubscriptions.joinToString(", ")}...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "¿Cómo deseas aplicar la restauración?",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { replaceExisting = true },
                            colors = if (replaceExisting) {
                                ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            } else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Reemplazar todo",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (replaceExisting) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }

                        OutlinedButton(
                            onClick = { replaceExisting = false },
                            colors = if (!replaceExisting) {
                                ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            } else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Fusionar datos",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (!replaceExisting) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }

                    Text(
                        text = if (replaceExisting) {
                            "⚠️ 'Reemplazar todo' eliminará los datos actuales y dejará exactamente el contenido de la copia."
                        } else {
                            "ℹ️ 'Fusionar datos' mantendrá tus suscripciones actuales y añadirá las de la copia de seguridad."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (replaceExisting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "El archivo no contiene un formato de copia de seguridad válido:\n${preview.errorMessage}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            if (preview.isValid) {
                Button(
                    onClick = { onConfirm(replaceExisting) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Restaurar ahora")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/components/FinancialSummaryCard.kt`

```kotlin
package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenBg
import com.example.ui.viewmodel.FinancialOverview
import java.util.Locale

@Composable
fun FinancialSummaryCard(
    overview: FinancialOverview,
    modifier: Modifier = Modifier
) {
    val isProfit = overview.netBalance > 0.001
    val isBreakEven = kotlin.math.abs(overview.netBalance) < 0.01

    val coveragePercent = if (overview.totalCost > 0) {
        ((overview.totalContributed / overview.totalCost) * 100).toFloat().coerceIn(0f, 250f)
    } else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("financial_summary_card"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Main Highlight Banner: Dinero Neto Ganado / Balance
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = if (isProfit) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isProfit) "GANANCIA NETA MENSUAL" else "BALANCE NETO MENSUAL",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AnimatedContent(targetState = overview.netBalance, label = "net_balance") { net ->
                            val formatted = String.format(Locale.getDefault(), "%.2f", net)
                            val prefix = if (net > 0.001) "+" else ""
                            Text(
                                text = "$prefix$formatted €/mes",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 26.sp
                                ),
                                color = if (isProfit) ProfitGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (isProfit) {
                                        listOf(ProfitGreen, Color(0xFF34D399))
                                    } else {
                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isProfit) Icons.Default.TrendingUp else Icons.Default.Savings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two-column Breakdown: Me cuesta a mí vs Me aportan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cost Column
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Te cuesta a ti",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.2f", overview.totalCost)} €",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gasto mensual",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                // Contributed Column
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(ProfitGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = ProfitGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Te aportan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.2f", overview.totalContributed)} €",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = ProfitGreen
                        )
                        Text(
                            text = "Aporte mensual",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar of Cost Coverage
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cobertura de Costes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", coveragePercent)}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (coveragePercent >= 100f) ProfitGreen else MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (coveragePercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (coveragePercent >= 100f) ProfitGreen else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Metric Tags: Total Active Subscriptions & Total Active Users
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subscriptions,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${overview.totalSubscriptionsCount} Suscripciones",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${overview.totalMembersCount} Miembros activos",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/components/PlatformIconBadge.kt`

```kotlin
package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.SubscriptionEntity
import com.example.data.model.IconLibrary
import com.example.data.model.PlatformPresets
import java.io.File

@Composable
fun PlatformIconBadge(
    platformName: String,
    modifier: Modifier = Modifier,
    iconType: String = "PRESET",
    iconKey: String = "Netflix",
    customImageUri: String = "",
    iconColorHex: String = "#6366F1",
    size: Dp = 48.dp,
    iconSize: Dp = 26.dp,
    cornerRadius: Dp = 14.dp
) {
    val context = LocalContext.current
    val parsedColor = remember(iconColorHex) {
        try {
            Color(android.graphics.Color.parseColor(iconColorHex))
        } catch (e: Exception) {
            Color(0xFF6366F1)
        }
    }

    val shape = RoundedCornerShape(cornerRadius)

    when {
        // Custom Image uploaded from gallery
        iconType == "CUSTOM_IMAGE" && customImageUri.isNotBlank() -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(shape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                parsedColor.copy(alpha = 0.35f),
                                parsedColor.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape),
                contentAlignment = Alignment.Center
            ) {
                // Fallback letter if image is still loading or fails
                Text(
                    text = platformName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = parsedColor
                    )
                )

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(
                            when {
                                customImageUri.startsWith("content://") || customImageUri.startsWith("file://") -> {
                                    Uri.parse(customImageUri)
                                }
                                File(customImageUri).exists() -> {
                                    File(customImageUri)
                                }
                                else -> {
                                    customImageUri
                                }
                            }
                        )
                        .crossfade(true)
                        .build(),
                    contentDescription = platformName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Custom Vector Icon selected from IconLibrary
        iconType == "VECTOR" -> {
            val vectorIcon = remember(iconKey) { IconLibrary.getIconByKey(iconKey) }
            Box(
                modifier = modifier
                    .size(size)
                    .clip(shape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                parsedColor,
                                parsedColor.copy(alpha = 0.75f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorIcon,
                    contentDescription = platformName,
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        // Platform Preset (default)
        else -> {
            val preset = PlatformPresets.getPreset(platformName)
            Box(
                modifier = modifier
                    .size(size)
                    .clip(shape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                preset.primaryColor,
                                preset.accentColor
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = preset.icon,
                    contentDescription = platformName,
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

@Composable
fun PlatformIconBadge(
    subscription: SubscriptionEntity,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 26.dp,
    cornerRadius: Dp = 14.dp
) {
    PlatformIconBadge(
        platformName = subscription.platformName,
        iconType = subscription.iconType,
        iconKey = subscription.iconKey,
        customImageUri = subscription.customImageUri,
        iconColorHex = subscription.iconColorHex,
        modifier = modifier,
        size = size,
        iconSize = iconSize,
        cornerRadius = cornerRadius
    )
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/components/ReminderMessageDialog.kt`

```kotlin
package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.MemberEntity
import com.example.data.local.SubscriptionEntity
import java.util.Locale

@Composable
fun ReminderMessageDialog(
    member: MemberEntity,
    subscription: SubscriptionEntity,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val defaultMessage = remember {
        "¡Hola ${member.memberName}! Te recuerdo la cuota mensual de ${String.format(Locale.getDefault(), "%.2f", member.contributionAmount)} € para la suscripción compartida de ${subscription.platformName} (${subscription.customPlanName.ifBlank { "Cuenta Compartida" }}). ¡Muchas gracias!"
    }

    var messageText by remember { mutableStateOf(defaultMessage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recordatorio de Pago",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Mensaje listo para enviar a ${member.memberName}:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_message_input"),
                    minLines = 4,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Recordatorio", messageText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Mensaje copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copiar")
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, messageText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Enviar recordatorio por...")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        modifier = modifier
    )
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/components/SplitzyLogo.kt`

```kotlin
package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

@Composable
fun SplitzyLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val cornerRadius = size * 0.23f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val painter = runCatching { painterResource(id = R.drawable.ic_launcher_foreground) }
            .getOrNull()
            ?: runCatching { painterResource(id = R.drawable.ic_splitzy_app_logo) }
                .getOrNull()

        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = "Splitzy Logo",
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(cornerRadius)),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Splitzy Logo",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
        }
    }
}



```


## ARCHIVO: `app/src/main/java/com/example/ui/components/SubscriptionCard.kt`

```kotlin
package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SharingPlatformEntity
import com.example.data.local.SubscriptionWithMembers
import com.example.data.model.SharingPlatforms
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenBg
import com.example.ui.util.HighlightedText
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubscriptionCard(
    subscriptionWithMembers: SubscriptionWithMembers,
    onClick: () -> Unit,
    onAddMemberClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMemberClick: (com.example.data.local.MemberEntity) -> Unit = {},
    searchQuery: String = "",
    availablePlatforms: List<SharingPlatformEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val sub = subscriptionWithMembers.subscription
    val members = subscriptionWithMembers.members
    val totalContributed = subscriptionWithMembers.totalContributed
    val myCostMonthly = subscriptionWithMembers.myCostMonthly
    val myCostEur = subscriptionWithMembers.myCostEur
    val netBalance = subscriptionWithMembers.netBalance
    val isProfit = subscriptionWithMembers.isNetProfit
    val period = subscriptionWithMembers.billingPeriodObj

    var isExpanded by rememberSaveable(sub.id) { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow_rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("subscription_card_${sub.id}"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isExpanded) 2.5.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Platform Icon, Platform + Plan Name, Expand Chevron, More Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlatformIconBadge(
                    subscription = sub,
                    size = 46.dp,
                    iconSize = 24.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    HighlightedText(
                        text = sub.platformName,
                        query = searchQuery,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (sub.customPlanName.isNotBlank()) {
                        HighlightedText(
                            text = sub.customPlanName,
                            query = searchQuery,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Expand / Collapse Chevron indicator
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.testTag("expand_toggle_${sub.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Replegar" else "Desplegar vista previa",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.rotate(arrowRotation)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("sub_card_menu_${sub.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Añadir usuario") },
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onAddMemberClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Editar suscripción") },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar suscripción", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            // Preview Details (Visible only when expanded)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

            // Main User / Titular on one line, and Renewal Date on the next line
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Line 1: Titular
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Titular",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Titular: ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        HighlightedText(
                            text = sub.mainUserName,
                            query = searchQuery,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Line 2: Fecha de renovación
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Fecha de renovación",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Renovación: ${com.example.data.model.BillingPeriod.formatSchedule(sub.billingDay, sub.billingMonth, period)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val platformPricesList = subscriptionWithMembers.platformPrices
            if (platformPricesList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    platformPricesList.forEach { pItem ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        ) {
                            Text(
                                text = "${pItem.platformName}: ${String.format(Locale.getDefault(), "%.2f", pItem.pricePerUser)} €",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Financial Summary Block in 3 independent lines
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val curr = com.example.data.model.CurrencyManager.findCurrency(sub.currency)

                    // Línea 1: Coste propio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Coste propio",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.2f", sub.cost)} ${curr.symbol}${period.suffix}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (curr.code != "EUR") {
                                val eurConvertedText = if (period != com.example.data.model.BillingPeriod.MONTHLY) {
                                    "≈ ${String.format(Locale.getDefault(), "%.2f", myCostEur)} € (${String.format(Locale.getDefault(), "%.2f", myCostMonthly)} €/mes)"
                                } else {
                                    "≈ ${String.format(Locale.getDefault(), "%.2f", myCostMonthly)} €/mes"
                                }
                                Text(
                                    text = eurConvertedText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else if (period != com.example.data.model.BillingPeriod.MONTHLY) {
                                Text(
                                    text = "(${String.format(Locale.getDefault(), "%.2f", myCostMonthly)} €/mes)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.8.dp
                    )

                    // Línea 2: Aportan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aportan (${members.size} miembros)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "+${String.format(Locale.getDefault(), "%.2f", totalContributed)} €/mes",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ProfitGreen
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.8.dp
                    )

                    // Línea 3: Ganancia neta o Balance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isProfit) "Ganancia neta" else "Balance neto",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isProfit) ProfitGreen else MaterialTheme.colorScheme.onSurface
                        )
                        val prefix = if (netBalance > 0.001) "+" else ""
                        Text(
                            text = "$prefix${String.format(Locale.getDefault(), "%.2f", netBalance)} €/mes",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (isProfit) ProfitGreen else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // List of User / Member Chips
            Text(
                text = "Usuarios en este momento (${members.size})",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (members.isEmpty()) {
                Text(
                    text = "Aún no has añadido miembros a esta suscripción.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    members.forEach { member ->
                        val chipBgColor = when {
                            member.isPendingRemoval -> Color(0xFFFFE4E6)
                            member.isPendingRegistration -> Color(0xFFDBEAFE)
                            member.isPendingPayment || !member.isPaidThisMonth -> Color(0xFFFEF3C7)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                        val dotColor = when {
                            member.isPendingRemoval -> Color(0xFFE11D48)
                            member.isPendingRegistration -> Color(0xFF2563EB)
                            member.isPendingPayment || !member.isPaidThisMonth -> Color(0xFFD97706)
                            else -> ProfitGreen
                        }
                        val memberTextColor = when {
                            member.isPendingRemoval -> Color(0xFF991B1B)
                            member.isPendingRegistration -> Color(0xFF1E40AF)
                            member.isPendingPayment || !member.isPaidThisMonth -> Color(0xFF92400E)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = chipBgColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onMemberClick(member) }
                                .testTag("member_chip_${member.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    HighlightedText(
                                        text = member.memberName,
                                        query = searchQuery,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            lineHeight = 14.sp
                                        ),
                                        color = memberTextColor,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (member.sharingPlatform.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        val platformInfo = SharingPlatforms.getInfo(member.sharingPlatform, availablePlatforms)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = platformInfo.badgeBgColor
                                        ) {
                                            Text(
                                                text = platformInfo.name,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = platformInfo.badgeTextColor
                                                ),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                ) {
                                    Text(
                                        text = "+${String.format(Locale.getDefault(), "%.2f", member.contributionAmount)} €",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (member.isPendingRemoval) Color(0xFFE11D48) else if (member.isPendingPayment || !member.isPaidThisMonth) Color(0xFFD97706) else ProfitGreen
                                        ),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: + Añadir usuario & Ver detalle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddMemberClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("add_member_btn_${sub.id}"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Añadir Usuario",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1
                    )
                }

                FilledTonalButton(
                    onClick = onClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("view_detail_btn_${sub.id}"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Ver Detalle",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
}
}


```


## ARCHIVO: `app/src/main/java/com/example/ui/components/SubscriptionDetailSheet.kt`

```kotlin
package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import com.example.data.model.SharingPlatforms
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MemberEntity
import com.example.data.local.SharingPlatformEntity
import com.example.data.local.SubscriptionWithMembers
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailSheet(
    subscriptionWithMembers: SubscriptionWithMembers,
    onDismiss: () -> Unit,
    onAddMemberClick: () -> Unit,
    onEditMemberClick: (MemberEntity) -> Unit,
    onDeleteMemberClick: (MemberEntity) -> Unit,
    onToggleMemberPayment: (Long, Boolean) -> Unit,
    onReminderClick: (MemberEntity) -> Unit,
    onEditSubscriptionClick: () -> Unit,
    onDeleteSubscriptionClick: () -> Unit,
    availablePlatforms: List<SharingPlatformEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sub = subscriptionWithMembers.subscription
    val members = subscriptionWithMembers.members
    val period = subscriptionWithMembers.billingPeriodObj
    val dateFormat = remember { SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "ES")) }

    var memberToDelete by remember { mutableStateOf<MemberEntity?>(null) }
    var showDeleteSubDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.testTag("subscription_detail_sheet")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Sheet Header: Platform Badge, Platform Name, Main User, Close button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlatformIconBadge(
                        subscription = sub,
                        size = 54.dp,
                        iconSize = 28.dp
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sub.platformName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (sub.customPlanName.isNotBlank()) {
                            Text(
                                text = sub.customPlanName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_detail_sheet_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Main Info Box: Primary user, Billing day & Period
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Usuario Principal / Titular",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = sub.mainUserName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (sub.mainUserContact.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = sub.mainUserContact,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Facturación: ${com.example.data.model.BillingPeriod.formatSchedule(sub.billingDay, sub.billingMonth, period)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = period.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (sub.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Notas: ${sub.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Financial Balance Card for this specific subscription
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (subscriptionWithMembers.isNetProfit) {
                            ProfitGreenBg.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BALANCE DE LA SUSCRIPCIÓN",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val coverage = subscriptionWithMembers.coveragePercentage
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.0f", coverage)}% Cubierto",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (coverage >= 100f) ProfitGreen else MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Large Net profit / Cost text
                        val net = subscriptionWithMembers.netBalance
                        val prefix = if (net > 0.001) "+" else ""
                        Text(
                            text = "$prefix${String.format(Locale.getDefault(), "%.2f", net)} € / mes",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (subscriptionWithMembers.isNetProfit) ProfitGreen else MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Text(
                            text = if (subscriptionWithMembers.isNetProfit) {
                                "Estás ganando dinero neto tras cubrir tu coste mensual."
                            } else {
                                "Tu coste neto resultante tras los aportes de tus usuarios."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // 3-metric summary in independent rows
                        val curr = com.example.data.model.CurrencyManager.findCurrency(sub.currency)
                        val isForeign = curr.code != "EUR"

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Línea 1: Coste propio
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Coste propio",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${String.format(Locale.getDefault(), "%.2f", sub.cost)} ${curr.symbol}${period.suffix}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isForeign || period != com.example.data.model.BillingPeriod.MONTHLY) {
                                        Text(
                                            text = "(${String.format(Locale.getDefault(), "%.2f", subscriptionWithMembers.myCostMonthly)} €/mes)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                thickness = 0.8.dp
                            )

                            // Línea 2: Total que te aportan
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total que te aportan",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+${String.format(Locale.getDefault(), "%.2f", subscriptionWithMembers.totalContributed)} €/mes",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ProfitGreen
                                )
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                thickness = 0.8.dp
                            )

                            // Línea 3: Usuarios activos
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Usuarios activos",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${members.size} miembros",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (isForeign) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyExchange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Conversión de divisa: ${String.format(Locale.getDefault(), "%.2f", sub.cost)} ${curr.code} = ${String.format(Locale.getDefault(), "%.2f", subscriptionWithMembers.myCostEur)} € (${String.format(Locale.getDefault(), "%.2f", subscriptionWithMembers.myCostMonthly)} €/mes)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Configured Sharing Platforms and their independent pricing (Up to 3)
            val platformPricesList = subscriptionWithMembers.platformPrices
            if (platformPricesList.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Plataformas de compartición y precios",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                platformPricesList.forEach { pItem ->
                                    val memberCountOnPlatform = members.count { it.sharingPlatform.equals(pItem.platformName, ignoreCase = true) }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 1.dp
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = pItem.platformName,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${String.format(Locale.getDefault(), "%.2f", pItem.pricePerUser)} €/mes",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "$memberCountOnPlatform miembros",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Members Header & Dedicated Horizontal "+ Añadir Usuario" button
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Lista de Usuarios (${members.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Miembros con los que compartes esta suscripción",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onAddMemberClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sheet_add_member_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Añadir Usuario a la Suscripción",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Members List or Empty State
            if (members.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay usuarios añadidos todavía",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pulsa el botón 'Añadir Usuario a la Suscripción' para registrar a los miembros y calcular sus cuotas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                items(members, key = { it.id }) { member ->
                    MemberDetailItem(
                        member = member,
                        dateFormat = dateFormat,
                        currency = sub.currency,
                        onTogglePayment = { onToggleMemberPayment(member.id, member.isPaidThisMonth) },
                        onEdit = { onEditMemberClick(member) },
                        onDelete = { memberToDelete = member },
                        onReminder = { onReminderClick(member) },
                        availablePlatforms = availablePlatforms
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Subscription Management Action Buttons
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditSubscriptionClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sheet_edit_sub_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Editar Suscripción")
                    }

                    Button(
                        onClick = { showDeleteSubDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sheet_delete_sub_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }

    // Confirmation dialog for deleting member
    memberToDelete?.let { member ->
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = { Text("Quitar usuario de la suscripción") },
            text = {
                Text("¿Estás seguro de que quieres eliminar a \"${member.memberName}\" de esta suscripción? Se actualizarán los totales y el balance de ganancias inmediatamente.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMemberClick(member)
                        memberToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Confirmation dialog for deleting subscription
    if (showDeleteSubDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSubDialog = false },
            title = { Text("Eliminar suscripción") },
            text = {
                Text("¿Deseas eliminar la suscripción \"${sub.platformName}\" y todos sus usuarios asociados? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteSubDialog = false
                        onDeleteSubscriptionClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar Suscripción")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSubDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun MemberDetailItem(
    member: MemberEntity,
    dateFormat: SimpleDateFormat,
    currency: String,
    onTogglePayment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReminder: () -> Unit,
    availablePlatforms: List<SharingPlatformEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val cardBgColor = when {
        member.isPendingRemoval -> Color(0xFFFFE4E6)
        member.isPendingRegistration -> Color(0xFFDBEAFE)
        member.isPendingPayment || !member.isPaidThisMonth -> Color(0xFFFEF3C7)
        else -> MaterialTheme.colorScheme.surface
    }

    val cardBorder = when {
        member.isPendingRemoval -> BorderStroke(1.5.dp, Color(0xFFEF4444))
        member.isPendingRegistration -> BorderStroke(1.5.dp, Color(0xFF3B82F6))
        member.isPendingPayment || !member.isPaidThisMonth -> BorderStroke(1.5.dp, Color(0xFFF59E0B))
        else -> null
    }

    val memberTextColor = when {
        member.isPendingRemoval -> Color(0xFF991B1B)
        member.isPendingRegistration -> Color(0xFF1E40AF)
        member.isPendingPayment || !member.isPaidThisMonth -> Color(0xFF92400E)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val memberSecondaryTextColor = when {
        member.isPendingRemoval -> Color(0xFFBE123C)
        member.isPendingRegistration -> Color(0xFF1D4ED8)
        member.isPendingPayment || !member.isPaidThisMonth -> Color(0xFFB45309)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("member_item_${member.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        ),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Status alert banner if any flag is active
            if (member.isPendingRemoval || member.isPendingRegistration || member.isPendingPayment || !member.isPaidThisMonth) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (member.isPendingRemoval) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEF4444)
                        ) {
                            Text(
                                text = "⚠️ Pendiente eliminar",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                            )
                        }
                    }
                    if (member.isPendingRegistration) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF3B82F6)
                        ) {
                            Text(
                                text = "👤 Pendiente dar de alta",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                            )
                        }
                    }
                    if (member.isPendingPayment || !member.isPaidThisMonth) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF59E0B)
                        ) {
                            Text(
                                text = "⏳ Pendiente de pago",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar initial
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                member.isPendingRemoval -> Color(0xFFFECACA)
                                member.isPendingRegistration -> Color(0xFFBFDBFE)
                                member.isPendingPayment || !member.isPaidThisMonth -> Color(0xFFFDE68A)
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.memberName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = when {
                            member.isPendingRemoval -> Color(0xFF991B1B)
                            member.isPendingRegistration -> Color(0xFF1E40AF)
                            member.isPendingPayment || !member.isPaidThisMonth -> Color(0xFF92400E)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Línea 1: Nombre de usuario y al lado la cantidad que aporta
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.memberName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 16.sp
                            ),
                            color = memberTextColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ProfitGreenBg
                        ) {
                            Text(
                                text = "+${String.format(Locale.getDefault(), "%.2f", member.contributionAmount)} €",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ProfitGreen
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Línea 2: Plataforma de compartición y al lado la fecha en la que se une
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (member.sharingPlatform.isNotBlank()) {
                            val platformInfo = SharingPlatforms.getInfo(member.sharingPlatform, availablePlatforms)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = platformInfo.badgeBgColor
                            ) {
                                Text(
                                    text = platformInfo.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = platformInfo.badgeTextColor
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = memberSecondaryTextColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Unido: ${dateFormat.format(Date(member.joinedDate))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = memberSecondaryTextColor
                            )
                        }
                    }
                }
            }

            if (member.memberContact.isNotBlank() || member.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (member.memberContact.isNotBlank()) {
                        Text(
                            text = "Contacto: ${member.memberContact}",
                            style = MaterialTheme.typography.labelSmall,
                            color = memberSecondaryTextColor
                        )
                    }
                    if (member.notes.isNotBlank()) {
                        Text(
                            text = "• ${member.notes}",
                            style = MaterialTheme.typography.labelSmall,
                            color = memberSecondaryTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(6.dp))

            // Action Toolbar: Payment Switch, Reminder, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Payment toggle switch
                val isPaid = member.isPaidThisMonth && !member.isPendingPayment
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("member_payment_toggle_${member.id}")
                ) {
                    Switch(
                        checked = isPaid,
                        onCheckedChange = { onTogglePayment() },
                        thumbContent = {
                            Icon(
                                imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ProfitGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFF59E0B)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPaid) "Cuota Pagada" else "Pago Pendiente",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (isPaid) ProfitGreen else Color(0xFFD97706)
                        )
                    )
                }

                // Action buttons: Reminder, Edit, Delete
                Row {
                    IconButton(
                        onClick = onReminder,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("member_reminder_btn_${member.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "Enviar recordatorio",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("member_edit_btn_${member.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar usuario",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("member_delete_btn_${member.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar usuario",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/screens/AuthGateScreen.kt`

```kotlin
package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.AuthState
import com.example.ui.components.SplitzyLogo
import com.example.ui.viewmodel.SubscriptionViewModel

@Composable
fun AuthGateScreen(
    viewModel: SubscriptionViewModel,
    authState: AuthState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Crear cuenta, 1 = Iniciar sesión
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val isLoading = authState is AuthState.Loading

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Brand Header
                SplitzyLogo(size = 72.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Splitzy",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Control inteligente de suscripciones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Error Message Card
                if (authState is AuthState.Error) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = authState.message,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { viewModel.clearAuthError() },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    "Aceptar",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Auth Mode Tabs (Crear cuenta / Iniciar sesión)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            viewModel.clearAuthError()
                        },
                        text = {
                            Text(
                                "Crear cuenta",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            viewModel.clearAuthError()
                        },
                        text = {
                            Text(
                                "Iniciar sesión",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Google Sign In
                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.signInWithGoogle()
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_auth_gate_google"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (selectedTab == 0) "Registrarme con Google" else "Continuar con Google",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "o con correo electrónico",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_auth_gate_email"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(if (selectedTab == 0) "Crear contraseña (mínimo 6 caracteres)" else "Contraseña")
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (email.isNotBlank() && password.isNotBlank()) {
                                if (selectedTab == 0) {
                                    viewModel.registerWithEmail(email.trim(), password.trim())
                                } else {
                                    viewModel.signInWithEmail(email.trim(), password.trim())
                                }
                            }
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_auth_gate_password"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (selectedTab == 0) {
                            viewModel.registerWithEmail(email.trim(), password.trim())
                        } else {
                            viewModel.signInWithEmail(email.trim(), password.trim())
                        }
                    },
                    enabled = !isLoading && email.isNotBlank() && password.length >= 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_auth_gate_submit"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Verificando...")
                    } else {
                        Text(
                            text = if (selectedTab == 0) "Crear mi Cuenta" else "Iniciar Sesión",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Value propositions / Cloud backup benefits
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BenefitRow(
                            icon = Icons.Default.Sync,
                            title = "Sincronización en la Nube",
                            subtitle = "Tus suscripciones y miembros seguros y accesibles en cualquier dispositivo."
                        )
                        BenefitRow(
                            icon = Icons.Default.Shield,
                            title = "Privacidad y Control",
                            subtitle = "Tus datos viajan encriptados y asociados únicamente a tu cuenta."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun BenefitRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/screens/HomeScreen.kt`

```kotlin
package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.SubscriptionEntity
import com.example.ui.components.AddEditMemberDialog
import com.example.ui.components.AddEditSubscriptionDialog
import com.example.ui.components.FinancialSummaryCard
import com.example.ui.components.ReminderMessageDialog
import com.example.ui.components.SplitzyLogo
import com.example.ui.components.SubscriptionCard
import com.example.ui.components.SubscriptionDetailSheet
import com.example.ui.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SubscriptionViewModel,
    modifier: Modifier = Modifier
) {
    val allSubscriptions by viewModel.allSubscriptions.collectAsStateWithLifecycle()
    val filteredSubscriptions by viewModel.filteredSubscriptions.collectAsStateWithLifecycle()
    val financialOverview by viewModel.financialOverview.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()

    val selectedSubscriptionId by viewModel.selectedSubscriptionId.collectAsStateWithLifecycle()
    val showAddEditSubDialog by viewModel.showAddEditSubscriptionDialog.collectAsStateWithLifecycle()
    val subscriptionToEdit by viewModel.subscriptionToEdit.collectAsStateWithLifecycle()

    val showAddEditMemberDialog by viewModel.showAddEditMemberDialog.collectAsStateWithLifecycle()
    val memberToEdit by viewModel.memberToEdit.collectAsStateWithLifecycle()
    val targetSubForMember by viewModel.targetSubscriptionForNewMember.collectAsStateWithLifecycle()

    val reminderData by viewModel.reminderMemberData.collectAsStateWithLifecycle()

    var subscriptionToDelete by remember { mutableStateOf<SubscriptionEntity?>(null) }

    val categories = listOf("Todas", "Streaming", "Música", "Productividad", "Gaming", "Educación", "Salud")

    val showBackupRestoreDialog by viewModel.showBackupRestoreDialog.collectAsStateWithLifecycle()
    val pendingRestorePreview by viewModel.pendingRestorePreview.collectAsStateWithLifecycle()
    val backupStatusMessage by viewModel.backupStatusMessage.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val showAuthDialog by viewModel.showAuthDialog.collectAsStateWithLifecycle()
    val showAppMenu by viewModel.showAppMenu.collectAsStateWithLifecycle()
    val sharingPlatforms by viewModel.sharingPlatforms.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Find the currently selected subscription with members for the detail sheet
    val currentSelectedSub = allSubscriptions.find { it.subscription.id == selectedSubscriptionId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.openAppMenu() }
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .testTag("btn_app_logo_menu")
                    ) {
                        SplitzyLogo(size = 38.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Splitzy",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Menú y Configuración",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (authState is com.example.data.remote.AuthState.Authenticated) {
                                    (authState as com.example.data.remote.AuthState.Authenticated).user.email ?: "En la nube"
                                } else {
                                    "Toca para abrir ajustes"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Quick Settings Icon Button
                    IconButton(
                        onClick = { viewModel.openSettingsScreen() },
                        modifier = Modifier.testTag("btn_top_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Account / Cloud Sync Button with Google Avatar or Icon
                    IconButton(
                        onClick = { viewModel.openAuthDialog() },
                        modifier = Modifier.testTag("btn_auth_account")
                    ) {
                        val currentAuthState = authState
                        if (currentAuthState is com.example.data.remote.AuthState.Authenticated) {
                            val user = currentAuthState.user
                            if (user.photoUrl != null) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = "Cuenta de Google: ${user.displayName ?: user.email}",
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (user.displayName?.firstOrNull() ?: user.email?.firstOrNull() ?: 'U').uppercase(),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Cuenta y Sincronización en la nube",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddSubscription() },
                icon = { Icon(Icons.Default.Add, contentDescription = "Añadir suscripción") },
                text = { Text("Nueva Suscripción", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_subscription")
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Financial Overview Summary Card
            item {
                FinancialSummaryCard(overview = financialOverview)
            }

            // 2. Search & Category Filters
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Buscar", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.setSearchQuery("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Limpiar",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_bar_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSelectedCategory(category) },
                                label = { Text(category) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sorting Options Chips (Alfabético, Fecha de renovación, etc.)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Ordenar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ordenar:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        com.example.ui.viewmodel.SubscriptionSortOrder.values().forEach { order ->
                            val isSelected = sortOrder == order
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSortOrder(order) },
                                label = {
                                    Text(
                                        text = order.chipText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // 3. Section Title & Subscriptions List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis Suscripciones (${filteredSubscriptions.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (filteredSubscriptions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subscriptions,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank() || selectedCategory != "Todas") {
                                    "No se encontraron suscripciones con los filtros actuales"
                                } else {
                                    "No tienes ninguna suscripción registrada"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pulsa el botón '+ Nueva Suscripción' para añadir tus servicios compartidos y llevar el control de los usuarios.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.openAddSubscription() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Añadir Suscripción")
                            }
                        }
                    }
                }
            } else {
                items(filteredSubscriptions, key = { it.subscription.id }) { item ->
                    SubscriptionCard(
                        subscriptionWithMembers = item,
                        searchQuery = searchQuery,
                        availablePlatforms = sharingPlatforms,
                        onClick = { viewModel.openSubscriptionDetail(item.subscription.id) },
                        onAddMemberClick = { viewModel.openAddMember(item) },
                        onEditClick = { viewModel.openEditSubscription(item.subscription) },
                        onDeleteClick = { subscriptionToDelete = item.subscription },
                        onMemberClick = { member -> viewModel.openEditMember(member, item) }
                    )
                }
            }
        }
    }

    // Detail Bottom Sheet
    currentSelectedSub?.let { selectedSub ->
        SubscriptionDetailSheet(
            subscriptionWithMembers = selectedSub,
            onDismiss = { viewModel.closeSubscriptionDetail() },
            onAddMemberClick = { viewModel.openAddMember(selectedSub) },
            onEditMemberClick = { member -> viewModel.openEditMember(member, selectedSub) },
            onDeleteMemberClick = { member -> viewModel.deleteMember(member) },
            onToggleMemberPayment = { memberId, currentStatus ->
                viewModel.toggleMemberPaymentStatus(memberId, currentStatus)
            },
            onReminderClick = { member -> viewModel.openReminderGenerator(member, selectedSub.subscription) },
            onEditSubscriptionClick = {
                viewModel.closeSubscriptionDetail()
                viewModel.openEditSubscription(selectedSub.subscription)
            },
            onDeleteSubscriptionClick = {
                viewModel.deleteSubscription(selectedSub.subscription)
            },
            availablePlatforms = sharingPlatforms
        )
    }

    // Add / Edit Subscription Dialog
    if (showAddEditSubDialog) {
        AddEditSubscriptionDialog(
            subscriptionToEdit = subscriptionToEdit,
            onDismiss = { viewModel.closeAddEditSubscription() },
            onSave = { entity -> viewModel.saveSubscription(entity) },
            availablePlatforms = sharingPlatforms
        )
    }

    // Add / Edit Member Dialog
    if (showAddEditMemberDialog && targetSubForMember != null) {
        AddEditMemberDialog(
            targetSubscription = targetSubForMember!!,
            memberToEdit = memberToEdit,
            onDismiss = { viewModel.closeAddEditMember() },
            onSave = { entity -> viewModel.saveMember(entity) },
            availablePlatforms = sharingPlatforms
        )
    }

    // App Menu Bottom Sheet (Opened from App Logo)
    if (showAppMenu) {
        com.example.ui.components.AppMenuSheet(
            onDismiss = { viewModel.closeAppMenu() },
            onOpenSettings = { viewModel.openSettingsScreen() },
            onOpenBackupRestore = {
                viewModel.closeAppMenu()
                viewModel.openBackupRestoreDialog()
            },
            onOpenCloudSync = {
                viewModel.closeAppMenu()
                viewModel.openAuthDialog()
            },
            userEmail = (authState as? com.example.data.remote.AuthState.Authenticated)?.user?.email
        )
    }


    // Payment Reminder Message Generator Dialog
    reminderData?.let { (member, sub) ->
        ReminderMessageDialog(
            member = member,
            subscription = sub,
            onDismiss = { viewModel.closeReminderGenerator() }
        )
    }

    // Delete Subscription Confirmation Dialog
    subscriptionToDelete?.let { sub ->
        AlertDialog(
            onDismissRequest = { subscriptionToDelete = null },
            title = { Text("Eliminar suscripción") },
            text = {
                Text("¿Estás seguro de que deseas eliminar la suscripción \"${sub.platformName}\" y todos sus miembros asociados?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubscription(sub)
                        subscriptionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { subscriptionToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Backup & Restore Dialog
    if (showBackupRestoreDialog) {
        com.example.ui.components.BackupRestoreDialog(
            onDismissRequest = { viewModel.closeBackupRestoreDialog() },
            onGetBackupJson = { viewModel.getBackupJson() },
            onPreviewBackup = { json -> viewModel.previewBackupContent(json) },
            pendingPreview = pendingRestorePreview,
            onConfirmRestore = { replaceExisting -> viewModel.executeRestore(replaceExisting) },
            onDismissPreview = { viewModel.dismissRestorePreview() }
        )
    }

    // Auth & Cloud Sync Dialog
    if (showAuthDialog) {
        com.example.ui.components.AuthAccountDialog(
            authState = authState,
            isSyncing = isSyncing,
            onDismissRequest = { viewModel.closeAuthDialog() },
            onSignInWithGoogle = { viewModel.signInWithGoogle() },
            onSignInWithEmail = { email, pass -> viewModel.signInWithEmail(email, pass) },
            onRegisterWithEmail = { email, pass -> viewModel.registerWithEmail(email, pass) },
            onSignOut = { viewModel.signOut() },
            onSyncToCloud = { viewModel.syncToCloud() },
            onSyncFromCloud = { viewModel.syncFromCloud() },
            onClearError = { viewModel.clearAuthError() }
        )
    }

    // Sync status toast
    syncStatus?.let { msg ->
        LaunchedEffect(msg) {
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearSyncStatus()
        }
    }

    // Toast feedback message
    backupStatusMessage?.let { msg ->
        LaunchedEffect(msg) {
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearBackupStatusMessage()
        }
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/screens/SettingsScreen.kt`

```kotlin
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.SharingPlatformEntity
import com.example.data.model.SharingPlatforms
import com.example.data.util.AppThemeMode
import com.example.ui.components.AddEditPlatformDialog
import com.example.ui.components.DeletePlatformConfirmDialog
import com.example.ui.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SubscriptionViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentThemeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val platforms by viewModel.sharingPlatforms.collectAsStateWithLifecycle()

    val showAddEditPlatformDialog by viewModel.showAddEditPlatformDialog.collectAsStateWithLifecycle()
    val platformToEdit by viewModel.platformToEdit.collectAsStateWithLifecycle()
    val platformToDelete by viewModel.platformToDelete.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configuración",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_settings_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // ==========================================
            // SECCIÓN 1: APARIENCIA / TEMA
            // ==========================================
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Apariencia",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Elige el tema visual de la aplicación",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOptionCard(
                                title = "Claro",
                                description = "Fondo luminoso con contraste optimizado",
                                icon = Icons.Default.LightMode,
                                isSelected = currentThemeMode == AppThemeMode.LIGHT,
                                onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                                testTag = "theme_option_light"
                            )

                            ThemeOptionCard(
                                title = "Oscuro",
                                description = "Fondo oscuro de alto contraste y ahorro de energía",
                                icon = Icons.Default.DarkMode,
                                isSelected = currentThemeMode == AppThemeMode.DARK,
                                onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                                testTag = "theme_option_dark"
                            )

                            ThemeOptionCard(
                                title = "Usar configuración del sistema",
                                description = "Se adapta automáticamente al tema de tu dispositivo Android",
                                icon = Icons.Default.BrightnessAuto,
                                isSelected = currentThemeMode == AppThemeMode.SYSTEM,
                                onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) },
                                testTag = "theme_option_system"
                            )
                        }
                    }
                }
            }

            // ==========================================
            // SECCIÓN 2: GESTIÓN DE PLATAFORMAS DE COMPARTICIÓN
            // ==========================================
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Gestión de plataformas de compartición",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${platforms.size} plataformas configuradas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.openAddSharingPlatform() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_add_platform_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Añadir", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Personaliza al 100% las plataformas que usas para compartir gastos. Modifica sus nombres, colores o añade nuevas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Platforms List items
            if (platforms.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No hay plataformas creadas",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Puedes crear una plataforma personalizada o restaurar las 6 plataformas iniciales.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { viewModel.restoreDefaultPlatforms() },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restablecer plataformas iniciales")
                            }
                        }
                    }
                }
            } else {
                items(platforms, key = { it.id }) { platform ->
                    PlatformItemCard(
                        platform = platform,
                        onEdit = { viewModel.openEditSharingPlatform(platform) },
                        onDelete = { viewModel.openDeleteSharingPlatformConfirm(platform) }
                    )
                }
            }

            // Reset Platforms to Defaults button
            if (platforms.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = { viewModel.restoreDefaultPlatforms() },
                            modifier = Modifier.testTag("btn_restore_default_platforms")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restaurar las 6 plataformas iniciales")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Dialogs for Add/Edit and Delete
    if (showAddEditPlatformDialog) {
        AddEditPlatformDialog(
            platformToEdit = platformToEdit,
            onDismiss = { viewModel.closeAddEditSharingPlatform() },
            onSave = { entity -> viewModel.saveSharingPlatform(entity) }
        )
    }

    platformToDelete?.let { target ->
        DeletePlatformConfirmDialog(
            platform = target,
            onDismiss = { viewModel.closeDeleteSharingPlatformConfirm() },
            onConfirm = { viewModel.confirmDeleteSharingPlatform() }
        )
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun PlatformItemCard(
    platform: SharingPlatformEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val platformColor = SharingPlatforms.parseColor(platform.colorHex)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("platform_item_${platform.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Color Circle Swatch
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(platformColor)
                    .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = platform.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Badge preview pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = platformColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Vista: ${platform.name}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = platformColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Edit Action Button
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("btn_edit_platform_${platform.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar ${platform.name}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Delete Action Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("btn_delete_platform_${platform.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar ${platform.name}",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/theme/Color.kt`

```kotlin
package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme Palette
val IndigoPrimary = Color(0xFF4338CA)
val IndigoOnPrimary = Color(0xFFFFFFFF)
val IndigoPrimaryContainer = Color(0xFFEEF2FF)
val IndigoOnPrimaryContainer = Color(0xFF312E81)

val EmeraldSecondary = Color(0xFF059669)
val EmeraldOnSecondary = Color(0xFFFFFFFF)
val EmeraldSecondaryContainer = Color(0xFFECFDF5)
val EmeraldOnSecondaryContainer = Color(0xFF064E3B)

val AmberTertiary = Color(0xFFD97706)
val AmberOnTertiary = Color(0xFFFFFFFF)
val AmberTertiaryContainer = Color(0xFFFFFBEB)
val AmberOnTertiaryContainer = Color(0xFF78350F)

val LightBackground = Color(0xFFF8FAFC)
val LightOnBackground = Color(0xFF0F172A)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF0F172A)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnSurfaceVariant = Color(0xFF475569)
val LightOutline = Color(0xFFCBD5E1)
val LightOutlineVariant = Color(0xFFE2E8F0)

// Dark Theme Palette
val IndigoPrimaryDark = Color(0xFF818CF8)
val IndigoOnPrimaryDark = Color(0xFF1E1B4B)
val IndigoPrimaryContainerDark = Color(0xFF312E81)
val IndigoOnPrimaryContainerDark = Color(0xFFE0E7FF)

val EmeraldSecondaryDark = Color(0xFF34D399)
val EmeraldOnSecondaryDark = Color(0xFF064E3B)
val EmeraldSecondaryContainerDark = Color(0xFF065F46)
val EmeraldOnSecondaryContainerDark = Color(0xFFA7F3D0)

val AmberTertiaryDark = Color(0xFFFBBF24)
val AmberOnTertiaryDark = Color(0xFF78350F)
val AmberTertiaryContainerDark = Color(0xFF92400E)
val AmberOnTertiaryContainerDark = Color(0xFFFDE68A)

val DarkBackground = Color(0xFF0B0F19)
val DarkOnBackground = Color(0xFFF8FAFC)
val DarkSurface = Color(0xFF131B2E)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF334155)
val DarkOutlineVariant = Color(0xFF1E293B)

// Accent Status Colors
val ProfitGreen = Color(0xFF10B981)
val ProfitGreenBg = Color(0xFFD1FAE5)
val LossRed = Color(0xFFEF4444)
val LossRedBg = Color(0xFFFEE2E2)
val NeutralBlue = Color(0xFF3B82F6)
val NeutralBlueBg = Color(0xFFDBEAFE)
val WarningAmber = Color(0xFFF59E0B)
val WarningAmberBg = Color(0xFFFEF3C7)

```


## ARCHIVO: `app/src/main/java/com/example/ui/theme/Theme.kt`

```kotlin
package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryDark,
    onPrimary = IndigoOnPrimaryDark,
    primaryContainer = IndigoPrimaryContainerDark,
    onPrimaryContainer = IndigoOnPrimaryContainerDark,
    secondary = EmeraldSecondaryDark,
    onSecondary = EmeraldOnSecondaryDark,
    secondaryContainer = EmeraldSecondaryContainerDark,
    onSecondaryContainer = EmeraldOnSecondaryContainerDark,
    tertiary = AmberTertiaryDark,
    onTertiary = AmberOnTertiaryDark,
    tertiaryContainer = AmberTertiaryContainerDark,
    onTertiaryContainer = AmberOnTertiaryContainerDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = IndigoOnPrimary,
    primaryContainer = IndigoPrimaryContainer,
    onPrimaryContainer = IndigoOnPrimaryContainer,
    secondary = EmeraldSecondary,
    onSecondary = EmeraldOnSecondary,
    secondaryContainer = EmeraldSecondaryContainer,
    onSecondaryContainer = EmeraldOnSecondaryContainer,
    tertiary = AmberTertiary,
    onTertiary = AmberOnTertiary,
    tertiaryContainer = AmberTertiaryContainer,
    onTertiaryContainer = AmberOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted custom palette by default for maximum brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/theme/Type.kt`

```kotlin
package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography =
  Typography(
    bodyLarge =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
      )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
  )

```


## ARCHIVO: `app/src/main/java/com/example/ui/util/ImageStorageHelper.kt`

```kotlin
package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {

    private const val MAX_ICON_DIMENSION = 256
    private const val COMPRESSION_QUALITY = 85

    /**
     * Guarda una imagen seleccionada desde la galería optimizándola a resolución mínima
     * (~256x256 px JPEG/PNG, ~15-25 KB) para no saturar memoria ni almacenamiento.
     */
    fun saveImageFromUri(context: Context, sourceUri: Uri): String? {
        return try {
            val bitmap = decodeSampledBitmapFromUri(context, sourceUri, MAX_ICON_DIMENSION, MAX_ICON_DIMENSION)
                ?: return null

            val iconsDir = File(context.filesDir, "subscription_icons").apply {
                if (!exists()) mkdirs()
            }

            val fileName = "icon_${System.currentTimeMillis()}.jpg"
            val destFile = File(iconsDir, fileName)

            FileOutputStream(destFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, out)
                out.flush()
            }
            bitmap.recycle()

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convierte una imagen (por ruta local o URI) en una cadena Base64 compacta y ligera
     * ideal para incluir dentro del JSON de copia de seguridad o sincronización en la nube.
     */
    fun imageToBase64(
        context: Context,
        imagePathOrUri: String,
        maxDim: Int = MAX_ICON_DIMENSION,
        quality: Int = COMPRESSION_QUALITY
    ): String? {
        return try {
            if (imagePathOrUri.isBlank()) return null
            val bitmap = if (imagePathOrUri.startsWith("content://") || imagePathOrUri.startsWith("file://")) {
                decodeSampledBitmapFromUri(context, Uri.parse(imagePathOrUri), maxDim, maxDim)
            } else {
                val file = File(imagePathOrUri)
                if (!file.exists()) return null
                decodeSampledBitmapFromFile(file.absolutePath, maxDim, maxDim)
            } ?: return null

            val byteStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteStream)
            val bytes = byteStream.toByteArray()
            bitmap.recycle()

            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Restaura una imagen guardada en Base64 en el almacenamiento interno privado de la app
     * y devuelve la ruta absoluta del archivo para asociarla a la suscripción.
     */
    fun saveBase64Image(context: Context, base64String: String, identifier: String = "icon"): String? {
        return try {
            if (base64String.isBlank()) return null
            val bytes = Base64.decode(base64String, Base64.DEFAULT)
            if (bytes == null || bytes.isEmpty()) return null

            val iconsDir = File(context.filesDir, "subscription_icons").apply {
                if (!exists()) mkdirs()
            }

            val fileName = "icon_${identifier}_${System.currentTimeMillis()}.jpg"
            val destFile = File(iconsDir, fileName)

            FileOutputStream(destFile).use { out ->
                out.write(bytes)
                out.flush()
            }

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun decodeSampledBitmapFromUri(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            var bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null

            if (bitmap.width > reqWidth || bitmap.height > reqHeight) {
                val scale = minOf(reqWidth.toFloat() / bitmap.width, reqHeight.toFloat() / bitmap.height)
                val targetW = (bitmap.width * scale).toInt().coerceAtLeast(1)
                val targetH = (bitmap.height * scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
                if (scaled != bitmap) {
                    bitmap.recycle()
                    bitmap = scaled
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun decodeSampledBitmapFromFile(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, options)

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            var bitmap = BitmapFactory.decodeFile(path, options) ?: return null

            if (bitmap.width > reqWidth || bitmap.height > reqHeight) {
                val scale = minOf(reqWidth.toFloat() / bitmap.width, reqHeight.toFloat() / bitmap.height)
                val targetW = (bitmap.width * scale).toInt().coerceAtLeast(1)
                val targetH = (bitmap.height * scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
                if (scaled != bitmap) {
                    bitmap.recycle()
                    bitmap = scaled
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}


```


## ARCHIVO: `app/src/main/java/com/example/ui/util/TextHighlightHelper.kt`

```kotlin
package com.example.ui.util

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

object TextHighlightHelper {

    // Pastel yellow highlight colors
    val PastelYellowBg = Color(0xFFFEF08A) // Tailwind Yellow 200 (Warm pastel yellow)
    val HighlightDarkText = Color(0xFF713F12) // Warm dark text for strong contrast on yellow

    fun buildHighlightedText(
        text: String,
        query: String,
        highlightBgColor: Color = PastelYellowBg,
        highlightTextColor: Color = HighlightDarkText
    ): AnnotatedString {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty() || !text.contains(trimmedQuery, ignoreCase = true)) {
            return AnnotatedString(text)
        }

        val builder = AnnotatedString.Builder()
        val lowerText = text.lowercase()
        val lowerQuery = trimmedQuery.lowercase()

        var startIndex = 0
        while (startIndex < text.length) {
            val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
            if (matchIndex == -1) {
                builder.append(text.substring(startIndex))
                break
            }

            if (matchIndex > startIndex) {
                builder.append(text.substring(startIndex, matchIndex))
            }

            val endIndex = matchIndex + lowerQuery.length
            builder.pushStyle(
                SpanStyle(
                    background = highlightBgColor,
                    color = highlightTextColor,
                    fontWeight = FontWeight.Bold
                )
            )
            builder.append(text.substring(matchIndex, endIndex))
            builder.pop()

            startIndex = endIndex
        }

        return builder.toAnnotatedString()
    }
}

@Composable
fun HighlightedText(
    text: String,
    query: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    highlightBgColor: Color = TextHighlightHelper.PastelYellowBg,
    highlightTextColor: Color = TextHighlightHelper.HighlightDarkText,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null
) {
    val annotatedString = TextHighlightHelper.buildHighlightedText(
        text = text,
        query = query,
        highlightBgColor = highlightBgColor,
        highlightTextColor = highlightTextColor
    )

    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign
    )
}

```


## ARCHIVO: `app/src/main/java/com/example/ui/viewmodel/SubscriptionViewModel.kt`

```kotlin
package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.MemberEntity
import com.example.data.local.SharingPlatformEntity
import com.example.data.local.SubscriptionEntity
import com.example.data.local.SubscriptionWithMembers
import com.example.data.model.SharingPlatforms
import com.example.data.remote.AuthState
import com.example.data.remote.FirebaseAuthService
import com.example.data.repository.SubscriptionRepository
import com.example.data.util.AppThemeMode
import com.example.data.util.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FinancialOverview(
    val totalCost: Double = 0.0,
    val totalContributed: Double = 0.0,
    val netBalance: Double = 0.0,
    val totalSubscriptionsCount: Int = 0,
    val totalMembersCount: Int = 0,
    val pendingPaymentsCount: Int = 0,
    val pendingAmount: Double = 0.0,
    val profitSubscriptionsCount: Int = 0
)

enum class SubscriptionSortOrder(val label: String, val chipText: String) {
    DEFAULT("Por defecto", "Recientes"),
    ALPHABETICAL("Alfabético (A-Z)", "🔤 Alfabético"),
    RENEWAL_DATE("Fecha de renovación", "📅 Por Renovación")
}

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SubscriptionRepository
    private val authService: FirebaseAuthService
    private val themePreferences: ThemePreferences = ThemePreferences(application)

    private val _themeMode = MutableStateFlow(themePreferences.getThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        themePreferences.setThemeMode(mode)
    }

    init {
        val database = AppDatabase.getInstance(application)
        val dao = database.subscriptionDao()
        repository = SubscriptionRepository(dao)
        authService = FirebaseAuthService(
            context = application.applicationContext,
            dao = dao,
            scope = viewModelScope
        )
        viewModelScope.launch {
            repository.ensureDefaultPlatformsSeeded()
            com.example.data.util.CurrencyRateService.fetchLatestRates()
        }
    }

    // Dynamic Sharing Platforms Flow
    val sharingPlatforms: StateFlow<List<SharingPlatformEntity>> = repository.allSharingPlatforms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SharingPlatforms.defaultList
        )


    val authState: StateFlow<AuthState> = authService.authState
    val isSyncing: StateFlow<Boolean> = authService.isSyncing
    val syncStatus: StateFlow<String?> = authService.syncStatus

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog

    fun openAuthDialog() {
        _showAuthDialog.value = true
    }

    fun closeAuthDialog() {
        _showAuthDialog.value = false
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            authService.signInWithGoogle()
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            authService.signInWithEmail(email, pass)
        }
    }

    fun registerWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            authService.registerWithEmail(email, pass)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
        }
    }

    fun syncToCloud() {
        viewModelScope.launch {
            authService.syncToCloud()
        }
    }

    fun syncFromCloud() {
        viewModelScope.launch {
            authService.syncFromCloud()
        }
    }

    fun clearAuthError() {
        authService.clearError()
    }

    fun clearSyncStatus() {
        authService.clearSyncStatus()
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("Todas")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _sortOrder = MutableStateFlow(SubscriptionSortOrder.DEFAULT)
    val sortOrder: StateFlow<SubscriptionSortOrder> = _sortOrder

    val allSubscriptions: StateFlow<List<SubscriptionWithMembers>> = repository.allSubscriptions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredSubscriptions: StateFlow<List<SubscriptionWithMembers>> = combine(
        repository.allSubscriptions,
        _searchQuery,
        _selectedCategory,
        _sortOrder
    ) { subs, query, category, sort ->
        val filtered = subs.filter { item ->
            val matchesCategory = (category == "Todas") ||
                    item.subscription.category.equals(category, ignoreCase = true) ||
                    item.subscription.platformName.contains(category, ignoreCase = true)

            val matchesQuery = query.isBlank() ||
                    item.subscription.platformName.contains(query, ignoreCase = true) ||
                    item.subscription.customPlanName.contains(query, ignoreCase = true) ||
                    item.subscription.mainUserName.contains(query, ignoreCase = true) ||
                    item.members.any { it.memberName.contains(query, ignoreCase = true) }

            matchesCategory && matchesQuery
        }

        when (sort) {
            SubscriptionSortOrder.ALPHABETICAL -> filtered.sortedBy { it.subscription.platformName.lowercase() }
            SubscriptionSortOrder.RENEWAL_DATE -> filtered.sortedBy { it.nextRenewalTimestamp }
            SubscriptionSortOrder.DEFAULT -> filtered.sortedByDescending { it.subscription.createdAt }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val financialOverview: StateFlow<FinancialOverview> = repository.allSubscriptions
        .combine(_searchQuery) { subs, _ ->
            val totalCost = subs.sumOf { it.myCostMonthly }
            val totalContributed = subs.sumOf { it.totalContributed }
            val netBalance = totalContributed - totalCost
            val totalSubs = subs.size
            val totalMembers = subs.sumOf { it.members.size }
            val pendingMembers = subs.flatMap { it.members }.filter { !it.isPaidThisMonth }
            val pendingAmount = pendingMembers.sumOf { it.contributionAmount }
            val profitSubs = subs.count { it.isNetProfit }

            FinancialOverview(
                totalCost = totalCost,
                totalContributed = totalContributed,
                netBalance = netBalance,
                totalSubscriptionsCount = totalSubs,
                totalMembersCount = totalMembers,
                pendingPaymentsCount = pendingMembers.size,
                pendingAmount = pendingAmount,
                profitSubscriptionsCount = profitSubs
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FinancialOverview()
        )

    // UI Dialog & Navigation States
    private val _selectedSubscriptionId = MutableStateFlow<Long?>(null)
    val selectedSubscriptionId: StateFlow<Long?> = _selectedSubscriptionId

    private val _showAddEditSubscriptionDialog = MutableStateFlow(false)
    val showAddEditSubscriptionDialog: StateFlow<Boolean> = _showAddEditSubscriptionDialog

    private val _subscriptionToEdit = MutableStateFlow<SubscriptionEntity?>(null)
    val subscriptionToEdit: StateFlow<SubscriptionEntity?> = _subscriptionToEdit

    private val _showAddEditMemberDialog = MutableStateFlow(false)
    val showAddEditMemberDialog: StateFlow<Boolean> = _showAddEditMemberDialog

    private val _memberToEdit = MutableStateFlow<MemberEntity?>(null)
    val memberToEdit: StateFlow<MemberEntity?> = _memberToEdit

    private val _targetSubscriptionForNewMember = MutableStateFlow<SubscriptionWithMembers?>(null)
    val targetSubscriptionForNewMember: StateFlow<SubscriptionWithMembers?> = _targetSubscriptionForNewMember

    private val _reminderMemberData = MutableStateFlow<Pair<MemberEntity, SubscriptionEntity>?>(null)
    val reminderMemberData: StateFlow<Pair<MemberEntity, SubscriptionEntity>?> = _reminderMemberData

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSortOrder(order: SubscriptionSortOrder) {
        _sortOrder.value = order
    }

    fun openSubscriptionDetail(subscriptionId: Long) {
        _selectedSubscriptionId.value = subscriptionId
    }

    fun closeSubscriptionDetail() {
        _selectedSubscriptionId.value = null
    }

    fun openAddSubscription() {
        _subscriptionToEdit.value = null
        _showAddEditSubscriptionDialog.value = true
    }

    fun openEditSubscription(subscription: SubscriptionEntity) {
        _subscriptionToEdit.value = subscription
        _showAddEditSubscriptionDialog.value = true
    }

    fun closeAddEditSubscription() {
        _showAddEditSubscriptionDialog.value = false
        _subscriptionToEdit.value = null
    }

    fun saveSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            if (subscription.id == 0L) {
                repository.insertSubscription(subscription)
            } else {
                repository.updateSubscription(subscription)
            }
            closeAddEditSubscription()
            // Sincronización automática a la nube en segundo plano si el usuario está autenticado
            if (authState.value is AuthState.Authenticated) {
                authService.syncToCloud()
            }
        }
    }

    fun deleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
            if (_selectedSubscriptionId.value == subscription.id) {
                closeSubscriptionDetail()
            }
            // Sincronización automática a la nube en segundo plano si el usuario está autenticado
            if (authState.value is AuthState.Authenticated) {
                authService.deleteSubscriptionFromCloud(subscription.id)
            }
        }
    }

    fun openAddMember(subscriptionWithMembers: SubscriptionWithMembers) {
        _targetSubscriptionForNewMember.value = subscriptionWithMembers
        _memberToEdit.value = null
        _showAddEditMemberDialog.value = true
    }

    fun openEditMember(member: MemberEntity, subscriptionWithMembers: SubscriptionWithMembers) {
        _targetSubscriptionForNewMember.value = subscriptionWithMembers
        _memberToEdit.value = member
        _showAddEditMemberDialog.value = true
    }

    fun closeAddEditMember() {
        _showAddEditMemberDialog.value = false
        _memberToEdit.value = null
        _targetSubscriptionForNewMember.value = null
    }

    fun saveMember(member: MemberEntity) {
        viewModelScope.launch {
            if (member.id == 0L) {
                repository.insertMember(member)
            } else {
                repository.updateMember(member)
            }
            closeAddEditMember()
            // Sincronización automática a la nube en segundo plano si el usuario está autenticado
            if (authState.value is AuthState.Authenticated) {
                authService.syncToCloud()
            }
        }
    }

    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
            // Sincronización automática a la nube en segundo plano si el usuario está autenticado
            if (authState.value is AuthState.Authenticated) {
                authService.syncToCloud()
            }
        }
    }

    fun toggleMemberPaymentStatus(memberId: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleMemberPayment(memberId, !currentStatus)
            if (authState.value is AuthState.Authenticated) {
                authService.syncToCloud()
            }
        }
    }

    fun toggleMemberPendingPayment(memberId: Long, isPending: Boolean) {
        viewModelScope.launch {
            repository.toggleMemberPendingPayment(memberId, isPending)
            if (authState.value is AuthState.Authenticated) {
                authService.syncToCloud()
            }
        }
    }

    fun toggleMemberPendingRemoval(memberId: Long, isPending: Boolean) {
        viewModelScope.launch {
            repository.toggleMemberPendingRemoval(memberId, isPending)
            if (authState.value is AuthState.Authenticated) {
                authService.syncToCloud()
            }
        }
    }

    fun toggleMemberPendingRegistration(memberId: Long, isPending: Boolean) {
        viewModelScope.launch {
            repository.toggleMemberPendingRegistration(memberId, isPending)
            if (authState.value is AuthState.Authenticated) {
                authService.syncToCloud()
            }
        }
    }

    fun openReminderGenerator(member: MemberEntity, subscription: SubscriptionEntity) {
        _reminderMemberData.value = Pair(member, subscription)
    }

    fun closeReminderGenerator() {
        _reminderMemberData.value = null
    }

    // Backup & Restore logic
    private val _showBackupRestoreDialog = MutableStateFlow(false)
    val showBackupRestoreDialog: StateFlow<Boolean> = _showBackupRestoreDialog

    private val _pendingRestorePreview = MutableStateFlow<com.example.data.util.BackupPreview?>(null)
    val pendingRestorePreview: StateFlow<com.example.data.util.BackupPreview?> = _pendingRestorePreview

    private var pendingRestoreJson: String? = null

    private val _backupStatusMessage = MutableStateFlow<String?>(null)
    val backupStatusMessage: StateFlow<String?> = _backupStatusMessage

    fun openBackupRestoreDialog() {
        _showBackupRestoreDialog.value = true
    }

    fun closeBackupRestoreDialog() {
        _showBackupRestoreDialog.value = false
        _pendingRestorePreview.value = null
        pendingRestoreJson = null
    }

    fun clearBackupStatusMessage() {
        _backupStatusMessage.value = null
    }

    suspend fun getBackupJson(): String {
        val subs = repository.getAllSubscriptionsDirect()
        val members = repository.getAllMembersDirect()
        return com.example.data.util.BackupManager.generateBackupJson(
            context = getApplication(),
            subscriptions = subs,
            members = members
        )
    }

    fun previewBackupContent(jsonString: String) {
        val preview = com.example.data.util.BackupManager.parseBackupPreview(jsonString)
        pendingRestoreJson = if (preview.isValid) jsonString else null
        _pendingRestorePreview.value = preview
    }

    fun dismissRestorePreview() {
        _pendingRestorePreview.value = null
        pendingRestoreJson = null
    }

    fun executeRestore(replaceExisting: Boolean) {
        val json = pendingRestoreJson ?: return
        viewModelScope.launch {
            val result = com.example.data.util.BackupManager.restoreFromJson(
                context = getApplication(),
                jsonString = json,
                dao = repository.rawDao,
                replaceExisting = replaceExisting
            )
            if (result.success) {
                _backupStatusMessage.value = "¡Restauración completada con éxito! (${result.subscriptionsRestored} suscripciones, ${result.membersRestored} miembros)"
                dismissRestorePreview()
                closeBackupRestoreDialog()
            } else {
                _backupStatusMessage.value = "Error al restaurar: ${result.errorMessage}"
            }
        }
    }

    // App Menu & Settings Navigation
    private val _showAppMenu = MutableStateFlow(false)
    val showAppMenu: StateFlow<Boolean> = _showAppMenu

    private val _showSettingsScreen = MutableStateFlow(false)
    val showSettingsScreen: StateFlow<Boolean> = _showSettingsScreen

    fun openAppMenu() {
        _showAppMenu.value = true
    }

    fun closeAppMenu() {
        _showAppMenu.value = false
    }

    fun openSettingsScreen() {
        _showAppMenu.value = false
        _showSettingsScreen.value = true
    }

    fun closeSettingsScreen() {
        _showSettingsScreen.value = false
    }

    // Sharing Platform CRUD dialogs
    private val _showAddEditPlatformDialog = MutableStateFlow(false)
    val showAddEditPlatformDialog: StateFlow<Boolean> = _showAddEditPlatformDialog

    private val _platformToEdit = MutableStateFlow<SharingPlatformEntity?>(null)
    val platformToEdit: StateFlow<SharingPlatformEntity?> = _platformToEdit

    private val _platformToDelete = MutableStateFlow<SharingPlatformEntity?>(null)
    val platformToDelete: StateFlow<SharingPlatformEntity?> = _platformToDelete

    fun openAddSharingPlatform() {
        _platformToEdit.value = null
        _showAddEditPlatformDialog.value = true
    }

    fun openEditSharingPlatform(platform: SharingPlatformEntity) {
        _platformToEdit.value = platform
        _showAddEditPlatformDialog.value = true
    }

    fun closeAddEditSharingPlatform() {
        _showAddEditPlatformDialog.value = false
        _platformToEdit.value = null
    }

    fun openDeleteSharingPlatformConfirm(platform: SharingPlatformEntity) {
        _platformToDelete.value = platform
    }

    fun closeDeleteSharingPlatformConfirm() {
        _platformToDelete.value = null
    }

    fun saveSharingPlatform(platform: SharingPlatformEntity) {
        viewModelScope.launch {
            if (platform.id == 0L) {
                repository.insertSharingPlatform(platform)
            } else {
                repository.updateSharingPlatform(platform)
            }
            closeAddEditSharingPlatform()
        }
    }

    fun confirmDeleteSharingPlatform() {
        val target = _platformToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteSharingPlatform(target)
            closeDeleteSharingPlatformConfirm()
        }
    }

    fun restoreDefaultPlatforms() {
        viewModelScope.launch {
            val initialList = SharingPlatforms.defaultList.map {
                it.copy(id = 0)
            }
            repository.rawDao.insertSharingPlatforms(initialList)
        }
    }
}


```
