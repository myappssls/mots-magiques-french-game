package com.manus.motsmagiques

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WordRepository
    val allWords: LiveData<List<WordEntity>>

    private val _xp = MutableLiveData(0)
    val xp: LiveData<Int> = _xp

    private val _level = MutableLiveData(1)
    val level: LiveData<Int> = _level

    private val _streak = MutableLiveData(0)
    val streak: LiveData<Int> = _streak

    init {
        val wordDao = AppDatabase.getDatabase(application).wordDao()
        val apiService = RetrofitClient.instance
        repository = WordRepository(wordDao, apiService)
        allWords = repository.allWords
        
        loadInitialDataIfNeeded()
    }

    private fun loadInitialDataIfNeeded() {
        viewModelScope.launch {
            if (repository.getCount() == 0) {
                val jsonString = getApplication<Application>().assets.open("vocabulary.json")
                    .bufferedReader().use { it.readText() }
                val listType = object : TypeToken<List<WordEntity>>() {}.type
                val words: List<WordEntity> = Gson().fromJson(jsonString, listType)
                repository.insertAll(words)
            }
        }
    }

    fun onCorrectAnswer(word: WordEntity) {
        viewModelScope.launch {
            word.masteryLevel = (word.masteryLevel + 1).coerceAtMost(5)
            word.lastReviewed = System.currentTimeMillis()
            val intervalDays = when (word.masteryLevel) {
                1 -> 1
                2 -> 3
                3 -> 7
                4 -> 15
                5 -> 30
                else -> 0
            }
            word.nextReview = System.currentTimeMillis() + (intervalDays * 24 * 60 * 60 * 1000L)
            repository.updateWord(word)

            _xp.value = (_xp.value ?: 0) + 10
            checkLevelUp()
        }
    }

    fun onIncorrectAnswer(word: WordEntity) {
        viewModelScope.launch {
            word.masteryLevel = (word.masteryLevel - 1).coerceAtLeast(0)
            word.incorrectAttempts++
            word.nextReview = System.currentTimeMillis()
            repository.updateWord(word)
        }
    }

    private fun checkLevelUp() {
        val currentXp = _xp.value ?: 0
        val currentLevel = _level.value ?: 1
        val nextLevelThreshold = currentLevel * 100
        if (currentXp >= nextLevelThreshold) {
            _level.value = currentLevel + 1
        }
    }

    // New function to add a word dynamically from the internet
    fun addNewWordFromInternet(frenchWord: String) {
        viewModelScope.launch {
            val arabicTranslation = repository.fetchTranslationOnline(frenchWord)
            if (arabicTranslation != null) {
                val newWord = WordEntity(
                    frenchWord = frenchWord,
                    arabicTranslation = arabicTranslation,
                    category = "Internet",
                    level = 3
                )
                repository.insertAll(listOf(newWord))
            }
        }
    }
}
