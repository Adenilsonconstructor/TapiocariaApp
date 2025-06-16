package com.adenilsonsilva.tapiocariaapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.adenilsonsilva.tapiocariaapp.model.Venda
import kotlinx.coroutines.flow.Flow

@Dao
interface VendaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirVenda(venda: Venda)

    @Delete
    suspend fun deletarVenda(venda: Venda)

    @Query("SELECT * FROM tabela_de_vendas ORDER BY data DESC")
    fun getTodasAsVendas(): Flow<List<Venda>>

    /**
     * (NOVO) Seleciona todas as vendas dentro de um intervalo de tempo (um dia).
     * @param startOfDay O timestamp do início do dia (00:00:00).
     * @param endOfDay O timestamp do fim do dia (23:59:59).
     */
    @Query("SELECT * FROM tabela_de_vendas WHERE data BETWEEN :startOfDay AND :endOfDay ORDER BY data DESC")
    fun getVendasDoDia(startOfDay: Long, endOfDay: Long): Flow<List<Venda>>

    @Query("SELECT * FROM tabela_de_vendas")
    suspend fun getTodasAsVendasParaBackup(): List<Venda>

}