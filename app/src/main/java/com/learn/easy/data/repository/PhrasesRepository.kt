package com.learn.easy.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.learn.easy.data.Phrase
import com.learn.easy.data.TopicCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhrasesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val categories: List<TopicCategory> by lazy {
        loadPhrasesFromJson()
    }

    private fun loadPhrasesFromJson(): List<TopicCategory> {
        return try {
            // 1. Abrir el archivo desde assets
            // IMPORTANTE: Mueve tu archivo phrases.json a: app/src/main/assets/phrases.json
            val jsonString =
                context.assets.open("phrases.json").bufferedReader().use { it.readText() }

            // 2. Definir el tipo exacto: List<TopicCategory>
            val listType = object : TypeToken<List<TopicCategory>>() {}.type

           val result  : List<TopicCategory> =  Gson().fromJson(jsonString, listType)
            println("result + ${result.size}")
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Devuelve lista vacía si hay error
        }
    }

    // Ejemplo: Obtener todas las frases de un tópico específico
    fun getPhrasesByTopic(topicName: String): List<Phrase> {
        return categories.find { it.topic == topicName }?.list ?: emptyList()
    }

    // Ejemplo: Obtener una frase aleatoria de cualquier categoría
    fun getRandomPhrase(): Phrase? {
        return categories.flatMap { it.list }.randomOrNull()
    }

    // Get all phrases across all categories
    fun getAllPhrases(): List<Phrase> {
        return categories.flatMap { it.list }
    }

    // Ejemplo: Obtener la lista de todos los tópicos disponibles
    fun getTopics(): List<String> {
        return categories.map { it.topic }
    }




}


