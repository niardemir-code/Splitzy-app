package com.apleq.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apleq.app.data.local.AppDatabase
import com.apleq.app.data.local.MemberEntity
import com.apleq.app.data.local.SharingPlatformEntity
import com.apleq.app.data.local.SubscriptionEntity
import com.apleq.app.data.local.SubscriptionWithMembers
import com.apleq.app.data.model.CurrencyManager
import com.apleq.app.data.model.SharingPlatforms
import com.apleq.app.data.remote.AuthState
import com.apleq.app.data.remote.FirebaseAuthService
import com.apleq.app.data.repository.SubscriptionRepository
import com.apleq.app.data.util.AppThemeMode
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
            com.apleq.app.data.util.CurrencyRateService.fetchLatestRates()
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

