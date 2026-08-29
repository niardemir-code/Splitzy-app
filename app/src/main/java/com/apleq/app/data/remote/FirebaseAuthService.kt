package com.apleq.app.data.remote

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.apleq.app.data.local.MemberEntity
import com.apleq.app.data.local.SubscriptionDao
import com.apleq.app.data.local.SubscriptionEntity
import com.apleq.app.data.local.SubscriptionWithMembers
import com.apleq.app.data.model.BillingPeriod
import com.apleq.app.data.model.CurrencyItem
import com.apleq.app.data.model.CurrencyManager
import com.apleq.app.data.model.PlatformPriceItem
import com.apleq.app.data.model.PlatformPricingHelper
import com.apleq.app.ui.util.ImageStorageHelper
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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
                        // Descargar y sincronizar datos de la nube primero al autenticar
                        scope.launch(Dispatchers.IO) {
                            try {
                                syncFromCloud()
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
                        .setApplicationId("1:498651324948:android:1cacae6d0919a7a78dbd01")
                        .setApiKey("AIzaSyAfdb5NIJtcratWCAebPD41hvWGTe5FSNA")
                        .setProjectId("apleq-76e0a")
                        .setStorageBucket("apleq-76e0a.firebasestorage.app")
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
            val serverClientId = getWebClientId() ?: "498651324948-18qocdi9iqatn6kc4isaof5d0bhate0q.apps.googleusercontent.com"

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

    private fun formatIsoTimestamp(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            sdf.format(Date(if (timestamp > 0) timestamp else System.currentTimeMillis()))
        } catch (_: Exception) {
            System.currentTimeMillis().toString()
        }
    }

    private fun formatIsoDay(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            sdf.format(Date(if (timestamp > 0) timestamp else System.currentTimeMillis()))
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Sube todos los datos locales a Firestore de manera limpia y unificada en users/{uid}/subscriptions/{sub.id}
     * y en la subcolección granular users/{uid}/subscriptions/{sub.id}/members/{member.id}
     */
    suspend fun syncToCloud(): Boolean = withContext(Dispatchers.IO) {
        val user = auth?.currentUser ?: return@withContext false
        val db = firestore ?: return@withContext false
        _isSyncing.value = true
        _syncStatus.value = "☁️ Sincronizando datos con la nube..."
        try {
            val subs = dao.getAllSubscriptionsDirect()
            val members = dao.getAllMembersDirect()

            val membersBySubId = members.groupBy { it.subscriptionId }
            val batch = db.batch()
            val userSubsCollection = db.collection("users").document(user.uid).collection("subscriptions")
            val now = System.currentTimeMillis()

            for (sub in subs) {
                // --- Imagen personalizada: subir a Storage y guardar URL (en vez de base64) ---
                val hasCustomImage = (sub.iconType == "CUSTOM_IMAGE" || sub.customImageUri.isNotBlank()) && sub.customImageUri.isNotBlank()
                val alreadyRemote = sub.customImageUri.startsWith("http://") || sub.customImageUri.startsWith("https://")

                var remoteImageUrl = ""
                var imageBase64 = ""
                if (alreadyRemote) {
                    remoteImageUrl = sub.customImageUri
                } else if (hasCustomImage) {
                    val uploaded = ImageStorageHelper.uploadImageToStorage(
                        context,
                        sub.customImageUri,
                        "users/${user.uid}/subscriptions/${sub.id}/custom_logo.jpg"
                    )
                    if (uploaded != null) {
                        remoteImageUrl = uploaded
                        // Actualiza la entidad local para no re-subir en cada sincronización
                        try { dao.updateSubscription(sub.copy(customImageUri = uploaded)) } catch (_: Exception) {}
                    } else {
                        // Fallback: si Storage falla, mantenemos base64 para no perder la imagen
                        imageBase64 = ImageStorageHelper.imageToBase64(context, sub.customImageUri) ?: ""
                    }
                }
                val dataUriString = if (imageBase64.isNotBlank()) "data:image/jpeg;base64,$imageBase64" else ""
                val imageFieldValue = if (remoteImageUrl.isNotBlank()) remoteImageUrl else dataUriString

                val subMembers = membersBySubId[sub.id] ?: emptyList()
                val isoCreatedAt = formatIsoTimestamp(sub.createdAt)
                val isoUpdatedAt = formatIsoTimestamp(now)
                val firstPaymentDateStr = formatIsoDay(sub.createdAt)

                val normalizedCurrency = when (sub.currency.trim()) {
                    "€" -> "EUR"
                    "$" -> "USD"
                    "£" -> "GBP"
                    "¥" -> "JPY"
                    else -> sub.currency.ifBlank { "EUR" }
                }

                val normalizedPeriod = when (sub.billingPeriod.uppercase().trim()) {
                    "YEARLY", "ANNUAL", "YEAR" -> "ANNUAL"
                    "SEMI_ANNUAL", "SEMIANNUAL", "SEMI_ANNUALLY" -> "SEMI_ANNUAL"
                    "QUARTERLY", "QUARTER" -> "QUARTERLY"
                    else -> "MONTHLY"
                }

                val effectiveSlots = if (sub.maxSlots > 0) sub.maxSlots else if (subMembers.size + 1 > 4) subMembers.size + 1 else 4

                val parsedPricingList = PlatformPricingHelper.parse(sub.platformPricing).map {
                    mapOf(
                        "platformName" to it.platformName,
                        "platform_name" to it.platformName,
                        "platform" to it.platformName,
                        "name" to it.platformName,
                        "price" to it.pricePerUser,
                        "pricePerUser" to it.pricePerUser,
                        "price_per_user" to it.pricePerUser,
                        "cost" to it.pricePerUser,
                        "amount" to it.pricePerUser,
                        "currency" to it.currency,
                        "currencyCode" to it.currency,
                        "currency_code" to it.currency,
                        "billingPeriod" to it.billingPeriod,
                        "billing_period" to it.billingPeriod
                    )
                }

                val memberMapList = subMembers.map { m ->
                    val isPaid = m.isPaidThisMonth && !m.isPendingPayment && !m.isPendingRemoval && !m.isPendingRegistration
                    val isoJoined = if (m.joinedDateStr.isNotBlank()) m.joinedDateStr else if (m.joinedDate > 0) formatIsoDay(m.joinedDate) else formatIsoDay(now)
                    val pStatus = if (m.paymentStatus.isNotBlank()) m.paymentStatus else if (isPaid) "paid" else "pending"
                    val memCurrency = if (m.currency.isNotBlank()) CurrencyManager.findCurrency(m.currency).code else normalizedCurrency

                    mapOf(
                        "id" to m.id,
                        "subscriptionId" to sub.id,
                        "subscription_id" to sub.id,
                        "memberName" to m.memberName,
                        "member_name" to m.memberName,
                        "name" to m.memberName,
                        "sharingPlatform" to m.sharingPlatform,
                        "sharing_platform" to m.sharingPlatform,
                        "platform" to m.sharingPlatform,
                        "memberContact" to m.memberContact,
                        "member_contact" to m.memberContact,
                        "contact" to m.memberContact,
                        "joinedDate" to m.joinedDate,
                        "joinedDateStr" to isoJoined,
                        "joined_date" to isoJoined,
                        "nextPaymentDate" to m.nextPaymentDate,
                        "next_payment_date" to m.nextPaymentDate,
                        "paymentFrequencyValue" to m.paymentFrequencyValue,
                        "payment_frequency_value" to m.paymentFrequencyValue,
                        "paymentFrequencyUnit" to m.paymentFrequencyUnit,
                        "payment_frequency_unit" to m.paymentFrequencyUnit,
                        "autoRepeatPayment" to m.autoRepeatPayment,
                        "auto_repeat_payment" to m.autoRepeatPayment,
                        "paymentMethod" to m.paymentMethod,
                        "payment_method" to m.paymentMethod,
                        "lastPaymentDate" to m.lastPaymentDate,
                        "last_payment_date" to m.lastPaymentDate,
                        "enableAlarm" to m.enableAlarm,
                        "enable_alarm" to m.enableAlarm,
                        "hasAlarm" to m.enableAlarm,
                        "alarmValue" to m.alarmValue,
                        "alarm_value" to m.alarmValue,
                        "alarmUnit" to m.alarmUnit,
                        "alarm_unit" to m.alarmUnit,
                        "alarmDaysBefore" to m.alarmDaysBefore,
                        "alarm_days_before" to m.alarmDaysBefore,
                        "contributionAmount" to m.contributionAmount,
                        "amount" to m.contributionAmount,
                        "currency" to memCurrency,
                        "currencyCode" to memCurrency,
                        "currency_code" to memCurrency,
                        "isPaidThisMonth" to isPaid,
                        "is_paid_this_month" to isPaid,
                        "paidThisMonth" to isPaid,
                        "isPendingPayment" to m.isPendingPayment,
                        "is_pending_payment" to m.isPendingPayment,
                        "isPendingRemoval" to m.isPendingRemoval,
                        "is_pending_removal" to m.isPendingRemoval,
                        "isPendingRegistration" to m.isPendingRegistration,
                        "is_pending_registration" to m.isPendingRegistration,
                        "paymentStatus" to pStatus,
                        "payment_status" to pStatus,
                        "notes" to m.notes
                    )
                }

                val cleanMap = mapOf(
                    "id" to sub.id.toString(),
                    "userId" to user.uid,
                    "user_id" to user.uid,
                    "name" to sub.platformName,
                    "platformName" to sub.platformName,
                    "platform_name" to sub.platformName,
                    "mainUserName" to sub.mainUserName,
                    "main_user_name" to sub.mainUserName,
                    "mainUserContact" to sub.mainUserContact,
                    "main_user_contact" to sub.mainUserContact,
                    "price" to sub.cost,
                    "cost" to sub.cost,
                    "currency" to normalizedCurrency,
                    "billingPeriod" to normalizedPeriod,
                    "billing_period" to normalizedPeriod,
                    "category" to sub.category.ifBlank { "Streaming" },
                    "totalSlots" to effectiveSlots,
                    "maxSlots" to effectiveSlots,
                    "max_slots" to effectiveSlots,
                    "billingDay" to sub.billingDay,
                    "billing_day" to sub.billingDay,
                    "billingMonth" to sub.billingMonth,
                    "billing_month" to sub.billingMonth,
                    "defaultContributionPerUser" to sub.defaultContributionPerUser,
                    "default_contribution" to sub.defaultContributionPerUser,
                    "firstPaymentDate" to firstPaymentDateStr,
                    "iconType" to if (sub.iconType == "CUSTOM_IMAGE" || sub.customImageUri.isNotBlank()) "CUSTOM_IMAGE" else sub.iconType.ifBlank { "PRESET" },
                    "icon_type" to if (sub.iconType == "CUSTOM_IMAGE" || sub.customImageUri.isNotBlank()) "CUSTOM_IMAGE" else sub.iconType.ifBlank { "PRESET" },
                    "iconKey" to sub.iconKey.ifBlank { "Netflix" },
                    "icon_key" to sub.iconKey.ifBlank { "Netflix" },
                    "iconColorHex" to sub.iconColorHex.ifBlank { "#6366F1" },
                    "icon_color_hex" to sub.iconColorHex.ifBlank { "#6366F1" },
                    "customImageUri" to (if (remoteImageUrl.isNotBlank()) remoteImageUrl else sub.customImageUri),
                    "customImageBase64" to imageBase64,
                    "custom_image_base64" to imageBase64,
                    "customImage" to (if (imageFieldValue.isNotBlank()) imageFieldValue else sub.customImageUri),
                    "logo" to (if (imageFieldValue.isNotBlank()) imageFieldValue else sub.customImageUri),
                    "image" to (if (imageFieldValue.isNotBlank()) imageFieldValue else sub.customImageUri),
                    "enableAlarm" to sub.enableAlarm,
                    "enable_alarm" to sub.enableAlarm,
                    "alarmValue" to sub.alarmValue,
                    "alarm_value" to sub.alarmValue,
                    "alarmUnit" to sub.alarmUnit,
                    "alarm_unit" to sub.alarmUnit,
                    "alarmDaysBefore" to sub.alarmDaysBefore,
                    "alarm_days_before" to sub.alarmDaysBefore,
                    "notes" to sub.notes,
                    "platformPricing" to sub.platformPricing,
                    "platform_pricing" to sub.platformPricing,
                    "platformPrices" to parsedPricingList,
                    "platform_prices" to parsedPricingList,
                    "platforms" to parsedPricingList,
                    "sharingPlatforms" to parsedPricingList,
                    "createdAt" to isoCreatedAt,
                    "created_at" to isoCreatedAt,
                    "updatedAt" to isoUpdatedAt,
                    "updated_at" to isoUpdatedAt,
                    "members" to memberMapList
                )

                // Escribir en la ruta principal del documento
                val userDocRef = userSubsCollection.document(sub.id.toString())
                batch.set(userDocRef, cleanMap)

                // Limpiar miembros eliminados de la subcolección granular
                val currentMemberIds = subMembers.map { it.id.toString() }.toSet()
                try {
                    val existingSubMems = userDocRef.collection("members").get().await()
                    if (existingSubMems != null && !existingSubMems.isEmpty) {
                        for (docMem in existingSubMems.documents) {
                            if (!currentMemberIds.contains(docMem.id)) {
                                docMem.reference.delete()
                            }
                        }
                    }
                } catch (_: Exception) {}

                // Sincronizar subcolección granular de miembros actuales
                for ((idx, m) in subMembers.withIndex()) {
                    val memMap = memberMapList.getOrNull(idx) ?: continue
                    val memberDocRef = userDocRef.collection("members").document(m.id.toString())
                    batch.set(memberDocRef, memMap)
                }
            }

            batch.commit().await()

            // Limpieza en segundo plano de documentos legacy en la raíz subscriptions para este usuario
            try {
                val legacy1 = db.collection("subscriptions").whereEqualTo("userId", user.uid).get().await()
                legacy1?.documents?.forEach { doc ->
                    try { doc.reference.delete() } catch (_: Exception) {}
                }
                val legacy2 = db.collection("subscriptions").whereEqualTo("user_id", user.uid).get().await()
                legacy2?.documents?.forEach { doc ->
                    try { doc.reference.delete() } catch (_: Exception) {}
                }
            } catch (_: Exception) {}

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
     * Elimina una suscripción de Firestore
     */
    suspend fun deleteSubscriptionFromCloud(subscriptionId: Long) = withContext(Dispatchers.IO) {
        val user = auth?.currentUser ?: return@withContext
        val db = firestore ?: return@withContext
        try {
            val userSubDoc = db.collection("users").document(user.uid)
                .collection("subscriptions").document(subscriptionId.toString())
            try {
                val subMembersSnap = userSubDoc.collection("members").get().await()
                for (doc in subMembersSnap.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) {
                Log.w("FirebaseAuthService", "Error deleting sub members from user collection: ${e.message}")
            }
            userSubDoc.delete().await()
        } catch (e: Exception) {
            Log.w("FirebaseAuthService", "Error deleting sub from user collection: ${e.message}")
        }
        // Limpieza de seguridad en caso de existir en raíz
        try {
            val rootSubDoc = db.collection("subscriptions").document(subscriptionId.toString())
            try {
                val rootMembersSnap = rootSubDoc.collection("members").get().await()
                for (doc in rootMembersSnap.documents) {
                    doc.reference.delete().await()
                }
            } catch (_: Exception) {}
            rootSubDoc.delete().await()
        } catch (_: Exception) {}

        // Borrar el logo personalizado de Storage si existe (ignorar si no hay)
        try {
            FirebaseStorage.getInstance().reference
                .child("users/${user.uid}/subscriptions/$subscriptionId/custom_logo.jpg")
                .delete().await()
        } catch (_: Exception) {
            // No existe o ya estaba borrado: ignorar
        }
    }

    /**
     * Elimina un miembro específico de Firestore tanto de la subcolección como del array
     */
    suspend fun deleteMemberFromCloud(member: MemberEntity) = withContext(Dispatchers.IO) {
        val user = auth?.currentUser ?: return@withContext
        val db = firestore ?: return@withContext
        try {
            db.collection("users").document(user.uid)
                .collection("subscriptions").document(member.subscriptionId.toString())
                .collection("members").document(member.id.toString())
                .delete().await()
        } catch (_: Exception) {}
        // Sincronizar inmediatamente el estado actualizado a Firestore
        syncToCloud()
    }

    /**
     * DEPURA Y LIMPIA LA BASE DE DATOS EN FIREBASE:
     * - Elimina documentos duplicados y colecciones residuales en la raíz ('subscriptions')
     * - Agrupa y unifica las suscripciones en 'users/{uid}/subscriptions/{sub.id}'
     * - Fusiona todos los miembros para no perder a ninguno
     * - Deja el esquema 100% limpio y optimizado para Web y Android
     */
    suspend fun cleanAndPruneFirebaseDatabase(): Result<String> = withContext(Dispatchers.IO) {
        val user = auth?.currentUser ?: return@withContext Result.failure(Exception("Debes iniciar sesión para depurar la base de datos"))
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore no disponible"))
        _isSyncing.value = true
        _syncStatus.value = "🧹 Depurando y limpiando base de datos en la nube..."

        try {
            val allDocs = mutableListOf<com.google.firebase.firestore.DocumentSnapshot>()

            // 1. Obtener todos los documentos de la colección del usuario
            val userSubsSnap = try {
                db.collection("users").document(user.uid).collection("subscriptions").get().await()
            } catch (e: Exception) {
                null
            }
            if (userSubsSnap != null) {
                allDocs.addAll(userSubsSnap.documents)
            }

            // 2. Obtener documentos residuales de la raíz
            val rootSubsSnap1 = try {
                db.collection("subscriptions").whereEqualTo("userId", user.uid).get().await()
            } catch (_: Exception) { null }
            if (rootSubsSnap1 != null) {
                allDocs.addAll(rootSubsSnap1.documents)
            }

            val rootSubsSnap2 = try {
                db.collection("subscriptions").whereEqualTo("user_id", user.uid).get().await()
            } catch (_: Exception) { null }
            if (rootSubsSnap2 != null) {
                allDocs.addAll(rootSubsSnap2.documents)
            }

            // 3. Procesar y fusionar todos los datos
            val parsedTriples = mutableListOf<Triple<SubscriptionEntity, List<MemberEntity>, Long>>()
            for (doc in allDocs) {
                try {
                    val parsed = parseSubscriptionFromSnapshot(doc)
                    if (parsed != null) {
                        parsedTriples.add(parsed)
                    }
                } catch (e: Exception) {
                    Log.w("FirebaseAuthService", "Error parsing snapshot during cleanup: ${e.message}")
                }
            }

            // También fusionar con los datos locales actuales para máxima seguridad
            val localSubs = dao.getAllSubscriptionsDirect()
            val localMembers = dao.getAllMembersDirect().groupBy { it.subscriptionId }
            for (lSub in localSubs) {
                val lMems = localMembers[lSub.id] ?: emptyList()
                parsedTriples.add(Triple(lSub, lMems, lSub.createdAt))
            }

            // 4. Consolidar suscripciones duplicadas y fusionar miembros
            val mergedMap = mutableMapOf<String, Triple<SubscriptionEntity, MutableMap<String, MemberEntity>, Long>>()
            for (item in parsedTriples) {
                val sub = item.first
                val members = item.second
                val timestamp = item.third

                val subKey = if (sub.id > 0) {
                    sub.id.toString()
                } else if (sub.platformName.isNotBlank()) {
                    "${sub.platformName.trim().lowercase()}_${sub.mainUserName.trim().lowercase()}"
                } else {
                    sub.id.toString()
                }

                val existing = mergedMap[subKey]
                if (existing == null) {
                    val memberMap = mutableMapOf<String, MemberEntity>()
                    members.forEach { m ->
                        if (m.memberName.isNotBlank()) {
                            memberMap[m.memberName.trim().lowercase()] = m
                        }
                    }
                    mergedMap[subKey] = Triple(sub, memberMap, timestamp)
                } else {
                    val preferredSub = if (timestamp >= existing.third) sub else existing.first
                    val otherSub = if (timestamp >= existing.third) existing.first else sub

                    val finalCustomImageUri = if (preferredSub.customImageUri.isNotBlank()) {
                        preferredSub.customImageUri
                    } else {
                        otherSub.customImageUri
                    }
                    val finalIconType = if (preferredSub.iconType == "CUSTOM_IMAGE" || otherSub.iconType == "CUSTOM_IMAGE" || finalCustomImageUri.isNotBlank()) {
                        "CUSTOM_IMAGE"
                    } else {
                        preferredSub.iconType
                    }
                    val finalSub = preferredSub.copy(
                        customImageUri = finalCustomImageUri,
                        iconType = finalIconType
                    )

                    val finalTimestamp = maxOf(timestamp, existing.third)
                    val memberMap = if (timestamp >= existing.third) {
                        mutableMapOf<String, MemberEntity>().apply {
                            members.forEach { m ->
                                if (m.memberName.isNotBlank()) {
                                    put(m.memberName.trim().lowercase(), m)
                                }
                            }
                        }
                    } else {
                        existing.second
                    }
                    mergedMap[subKey] = Triple(finalSub, memberMap, finalTimestamp)
                }
            }

            // 5. Borrar todos los documentos antiguos / residuales en Firebase
            var deletedOldDocsCount = 0

            // Borrar de raíz 'subscriptions'
            if (rootSubsSnap1 != null) {
                for (d in rootSubsSnap1.documents) {
                    try { d.reference.delete().await(); deletedOldDocsCount++ } catch (_: Exception) {}
                }
            }
            if (rootSubsSnap2 != null) {
                for (d in rootSubsSnap2.documents) {
                    try { d.reference.delete().await(); deletedOldDocsCount++ } catch (_: Exception) {}
                }
            }

            // Borrar de users/{uid}/subscriptions los que no correspondan al ID consolidado
            val validSubIds = mergedMap.values.map { it.first.id.toString() }.toSet()
            if (userSubsSnap != null) {
                for (d in userSubsSnap.documents) {
                    if (!validSubIds.contains(d.id)) {
                        try { d.reference.delete().await(); deletedOldDocsCount++ } catch (_: Exception) {}
                    }
                    // Borrar subcolecciones 'members' obsoletas si las hubiera
                    try {
                        val subMems = d.reference.collection("members").get().await()
                        for (sm in subMems.documents) {
                            sm.reference.delete().await()
                        }
                    } catch (_: Exception) {}
                }
            }

            // 6. Escribir las suscripciones limpias y consolidadas en users/{uid}/subscriptions/{sub.id}
            val cleanBatch = db.batch()
            val userSubsCol = db.collection("users").document(user.uid).collection("subscriptions")
            val now = System.currentTimeMillis()

            val finalItemsToSync = mergedMap.values.map { triple ->
                val sub = triple.first
                val membersList = triple.second.values.mapIndexed { idx, m ->
                    m.copy(subscriptionId = sub.id, id = if (m.id > 0) m.id else (sub.id * 1000L + idx + 1))
                }
                sub to membersList
            }

            for (pair in finalItemsToSync) {
                val sub = pair.first
                val mems = pair.second

                val normalizedCurrency = when (sub.currency.trim()) {
                    "€" -> "EUR"
                    "$" -> "USD"
                    "£" -> "GBP"
                    "¥" -> "JPY"
                    else -> sub.currency.ifBlank { "EUR" }
                }

                val normalizedPeriod = when (sub.billingPeriod.uppercase().trim()) {
                    "YEARLY", "ANNUAL", "YEAR" -> "ANNUAL"
                    "SEMI_ANNUAL", "SEMIANNUAL", "SEMI_ANNUALLY" -> "SEMI_ANNUAL"
                    "QUARTERLY", "QUARTER" -> "QUARTERLY"
                    else -> "MONTHLY"
                }

                val isoCreatedAt = formatIsoTimestamp(sub.createdAt)
                val isoUpdatedAt = formatIsoTimestamp(now)
                val firstPaymentDateStr = formatIsoDay(sub.createdAt)

                val parsedPricingList = PlatformPricingHelper.parse(sub.platformPricing).map {
                    mapOf(
                        "platformName" to it.platformName,
                        "platform_name" to it.platformName,
                        "platform" to it.platformName,
                        "name" to it.platformName,
                        "price" to it.pricePerUser,
                        "pricePerUser" to it.pricePerUser,
                        "price_per_user" to it.pricePerUser,
                        "cost" to it.pricePerUser,
                        "amount" to it.pricePerUser,
                        "currency" to it.currency,
                        "currencyCode" to it.currency,
                        "currency_code" to it.currency,
                        "billingPeriod" to it.billingPeriod,
                        "billing_period" to it.billingPeriod
                    )
                }

                val memberMapList = mems.map { m ->
                    val isPaid = m.isPaidThisMonth && !m.isPendingPayment && !m.isPendingRemoval && !m.isPendingRegistration
                    val isoJoined = if (m.joinedDateStr.isNotBlank()) m.joinedDateStr else if (m.joinedDate > 0) formatIsoDay(m.joinedDate) else formatIsoDay(now)
                    val pStatus = if (m.paymentStatus.isNotBlank()) m.paymentStatus else if (isPaid) "paid" else "pending"
                    val memCurrency = if (m.currency.isNotBlank()) CurrencyManager.findCurrency(m.currency).code else normalizedCurrency

                    mapOf(
                        "id" to m.id,
                        "subscriptionId" to sub.id,
                        "subscription_id" to sub.id,
                        "memberName" to m.memberName,
                        "member_name" to m.memberName,
                        "name" to m.memberName,
                        "sharingPlatform" to m.sharingPlatform,
                        "sharing_platform" to m.sharingPlatform,
                        "platform" to m.sharingPlatform,
                        "memberContact" to m.memberContact,
                        "member_contact" to m.memberContact,
                        "contact" to m.memberContact,
                        "joinedDate" to m.joinedDate,
                        "joinedDateStr" to isoJoined,
                        "joined_date" to isoJoined,
                        "nextPaymentDate" to m.nextPaymentDate,
                        "next_payment_date" to m.nextPaymentDate,
                        "paymentFrequencyValue" to m.paymentFrequencyValue,
                        "payment_frequency_value" to m.paymentFrequencyValue,
                        "paymentFrequencyUnit" to m.paymentFrequencyUnit,
                        "payment_frequency_unit" to m.paymentFrequencyUnit,
                        "autoRepeatPayment" to m.autoRepeatPayment,
                        "auto_repeat_payment" to m.autoRepeatPayment,
                        "paymentMethod" to m.paymentMethod,
                        "payment_method" to m.paymentMethod,
                        "lastPaymentDate" to m.lastPaymentDate,
                        "last_payment_date" to m.lastPaymentDate,
                        "enableAlarm" to m.enableAlarm,
                        "enable_alarm" to m.enableAlarm,
                        "hasAlarm" to m.enableAlarm,
                        "alarmValue" to m.alarmValue,
                        "alarm_value" to m.alarmValue,
                        "alarmUnit" to m.alarmUnit,
                        "alarm_unit" to m.alarmUnit,
                        "alarmDaysBefore" to m.alarmDaysBefore,
                        "alarm_days_before" to m.alarmDaysBefore,
                        "contributionAmount" to m.contributionAmount,
                        "amount" to m.contributionAmount,
                        "currency" to memCurrency,
                        "currencyCode" to memCurrency,
                        "currency_code" to memCurrency,
                        "isPaidThisMonth" to isPaid,
                        "is_paid_this_month" to isPaid,
                        "paidThisMonth" to isPaid,
                        "isPendingPayment" to m.isPendingPayment,
                        "is_pending_payment" to m.isPendingPayment,
                        "isPendingRemoval" to m.isPendingRemoval,
                        "is_pending_removal" to m.isPendingRemoval,
                        "isPendingRegistration" to m.isPendingRegistration,
                        "is_pending_registration" to m.isPendingRegistration,
                        "paymentStatus" to pStatus,
                        "payment_status" to pStatus,
                        "notes" to m.notes
                    )
                }

                // --- Imagen personalizada: subir a Storage y guardar URL (en vez de base64) ---
                val hasCustomImage = (sub.iconType == "CUSTOM_IMAGE" || sub.customImageUri.isNotBlank()) && sub.customImageUri.isNotBlank()
                val alreadyRemote = sub.customImageUri.startsWith("http://") || sub.customImageUri.startsWith("https://")

                var remoteImageUrl = ""
                var imageBase64 = ""
                if (alreadyRemote) {
                    remoteImageUrl = sub.customImageUri
                } else if (hasCustomImage) {
                    val uploaded = ImageStorageHelper.uploadImageToStorage(
                        context,
                        sub.customImageUri,
                        "users/${user.uid}/subscriptions/${sub.id}/custom_logo.jpg"
                    )
                    if (uploaded != null) {
                        remoteImageUrl = uploaded
                        // Actualiza la entidad local para no re-subir en cada sincronización
                        try { dao.updateSubscription(sub.copy(customImageUri = uploaded)) } catch (_: Exception) {}
                    } else {
                        // Fallback: si Storage falla, mantenemos base64 para no perder la imagen
                        imageBase64 = ImageStorageHelper.imageToBase64(context, sub.customImageUri) ?: ""
                    }
                }
                val dataUriString = if (imageBase64.isNotBlank()) "data:image/jpeg;base64,$imageBase64" else ""
                val imageFieldValue = if (remoteImageUrl.isNotBlank()) remoteImageUrl else dataUriString
                val effectiveSlots = if (sub.maxSlots > 0) sub.maxSlots else if (mems.size + 1 > 4) mems.size + 1 else 4

                val cleanMap = mapOf(
                    "id" to sub.id.toString(),
                    "userId" to user.uid,
                    "user_id" to user.uid,
                    "name" to sub.platformName,
                    "platformName" to sub.platformName,
                    "platform_name" to sub.platformName,
                    "mainUserName" to sub.mainUserName,
                    "main_user_name" to sub.mainUserName,
                    "mainUserContact" to sub.mainUserContact,
                    "main_user_contact" to sub.mainUserContact,
                    "price" to sub.cost,
                    "cost" to sub.cost,
                    "currency" to normalizedCurrency,
                    "billingPeriod" to normalizedPeriod,
                    "billing_period" to normalizedPeriod,
                    "category" to sub.category.ifBlank { "Streaming" },
                    "totalSlots" to effectiveSlots,
                    "maxSlots" to effectiveSlots,
                    "max_slots" to effectiveSlots,
                    "billingDay" to sub.billingDay,
                    "billing_day" to sub.billingDay,
                    "billingMonth" to sub.billingMonth,
                    "billing_month" to sub.billingMonth,
                    "defaultContributionPerUser" to sub.defaultContributionPerUser,
                    "default_contribution" to sub.defaultContributionPerUser,
                    "firstPaymentDate" to firstPaymentDateStr,
                    "iconType" to if (sub.iconType == "CUSTOM_IMAGE" || sub.customImageUri.isNotBlank()) "CUSTOM_IMAGE" else sub.iconType.ifBlank { "PRESET" },
                    "icon_type" to if (sub.iconType == "CUSTOM_IMAGE" || sub.customImageUri.isNotBlank()) "CUSTOM_IMAGE" else sub.iconType.ifBlank { "PRESET" },
                    "iconKey" to sub.iconKey.ifBlank { "Netflix" },
                    "icon_key" to sub.iconKey.ifBlank { "Netflix" },
                    "iconColorHex" to sub.iconColorHex.ifBlank { "#6366F1" },
                    "icon_color_hex" to sub.iconColorHex.ifBlank { "#6366F1" },
                    "customImageUri" to (if (remoteImageUrl.isNotBlank()) remoteImageUrl else sub.customImageUri),
                    "customImageBase64" to imageBase64,
                    "custom_image_base64" to imageBase64,
                    "customImage" to (if (imageFieldValue.isNotBlank()) imageFieldValue else sub.customImageUri),
                    "logo" to (if (imageFieldValue.isNotBlank()) imageFieldValue else sub.customImageUri),
                    "image" to (if (imageFieldValue.isNotBlank()) imageFieldValue else sub.customImageUri),
                    "enableAlarm" to sub.enableAlarm,
                    "enable_alarm" to sub.enableAlarm,
                    "alarmValue" to sub.alarmValue,
                    "alarm_value" to sub.alarmValue,
                    "alarmUnit" to sub.alarmUnit,
                    "alarm_unit" to sub.alarmUnit,
                    "alarmDaysBefore" to sub.alarmDaysBefore,
                    "alarm_days_before" to sub.alarmDaysBefore,
                    "notes" to sub.notes,
                    "platformPricing" to sub.platformPricing,
                    "platform_pricing" to sub.platformPricing,
                    "platformPrices" to parsedPricingList,
                    "platform_prices" to parsedPricingList,
                    "platforms" to parsedPricingList,
                    "sharingPlatforms" to parsedPricingList,
                    "createdAt" to isoCreatedAt,
                    "created_at" to isoCreatedAt,
                    "updatedAt" to isoUpdatedAt,
                    "updated_at" to isoUpdatedAt,
                    "members" to memberMapList
                )

                val docRef = userSubsCol.document(sub.id.toString())
                cleanBatch.set(docRef, cleanMap)

                // Subcolección miembros
                for ((idx, m) in mems.withIndex()) {
                    val memMap = memberMapList.getOrNull(idx) ?: continue
                    val memberDocRef = docRef.collection("members").document(m.id.toString())
                    cleanBatch.set(memberDocRef, memMap)
                }
            }

            cleanBatch.commit().await()

            // 7. Sincronizar el resultado consolidado en la base de datos local Room
            dao.replaceAllSubscriptionsAndMembers(finalItemsToSync)

            val summary = "✨ Base de datos depurada: ${finalItemsToSync.size} suscripciones consolidadas, $deletedOldDocsCount documentos duplicados eliminados."
            _syncStatus.value = "✅ $summary"
            _isSyncing.value = false
            Result.success(summary)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Error during cloud database cleanup", e)
            val msg = e.localizedMessage ?: "Error desconocido al depurar"
            _syncStatus.value = "⚠️ Error al depurar: $msg"
            _isSyncing.value = false
            Result.failure(e)
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

    private fun parseSafeDouble(value: Any?, default: Double = 0.0): Double {
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.trim().replace(',', '.').toDoubleOrNull() ?: default
            else -> default
        }
    }

    private fun parseSafeInt(value: Any?, default: Int = 1): Int {
        return when (value) {
            is Number -> value.toInt()
            is String -> value.trim().toIntOrNull() ?: default
            else -> default
        }
    }

    private fun parseSafeLong(value: Any?, default: Long = 0L): Long {
        return when (value) {
            is Number -> value.toLong()
            is com.google.firebase.Timestamp -> value.toDate().time
            is java.util.Date -> value.time
            is String -> value.trim().toLongOrNull() ?: default
            else -> default
        }
    }

    private fun parseSafeTimestamp(value: Any?): Long {
        return when (value) {
            is Number -> value.toLong()
            is com.google.firebase.Timestamp -> value.toDate().time
            is java.util.Date -> value.time
            is String -> {
                val asLong = value.trim().toLongOrNull()
                if (asLong != null) {
                    asLong
                } else {
                    try {
                        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(value.trim())?.time
                            ?: System.currentTimeMillis()
                    } catch (_: Exception) {
                        System.currentTimeMillis()
                    }
                }
            }
            else -> System.currentTimeMillis()
        }
    }

    /**
     * Extrae el timestamp de actualización más reciente disponible en el documento
     */
    private fun extractUpdatedAtTimestamp(data: Map<String, Any?>): Long {
        val candidates = listOfNotNull(
            data["updatedAt"],
            data["updated_at"],
            data["updatedAtMs"],
            data["lastModified"],
            data["last_modified"],
            data["createdAt"],
            data["created_at"]
        ).map { parseSafeTimestamp(it) }
        return candidates.maxOrNull() ?: System.currentTimeMillis()
    }

    /**
     * Intenta descargar la imagen personalizada de Firebase Storage si no viene en base64
     */
    private suspend fun tryFetchStorageImage(userId: String, subId: Long): String? = withContext(Dispatchers.IO) {
        try {
            val storage = FirebaseStorage.getInstance()
            val paths = listOf(
                "users/$userId/subscriptions/$subId/custom_logo.jpg",
                "users/$userId/subscriptions/$subId/custom_logo.png",
                "users/$userId/subscriptions/$subId/logo.jpg",
                "users/$userId/subscriptions/$subId/logo.png",
                "users/$userId/subscriptions/$subId/icon.jpg",
                "users/$userId/subscriptions/$subId/icon.png",
                "users/$userId/subscriptions/$subId/custom_image.jpg",
                "users/$userId/subscriptions/$subId/custom_image.png",
                "subscriptions/$subId/custom_logo.jpg",
                "subscriptions/$subId/logo.jpg"
            )
            for (p in paths) {
                try {
                    val ref = storage.reference.child(p)
                    val bytes = ref.getBytes(5 * 1024 * 1024L).await()
                    if (bytes != null && bytes.isNotEmpty()) {
                        val saved = ImageStorageHelper.saveByteArrayImage(context, bytes, "cloud_$subId")
                        if (saved != null) return@withContext saved
                    }
                } catch (_: Exception) {
                    // Siguiente ruta
                }
            }
            null
        } catch (e: Exception) {
            Log.d("FirebaseAuthService", "Error fetching storage image for $subId: ${e.message}")
            null
        }
    }

    /**
     * Parsea un DocumentSnapshot de forma ultra robusta y a prueba de fallos sin depender de reflection estricta
     */
    private suspend fun parseSubscriptionFromSnapshot(doc: com.google.firebase.firestore.DocumentSnapshot): Triple<SubscriptionEntity, List<MemberEntity>, Long>? {
        val data = doc.data ?: return null

        val docIdAsLong = doc.id.toLongOrNull()
        val rawId = parseSafeLong(data["id"], 0L)
        val effectiveId = when {
            rawId > 0 -> rawId
            docIdAsLong != null && docIdAsLong > 0 -> docIdAsLong
            else -> Math.abs(doc.id.hashCode().toLong().let { if (it == 0L) 1L else it })
        }

        val platformName = (data["platformName"] as? String)?.trim()
            ?: (data["name"] as? String)?.trim()
            ?: ""
        val customPlanName = (data["customPlanName"] as? String)?.trim()
            ?: (data["planName"] as? String)?.trim()
            ?: (data["plan"] as? String)?.trim()
            ?: ""
        val mainUserName = (data["mainUserName"] as? String)?.trim()
            ?: (data["ownerName"] as? String)?.trim()
            ?: (data["mainUser"] as? String)?.trim()
            ?: ""
        val mainUserContact = (data["mainUserContact"] as? String)?.trim()
            ?: (data["ownerContact"] as? String)?.trim()
            ?: ""
        val cost = parseSafeDouble(data["price"], parseSafeDouble(data["cost"], 0.0))
        val rawPeriod = (data["billingPeriod"] as? String)?.trim()
            ?: (data["period"] as? String)?.trim()
            ?: "MONTHLY"
        val billingPeriod = when (rawPeriod.uppercase()) {
            "YEARLY", "ANNUAL", "YEAR" -> "ANNUAL"
            "SEMI_ANNUAL", "SEMIANNUAL", "SEMI_ANNUALLY" -> "SEMI_ANNUAL"
            "QUARTERLY", "QUARTER" -> "QUARTERLY"
            else -> "MONTHLY"
        }
        val billingDay = parseSafeInt(data["billingDay"], parseSafeInt(data["day"], 1))
        val billingMonth = parseSafeInt(data["billingMonth"], parseSafeInt(data["month"], 1))
        val rawCurrency = (data["currency"] as? String)?.trim() ?: "€"
        val currency = when (rawCurrency.uppercase()) {
            "EUR" -> "€"
            "USD" -> "$"
            "GBP" -> "£"
            "JPY" -> "¥"
            else -> rawCurrency.ifBlank { "€" }
        }
        val defaultContributionPerUser = parseSafeDouble(data["defaultContributionPerUser"], parseSafeDouble(data["defaultContribution"], 0.0))
        val rawPlatformPricing = data["platformPricing"]
            ?: data["platform_pricing"]
            ?: data["platformPrices"]
            ?: data["platform_prices"]
            ?: data["platforms"]
            ?: data["sharingPlatforms"]
        val parsedPlatformPrices = PlatformPricingHelper.parseAny(rawPlatformPricing)
        val platformPricing = PlatformPricingHelper.serialize(parsedPlatformPrices)
        val category = (data["category"] as? String)?.trim() ?: "Streaming"
        val notes = (data["notes"] as? String)?.trim()
            ?: (data["note"] as? String)?.trim()
            ?: ""

        val customImageUriCandidate = (data["customImageUri"] as? String)?.trim()
            ?: (data["custom_image_uri"] as? String)?.trim()
            ?: (data["customImage"] as? String)?.trim()
            ?: (data["custom_image"] as? String)?.trim()
            ?: (data["imageUrl"] as? String)?.trim()
            ?: (data["image_url"] as? String)?.trim()
            ?: (data["iconUrl"] as? String)?.trim()
            ?: (data["icon_url"] as? String)?.trim()
            ?: (data["logoUrl"] as? String)?.trim()
            ?: (data["logo_url"] as? String)?.trim()
            ?: (data["photoUrl"] as? String)?.trim()
            ?: (data["avatarUrl"] as? String)?.trim()
            ?: (data["imageUri"] as? String)?.trim()
            ?: (data["image_uri"] as? String)?.trim()
            ?: (data["iconUri"] as? String)?.trim()
            ?: (data["icon_uri"] as? String)?.trim()
            ?: (data["logo"] as? String)?.trim()
            ?: (data["image"] as? String)?.trim()
            ?: (data["customLogo"] as? String)?.trim()
            ?: (data["custom_logo"] as? String)?.trim()
            ?: (data["customIcon"] as? String)?.trim()
            ?: (data["custom_icon"] as? String)?.trim()
            ?: (data["icon"] as? String)?.takeIf { it.startsWith("data:") || it.startsWith("http") || it.startsWith("/") || it.length > 100 }?.trim()
            ?: ""

        val customImageBase64Candidate = (data["customImageBase64"] as? String)?.trim()
            ?: (data["custom_image_base64"] as? String)?.trim()
            ?: (data["imageBase64"] as? String)?.trim()
            ?: (data["image_base64"] as? String)?.trim()
            ?: (data["iconBase64"] as? String)?.trim()
            ?: (data["icon_base64"] as? String)?.trim()
            ?: (data["logoBase64"] as? String)?.trim()
            ?: (data["logo_base64"] as? String)?.trim()
            ?: (data["photoBase64"] as? String)?.trim()
            ?: (data["base64"] as? String)?.trim()
            ?: (data["dataUri"] as? String)?.trim()
            ?: (data["data_uri"] as? String)?.trim()
            ?: ""

        val rawIconType = (data["iconType"] as? String)?.trim().orEmpty()
        val isExplicitCustom = rawIconType.equals("CUSTOM_IMAGE", ignoreCase = true) ||
                               rawIconType.equals("CUSTOM", ignoreCase = true) ||
                               rawIconType.equals("IMAGE", ignoreCase = true) ||
                               rawIconType.equals("GALLERY", ignoreCase = true) ||
                               customImageUriCandidate.isNotBlank() ||
                               customImageBase64Candidate.isNotBlank()

        val iconType = if (isExplicitCustom) "CUSTOM_IMAGE" else (if (rawIconType.isNotBlank()) rawIconType else "PRESET")
        val iconKey = (data["iconKey"] as? String)?.trim()
            ?: if (platformName.isNotBlank()) platformName else "Netflix"
        val iconColorHex = (data["iconColorHex"] as? String)?.trim() ?: "#6366F1"
        val createdAt = parseSafeTimestamp(data["createdAt"] ?: data["created_at"])
        val updatedAt = extractUpdatedAtTimestamp(data)

        var localCustomImageUri = customImageUriCandidate
        val rawBase64 = when {
            customImageBase64Candidate.isNotBlank() -> customImageBase64Candidate
            customImageUriCandidate.startsWith("data:image") -> customImageUriCandidate
            customImageUriCandidate.length > 200 && !customImageUriCandidate.startsWith("http") && !customImageUriCandidate.startsWith("/") && !customImageUriCandidate.startsWith("content:") && !customImageUriCandidate.startsWith("file:") -> customImageUriCandidate
            else -> ""
        }
        if (rawBase64.isNotBlank()) {
            val restoredPath = ImageStorageHelper.saveBase64Image(context, rawBase64, "cloud_$effectiveId")
            if (restoredPath != null) {
                localCustomImageUri = restoredPath
            }
        }

        // Si es una imagen personalizada pero no tenemos archivo local ni base64, intentamos descargar de Firebase Storage
        if (isExplicitCustom && (localCustomImageUri.isBlank() || localCustomImageUri.startsWith("gs://") || (!localCustomImageUri.startsWith("http") && !localCustomImageUri.startsWith("data:image") && !File(localCustomImageUri).exists()))) {
            val currentUid = auth?.currentUser?.uid ?: (data["userId"] as? String)?.trim() ?: (data["user_id"] as? String)?.trim().orEmpty()
            if (currentUid.isNotBlank()) {
                val fetchedLocalPath = tryFetchStorageImage(currentUid, effectiveId)
                if (fetchedLocalPath != null) {
                    localCustomImageUri = fetchedLocalPath
                }
            }
        }

        val maxSlots = parseSafeInt(data["maxSlots"] ?: data["max_slots"] ?: data["totalSlots"] ?: data["total_slots"], 4)

        val subEnableAlarm = parseSafeBoolean(data["enableAlarm"] ?: data["enable_alarm"] ?: data["hasAlarm"] ?: data["alarm"])
        val subAlarmValue = parseSafeInt(data["alarmValue"] ?: data["alarm_value"], 3)
        val subAlarmUnit = (data["alarmUnit"] as? String)?.trim() ?: (data["alarm_unit"] as? String)?.trim() ?: "days"
        val subAlarmDaysBefore = parseSafeInt(data["alarmDaysBefore"] ?: data["alarm_days_before"], subAlarmValue)

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
            maxSlots = maxSlots,
            notes = notes,
            iconType = iconType,
            iconKey = iconKey,
            customImageUri = localCustomImageUri,
            iconColorHex = iconColorHex,
            enableAlarm = subEnableAlarm,
            alarmValue = subAlarmValue,
            alarmUnit = subAlarmUnit,
            alarmDaysBefore = subAlarmDaysBefore,
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

            val mId = parseSafeLong(rawItem["id"], parseSafeLong(rawItem["memberId"], parseSafeLong(rawItem["member_id"], effectiveId * 1000L + index + 1)))

            val mSharing = (rawItem["sharingPlatform"] as? String)?.trim()
                ?: (rawItem["platform"] as? String)?.trim()
                ?: (rawItem["sharing_platform"] as? String)?.trim()
                ?: (rawItem["service"] as? String)?.trim()
                ?: ""

            val mContact = (rawItem["memberContact"] as? String)?.trim()
                ?: (rawItem["contact"] as? String)?.trim()
                ?: (rawItem["email"] as? String)?.trim()
                ?: (rawItem["phone"] as? String)?.trim()
                ?: (rawItem["member_contact"] as? String)?.trim().orEmpty()

            val rawJoined = rawItem["joinedDate"] ?: rawItem["joined_date"] ?: rawItem["date"] ?: rawItem["createdAt"] ?: rawItem["created_at"]
            val mJoinedTimestamp = parseSafeTimestamp(rawJoined)
            val mJoinedStr = when (rawJoined) {
                is String -> if (rawJoined.length >= 10 && rawJoined[4] == '-' && rawJoined[7] == '-') rawJoined.substring(0, 10) else formatIsoDay(mJoinedTimestamp)
                else -> formatIsoDay(mJoinedTimestamp)
            }

            val mNextPaymentDate = (rawItem["nextPaymentDate"] as? String)?.trim()
                ?: (rawItem["next_payment_date"] as? String)?.trim()
                ?: (rawItem["nextPayment"] as? String)?.trim()
                ?: ""

            val mPaymentFrequencyValue = parseSafeInt(rawItem["paymentFrequencyValue"] ?: rawItem["payment_frequency_value"] ?: rawItem["frequencyValue"] ?: rawItem["frequency"], 1)
            val mPaymentFrequencyUnit = (rawItem["paymentFrequencyUnit"] as? String)?.trim()
                ?: (rawItem["payment_frequency_unit"] as? String)?.trim()
                ?: (rawItem["frequencyUnit"] as? String)?.trim()
                ?: "months"

            val mAutoRepeatPayment = parseSafeBoolean(rawItem["autoRepeatPayment"] ?: rawItem["auto_repeat_payment"] ?: rawItem["autoRepeat"] ?: true)

            val mPaymentMethod = (rawItem["paymentMethod"] as? String)?.trim()
                ?: (rawItem["payment_method"] as? String)?.trim()
                ?: (rawItem["method"] as? String)?.trim()
                ?: ""

            val mLastPaymentDate = (rawItem["lastPaymentDate"] as? String)?.trim()
                ?: (rawItem["last_payment_date"] as? String)?.trim()
                ?: ""

            val mEnableAlarm = parseSafeBoolean(rawItem["enableAlarm"] ?: rawItem["enable_alarm"] ?: rawItem["hasAlarm"] ?: rawItem["alarm"])
            val mAlarmValue = parseSafeInt(rawItem["alarmValue"] ?: rawItem["alarm_value"], 3)
            val mAlarmUnit = (rawItem["alarmUnit"] as? String)?.trim() ?: (rawItem["alarm_unit"] as? String)?.trim() ?: "days"
            val mAlarmDaysBefore = parseSafeInt(rawItem["alarmDaysBefore"] ?: rawItem["alarm_days_before"], mAlarmValue)

            val matchedPlatformPrice = parsedPlatformPrices.find { it.platformName.equals(mSharing, ignoreCase = true) }

            val rawContribution = parseSafeDouble(
                rawItem["contributionAmount"] ?: rawItem["amount"] ?: rawItem["contribution"] ?: rawItem["price"] ?: rawItem["cost"]
            )
            val mContribution = if (rawContribution > 0.0) {
                rawContribution
            } else if (matchedPlatformPrice != null && matchedPlatformPrice.pricePerUser > 0.0) {
                matchedPlatformPrice.pricePerUser
            } else if (defaultContributionPerUser > 0.0) {
                defaultContributionPerUser
            } else {
                0.0
            }

            val explicitCurrencyRaw = (rawItem["currency"] as? String)?.trim()
                ?: (rawItem["moneda"] as? String)?.trim()
                ?: (rawItem["currencyCode"] as? String)?.trim()
                ?: (rawItem["currency_code"] as? String)?.trim()

            val mCurrency = when {
                matchedPlatformPrice != null && matchedPlatformPrice.currency.isNotBlank() && (
                    explicitCurrencyRaw.isNullOrBlank() ||
                    explicitCurrencyRaw.equals(currency, ignoreCase = true) ||
                    (explicitCurrencyRaw.equals("TRY", ignoreCase = true) && !matchedPlatformPrice.currency.equals("TRY", ignoreCase = true)) ||
                    (explicitCurrencyRaw.equals("€", ignoreCase = true) && matchedPlatformPrice.currency.equals("EUR", ignoreCase = true))
                ) -> {
                    CurrencyManager.findCurrency(matchedPlatformPrice.currency).code
                }
                !explicitCurrencyRaw.isNullOrBlank() -> {
                    CurrencyManager.findCurrency(explicitCurrencyRaw).code
                }
                matchedPlatformPrice != null && matchedPlatformPrice.currency.isNotBlank() -> {
                    CurrencyManager.findCurrency(matchedPlatformPrice.currency).code
                }
                else -> {
                    CurrencyManager.findCurrency(currency).code
                }
            }

            val statusStr = (rawItem["paymentStatus"] as? String)?.lowercase()?.trim()
                ?: (rawItem["payment_status"] as? String)?.lowercase()?.trim()
                ?: (rawItem["status"] as? String)?.lowercase()?.trim()
                ?: ""

            val isPendingPayment = parseSafeBoolean(rawItem["isPendingPayment"]) ||
                    parseSafeBoolean(rawItem["is_pending_payment"]) ||
                    parseSafeBoolean(rawItem["pendingPayment"]) ||
                    parseSafeBoolean(rawItem["pending_payment"]) ||
                    statusStr == "pending_payment" || statusStr == "pending" || statusStr == "pendiente_pago" || statusStr == "pendiente"

            val isPendingRemoval = parseSafeBoolean(rawItem["isPendingRemoval"]) ||
                    parseSafeBoolean(rawItem["is_pending_removal"]) ||
                    parseSafeBoolean(rawItem["pendingRemoval"]) ||
                    parseSafeBoolean(rawItem["pending_removal"]) ||
                    parseSafeBoolean(rawItem["isPendingDelete"]) ||
                    parseSafeBoolean(rawItem["pendingDelete"]) ||
                    statusStr == "pending_removal" || statusStr == "removal" || statusStr == "baja" || statusStr == "pendiente_baja" || statusStr == "pendiente_eliminar"

            val isPendingRegistration = parseSafeBoolean(rawItem["isPendingRegistration"]) ||
                    parseSafeBoolean(rawItem["is_pending_registration"]) ||
                    parseSafeBoolean(rawItem["pendingRegistration"]) ||
                    parseSafeBoolean(rawItem["pending_registration"]) ||
                    parseSafeBoolean(rawItem["isPendingAdd"]) ||
                    parseSafeBoolean(rawItem["pendingAdd"]) ||
                    statusStr == "pending_registration" || statusStr == "registration" || statusStr == "alta" || statusStr == "pendiente_alta" || statusStr == "pendiente_dar_de_alta"

            // Toggle exclusivity rule:
            val finalPendingPayment = isPendingPayment && !isPendingRemoval && !isPendingRegistration
            val finalPendingRemoval = isPendingRemoval && !isPendingRegistration
            val finalPendingRegistration = isPendingRegistration

            val rawPaid = if (rawItem.containsKey("isPaidThisMonth")) {
                parseSafeBoolean(rawItem["isPaidThisMonth"])
            } else if (rawItem.containsKey("is_paid_this_month")) {
                parseSafeBoolean(rawItem["is_paid_this_month"])
            } else if (rawItem.containsKey("paidThisMonth")) {
                parseSafeBoolean(rawItem["paidThisMonth"])
            } else if (rawItem.containsKey("paid_this_month")) {
                parseSafeBoolean(rawItem["paid_this_month"])
            } else if (rawItem.containsKey("paid")) {
                parseSafeBoolean(rawItem["paid"])
            } else if (rawItem.containsKey("is_paid")) {
                parseSafeBoolean(rawItem["is_paid"])
            } else {
                statusStr == "paid" || statusStr == "al_dia" || statusStr == "active" || (!finalPendingPayment && !finalPendingRemoval && !finalPendingRegistration)
            }

            val finalPaid = rawPaid && !finalPendingPayment && !finalPendingRemoval && !finalPendingRegistration
            val finalPaymentStatus = if (statusStr.isNotBlank()) statusStr else if (finalPaid) "paid" else "pending"

            val mNotes = (rawItem["notes"] as? String)?.trim() ?: (rawItem["note"] as? String)?.trim().orEmpty()

            val memberObj = MemberEntity(
                id = if (mId > 0) mId else (effectiveId * 1000L + index + 1),
                subscriptionId = effectiveId,
                memberName = mName,
                sharingPlatform = mSharing,
                memberContact = mContact,
                joinedDate = mJoinedTimestamp,
                joinedDateStr = mJoinedStr,
                nextPaymentDate = mNextPaymentDate,
                paymentFrequencyValue = mPaymentFrequencyValue,
                paymentFrequencyUnit = mPaymentFrequencyUnit,
                autoRepeatPayment = mAutoRepeatPayment,
                paymentMethod = mPaymentMethod,
                lastPaymentDate = mLastPaymentDate,
                enableAlarm = mEnableAlarm,
                alarmValue = mAlarmValue,
                alarmUnit = mAlarmUnit,
                alarmDaysBefore = mAlarmDaysBefore,
                contributionAmount = mContribution,
                currency = mCurrency,
                isPaidThisMonth = finalPaid,
                isPendingPayment = finalPendingPayment,
                isPendingRemoval = finalPendingRemoval,
                isPendingRegistration = finalPendingRegistration,
                paymentStatus = finalPaymentStatus,
                notes = mNotes
            )

            val memberKey = mName.lowercase().trim()
            memberEntitiesMap[memberKey] = memberObj
        }

        // 1. Parsear del array 'members' si existe en el documento
        val hasMembersArray = data.containsKey("members") || data.containsKey("memberList")
        val rawMembersList = (data["members"] as? List<*>) ?: (data["memberList"] as? List<*>)
        if (rawMembersList != null) {
            rawMembersList.forEachIndexed { index, rawItem ->
                if (rawItem is Map<*, *>) {
                    addMemberFromMap(rawItem, index)
                }
            }
        } else if (!hasMembersArray) {
            // Solo si NO existe la propiedad 'members' en el documento raíz (esquema legacy basado exclusivamente en subcolección),
            // consultar la subcolección 'members'. Si el documento tiene la clave 'members', el array es la fuente canónica
            // para evitar resucitar miembros que fueron eliminados desde la Web App o la app móvil.
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
        }

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

            val subKey = if (sub.id > 0) {
                sub.id.toString()
            } else if (sub.platformName.isNotBlank()) {
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
                val preferredSub = if (timestamp >= existing.third) sub else existing.first
                val otherSub = if (timestamp >= existing.third) existing.first else sub

                val finalCustomImageUri = if (preferredSub.customImageUri.isNotBlank()) {
                    preferredSub.customImageUri
                } else {
                    otherSub.customImageUri
                }
                val finalIconType = if (preferredSub.iconType == "CUSTOM_IMAGE" || otherSub.iconType == "CUSTOM_IMAGE" || finalCustomImageUri.isNotBlank()) {
                    "CUSTOM_IMAGE"
                } else {
                    preferredSub.iconType
                }
                val finalSub = preferredSub.copy(
                    customImageUri = finalCustomImageUri,
                    iconType = finalIconType
                )

                val finalTimestamp = maxOf(timestamp, existing.third)
                val memberMap = if (timestamp >= existing.third) {
                    mutableMapOf<String, MemberEntity>().apply {
                        members.forEach { m -> put(m.memberName.trim().lowercase(), m) }
                    }
                } else {
                    existing.second
                }

                mergedMap[subKey] = Triple(finalSub, memberMap, finalTimestamp)
            }
        }

        val finalItemsToSync = mergedMap.values.map { triple ->
            val sub = triple.first
            val parsedPlatformPrices = PlatformPricingHelper.parse(sub.platformPricing)
            val membersList = triple.second.values.mapIndexed { idx, m ->
                val matchedPlatformPrice = parsedPlatformPrices.find { it.platformName.equals(m.sharingPlatform, ignoreCase = true) }
                val resolvedCurrency = when {
                    matchedPlatformPrice != null && matchedPlatformPrice.currency.isNotBlank() && (
                        m.currency.isBlank() ||
                        m.currency.equals(sub.currency, ignoreCase = true) ||
                        (m.currency.equals("TRY", ignoreCase = true) && !matchedPlatformPrice.currency.equals("TRY", ignoreCase = true)) ||
                        (m.currency.equals("€", ignoreCase = true) && matchedPlatformPrice.currency.equals("EUR", ignoreCase = true))
                    ) -> {
                        CurrencyManager.findCurrency(matchedPlatformPrice.currency).code
                    }
                    m.currency.isNotBlank() -> {
                        CurrencyManager.findCurrency(m.currency).code
                    }
                    matchedPlatformPrice != null && matchedPlatformPrice.currency.isNotBlank() -> {
                        CurrencyManager.findCurrency(matchedPlatformPrice.currency).code
                    }
                    else -> {
                        CurrencyManager.findCurrency(sub.currency).code
                    }
                }
                val resolvedContribution = if (m.contributionAmount > 0.0) {
                    m.contributionAmount
                } else if (matchedPlatformPrice != null && matchedPlatformPrice.pricePerUser > 0.0) {
                    matchedPlatformPrice.pricePerUser
                } else if (sub.defaultContributionPerUser > 0.0) {
                    sub.defaultContributionPerUser
                } else {
                    0.0
                }
                m.copy(
                    subscriptionId = sub.id,
                    id = if (m.id > 0) m.id else (sub.id * 1000L + idx + 1),
                    currency = resolvedCurrency,
                    contributionAmount = resolvedContribution
                )
            }
            sub to membersList
        }

        dao.replaceAllSubscriptionsAndMembers(finalItemsToSync)
    }

    /**
     * Descarga y fusiona las suscripciones de Firestore a la base de datos local consultando todas las rutas posibles
     */
    suspend fun syncFromCloud(): Boolean = withContext(Dispatchers.IO) {
        val user = auth?.currentUser
        if (user == null) {
            _syncStatus.value = "⚠️ Debes iniciar sesión con tu cuenta de Google para descargar datos de la nube."
            return@withContext false
        }
        val db = firestore
        if (db == null) {
            _syncStatus.value = "⚠️ Firestore no está disponible en este momento."
            return@withContext false
        }
        _isSyncing.value = true
        _syncStatus.value = "☁️ Descargando datos desde la nube..."
        try {
            val allDocs = mutableListOf<com.google.firebase.firestore.DocumentSnapshot>()

            // 1. Ruta users/{uid}/subscriptions
            try {
                val userSnap = db.collection("users").document(user.uid).collection("subscriptions").get().await()
                if (userSnap != null) {
                    allDocs.addAll(userSnap.documents)
                }
            } catch (e: Exception) {
                Log.w("FirebaseAuthService", "Error reading users/${user.uid}/subscriptions: ${e.message}")
            }

            // 2. Ruta users/{uid} (documento raíz por si la web guarda el array directo)
            try {
                val userDocSnap = db.collection("users").document(user.uid).get().await()
                if (userDocSnap != null && userDocSnap.exists()) {
                    val rootData = userDocSnap.data
                    val rawSubsList = rootData?.get("subscriptions") as? List<*>
                    if (rawSubsList != null && rawSubsList.isNotEmpty()) {
                        // Procesar cada mapa del array como suscripción
                        val parsedItems = mutableListOf<Triple<SubscriptionEntity, List<MemberEntity>, Long>>()
                        for (rawSub in rawSubsList) {
                            if (rawSub is Map<*, *>) {
                                val sId = (rawSub["id"] as? Number)?.toLong() ?: 0L
                                val pName = (rawSub["platformName"] as? String)?.trim() ?: (rawSub["name"] as? String)?.trim().orEmpty()
                                if (sId > 0 || pName.isNotBlank()) {
                                    val rawImgUri = (rawSub["customImageUri"] as? String)?.trim() ?: (rawSub["imageUrl"] as? String)?.trim().orEmpty()
                                    val rawImgBase64 = (rawSub["customImageBase64"] as? String)?.trim() ?: (rawSub["imageBase64"] as? String)?.trim().orEmpty()
                                    var finalLocalImg = rawImgUri
                                    val candidateBase64 = if (rawImgBase64.isNotBlank()) rawImgBase64 else if (rawImgUri.startsWith("data:image")) rawImgUri else ""
                                    if (candidateBase64.isNotBlank()) {
                                        val restored = ImageStorageHelper.saveBase64Image(context, candidateBase64, "cloud_${if (sId > 0) sId else Math.abs(pName.hashCode().toLong())}")
                                        if (restored != null) finalLocalImg = restored
                                    }
                                    val rawIconTypeStr = (rawSub["iconType"] as? String)?.trim().orEmpty()
                                    val parsedIconType = if (rawIconTypeStr.equals("CUSTOM_IMAGE", ignoreCase = true) || finalLocalImg.isNotBlank() || candidateBase64.isNotBlank()) "CUSTOM_IMAGE" else (if (rawIconTypeStr.isNotBlank()) rawIconTypeStr else "PRESET")

                                    val fakeEntity = SubscriptionEntity(
                                        id = if (sId > 0) sId else Math.abs(pName.hashCode().toLong()),
                                        platformName = pName,
                                        customPlanName = (rawSub["customPlanName"] as? String)?.trim() ?: (rawSub["plan"] as? String)?.trim().orEmpty(),
                                        mainUserName = (rawSub["mainUserName"] as? String)?.trim() ?: (rawSub["mainUser"] as? String)?.trim().orEmpty(),
                                        mainUserContact = (rawSub["mainUserContact"] as? String)?.trim().orEmpty(),
                                        cost = (rawSub["cost"] as? Number)?.toDouble() ?: (rawSub["price"] as? Number)?.toDouble() ?: 0.0,
                                        billingPeriod = (rawSub["billingPeriod"] as? String)?.trim() ?: "MONTHLY",
                                        billingDay = (rawSub["billingDay"] as? Number)?.toInt() ?: 1,
                                        billingMonth = (rawSub["billingMonth"] as? Number)?.toInt() ?: 1,
                                        currency = (rawSub["currency"] as? String)?.trim() ?: "€",
                                        defaultContributionPerUser = (rawSub["defaultContributionPerUser"] as? Number)?.toDouble() ?: 0.0,
                                        platformPricing = (rawSub["platformPricing"] as? String)?.trim().orEmpty(),
                                        category = (rawSub["category"] as? String)?.trim() ?: "Streaming",
                                        notes = (rawSub["notes"] as? String)?.trim().orEmpty(),
                                        iconType = parsedIconType,
                                        iconKey = (rawSub["iconKey"] as? String)?.trim() ?: "Netflix",
                                        customImageUri = finalLocalImg,
                                        iconColorHex = (rawSub["iconColorHex"] as? String)?.trim() ?: "#6366F1",
                                        enableAlarm = parseSafeBoolean(rawSub["enableAlarm"] ?: rawSub["enable_alarm"] ?: rawSub["hasAlarm"]),
                                        alarmValue = parseSafeInt(rawSub["alarmValue"] ?: rawSub["alarm_value"], 3),
                                        alarmUnit = (rawSub["alarmUnit"] as? String)?.trim() ?: (rawSub["alarm_unit"] as? String)?.trim() ?: "days",
                                        alarmDaysBefore = parseSafeInt(rawSub["alarmDaysBefore"] ?: rawSub["alarm_days_before"], 3),
                                        createdAt = (rawSub["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                                    )
                                    val memsList = mutableListOf<MemberEntity>()
                                    val rawMems = rawSub["members"] as? List<*>
                                    rawMems?.forEachIndexed { mIdx, rm ->
                                        if (rm is Map<*, *>) {
                                            val mName = (rm["memberName"] as? String)?.trim() ?: (rm["name"] as? String)?.trim().orEmpty()
                                            if (mName.isNotBlank()) {
                                                val mId = (rm["id"] as? Number)?.toLong() ?: (fakeEntity.id * 1000L + mIdx + 1)
                                                val isPending = parseSafeBoolean(rm["isPendingPayment"]) || parseSafeBoolean(rm["pending"])
                                                val isPaid = (parseSafeBoolean(rm["isPaidThisMonth"]) || parseSafeBoolean(rm["paid"])) && !isPending
                                                val mJoinedDate = (rm["joinedDate"] as? Number)?.toLong()
                                                    ?: (rm["joined_date"] as? Number)?.toLong()
                                                    ?: System.currentTimeMillis()
                                                memsList.add(
                                                    MemberEntity(
                                                        id = mId,
                                                        subscriptionId = fakeEntity.id,
                                                        memberName = mName,
                                                        sharingPlatform = (rm["sharingPlatform"] as? String)?.trim().orEmpty(),
                                                        memberContact = (rm["memberContact"] as? String)?.trim().orEmpty(),
                                                        joinedDate = mJoinedDate,
                                                        contributionAmount = (rm["contributionAmount"] as? Number)?.toDouble() ?: (rm["amount"] as? Number)?.toDouble() ?: 0.0,
                                                        isPaidThisMonth = isPaid,
                                                        isPendingPayment = isPending,
                                                        isPendingRemoval = parseSafeBoolean(rm["isPendingRemoval"]),
                                                        isPendingRegistration = parseSafeBoolean(rm["isPendingRegistration"]),
                                                        notes = (rm["notes"] as? String)?.trim().orEmpty()
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    parsedItems.add(Triple(fakeEntity, memsList, System.currentTimeMillis()))
                                }
                            }
                        }
                        if (parsedItems.isNotEmpty()) {
                            dao.replaceAllSubscriptionsAndMembers(parsedItems.map { it.first to it.second })
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("FirebaseAuthService", "Error reading users/${user.uid} root doc: ${e.message}")
            }

            // 3. Ruta subscriptions (userId == uid)
            try {
                val rootSnap = db.collection("subscriptions").whereEqualTo("userId", user.uid).get().await()
                if (rootSnap != null) {
                    allDocs.addAll(rootSnap.documents)
                }
            } catch (_: Exception) {}

            // 4. Ruta subscriptions (user_id == uid)
            try {
                val rootSnapSnake = db.collection("subscriptions").whereEqualTo("user_id", user.uid).get().await()
                if (rootSnapSnake != null) {
                    allDocs.addAll(rootSnapSnake.documents)
                }
            } catch (_: Exception) {}

            if (allDocs.isNotEmpty()) {
                processSnapshotAndSyncToRoom(allDocs)
                _syncStatus.value = "✅ Suscripciones sincronizadas desde la nube"
            } else {
                _syncStatus.value = "ℹ️ Sincronización completada. No hay cambios pendientes."
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
     * Escucha en tiempo real cambios desde la ruta canónica unificada (users/{uid}/subscriptions)
     */
    private fun setupFirestoreRealtimeSync(uid: String) {
        val db = firestore ?: return
        removeActiveListeners()

        // Listener canónico y exclusivo en users/{uid}/subscriptions
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
    }

    private fun getWebClientId(): String? {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else "498651324948-18qocdi9iqatn6kc4isaof5d0bhate0q.apps.googleusercontent.com"
        } catch (_: Exception) {
            "498651324948-18qocdi9iqatn6kc4isaof5d0bhate0q.apps.googleusercontent.com"
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
