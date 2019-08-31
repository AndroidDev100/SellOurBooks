package com.bylancer.classified.bylancerclassified.database

import androidx.room.Room
import android.content.Context
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.SessionState

class DBUtil {
    companion object {
        var mPropertyDatabase: FavPropertyDatabase? = null

        fun getDatabaseInstance(context: Context): FavPropertyDatabase {
            if (mPropertyDatabase == null) {
                mPropertyDatabase = Room.databaseBuilder(context,
                        FavPropertyDatabase::class.java, getDataBaseName(context))
                        .fallbackToDestructiveMigration()
                        .build()
            }

            return mPropertyDatabase!!
        }

        private fun getDataBaseName(context: Context): String {
            val dbPrefs = context.getSharedPreferences(AppConstants.PREF_FILE, Context.MODE_PRIVATE)
            var databaseName = dbPrefs.getString(AppConstants.Companion.PREFERENCES.DATABASE_DETAIL.toString(), "")
            if (databaseName.isNullOrEmpty() && !SessionState.instance.appName.isNullOrEmpty()) {
                databaseName = SessionState.instance.appName + AppConstants.DATABASE_NAME
            } else {
                databaseName = AppConstants.DATABASE_NAME
            }
            SessionState.instance.saveValuesToPreferences(context,
                    AppConstants.Companion.PREFERENCES.DATABASE_DETAIL.toString(), databaseName)

            return databaseName
        }
    }

}