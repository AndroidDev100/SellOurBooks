package com.bylancer.classified.bylancerclassified.database

import androidx.room.TypeConverter
import com.bylancer.classified.bylancerclassified.dashboard.CustomData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class DataConverterCustomData {

    @TypeConverter
    fun fromCustomDataList(customData: List<CustomData>?): String? {
        if (customData == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<CustomData>>() {

        }.type
        return gson.toJson(customData, type)
    }

    @TypeConverter
    fun toCustomDataList(customDataString: String?): List<CustomData>? {
        if (customDataString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<CustomData>>() {

        }.type
        return gson.fromJson(customDataString, type)
    }
}