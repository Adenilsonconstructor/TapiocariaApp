package com.adenilsonsilva.tapiocariaapp.viewmodel

import androidx.lifecycle.*
import com.adenilsonsilva.tapiocariaapp.model.Saida
import com.adenilsonsilva.tapiocariaapp.model.Venda
import com.adenilsonsilva.tapiocariaapp.repository.TapiocaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// (MUDANÇA) O ViewModel agora só conhece o Repository.
class VendasViewModel(private val repository: TapiocaRepository) : ViewModel() {

    // Ele simplesmente pede os dados ao repositório.
    val todasAsVendas = repository.todasAsVendas.asLiveData()
    val todasAsSaidas = repository.todasAsSaidas.asLiveData()

    suspend fun getTodasAsVendasParaBackup(): List<Venda> {
        return repository.getTodasAsVendasParaBackup()
    }

    suspend fun getTodasAsSaidasParaBackup(): List<Saida> {
        return repository.getTodasAsSaidasParaBackup()
    }

    fun getVendasDoDia(startOfDay: Long, endOfDay: Long): Flow<List<Venda>> {
        return repository.getVendasDoDia(startOfDay, endOfDay)
    }

    fun getSaidasDoDia(startOfDay: Long, endOfDay: Long): Flow<List<Saida>> {
        return repository.getSaidasDoDia(startOfDay, endOfDay)
    }

    fun inserirVenda(venda: Venda) = viewModelScope.launch {
        repository.inserirVenda(venda)
    }

    fun deletarVenda(venda: Venda) = viewModelScope.launch {
        repository.deletarVenda(venda)
    }

    fun inserirSaida(saida: Saida) = viewModelScope.launch {
        repository.inserirSaida(saida)
    }
}

// (MUDANÇA) A Factory agora recebe o Repository para o poder passar ao ViewModel.
class VendasViewModelFactory(private val repository: TapiocaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VendasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VendasViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}





