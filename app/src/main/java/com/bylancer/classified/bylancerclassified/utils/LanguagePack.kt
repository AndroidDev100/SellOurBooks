package com.bylancer.classified.bylancerclassified.utils

import android.content.Context
import com.bylancer.classified.bylancerclassified.webservices.languagepack.LanguagePackModel
import com.google.gson.Gson
import org.json.JSONArray
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.Reader
import java.io.StringReader


class LanguagePack private constructor() {
    var languagePackData:List<LanguagePackModel>? = null
    var currentLanguagePackData:LanguagePackModel? = null

    companion object {
        var languagePack:LanguagePack? = null

        val instance: LanguagePack
        get() {
            if(languagePack == null) {
                languagePack = LanguagePack()
            }
            return languagePack!!
        }

        fun getString(key: String?): String {
            if (this.instance.languagePackData != null && key != null) {
                if (this.instance.currentLanguagePackData == null) {
                    for (languagePack in this.instance.languagePackData!!) {
                        if (SessionState.instance.selectedLanguage.equals(languagePack.language, true)) {
                            this.instance.currentLanguagePackData = languagePack
                            break
                        }
                    }
                }

                if (instance.currentLanguagePackData != null
                        && SessionState.instance.selectedLanguage.equals(this.instance.currentLanguagePackData?.language)
                        && this.instance.currentLanguagePackData?.text!!.containsKey(key)) {
                    return  this.instance.currentLanguagePackData!!.text!![key] ?: ""
                } else if(this.instance.currentLanguagePackData == null) {
                    return updateLanguagePack(key, languagePack)
                } else if (!SessionState.instance.selectedLanguage.equals(this.instance.currentLanguagePackData?.language)) {
                    return updateLanguagePack(key, languagePack)
                }
            }

            return if (key.isNullOrEmpty()) "" else key
        }

        private fun updateLanguagePack(key: String, languagePack: LanguagePack?): String {
            for (languagePack in this.instance.languagePackData!!) {
                if (SessionState.instance.selectedLanguage.equals(languagePack.language)) {
                    this.instance.currentLanguagePackData = languagePack
                    if (languagePack.text!!.containsKey(key)) {
                        return languagePack.text!![key] ?: ""
                    }
                }
            }

            return key
        }
    }

    fun saveLanguageData(context: Context, languagePackData:String) {
        SessionState.instance.saveValuesToPreferences(context, AppConstants.Companion.PREFERENCES.LANGUAGE_PACK.toString(),
                languagePackData)
    }

    fun setLanguageData(languagePackDataStr: String) {
        if (languagePackDataStr != null && languagePackDataStr.trim().isNotEmpty()) {
            val reader = JsonReader(StringReader(languagePackDataStr) as Reader?)
            reader.isLenient = true
            languagePackData = Gson().fromJson(reader, object : TypeToken<List<LanguagePackModel>>() {}.type)
        }
    }
}