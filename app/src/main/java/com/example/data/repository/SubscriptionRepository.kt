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
        dao.updateMemberPaymentStatus(memberId = memberId, isPaid = isPaid, isPendingPayment = !isPaid, paymentStatus = if (isPaid) "paid" else "pending")
    }

    suspend fun toggleMemberPendingPayment(memberId: Long, isPending: Boolean) {
        dao.updateMemberPendingPayment(memberId = memberId, isPending = isPending)
    }

    suspend fun toggleMemberPendingRemoval(memberId: Long, isPending: Boolean) {
        dao.updateMemberPendingRemoval(memberId = memberId, isPending = isPending)
    }

    suspend fun toggleMemberPendingRegistration(memberId: Long, isPending: Boolean) {
        dao.updateMemberPendingRegistration(memberId = memberId, isPending = isPending)
    }

    suspend fun updateMemberPaymentFlags(
        memberId: Long,
        isPaid: Boolean,
        isPendingPayment: Boolean,
        isPendingRemoval: Boolean,
        isPendingRegistration: Boolean,
        paymentStatus: String = if (isPaid) "paid" else "pending"
    ) {
        dao.updateMemberPaymentFlags(
            memberId = memberId,
            isPaid = isPaid,
            isPendingPayment = isPendingPayment,
            isPendingRemoval = isPendingRemoval,
            isPendingRegistration = isPendingRegistration,
            paymentStatus = paymentStatus
        )
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

