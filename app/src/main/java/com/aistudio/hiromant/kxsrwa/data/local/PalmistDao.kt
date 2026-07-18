package com.aistudio.hiromant.kxsrwa.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PalmistDao {
    @Query("SELECT * FROM readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<ReadingEntity>>

    @Query("SELECT * FROM readings WHERE id = :id LIMIT 1")
    suspend fun getReadingById(id: Long): ReadingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: ReadingEntity): Long

    @Query("DELETE FROM readings WHERE id = :id")
    suspend fun deleteReadingById(id: Long)

    @Query("DELETE FROM readings")
    suspend fun clearHistory()

    @Query("SELECT * FROM user_profile WHERE id = 'current_user' LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 'current_user' LIMIT 1")
    suspend fun getUserProfileSync(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profile")
    suspend fun clearUserProfile()

    @Query("SELECT * FROM app_billing_state WHERE id = 1 LIMIT 1")
    fun getBillingState(): Flow<BillingStateEntity?>

    @Query("SELECT * FROM app_billing_state WHERE id = 1 LIMIT 1")
    suspend fun getBillingStateSync(): BillingStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillingState(state: BillingStateEntity)

    // --- Методы для работы с историей платежей ---

    @Query("SELECT * FROM payment_history ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<PaymentHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentHistoryEntity): Long

    @Query("DELETE FROM payment_history")
    suspend fun clearPaymentHistory()
}
