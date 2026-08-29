package com.apleq.app.data.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.apleq.app.data.local.MemberEntity
import com.apleq.app.data.local.SubscriptionDao
import com.apleq.app.data.local.SubscriptionEntity
import com.apleq.app.ui.util.ImageStorageHelper
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
                put("maxSlots", sub.maxSlots)
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
                put("enableAlarm", sub.enableAlarm)
                put("alarmValue", sub.alarmValue)
                put("alarmUnit", sub.alarmUnit)
                put("alarmDaysBefore", sub.alarmDaysBefore)
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
                put("joinedDateStr", member.joinedDateStr)
                put("nextPaymentDate", member.nextPaymentDate)
                put("paymentFrequencyValue", member.paymentFrequencyValue)
                put("paymentFrequencyUnit", member.paymentFrequencyUnit)
                put("autoRepeatPayment", member.autoRepeatPayment)
                put("paymentMethod", member.paymentMethod)
                put("lastPaymentDate", member.lastPaymentDate)
                put("enableAlarm", member.enableAlarm)
                put("alarmValue", member.alarmValue)
                put("alarmUnit", member.alarmUnit)
                put("alarmDaysBefore", member.alarmDaysBefore)
                put("contributionAmount", member.contributionAmount)
                put("currency", member.currency)
                put("isPaidThisMonth", member.isPaidThisMonth)
                put("isPendingPayment", member.isPendingPayment)
                put("isPendingRemoval", member.isPendingRemoval)
                put("isPendingRegistration", member.isPendingRegistration)
                put("paymentStatus", member.paymentStatus)
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
                    maxSlots = obj.optInt("maxSlots", 4),
                    notes = obj.optString("notes", ""),
                    iconType = obj.optString("iconType", "PRESET"),
                    iconKey = obj.optString("iconKey", "Netflix"),
                    customImageUri = finalCustomImageUri,
                    iconColorHex = obj.optString("iconColorHex", "#6366F1"),
                    enableAlarm = obj.optBoolean("enableAlarm", false),
                    alarmValue = obj.optInt("alarmValue", 3),
                    alarmUnit = obj.optString("alarmUnit", "days"),
                    alarmDaysBefore = obj.optInt("alarmDaysBefore", 3),
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
                    val isPendingPayment = obj.optBoolean("isPendingPayment", false)
                    val isPendingRemoval = obj.optBoolean("isPendingRemoval", false)
                    val isPendingRegistration = obj.optBoolean("isPendingRegistration", false)
                    val isPaid = obj.optBoolean("isPaidThisMonth", true) && !isPendingPayment && !isPendingRemoval && !isPendingRegistration

                    val memberEntity = MemberEntity(
                        id = 0, // Auto-generate new primary key
                        subscriptionId = newSubId,
                        memberName = obj.optString("memberName", "Usuario"),
                        sharingPlatform = obj.optString("sharingPlatform", ""),
                        memberContact = obj.optString("memberContact", ""),
                        joinedDate = obj.optLong("joinedDate", System.currentTimeMillis()),
                        joinedDateStr = obj.optString("joinedDateStr", ""),
                        nextPaymentDate = obj.optString("nextPaymentDate", ""),
                        paymentFrequencyValue = obj.optInt("paymentFrequencyValue", 1),
                        paymentFrequencyUnit = obj.optString("paymentFrequencyUnit", "months"),
                        autoRepeatPayment = obj.optBoolean("autoRepeatPayment", true),
                        paymentMethod = obj.optString("paymentMethod", "Bizum"),
                        lastPaymentDate = obj.optString("lastPaymentDate", ""),
                        enableAlarm = obj.optBoolean("enableAlarm", false),
                        alarmValue = obj.optInt("alarmValue", 3),
                        alarmUnit = obj.optString("alarmUnit", "days"),
                        alarmDaysBefore = obj.optInt("alarmDaysBefore", 3),
                        contributionAmount = obj.optDouble("contributionAmount", 0.0),
                        currency = obj.optString("currency", "EUR"),
                        isPaidThisMonth = isPaid,
                        isPendingPayment = isPendingPayment,
                        isPendingRemoval = isPendingRemoval,
                        isPendingRegistration = isPendingRegistration,
                        paymentStatus = obj.optString("paymentStatus", if (isPaid) "paid" else "pending"),
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
