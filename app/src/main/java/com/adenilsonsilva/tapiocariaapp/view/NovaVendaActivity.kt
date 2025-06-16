package com.adenilsonsilva.tapiocariaapp.view

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adenilsonsilva.tapiocariaapp.R
import com.adenilsonsilva.tapiocariaapp.databinding.ActivityNovaVendaBinding
import com.adenilsonsilva.tapiocariaapp.model.Venda
import java.util.UUID

class NovaVendaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNovaVendaBinding
    private var vendaExistente: Venda? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNovaVendaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vendaExistente = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("VENDA_PARA_EDITAR", Venda::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("VENDA_PARA_EDITAR")
        }

        if (vendaExistente != null) {
            binding.tituloTextView.text = "Editar Venda"
            binding.saborEditText.setText(vendaExistente?.sabor)
            binding.precoEditText.setText(vendaExistente?.preco.toString())
            when (vendaExistente?.formaDePagamento) {
                "Cartão" -> binding.pagamentoRadioGroup.check(R.id.cartaoRadioButton)
                "Pix" -> binding.pagamentoRadioGroup.check(R.id.pixRadioButton)
                else -> binding.pagamentoRadioGroup.check(R.id.dinheiroRadioButton)
            }
        } else {
            binding.tituloTextView.text = "Registrar Nova Venda"
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = binding.tituloTextView.text

        binding.salvarVendaButton.setOnClickListener {
            salvarVenda()
        }
    }

    private fun salvarVenda() {
        val sabor = binding.saborEditText.text.toString().trim()
        val precoStr = binding.precoEditText.text.toString().trim()

        // (AQUI ESTÁ A CORREÇÃO FINAL) O operador "OU" agora é || junto.
        if (sabor.isEmpty() || precoStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val preco = precoStr.toDoubleOrNull() ?: 0.0

        val idPagamentoSelecionado = binding.pagamentoRadioGroup.checkedRadioButtonId
        val formaDePagamento = when (idPagamentoSelecionado) {
            R.id.cartaoRadioButton -> "Cartão"
            R.id.pixRadioButton -> "Pix"
            else -> "Dinheiro"
        }

        val vendaParaSalvar = Venda(
            id = vendaExistente?.id ?: UUID.randomUUID().toString(),
            sabor = sabor,
            preco = preco,
            data = vendaExistente?.data ?: System.currentTimeMillis(),
            formaDePagamento = formaDePagamento
        )

        val resultadoIntent = Intent()
        resultadoIntent.putExtra("NOVA_VENDA", vendaParaSalvar)
        setResult(Activity.RESULT_OK, resultadoIntent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}