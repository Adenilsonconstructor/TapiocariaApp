package com.adenilsonsilva.tapiocariaapp.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adenilsonsilva.tapiocariaapp.databinding.ActivityNovaSaidaBinding
import com.adenilsonsilva.tapiocariaapp.model.Saida
import java.util.UUID

class NovaSaidaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNovaSaidaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNovaSaidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Nova Saída de Caixa"

        binding.salvarSaidaButton.setOnClickListener {
            salvarSaida()
        }
    }

    private fun salvarSaida() {
        val descricao = binding.descricaoEditText.text.toString().trim()
        val valorStr = binding.valorEditText.text.toString().trim()

        if (descricao.isEmpty() || valorStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val valor = valorStr.toDoubleOrNull()
        if (valor == null || valor <= 0) {
            Toast.makeText(this, "Insira um valor válido", Toast.LENGTH_SHORT).show()
            return
        }

        val novaSaida = Saida(
            id = UUID.randomUUID().toString(),
            descricao = descricao,
            valor = valor,
            data = System.currentTimeMillis()
        )

        // Devolvemos a nova saída para a MainActivity, que vai guardá-la.
        val resultadoIntent = Intent()
        resultadoIntent.putExtra("NOVA_SAIDA", novaSaida)
        setResult(Activity.RESULT_OK, resultadoIntent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
