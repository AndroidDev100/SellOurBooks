package com.bylancer.classified.bylancerclassified.database

import androidx.room.RoomDatabase
import androidx.room.Database
import com.bylancer.classified.bylancerclassified.dashboard.DashboardDetailModel
import com.bylancer.classified.bylancerclassified.utils.AppConstants


@Database(entities = [DashboardDetailModel::class], version = AppConstants.DATABASE_VERSION, exportSchema = false)
abstract class FavPropertyDatabase : RoomDatabase() {
    abstract fun daoAccess(): DaoAccess
}