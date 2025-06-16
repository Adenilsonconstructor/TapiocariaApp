package com.adenilsonsilva.tapiocariaapp.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.adenilsonsilva.tapiocariaapp.R
import com.adenilsonsilva.tapiocariaapp.TapiocariaApplication
import com.adenilsonsilva.tapiocariaapp.VendasAdapter
import com.adenilsonsilva.tapiocariaapp.databinding.ActivityRelatoriosBinding
import com.adenilsonsilva.tapiocariaapp.model.Saida
import com.adenilsonsilva.tapiocariaapp.model.Venda
import com.adenilsonsilva.tapiocariaapp.viewmodel.VendasViewModel
import com.adenilsonsilva.tapiocariaapp.viewmodel.VendasViewModelFactory
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class RelatoriosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRelatoriosBinding
    private lateinit var vendasAdapter: VendasAdapter
    private var vendasDoDiaAtual: List<Venda> = emptyList()
    private var saidasDoDiaAtual: List<Saida> = emptyList()
    private var dataSelecionada: Date = Date()

    private val vendasViewModel: VendasViewModel by viewModels {
        VendasViewModelFactory((application as TapiocariaApplication).repository)
    }

    private val criarDocumentoPdf =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
            uri?.let {
                try {
                    gerarPdf(it)
                    Toast.makeText(this, "Relatório PDF salvo com sucesso!", Toast.LENGTH_LONG).show()
                } catch (e: IOException) {
                    Toast.makeText(this, "Erro ao salvar o relatório PDF.", Toast.LENGTH_LONG).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRelatoriosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarRelatorios)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        setupGrafico()

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            dataSelecionada = calendar.time
            procurarDadosParaData(calendar)
        }

        binding.exportarButton.setOnClickListener {
            exportarRelatorio()
        }

        procurarDadosParaData(Calendar.getInstance())
    }

    private fun procurarDadosParaData(calendar: Calendar) {
        updateDateLabel(calendar.time)
        val startOfDay = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
        val endOfDay = calendar.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis

        vendasViewModel.getVendasDoDia(startOfDay, endOfDay).asLiveData().observe(this) { vendas ->
            this.vendasDoDiaAtual = vendas ?: emptyList()
            atualizarTodaUI()
        }

        vendasViewModel.getSaidasDoDia(startOfDay, endOfDay).asLiveData().observe(this) { saidas ->
            this.saidasDoDiaAtual = saidas ?: emptyList()
            atualizarTodaUI()
        }
    }

    private fun atualizarTodaUI() {
        val hasData = vendasDoDiaAtual.isNotEmpty() || saidasDoDiaAtual.isNotEmpty()
        binding.cardFechamentoCaixa.isGone = !hasData
        binding.exportarButton.isEnabled = hasData

        vendasAdapter.submitList(vendasDoDiaAtual)
        updateSummary(vendasDoDiaAtual, saidasDoDiaAtual)
        atualizarDadosGrafico(vendasDoDiaAtual)
    }

    private fun updateSummary(listaDeVendas: List<Venda>, listaDeSaidas: List<Saida>) {
        val totalVendas = listaDeVendas.sumOf { it.preco }
        binding.totalVendasDiaTextView.text = String.format("R$ %.2f", totalVendas)
        binding.quantidadeVendasDiaTextView.text = "${listaDeVendas.size} Vendas"

        val totalDinheiro = listaDeVendas.filter { it.formaDePagamento == "Dinheiro" }.sumOf { it.preco }
        val totalCartao = listaDeVendas.filter { it.formaDePagamento == "Cartão" }.sumOf { it.preco }
        val totalPix = listaDeVendas.filter { it.formaDePagamento == "Pix" }.sumOf { it.preco }
        binding.totalDinheiroTextView.text = String.format("R$ %.2f", totalDinheiro)
        binding.totalCartaoTextView.text = String.format("R$ %.2f", totalCartao)
        binding.totalPixTextView.text = String.format("R$ %.2f", totalPix)

        val totalSaidas = listaDeSaidas.sumOf { it.valor }
        binding.totalSaidasTextView.text = String.format(Locale("pt", "BR"), "- R$ %.2f", totalSaidas)

        val caixaLiquido = totalVendas - totalSaidas
        binding.caixaLiquidoTextView.text = String.format(Locale("pt", "BR"), "R$ %.2f", caixaLiquido)
    }

    private fun gerarPdf(uri: android.net.Uri) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textSize = 18f; color = Color.BLACK }
        val bodyPaint = Paint().apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); textSize = 12f; color = Color.DKGRAY }

        var yPosition = 40f

        val dateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
        canvas.drawText("Relatório de Vendas - ${dateFormat.format(dataSelecionada)}", 40f, yPosition, titlePaint)
        yPosition += 40

        val yStartOfSection = yPosition
        val summaryBlockHeight = desenharBlocoResumo(canvas, yStartOfSection, bodyPaint, saidasDoDiaAtual)
        var chartBlockHeight = 0f
        val chartBitmap = binding.pieChart.chartBitmap
        if (chartBitmap != null && !binding.pieChart.isGone) {
            val scale = (canvas.width / 2.5f) / chartBitmap.width.toFloat()
            val scaledWidth = (chartBitmap.width * scale).toInt()
            val scaledHeight = (chartBitmap.height * scale).toInt()
            chartBlockHeight = scaledHeight.toFloat()
            val resizedBitmap = android.graphics.Bitmap.createScaledBitmap(chartBitmap, scaledWidth, scaledHeight, false)
            canvas.drawBitmap(resizedBitmap, 290f, yStartOfSection, null)
        }

        yPosition = yStartOfSection + max(summaryBlockHeight, chartBlockHeight) + 40f

        if (vendasDoDiaAtual.isNotEmpty()) {
            canvas.drawText("Detalhes das Vendas", 40f, yPosition, titlePaint)
            yPosition += 25
            yPosition = desenharTabelaVendas(canvas, yPosition, bodyPaint)
            yPosition += 25
        }

        if (saidasDoDiaAtual.isNotEmpty()) {
            canvas.drawText("Detalhes das Saídas", 40f, yPosition, titlePaint)
            yPosition += 25
            desenharTabelaSaidas(canvas, yPosition, bodyPaint)
        }

        pdfDocument.finishPage(page)

        val outputStream = contentResolver.openOutputStream(uri)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        outputStream?.close()
    }

    private fun desenharBlocoResumo(canvas: Canvas, yPos: Float, paint: Paint, listaDeSaidas: List<Saida>): Float {
        var y = yPos
        val startY = y
        val totalVendas = vendasDoDiaAtual.sumOf { it.preco }
        canvas.drawText("Total Vendido: ${String.format(Locale("pt", "BR"), "R$ %.2f", totalVendas)} (${vendasDoDiaAtual.size} vendas)", 40f, y, paint)
        y += 20
        vendasDoDiaAtual.groupBy { it.formaDePagamento }.forEach { (forma, vendas) ->
            val total = vendas.sumOf { it.preco }
            canvas.drawText("$forma: ${String.format(Locale("pt", "BR"), "R$ %.2f", total)}", 40f, y, paint)
            y += 15
        }

        y += 5
        val totalSaidas = listaDeSaidas.sumOf { it.valor }
        canvas.drawText("Total Saídas: ${String.format(Locale("pt", "BR"), "- R$ %.2f", totalSaidas)}", 40f, y, paint)
        y += 20

        val caixaLiquido = totalVendas - totalSaidas
        val boldPaint = Paint(paint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        canvas.drawText("CAIXA LÍQUIDO: ${String.format(Locale("pt", "BR"), "R$ %.2f", caixaLiquido)}", 40f, y, boldPaint)
        y += 15

        return y - startY
    }

    private fun desenharTabelaVendas(canvas: Canvas, yPos: Float, paint: Paint): Float {
        var y = yPos
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val headerPaint = Paint(paint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }

        canvas.drawText("Hora", 40f, y, headerPaint)
        canvas.drawText("Sabor", 100f, y, headerPaint)
        canvas.drawText("Pagamento", 350f, y, headerPaint)
        canvas.drawText("Preço", 480f, y, headerPaint)
        y += 20
        canvas.drawLine(40f, y - 15, 555f, y - 15, paint)

        vendasDoDiaAtual.forEach { venda ->
            canvas.drawText(dateFormat.format(Date(venda.data)), 40f, y, paint)
            canvas.drawText(venda.sabor, 100f, y, paint)
            canvas.drawText(venda.formaDePagamento, 350f, y, paint)
            canvas.drawText(String.format(Locale("pt", "BR"), "R$ %.2f", venda.preco), 480f, y, paint)
            y += 15
        }
        return y
    }

    private fun desenharTabelaSaidas(canvas: Canvas, yPos: Float, paint: Paint): Float {
        var y = yPos
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val headerPaint = Paint(paint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }

        canvas.drawText("Hora", 40f, y, headerPaint)
        canvas.drawText("Descrição", 100f, y, headerPaint)
        canvas.drawText("Valor", 480f, y, headerPaint)
        y += 20
        canvas.drawLine(40f, y - 15, 555f, y - 15, paint)

        saidasDoDiaAtual.forEach { saida ->
            canvas.drawText(dateFormat.format(Date(saida.data)), 40f, y, paint)
            canvas.drawText(saida.descricao, 100f, y, paint)
            canvas.drawText(String.format(Locale("pt", "BR"), "- R$ %.2f", saida.valor), 480f, y, paint)
            y += 15
        }
        return y
    }

    private fun setupRecyclerView() {
        vendasAdapter = VendasAdapter { /* Sem ação de clique nos relatórios */ }
        binding.recyclerViewRelatorios.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRelatorios.adapter = vendasAdapter
    }

    private fun setupGrafico() {
        binding.pieChart.apply {
            setUsePercentValues(true); description.isEnabled = false; setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f; isDrawHoleEnabled = true; setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE); setTransparentCircleAlpha(110)
            holeRadius = 58f; transparentCircleRadius = 61f; setDrawCenterText(true)
            centerText = "Vendas por Pagamento"; rotationAngle = 0f; isRotationEnabled = true
            isHighlightPerTapEnabled = true; legend.isEnabled = false; setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
        }
    }

    private fun atualizarDadosGrafico(listaDeVendas: List<Venda>) {
        val totais = listaDeVendas.groupBy { it.formaDePagamento }.mapValues { it.value.sumOf { v -> v.preco } }
        val entries = ArrayList<PieEntry>()
        totais.forEach { (forma, total) -> if (total > 0) { entries.add(PieEntry(total.toFloat(), forma)) } }
        if (entries.isEmpty()) {
            binding.pieChart.clear(); binding.pieChart.centerText = "Sem vendas neste dia"
            binding.pieChart.invalidate(); return
        }
        val dataSet = PieDataSet(entries, "Formas de Pagamento")
        dataSet.sliceSpace = 3f; dataSet.selectionShift = 5f
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(binding.pieChart)); data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK); binding.pieChart.centerText = "Vendas por Pagamento"
        binding.pieChart.data = data; binding.pieChart.invalidate(); binding.pieChart.animateY(1400)
    }

    private fun exportarRelatorio() {
        if (vendasDoDiaAtual.isEmpty() && saidasDoDiaAtual.isEmpty()) {
            Toast.makeText(this, "Não há dados para exportar neste dia.", Toast.LENGTH_SHORT).show()
            return
        }
        val formatoNomeFicheiro = SimpleDateFormat("'relatorio_tapiocaria_'yyyy_MM_dd'.pdf'", Locale.getDefault())
        val nomeDoFicheiro = formatoNomeFicheiro.format(dataSelecionada)
        criarDocumentoPdf.launch(nomeDoFicheiro)
    }

    private fun updateDateLabel(date: Date) {
        val dateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
        binding.dataSelecionadaTextView.text = "Vendas de ${dateFormat.format(date)}"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}



