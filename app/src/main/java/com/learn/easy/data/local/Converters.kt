package com.learn.easy.data.local

import androidx.room.TypeConverter
import com.learn.easy.domain.model.LineResult
import com.learn.easy.domain.model.WordResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLineResultList(value: List<LineResult>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLineResultList(value: String): List<LineResult> {
        val listType = object : TypeToken<List<LineResult>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}
