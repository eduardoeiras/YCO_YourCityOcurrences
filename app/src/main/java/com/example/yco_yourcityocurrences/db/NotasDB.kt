package com.example.yco_yourcityocurrences.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.yco_yourcityocurrences.dao.NotaDao
import com.example.yco_yourcityocurrences.entities.Nota
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = arrayOf(Nota::class), version = 1, exportSchema = false)
abstract class NotasDB : RoomDatabase() {

    abstract fun notaDao(): NotaDao

    private class NotasDatabaseCallback(
        private val scope: CoroutineScope
    ): RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    database.notaDao()
                }

            }
        }
    }

    companion object {

        @Volatile
        private var INSTANCE: NotasDB? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NotasDB {
            val tmpInstance = INSTANCE
            if(tmpInstance != null) {
                return tmpInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext,
                NotasDB::class.java, "notas_database")
                    .addCallback(NotasDatabaseCallback(scope))
                    .build()

                INSTANCE = instance

                return instance
            }
        }
    }
}