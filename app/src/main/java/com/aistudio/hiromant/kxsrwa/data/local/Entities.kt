package com.aistudio.hiromant.kxsrwa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "readings")
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val name: String,
    val gender: String,
    val age: Int,
    val height: Int,
    val dominantHand: String,
    val analysisType: String, // "brief_char", "full_char", "brief_path", "full_path", "compatibility"
    val resultJson: String,
    val imageUrl: String? = null,
    val partnerName: String? = null,
    val partnerImageUrl: String? = null,
    val leftPalmPath: String? = null,
    val leftBackPath: String? = null,
    val rightPalmPath: String? = null,
    val rightBackPath: String? = null,
    val videoPath: String? = null
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "current_user",
    val email: String? = null,
    val phone: String? = null,
    val isRegistered: Boolean = false,
    val name: String = "",
    val gender: String = "",
    val age: Int = 18,
    val height: Int = 170,
    val dominantHand: String = "Right"
)

@Entity(tableName = "app_billing_state")
data class BillingStateEntity(
    @PrimaryKey val id: Int = 1,
    val remainingAnalyses: Int = 3, // Start with some free credits or tracks
    val isPremiumSubscribed: Boolean = false,
    val purchasedItemIds: String = "" // comma separated IDs
)
