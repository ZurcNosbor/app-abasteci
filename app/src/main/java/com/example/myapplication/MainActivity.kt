package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.Abastecimento
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.AbastecimentoViewModel
import com.example.myapplication.viewmodel.VeiculoViewModel
import java.util.Calendar
import java.util.TimeZone

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier, viewModel: VeiculoViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "lista_veiculos", modifier = modifier) {
        composable("lista_veiculos") {
            TelaListaVeiculos(viewModel = viewModel, onAddClick = { navController.navigate("cadastro_veiculo") }, onVeiculoClick = { id -> navController.navigate("abastecimentos/$id") })
        }
        composable("cadastro_veiculo") {
            TelaCadastroVeiculo(viewModel = viewModel, onVoltarClick = { navController.popBackStack() })
        }
        composable("abastecimentos/{veiculoId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("veiculoId")?.toLongOrNull() ?: 0L
            TelaAbastecimentos(veiculoId = id, viewModel = viewModel, onVoltarClick = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaListaVeiculos(viewModel: VeiculoViewModel, onAddClick: () -> Unit, onVeiculoClick: (Long) -> Unit) {
    val listaVeiculos by viewModel.listaVeiculos.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Meus Veículos") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listaVeiculos) { veiculo ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onVeiculoClick(veiculo.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = veiculo.nome, style = MaterialTheme.typography.titleLarge)
                        Text(text = "Ano: ${veiculo.ano} | Tipo: ${veiculo.tipo}")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCadastroVeiculo(viewModel: VeiculoViewModel, onVoltarClick: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Moto") }
    var ano by remember { mutableStateOf("") }
    var odometro by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastrar Veículo") },
                navigationIcon = {
                    IconButton(onClick = { onVoltarClick() }) {
                        Text("<", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome do Veículo") }, modifier = Modifier.fillMaxWidth())

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { tipo = "Moto" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tipo == "Moto") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (tipo == "Moto") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Moto")
                }

                Button(
                    onClick = { tipo = "Carro" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tipo == "Carro") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (tipo == "Carro") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Carro")
                }
            }

            OutlinedTextField(value = ano, onValueChange = { if (it.length <= 4) ano = it }, label = { Text("Ano do Veículo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = odometro, onValueChange = { odometro = it }, label = { Text("Odômetro Inicial (KM)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            Button(onClick = {
                if (nome.isNotBlank() && ano.isNotBlank() && odometro.isNotBlank()) {
                    viewModel.salvarVeiculo(nome, tipo, ano, odometro.toIntOrNull() ?: 0)
                    onVoltarClick()
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Salvar e Voltar") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaAbastecimentos(
    veiculoId: Long,
    viewModel: VeiculoViewModel,
    onVoltarClick: () -> Unit,
    abastecimentoViewModel: AbastecimentoViewModel = viewModel()
) {
    val listaAbastecimentosBruta by abastecimentoViewModel.listarPorVeiculo(veiculoId).collectAsState(initial = emptyList())

    val listaAbastecimentos = listaAbastecimentosBruta.sortedWith(
        compareBy<Abastecimento> { it.data }.thenBy { it.odometro }
    )

    val totalGasto = listaAbastecimentos.sumOf { it.valorGasto }
    val totalLitros = listaAbastecimentos.sumOf { it.litros }
    val maiorOdometro = listaAbastecimentos.maxOfOrNull { it.odometro } ?: 0
    val menorOdometro = listaAbastecimentos.minOfOrNull { it.odometro } ?: 0
    val totalKm = if (maiorOdometro > menorOdometro) maiorOdometro - menorOdometro else 0
    val mediaGeral = if (totalKm > 0 && totalLitros > 0) totalKm.toDouble() / totalLitros else 0.0
    val custoPorKm = if (totalKm > 0) totalGasto / totalKm else 0.0

    var valorGasto by remember { mutableStateOf("") }
    var precoLitro by remember { mutableStateOf("") }
    var odometro by remember { mutableStateOf("") }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var dataSelecionada by remember { mutableStateOf(System.currentTimeMillis()) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var idParaEditar by remember { mutableStateOf<Long?>(null) }
    var mensagemErro by remember { mutableStateOf("") }

    // Variáveis para controlar o Dashboard e o Gráfico
    var mostrarDashboard by remember { mutableStateOf(true) }
    var mostrarGrafico by remember { mutableStateOf(false) }

    val formatadorData = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    fun calcularDias(dataFinal: Long, dataInicial: Long): Long = (dataFinal - dataInicial) / (24 * 60 * 60 * 1000)

    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dataSelecionada)
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // CORREÇÃO DO FUSO HORÁRIO
                    datePickerState.selectedDateMillis?.let { millis ->
                        val timeZone = TimeZone.getTimeZone("UTC")
                        val calendar = Calendar.getInstance(timeZone)
                        calendar.timeInMillis = millis

                        val ano = calendar.get(Calendar.YEAR)
                        val mes = calendar.get(Calendar.MONTH)
                        val dia = calendar.get(Calendar.DAY_OF_MONTH)

                        val localCalendar = Calendar.getInstance()
                        localCalendar.set(ano, mes, dia, 12, 0, 0)
                        dataSelecionada = localCalendar.timeInMillis
                    }
                    mostrarDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val context = LocalContext.current

    fun compartilharCsv() {
        if (listaAbastecimentos.isEmpty()) return
        val cabecalho = "Data,Odometro(KM),Valor(R$),Preco_Litro(R$),Litros,KM_Rodados,Media(KM/L)\n"
        val linhas = listaAbastecimentos.joinToString(separator = "\n") { ab ->
            val dataFormatada = formatadorData.format(ab.data)
            "${dataFormatada},${ab.odometro},${"%.2f".format(ab.valorGasto)},${"%.2f".format(ab.precoLitro)},${"%.2f".format(ab.litros)},${ab.kmRodados},${"%.2f".format(ab.consumoMedio)}"
        }
        val conteudoCsv = cabecalho + linhas
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Histórico de Abastecimentos")
            putExtra(Intent.EXTRA_TEXT, conteudoCsv)
        }
        val chooser = Intent.createChooser(intent, "Compartilhar histórico via")
        ContextCompat.startActivity(context, chooser, null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Abastecimentos") },
                navigationIcon = {
                    IconButton(onClick = { onVoltarClick() }) {
                        Text("<", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    TextButton(onClick = { compartilharCsv() }) {
                        Text("Exportar", color = MaterialTheme.colorScheme.onSurface) // Corrigido para modo escuro
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                mostrarFormulario = !mostrarFormulario
                if (mostrarFormulario) {
                    idParaEditar = null
                    valorGasto = ""
                    precoLitro = ""
                    odometro = ""
                    mensagemErro = ""
                    dataSelecionada = System.currentTimeMillis()
                }
            }) {
                Text(if (mostrarFormulario) "X" else "+")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {

            if (mostrarFormulario) {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (idParaEditar == null) "Novo Abastecimento" else "Editar Abastecimento", style = MaterialTheme.typography.titleMedium)

                        Button(onClick = { mostrarDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Data: ${formatadorData.format(dataSelecionada)}")
                        }

                        OutlinedTextField(value = valorGasto, onValueChange = { valorGasto = it }, label = { Text("Valor Gasto (R$)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = precoLitro, onValueChange = { precoLitro = it }, label = { Text("Preço do Litro (R$)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = odometro, onValueChange = { odometro = it }, label = { Text("Odômetro Atual (KM)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                        if (mensagemErro.isNotEmpty()) {
                            Text(mensagemErro, color = MaterialTheme.colorScheme.error)
                        }

                        Button(onClick = {
                            val v = valorGasto.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val p = precoLitro.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val o = odometro.toIntOrNull() ?: 0
                            val ultimoKm = listaAbastecimentos.lastOrNull()?.odometro ?: 0

                            if (v <= 0.0 || p <= 0.0 || o <= 0) {
                                mensagemErro = "Preencha todos os campos corretamente."
                            } else if (idParaEditar == null && o < ultimoKm) {
                                mensagemErro = "O odômetro não pode ser menor que o último ($ultimoKm km)."
                            } else {
                                if (idParaEditar == null) {
                                    abastecimentoViewModel.salvarAbastecimento(veiculoId, dataSelecionada, v, p, o)
                                } else {
                                    abastecimentoViewModel.atualizarAbastecimento(idParaEditar!!, veiculoId, dataSelecionada, v, p, o)
                                }
                                valorGasto = ""
                                precoLitro = ""
                                odometro = ""
                                mensagemErro = ""
                                mostrarFormulario = false
                                idParaEditar = null
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (idParaEditar == null) "Salvar Abastecimento" else "Atualizar Abastecimento")
                        }
                    }
                }
            }

            // --- BOTÃO PARA MOSTRAR/ESCONDER DASHBOARD ---
            TextButton(onClick = { mostrarDashboard = !mostrarDashboard }, modifier = Modifier.fillMaxWidth()) {
                Text(if (mostrarDashboard) "Ocultar Resumo Geral" else "Mostrar Resumo Geral")
            }

            // --- DASHBOARD ---
            if (listaAbastecimentos.isNotEmpty() && mostrarDashboard) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Resumo Geral", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Média Geral:", style = MaterialTheme.typography.bodyMedium)
                            Text("${"%.2f".format(mediaGeral)} km/L", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Custo por KM:", style = MaterialTheme.typography.bodyMedium)
                            Text("R$ ${"%.2f".format(custoPorKm)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Gasto:", style = MaterialTheme.typography.bodySmall)
                            Text("R$ ${"%.2f".format(totalGasto)}", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Litros:", style = MaterialTheme.typography.bodySmall)
                            Text("${"%.2f".format(totalLitros)} L", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Rodado:", style = MaterialTheme.typography.bodySmall)
                            Text("$totalKm km", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // --- BOTÃO PARA MOSTRAR/ESCONDER GRÁFICO ---
            TextButton(onClick = { mostrarGrafico = !mostrarGrafico }, modifier = Modifier.fillMaxWidth()) {
                Text(if (mostrarGrafico) "Ocultar Gráfico" else "Mostrar Gráfico")
            }

            // --- GRÁFICO ---
            if (listaAbastecimentos.isNotEmpty() && mostrarGrafico) {
                GraficoConsumo(listaAbastecimentos = listaAbastecimentos)
            }

            Text("Histórico:", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listaAbastecimentos.reversed()) { abastecimento ->
                    val anterior = listaAbastecimentos.filter { it.data < abastecimento.data || (it.data == abastecimento.data && it.id != abastecimento.id && it.odometro < abastecimento.odometro) }.maxByOrNull { it.data }
                    val dias = if (anterior != null) calcularDias(abastecimento.data, anterior.data) else 0
                    val kmRodadosReal = if (anterior != null) abastecimento.odometro - anterior.odometro else 0
                    val mediaReal = if (kmRodadosReal > 0 && abastecimento.litros > 0) kmRodadosReal / abastecimento.litros else 0.0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Data: ${formatadorData.format(abastecimento.data)}", style = MaterialTheme.typography.titleMedium)
                                Row {
                                    TextButton(onClick = {
                                        idParaEditar = abastecimento.id
                                        valorGasto = abastecimento.valorGasto.toString()
                                        precoLitro = abastecimento.precoLitro.toString()
                                        odometro = abastecimento.odometro.toString()
                                        dataSelecionada = abastecimento.data
                                        mensagemErro = ""
                                        mostrarFormulario = true
                                    }) {
                                        Text("Editar", color = MaterialTheme.colorScheme.primary)
                                    }
                                    TextButton(onClick = { abastecimentoViewModel.excluirAbastecimento(abastecimento.id) }) {
                                        Text("Excluir", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            Text("Valor: R$ ${"%.2f".format(abastecimento.valorGasto)} | Litros: ${"%.2f".format(abastecimento.litros)}L")
                            Text("Odômetro: ${abastecimento.odometro} km")
                            if (anterior != null && kmRodadosReal > 0) {
                                Text("Rodou: $kmRodadosReal km em $dias dias", color = MaterialTheme.colorScheme.primary)
                                Text("Média: ${"%.2f".format(mediaReal)} km/L", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            } else {
                                Text("Primeiro abastecimento (Aguardando o próximo para calcular a média)", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GraficoConsumo(listaAbastecimentos: List<Abastecimento>) {
    val dados = listaAbastecimentos.filter { it.consumoMedio > 0 }.takeLast(10)
    if (dados.isEmpty()) {
        Text("Adicione mais abastecimentos para gerar o gráfico.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(16.dp))
        return
    }
    val maxMedia = dados.maxOf { it.consumoMedio }
    val minMedia = dados.minOf { it.consumoMedio }
    val corLinha = MaterialTheme.colorScheme.primary
    val corPonto = MaterialTheme.colorScheme.secondary

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Evolução do Consumo (km/L)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                val largura = size.width.toFloat()
                val altura = size.height.toFloat()
                val margem = 20f
                val espacamentoX = if (dados.size > 1) largura / (dados.size - 1).toFloat() else largura
                val alturaUtil = altura - margem
                val path = Path()
                dados.forEachIndexed { index, abastecimento ->
                    val variacao = if (maxMedia == minMedia) 0.5f else ((abastecimento.consumoMedio - minMedia) / (maxMedia - minMedia)).toFloat()
                    val x = index * espacamentoX
                    val y = altura - (variacao * alturaUtil) - 10f
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = corLinha, style = Stroke(width = 5f))
                dados.forEachIndexed { index, abastecimento ->
                    val variacao = if (maxMedia == minMedia) 0.5f else ((abastecimento.consumoMedio - minMedia) / (maxMedia - minMedia)).toFloat()
                    val x = index * espacamentoX
                    val y = altura - (variacao * alturaUtil) - 10f
                    drawCircle(color = corPonto, radius = 8f, center = Offset(x, y))
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mín: ${"%.1f".format(minMedia)}", style = MaterialTheme.typography.labelSmall)
                Text("Máx: ${"%.1f".format(maxMedia)}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}