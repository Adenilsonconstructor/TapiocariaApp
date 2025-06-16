package com.adenilsonsilva.tapiocariaapp.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Data Class que representa uma saída de caixa (despesa, troco, etc.).
 */
@Parcelize
@Entity(tableName = "tabela_de_saidas")
data class Saida(
    @PrimaryKey
    val id: String,
    val descricao: String,
    val valor: Double,
    val data: Long
) : Parcelable
