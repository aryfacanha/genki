package br.edu.ifrs.genki.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MedicationEntity::class, ScheduleTimeEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GenkiDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao

    companion object {
        fun create(context: Context): GenkiDatabase =
            Room.databaseBuilder(context, GenkiDatabase::class.java, "genki.db")
                // Sem migrações até o congelamento (ESPEC §3).
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
