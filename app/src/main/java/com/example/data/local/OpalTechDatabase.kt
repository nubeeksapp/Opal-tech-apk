package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString("\u001F") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split("\u001F")
    }
}

@Database(entities = [CachedArticleEntity::class, ArticleViewCountEntity::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class OpalTechDatabase : RoomDatabase() {
    abstract fun cachedArticleDao(): CachedArticleDao
    abstract fun articleViewCountDao(): ArticleViewCountDao

    companion object {
        @Volatile
        private var INSTANCE: OpalTechDatabase? = null

        fun getInstance(context: Context): OpalTechDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OpalTechDatabase::class.java,
                    "opal_tech_room.db"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
