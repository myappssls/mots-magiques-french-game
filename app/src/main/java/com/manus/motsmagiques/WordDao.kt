package com.manus.motsmagiques

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY level ASC")
    fun getAllWords(): LiveData<List<WordEntity>>

    @Query("SELECT * FROM words WHERE nextReview <= :currentTime OR nextReview = 0")
    fun getWordsForReview(currentTime: Long): LiveData<List<WordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)

    @Update
    suspend fun updateWord(word: WordEntity)

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getCount(): Int
}
