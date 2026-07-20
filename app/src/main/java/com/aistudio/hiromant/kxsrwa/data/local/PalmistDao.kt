package com.aistudio.hiromant.kxsrwa.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Интерфейс доступа к данным (DAO) Room для выполнения SQL-запросов к таблицам приложения
@Dao
interface PalmistDao {
    
    // Получение всех сохраненных сеансов анализа, отсортированных по времени от новых к старым
    @Query("SELECT * FROM readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<ReadingEntity>>

    // Поиск конкретной записи сеанса анализа по его уникальному ID
    @Query("SELECT * FROM readings WHERE id = :id LIMIT 1")
    suspend fun getReadingById(id: Long): ReadingEntity?

    // Вставка нового или замена существующего сеанса анализа ладоней
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: ReadingEntity): Long

    // Удаление записи анализа из истории по ее уникальному идентификатору
    @Query("DELETE FROM readings WHERE id = :id")
    suspend fun deleteReadingById(id: Long)

    // Полная очистка всей сохраненной истории анализов
    @Query("DELETE FROM readings")
    suspend fun clearHistory()

    // Наблюдение за изменениями профиля текущего пользователя в реальном времени
    @Query("SELECT * FROM user_profile WHERE id = 'current_user' LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    // Синхронное получение данных профиля текущего пользователя (блокирующий/фоновый вызов)
    @Query("SELECT * FROM user_profile WHERE id = 'current_user' LIMIT 1")
    suspend fun getUserProfileSync(): UserProfileEntity?

    // Создание или обновление информации о пользователе в таблице профиля
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    // Сброс и удаление профиля пользователя из базы данных
    @Query("DELETE FROM user_profile")
    suspend fun clearUserProfile()

    // Асинхронное отслеживание изменений лимитов и состояния покупок
    @Query("SELECT * FROM app_billing_state WHERE id = 1 LIMIT 1")
    fun getBillingState(): Flow<BillingStateEntity?>

    // Синхронное получение текущего баланса сеансов и статуса подписки
    @Query("SELECT * FROM app_billing_state WHERE id = 1 LIMIT 1")
    suspend fun getBillingStateSync(): BillingStateEntity?

    // Вставка или обновление данных биллинга (начисление сеансов или покупка подписки)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillingState(state: BillingStateEntity)

    // --- Методы для работы с единой историей транзакций и оплат ---

    // Получение полного списка платежей и бонусов пользователя от новых к старым
    @Query("SELECT * FROM payment_history ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<PaymentHistoryEntity>>

    // Сохранение транзакции платежа или начисления реферального/шеринг-бонуса в БД
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentHistoryEntity): Long

    // Очистка списка истории платежей и бонусов
    @Query("DELETE FROM payment_history")
    suspend fun clearPaymentHistory()
}
