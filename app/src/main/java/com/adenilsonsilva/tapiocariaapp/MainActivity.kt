package com.adenilsonsilva.tapiocariaapp

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adenilsonsilva.tapiocariaapp.databinding.ActivityMainBinding
import com.adenilsonsilva.tapiocariaapp.model.Saida
import com.adenilsonsilva.tapiocariaapp.model.Venda
import com.adenilsonsilva.tapiocariaapp.view.NovaSaidaActivity
import com.adenilsonsilva.tapiocariaapp.view.NovaVendaActivity
import com.adenilsonsilva.tapiocariaapp.view.RelatoriosActivity
import com.adenilsonsilva.tapiocariaapp.viewmodel.VendasViewModel
import com.adenilsonsilva.tapiocariaapp.viewmodel.VendasViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var vendasAdapter: VendasAdapter

    private val vendasViewModel: VendasViewModel by viewModels {
        // Agora a Factory recebe o repository da nossa classe Application.
        VendasViewModelFactory((application as TapiocariaApplication).repository)
    }

    private val appLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data

            val vendaSalva = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableExtra("NOVA_VENDA", Venda::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableExtra<Venda>("NOVA_VENDA")
            }
            vendaSalva?.let { vendasViewModel.inserirVenda(it) }

            val saidaSalva = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableExtra("NOVA_SAIDA", Saida::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableExtra<Saida>("NOVA_SAIDA")
            }
            saidaSalva?.let {
                vendasViewModel.inserirSaida(it)
                Toast.makeText(this, "Saída registrada com sucesso!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var isFabMenuOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        setupFab()
        setupSwipeToDelete()

        vendasViewModel.todasAsVendas.observe(this) { vendas ->
            vendas?.let {
                vendasAdapter.submitList(it)
                updateSummary(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_relatorios -> {
                val intent = Intent(this, RelatoriosActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_backup -> {
                fazerBackup()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Função de backup robusta que envia Vendas e Saídas para o Firestore.
     */
    private fun fazerBackup() {
        Toast.makeText(this, "Iniciando backup na nuvem...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val vendas = vendasViewModel.getTodasAsVendasParaBackup()
                val saidas = vendasViewModel.getTodasAsSaidasParaBackup()

                if (vendas.isEmpty() && saidas.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Não há dados para fazer backup.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val db = Firebase.firestore
                val batch = db.batch()
                var operacoes = 0

                vendas.forEach { venda ->
                    val docRef = db.collection("vendas").document(venda.id)
                    batch.set(docRef, venda)
                    operacoes++
                }

                saidas.forEach { saida ->
                    val docRef = db.collection("saidas").document(saida.id)
                    batch.set(docRef, saida)
                    operacoes++
                }

                batch.commit().await()

                Toast.makeText(this@MainActivity, "Backup de $operacoes registros realizado com sucesso!", Toast.LENGTH_LONG).show()

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erro no backup: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("BackupFirebase", "Erro ao fazer o backup", e)
            }
        }
    }

    // --- Funções existentes da MainActivity ---
    private fun setupFab() {
        binding.fabPrincipal.setOnClickListener {
            if (isFabMenuOpen) {
                closeFabMenu()
            } else {
                openFabMenu()
            }
        }
        binding.fabNovaVenda.setOnClickListener {
            val intent = Intent(this, NovaVendaActivity::class.java)
            appLauncher.launch(intent)
            closeFabMenu()
        }
        binding.fabNovaSaida.setOnClickListener {
            val intent = Intent(this, NovaSaidaActivity::class.java)
            appLauncher.launch(intent)
            closeFabMenu()
        }
    }

    private fun openFabMenu() {
        isFabMenuOpen = true
        binding.fabPrincipal.setImageResource(R.drawable.ic_close)
        binding.fabNovaVenda.show()
        binding.fabNovaSaida.show()
        binding.fabNovaSaida.animate().translationY(-resources.getDimension(R.dimen.fab_margin_1))
        binding.fabNovaVenda.animate().translationY(-resources.getDimension(R.dimen.fab_margin_2))
    }

    private fun closeFabMenu() {
        isFabMenuOpen = false
        binding.fabPrincipal.setImageResource(R.drawable.ic_menu)
        binding.fabNovaVenda.animate().translationY(0f)
        binding.fabNovaSaida.animate().translationY(0f)
        binding.fabNovaVenda.hide()
        binding.fabNovaSaida.hide()
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val vendaParaApagar = vendasAdapter.currentList[position]
                vendasViewModel.deletarVenda(vendaParaApagar)
                Snackbar.make(binding.root, "Venda apagada", Snackbar.LENGTH_LONG).apply {
                    setAction("DESFAZER") {
                        vendasViewModel.inserirVenda(vendaParaApagar)
                    }
                    show()
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerViewVendas)
    }

    private fun setupRecyclerView() {
        vendasAdapter = VendasAdapter { venda ->
            val intent = Intent(this, NovaVendaActivity::class.java)
            intent.putExtra("VENDA_PARA_EDITAR", venda)
            appLauncher.launch(intent)
        }
        binding.recyclerViewVendas.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewVendas.adapter = vendasAdapter
    }

    private fun updateSummary(listaDeVendas: List<Venda>) {
        val totalVendas = listaDeVendas.sumOf { it.preco }
        val quantidadeVendas = listaDeVendas.size
        binding.totalVendasTextView.text = String.format("R$ %.2f", totalVendas)
        binding.quantidadeVendasTextView.text = "$quantidadeVendas Vendas"
    }
}

