package com.adenilsonsilva.tapiocariaapp.repository

import com.adenilsonsilva.tapiocariaapp.database.SaidaDao
import com.adenilsonsilva.tapiocariaapp.database.VendaDao
import com.adenilsonsilva.tapiocariaapp.model.Saida
import com.adenilsonsilva.tapiocariaapp.model.Venda
import kotlinx.coroutines.flow.Flow

/**
 * Repository que gere todas as fontes de dados (Vendas e Saídas).
 * Ele é a única fonte da verdade para o resto da aplicação.
 */
class TapiocaRepository(private val vendaDao: VendaDao, private val saidaDao: SaidaDao) {

    // --- Funções de Venda ---
    val todasAsVendas: Flow<List<Venda>> = vendaDao.getTodasAsVendas()

    fun getVendasDoDia(start: Long, end: Long): Flow<List<Venda>> {
        return vendaDao.getVendasDoDia(start, end)
    }

    suspend fun getTodasAsVendasParaBackup(): List<Venda> {
        return vendaDao.getTodasAsVendasParaBackup()
    }

    suspend fun inserirVenda(venda: Venda) {
        vendaDao.inserirVenda(venda)
    }

    suspend fun deletarVenda(venda: Venda) {
        vendaDao.deletarVenda(venda)
    }

    // --- Funções de Saída ---
    val todasAsSaidas: Flow<List<Saida>> = saidaDao.getTodasAsSaidas()

    fun getSaidasDoDia(start: Long, end: Long): Flow<List<Saida>> {
        return saidaDao.getSaidasDoDia(start, end)
    }

    suspend fun getTodasAsSaidasParaBackup(): List<Saida> {
        return saidaDao.getTodasAsSaidasParaBackup()
    }

    suspend fun inserirSaida(saida: Saida) {
        saidaDao.inserirSaida(saida)
    }
}