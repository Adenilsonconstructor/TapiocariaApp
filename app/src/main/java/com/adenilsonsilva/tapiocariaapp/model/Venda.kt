package com.adenilsonsilva.tapiocariaapp.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tabela_de_vendas")
data class Venda(
    @PrimaryKey
    val id: String,
    val sabor: String,
    val preco: Double,
    val data: Long,
    val formaDePagamento: String // Verifique se esta linha está exatamente assim
) : Parcelable
