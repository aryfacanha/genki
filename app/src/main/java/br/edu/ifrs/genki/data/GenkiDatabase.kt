package br.edu.ifrs.genki.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MedicationEntity::class, ScheduleTimeEntity::class], version = DB_VERSION, exportSchema = true)
@TypeConverters(Converters::class)
abstract class GenkiDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao

    companion object {
        fun create(context: Context): GenkiDatabase =
            Room.databaseBuilder(context, GenkiDatabase::class.java, "genki.db")
                // Atualizar o APK preserva os dados: sem apagamento automático; cada versão nova exige migração (`Migrations.kt`).
                .addMigrations(*MIGRATIONS)
                .build()
    }
}
