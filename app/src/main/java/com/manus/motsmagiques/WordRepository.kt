package com.manus.motsmagiques

import androidx.lifecycle.LiveData

class WordRepository(private val wordDao: WordDao, private val apiService: MyMemoryApiService) {
    val allWords: LiveData<List<WordEntity>> = wordDao.getAllWords()

    fun getWordsForReview(currentTime: Long): LiveData<List<WordEntity>> {
        return wordDao.getWordsForReview(currentTime)
    }

    suspend fun insertAll(words: List<WordEntity>) {
        wordDao.insertAll(words)
    }

    suspend fun updateWord(word: WordEntity) {
        wordDao.updateWord(word)
    }

    suspend fun getCount(): Int {
        return wordDao.getCount()
    }

    suspend fun fetchTranslationOnline(frenchWord: String): String? {
        return try {
            val response = apiService.translate(frenchWord)
            response.responseData.translatedText
        } catch (e: Exception) {
            null
        }
    }
}
