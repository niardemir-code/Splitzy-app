package com.apleq.app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apleq.app.data.local.AppDatabase
import com.apleq.app.data.local.MemberEntity
import com.apleq.app.data.local.SharingPlatformEntity
import com.apleq.app.data.local.SubscriptionEntity
import com.apleq.app.data.local.SubscriptionWithMembers
import com.apleq.app.data.model.AppNotification
import com.apleq.app.data.model.CurrencyManager
import com.apleq.app.data.model.NotificationGenerator
import com.apleq.app.data.model.SharingPlatforms
import com.apleq.app.data.remote.AuthState
import com.apleq.app.data.remote.FirebaseAuthService
import com.apleq.app.data.repository.SubscriptionRepository
import com.apleq.app.data.util.AppThemeMode
import com.apleq.app.data.util.calculateNextCycleDate
import com.apleq.app.data.util.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
    RENEWAL_DATE("Fecha de renovación", "📅 Por Renovación");

    val localizedChipText: String
        get() {
            val lang = java.util.Locale.getDefault().language.lowercase()
            return when {
                lang.startsWith("es") -> chipText
                lang.startsWith("ca") -> when (this) {
                    DEFAULT -> "Recents"
                    ALPHABETICAL -> "🔤 Alfabètic"
                    RENEWAL_DATE -> "📅 Per Renovació"
                }
                else -> when (this) {
                    DEFAULT -> "Recent"
                    ALPHABETICAL -> "🔤 Alphabetical"
                    RENEWAL_DATE -> "📅 By Renewal"
                }
            }
        }
}

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SubscriptionRepository
    private val authService: FirebaseAuthService
    private val themePreferences: ThemePreferences = ThemePreferences(application)
    private val notificationPrefs: SharedPreferences = application.getSharedPreferences("apleq_notifications_prefs", Context.MODE_PRIVATE)
    private val READ_NOTIFICATIONS_KEY = "read_notification_ids"
    private val DISMISSED_NOTIFICATIONS_KEY = "dismissed_notification_ids"

    private fun getReadNotificationIds(): Set<String> =
        notificationPrefs.getStringSet(READ_NOTIFICATIONS_KEY, emptySet()) ?: emptySet()

    private fun saveReadNotificationIds(ids: Set<String>) {
        notificationPrefs.edit().putStringSet(READ_NOTIFICATIONS_KEY, ids).apply()
    }

    fun getDismissedNotificationIds(): Set<String> =
        notificationPrefs.getStringSet(DISMISSED_NOTIFICATIONS_KEY, emptySet()) ?: emptySet()

    fun saveDismissedNotificationIds(ids: Set<String>) {
        notificationPrefs.edit().putStringSet(DISMISSED_NOTIFICATIONS_KEY, ids).apply()
    }

    private val _readNotificationIds = MutableStateFlow<Set<String>>(getReadNotificationIds())
    private val _dismissedNotificationIds = MutableStateFlow<Set<String>>(getDismissedNotificationIds())

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
        }
        viewModelScope.launch {
            // Aislada: si falla la red, no debe arrastrar a nada más.
            try {
                com.apleq.app.data.util.CurrencyRateService.fetchLatestRates()
            } catch (e: Exception) {
                android.util.Log.w("Rates", "No se pudieron obtener los tipos de cambio", e)
            }
        }
        viewModelScope.launch {
            authService.authState.collect { state ->
                if (state is AuthState.Authenticated) {
                    authService.startListeningParticipatingGroups()
                }
            }
        }
        // El reinicio de ciclos debe correr DESPUÉS de que la sincronización con
        // Firestore haya terminado; si no, la descarga del servidor lo sobrescribe.
        viewModelScope.launch {
            var hasSynced = false
            authService.isSyncing.collect { syncing ->
                android.util.Log.d("Rollover", "isSyncing = $syncing (hasSynced=$hasSynced)")
                if (syncing) {
                    hasSynced = true
                } else if (hasSynced) {
                    // La sincronización acaba de terminar.
                    hasSynced = false
                    android.util.Log.d("Rollover", "Sincronización terminada -> lanzando reinicio")
                    rolloverDuePaymentCycles()
                }
            }
        }
        // Caso sin sesión iniciada (solo datos locales): la sincronización nunca
        // ocurre, así que se ejecuta una vez tras un breve margen.
        viewModelScope.launch {
            kotlinx.coroutines.delay(2500)
            android.util.Log.d("Rollover", "Respaldo tras 2,5s. authState=${authState.value}")
            if (authState.value !is AuthState.Authenticated) {
                android.util.Log.d("Rollover", "Sin sesión -> lanzando reinicio de respaldo")
                rolloverDuePaymentCycles()
            }
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
    val participatingGroups: StateFlow<List<Map<String, Any>>> = authService.participatingGroups

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog

    fun openAuthDialog() {
        _showAuthDialog.value = true
    }

    fun closeAuthDialog() {
        _showAuthDialog.value = false
    }

    fun signInWithGoogle(activityContext: android.content.Context? = null) {
        viewModelScope.launch {
            authService.signInWithGoogle(activityContext)
        }
    }

    fun handleGoogleSignInResult(data: android.content.Intent?) {
        viewModelScope.launch {
            authService.handleGoogleSignInResult(data)
        }
    }

    fun getGoogleSignInIntent(ctx: android.content.Context): android.content.Intent {
        return authService.getGoogleSignInIntent(ctx)
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            authService.signInWithEmail(email, pass)
        }
    }

    fun registerWithEmail(email: String, pass: String, name: String = "") {
        viewModelScope.launch {
            authService.registerWithEmail(email, pass, name)
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

    fun cleanAndPruneFirebaseDatabase() {
        viewModelScope.launch {
            authService.cleanAndPruneFirebaseDatabase()
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
            val pendingMembersCount = subs.sumOf { it.pendingMembersCount }
            val pendingAmount = subs.sumOf { subWithMembers ->
                subWithMembers.members.filter { !it.isPaidThisMonth || it.isPendingPayment }.sumOf { member ->
                    val pPricing = subWithMembers.platformPrices.find { it.platformName.equals(member.sharingPlatform, ignoreCase = true) }
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
            }
            val profitSubs = subs.count { it.isNetProfit }

            FinancialOverview(
                totalCost = totalCost,
                totalContributed = totalContributed,
                netBalance = netBalance,
                totalSubscriptionsCount = totalSubs,
                totalMembersCount = totalMembers,
                pendingPaymentsCount = pendingMembersCount,
                pendingAmount = pendingAmount,
                profitSubscriptionsCount = profitSubs
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FinancialOverview()
        )

    val notifications: StateFlow<List<AppNotification>> = combine(
        repository.allSubscriptions,
        _readNotificationIds,
        _dismissedNotificationIds
    ) { subsWithMembers, readIds, dismissedIds ->
        val subscriptions = subsWithMembers.map { it.subscription }
        val membersBySub = subsWithMembers.associate { it.subscription.id.toString() to it.members }
        val generated = NotificationGenerator.generate(
            subscriptions = subscriptions,
            membersBySubscription = membersBySub,
            readIds = readIds
        )
        generated.filter { it.id !in dismissedIds }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unreadNotificationsCount: StateFlow<Int> = notifications
        .combine(MutableStateFlow(Unit)) { notifs, _ ->
            notifs.count { !it.isRead }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun markAllNotificationsRead() {
        val allIds = notifications.value.map { it.id }.toSet()
        val updated = _readNotificationIds.value + allIds
        _readNotificationIds.value = updated
        saveReadNotificationIds(updated)
    }

    fun markNotificationRead(id: String) {
        val updated = _readNotificationIds.value + id
        _readNotificationIds.value = updated
        saveReadNotificationIds(updated)
    }

    fun dismissNotification(id: String) {
        val updated = _dismissedNotificationIds.value + id
        _dismissedNotificationIds.value = updated
        saveDismissedNotificationIds(updated)
    }

    fun dismissAllNotifications() {
        val allVisibleIds = notifications.value.map { it.id }.toSet()
        val updated = _dismissedNotificationIds.value + allVisibleIds
        _dismissedNotificationIds.value = updated
        saveDismissedNotificationIds(updated)
    }

    /**
     * Reinicia el ciclo de cobro de los miembros cuya fecha de pago ya ha llegado
     * y que estaban marcados como pagados. Se ejecuta al abrir la app.
     * Los impagados NO se tocan, para que sigan constando como vencidos.
     */
    private var isRollingOver = false

    fun rolloverDuePaymentCycles() {
        if (isRollingOver) {
            android.util.Log.d("Rollover", "IGNORADO: ya hay un reinicio en curso")
            return
        }
        isRollingOver = true
        viewModelScope.launch {
            var changedCount = 0
            try {
                android.util.Log.d("Rollover", "===== INICIO del reinicio de ciclos =====")

                val todayMillis = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.timeInMillis

                val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                android.util.Log.d("Rollover", "Hoy = ${isoFormat.format(java.util.Date(todayMillis))}")

                val allMembers = repository.getAllMembersDirect()
                android.util.Log.d("Rollover", "Miembros encontrados en Room: ${allMembers.size}")

                allMembers.forEach { member ->
                    val quien = "[${member.id}] ${member.memberName}"

                    android.util.Log.d(
                        "Rollover",
                        "$quien -> fecha='${member.nextPaymentDate}' " +
                            "pendientePago=${member.isPendingPayment} " +
                            "pagado=${member.isPaidThisMonth} " +
                            "pendienteEliminar=${member.isPendingRemoval} " +
                            "freq=${member.paymentFrequencyValue}/${member.paymentFrequencyUnit}"
                    )

                    if (member.isPendingRemoval) {
                        android.util.Log.d("Rollover", "$quien DESCARTADO: pendiente de eliminar")
                        return@forEach
                    }
                    if (member.nextPaymentDate.isBlank()) {
                        android.util.Log.d("Rollover", "$quien DESCARTADO: sin fecha de pago")
                        return@forEach
                    }
                    if (member.isPendingPayment || !member.isPaidThisMonth) {
                        android.util.Log.d("Rollover", "$quien DESCARTADO: no está al día (impagado)")
                        return@forEach
                    }

                    val dueMillis = try {
                        isoFormat.parse(member.nextPaymentDate)?.time ?: run {
                            android.util.Log.w("Rollover", "$quien DESCARTADO: fecha no interpretable")
                            return@forEach
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("Rollover", "$quien DESCARTADO: error al leer la fecha", e)
                        return@forEach
                    }

                    if (dueMillis <= todayMillis) {
                        val newDateMillis = com.apleq.app.data.util.calculateNextCycleDate(
                            dueMillis,
                            member.paymentFrequencyValue,
                            member.paymentFrequencyUnit,
                            todayMillis
                        )
                        val nuevaFecha = isoFormat.format(java.util.Date(newDateMillis))
                        val updated = member.copy(
                            isPendingPayment = true,
                            isPaidThisMonth = false,
                            nextPaymentDate = nuevaFecha
                        )
                        repository.updateMember(updated)
                        changedCount++
                        android.util.Log.i(
                            "Rollover",
                            "$quien REINICIADO: '${member.nextPaymentDate}' -> '$nuevaFecha'"
                        )
                    } else {
                        android.util.Log.d("Rollover", "$quien DESCARTADO: la fecha aún no ha llegado")
                    }
                }

                android.util.Log.i("Rollover", "===== FIN. Miembros reiniciados: $changedCount =====")

                if (changedCount > 0) {
                    if (authState.value is AuthState.Authenticated) {
                        android.util.Log.d("Rollover", "Subiendo cambios a la nube...")
                        authService.syncToCloud()
                        android.util.Log.d("Rollover", "Subida completada")
                    } else {
                        android.util.Log.d("Rollover", "Sin sesión: no se sube a la nube")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("Rollover", "ERROR en el reinicio de ciclos", e)
            } finally {
                isRollingOver = false
            }
        }
    }

    // UI Dialog & Navigation States
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
                // Sincronizar automáticamente la cantidad de los miembros que no tengan precio fijado
                val parsedPlatformPrices = com.apleq.app.data.model.PlatformPricingHelper.parse(subscription.platformPricing)
                if (parsedPlatformPrices.isNotEmpty()) {
                    val existingMembers = repository.getAllMembersDirect().filter { it.subscriptionId == subscription.id }
                    for (member in existingMembers) {
                        val matchingPlatform = parsedPlatformPrices.find { it.platformName.equals(member.sharingPlatform, ignoreCase = true) }
                        if (matchingPlatform != null && matchingPlatform.pricePerUser > 0.0 && member.contributionAmount <= 0.0) {
                            repository.updateMember(member.copy(contributionAmount = matchingPlatform.pricePerUser))
                        }
                    }
                }
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

    fun generateInvite(subscriptionWithMembers: SubscriptionWithMembers) {
        viewModelScope.launch {
            val subId = subscriptionWithMembers.subscription.id
            val code = authService.generateInviteCode()
            val newMemberId = System.currentTimeMillis() * 1000 + (0..999).random()
            val reservedMember = MemberEntity(
                id = newMemberId,
                subscriptionId = subId,
                memberName = "Invitado (pendiente)",
                isPendingRegistration = true,
                inviteCode = code
            )
            repository.insertMember(reservedMember)
            if (authState.value is AuthState.Authenticated) {
                authService.createInvite(code, subId, newMemberId)
                authService.syncToCloud()
            }
        }
    }

    fun claimInvite(code: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val cleaned = code.uppercase().filter { it.isLetterOrDigit() }
            if (cleaned.length != 6) {
                onResult(false, "El código no es válido.")
                return@launch
            }
            try {
                val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
                functions.getHttpsCallable("claimSlot")
                    .call(hashMapOf("code" to cleaned))
                    .await()
                onResult(true, "¡Te has unido al grupo!")
            } catch (e: Exception) {
                val msg = e.message ?: "No se pudo unir. Revisa el código."
                onResult(false, msg)
            }
        }
    }

    fun deleteAccount(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authService.deleteAccount()
            if (result.isSuccess) {
                onResult(true, "Cuenta eliminada correctamente.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "No se pudo eliminar la cuenta.")
            }
        }
    }

    suspend fun leaveGroup(ownerUid: String, groupId: String): Result<Unit> {
        return authService.leaveGroup(ownerUid, groupId)
    }

    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
            if (authState.value is AuthState.Authenticated) {
                member.inviteCode?.let { authService.deleteInvite(it) }
                authService.deleteMemberFromCloud(member)
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

    private val _pendingRestorePreview = MutableStateFlow<com.apleq.app.data.util.BackupPreview?>(null)
    val pendingRestorePreview: StateFlow<com.apleq.app.data.util.BackupPreview?> = _pendingRestorePreview

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
        return com.apleq.app.data.util.BackupManager.generateBackupJson(
            context = getApplication(),
            subscriptions = subs,
            members = members
        )
    }

    fun previewBackupContent(jsonString: String) {
        val preview = com.apleq.app.data.util.BackupManager.parseBackupPreview(jsonString)
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
            val result = com.apleq.app.data.util.BackupManager.restoreFromJson(
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

