package com.manus.motsmagiques

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val frenchWord: String,
    val arabicTranslation: String,
    val category: String,
    val level: Int,
    var masteryLevel: Int = 0,
    var lastReviewed: Long = 0,
    var nextReview: Long = 0,
    var incorrectAttempts: Int = 0
)
