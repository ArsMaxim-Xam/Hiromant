package com.aistudio.hiromant.kxsrwa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Сущность (таблица) в базе данных Room, хранящая результаты проведенных анализов ладоней и совместимости
@Entity(tableName = "readings")
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Уникальный идентификатор записи с автогенерацией
    val timestamp: Long = System.currentTimeMillis(), // Системное время прохождения анализа ладоней
    val name: String, // Имя пользователя, прошедшего сеанс
    val gender: String, // Пол ("Мужской", "Женский")
    val age: Int, // Возраст пользователя
    val height: Int, // Рост пользователя
    val dominantHand: String, // Ведущая рука ("Правая", "Левая")
    val analysisType: String, // Тип анализа: "brief_char" (краткий характер), "full_char" (полный характер), "brief_path" (краткий путь), "full_path" (полный путь), "compatibility" (совместимость)
    val resultJson: String, // Текстовый результат анализа, сгенерированный ИИ Gemini в формате JSON
    val imageUrl: String? = null, // Путь к фотографии ладони первого человека
    val partnerName: String? = null, // Имя партнёра (заполняется только для совместимости)
    val partnerImageUrl: String? = null, // Путь к фотографии ладони партнёра
    val leftPalmPath: String? = null, // Локальный путь к снимку левой ладони
    val leftBackPath: String? = null, // Локальный путь к снимку левой тыльной стороны
    val rightPalmPath: String? = null, // Локальный путь к снимку правой ладони
    val rightBackPath: String? = null, // Локальный путь к снимку правой тыльной стороны
    val videoPath: String? = null // Локальный путь к видеозаписи рук (если была сделана)
)

// Сущность (таблица), содержащая информацию о профиле текущего пользователя приложения
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "current_user", // Фиксированный ключ для сохранения единственного профиля пользователя
    val email: String? = null, // Электронная почта
    val phone: String? = null, // Мобильный телефон
    val isRegistered: Boolean = false, // Флаг пройденной авторизации/регистрации
    val name: String = "", // Имя искателя
    val gender: String = "", // Пол пользователя
    val age: Int = 18, // Возраст пользователя
    val height: Int = 170, // Рост пользователя в см
    val dominantHand: String = "Right" // Ведущая рука (по умолчанию Правая)
)

// Сущность (таблица) состояния биллинга, баланса сеансов и наличия премиум-подписок
@Entity(tableName = "app_billing_state")
data class BillingStateEntity(
    @PrimaryKey val id: Int = 1, // Идентификатор записи состояния биллинга (всегда 1)
    val remainingAnalyses: Int = 3, // Количество доступных бесплатных/оплаченных сеансов ИИ-анализа ладони
    val isPremiumSubscribed: Boolean = false, // Активен ли режим полной безлимитной премиум-подписки
    val purchasedItemIds: String = "" // Идентификаторы приобретенных пакетов товаров через запятую (например, "3_analyses,10_analyses")
)

// Сущность (таблица) единой истории совершенных транзакций, платежей, донатов и начислений в приложении
@Entity(tableName = "payment_history")
data class PaymentHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Автогенерируемый ключ записи платежа
    val timestamp: Long = System.currentTimeMillis(), // Системное время совершения операции/транзакции
    val amountRub: Int, // Сумма платежа в рублях (0 для бесплатных бонусов)
    val paymentSystem: String, // Название платежной системы (например, "ЮKassa (СБП)", "Google Billing")
    val status: String, // Результат транзакции (например, "Успешно", "В процессе", "Ошибка")
    val readingType: String, // Описание назначения платежа (например, "Разблокировка: Анализ ладони", "Донат")
    val userName: String = "", // Имя пользователя на момент платежа
    val userAge: Int = 18, // Возраст пользователя на момент платежа
    val leftPalmPath: String? = null, // Путь к фото левой ладони при проведении оплаты
    val leftBackPath: String? = null, // Путь к фото левой тыльной стороны при проведении оплаты
    val rightPalmPath: String? = null, // Путь к фото правой ладони при проведении оплаты
    val rightBackPath: String? = null, // Путь к фото правой тыльной стороны при проведении оплаты
    val grantedAnalyses: Int = 0, // Количество начисленных анализов за эту транзакцию
    val remainingAnalysesAfterPayment: Int = 0 // Баланс анализов пользователя сразу после совершения этого платежа
)

