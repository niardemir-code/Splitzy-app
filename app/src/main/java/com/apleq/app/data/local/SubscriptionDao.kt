package com.apleq.app.data.local

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

    @Query("UPDATE members SET isPaidThisMonth = :isPaid, isPendingPayment = :isPendingPayment, isPendingRemoval = :isPendingRemoval, isPendingRegistration = :isPendingRegistration, paymentStatus = :paymentStatus WHERE id = :memberId")
    suspend fun updateMemberPaymentFlags(
        memberId: Long,
        isPaid: Boolean,
        isPendingPayment: Boolean,
        isPendingRemoval: Boolean,
        isPendingRegistration: Boolean,
        paymentStatus: String
    )

    @Query("UPDATE members SET isPaidThisMonth = :isPaid, isPendingPayment = :isPendingPayment, isPendingRemoval = 0, isPendingRegistration = 0, paymentStatus = :paymentStatus WHERE id = :memberId")
    suspend fun updateMemberPaymentStatus(memberId: Long, isPaid: Boolean, isPendingPayment: Boolean, paymentStatus: String = if (isPaid) "paid" else "pending")

    @Query("UPDATE members SET isPendingPayment = :isPending, isPaidThisMonth = CASE WHEN :isPending = 1 THEN 0 ELSE 1 END, isPendingRemoval = 0, isPendingRegistration = 0, paymentStatus = CASE WHEN :isPending = 1 THEN 'pending' ELSE 'paid' END WHERE id = :memberId")
    suspend fun updateMemberPendingPayment(memberId: Long, isPending: Boolean)

    @Query("UPDATE members SET isPendingRemoval = :isPending, isPaidThisMonth = CASE WHEN :isPending = 1 THEN 0 ELSE 1 END, isPendingPayment = 0, isPendingRegistration = 0, paymentStatus = CASE WHEN :isPending = 1 THEN 'pending' ELSE 'paid' END WHERE id = :memberId")
    suspend fun updateMemberPendingRemoval(memberId: Long, isPending: Boolean)

    @Query("UPDATE members SET isPendingRegistration = :isPending, isPaidThisMonth = CASE WHEN :isPending = 1 THEN 0 ELSE 1 END, isPendingPayment = 0, isPendingRemoval = 0, paymentStatus = CASE WHEN :isPending = 1 THEN 'pending' ELSE 'paid' END WHERE id = :memberId")
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

