package com.example.festival.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FestivalEntity::class], version=1)
abstract class FestivalDatabase :  RoomDatabase() {
    abstract fun festivalDao() : FestivalDao

    companion object {
        @Volatile
        private var INSTANCE : FestivalDatabase? = null

        fun getDatabase(context: Context) : FestivalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, FestivalDatabase::class.java, "my_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}