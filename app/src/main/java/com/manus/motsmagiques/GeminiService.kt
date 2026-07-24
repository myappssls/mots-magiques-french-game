package com.manus.motsmagiques

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

class GeminiService {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun getExampleSentence(word: String): String? {
        return try {
            val prompt = "Provide a simple French example sentence for the word '$word' with its Arabic translation. Format: Sentence - Translation."
            val response = generativeModel.generateContent(prompt)
            response.text
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getGrammarExplanation(word: String): String? {
        return try {
            val prompt = "Explain the grammar or usage of the French word '$word' in Arabic briefly."
            val response = generativeModel.generateContent(prompt)
            response.text
        } catch (e: Exception) {
            null
        }
    }
}
