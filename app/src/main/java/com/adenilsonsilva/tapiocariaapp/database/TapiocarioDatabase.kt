package com.adenilsonsilva.tapiocariaapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.adenilsonsilva.tapiocariaapp.model.Saida
import com.adenilsonsilva.tapiocariaapp.model.Venda

// (MUDANÇA) A versão aumentou para 3 e adicionámos a Saida::class às entidades.
@Database(entities = [Venda::class, Saida::class], version = 3, exportSchema = false)
abstract class TapiocariaDatabase : RoomDatabase() {

    abstract fun vendaDao(): VendaDao
    abstract fun saidaDao(): SaidaDao // (NOVO) O banco de dados agora conhece o SaidaDao.

    companion object {
        @Volatile
        private var INSTANCE: TapiocariaDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tabela_de_vendas ADD COLUMN formaDePagamento TEXT NOT NULL DEFAULT 'Dinheiro'")
            }
        }

        // (NOVO) Migração da versão 2 para a 3 para criar a nova tabela.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `tabela_de_saidas` " +
                            "(`id` TEXT NOT NULL, `descricao` TEXT NOT NULL, `valor` REAL NOT NULL, `data` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        fun getDatabase(context: Context): TapiocariaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TapiocariaDatabase::class.java,
                    "tapiocaria_database"
                )
                    // (MUDANÇA) Adicionamos a nova migração.
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
