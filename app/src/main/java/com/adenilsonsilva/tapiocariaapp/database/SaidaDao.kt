package com.adenilsonsilva.tapiocariaapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.adenilsonsilva.tapiocariaapp.model.Saida
import kotlinx.coroutines.flow.Flow

@Dao
interface SaidaDao {

    /**
     * Insere uma nova saída na tabela. Se uma saída com o mesmo id já existir,
     * ela será substituída.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirSaida(saida: Saida)

    /**
     * Seleciona todas as saídas de um dia específico, para o ecrã de relatórios.
     */
    @Query("SELECT * FROM tabela_de_saidas WHERE data BETWEEN :startOfDay AND :endOfDay ORDER BY data DESC")
    fun getSaidasDoDia(startOfDay: Long, endOfDay: Long): Flow<List<Saida>>

    /**
     * Seleciona todas as saídas da tabela.
     * Isto é usado para a função de backup.
     */
    @Query("SELECT * FROM tabela_de_saidas")
    fun getTodasAsSaidas(): Flow<List<Saida>>

    @Query("SELECT * FROM tabela_de_saidas")
    suspend fun getTodasAsSaidasParaBackup(): List<Saida>

}