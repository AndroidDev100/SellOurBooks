package com.bylancer.classified.bylancerclassified.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class DataConverterListImages {

    @TypeConverter
    fun fromStringList(customData: List<String>?): String? {
        if (customData == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {

        }.type
        return gson.toJson(customData, type)
    }

    @TypeConverter
    fun toStringList(customDataString: String?): List<String>? {
        if (customDataString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {

        }.type
        return gson.fromJson(customDataString, type)
    }
}