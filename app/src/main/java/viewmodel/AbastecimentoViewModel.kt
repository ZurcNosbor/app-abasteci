package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Abastecimento
import com.example.myapplication.data.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AbastecimentoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val abastecimentoDao = db.abastecimentoDao()
    private val veiculoDao = db.veiculoDao()

    fun listarPorVeiculo(veiculoId: Long): Flow<List<Abastecimento>> {
        return abastecimentoDao.listarPorVeiculo(veiculoId)
    }

    fun atualizarAbastecimento(
        id: Long,
        veiculoId: Long,
        dataMillis: Long,
        valorGasto: Double,
        precoLitro: Double,
        odometroAtual: Int
    ) {
        viewModelScope.launch {
            // A lógica de cálculo é a mesma do salvar
            val ultimoOdometro = abastecimentoDao.ultimoOdometro(veiculoId) ?: veiculoDao.buscarPorId(veiculoId)?.odometroAtual ?: 0
            val litros = if (precoLitro > 0) valorGasto / precoLitro else 0.0
            val kmRodados = odometroAtual - ultimoOdometro
            val consumoMedio = if (kmRodados > 0 && litros > 0) kmRodados / litros else 0.0

            val abastecimentoAtualizado = Abastecimento(
                id = id, // Aqui está a diferença: passamos o ID existente
                veiculoId = veiculoId,
                data = dataMillis,
                valorGasto = valorGasto,
                precoLitro = precoLitro,
                odometro = odometroAtual,
                litros = litros,
                kmRodados = kmRodados,
                consumoMedio = consumoMedio
            )
            abastecimentoDao.atualizar(abastecimentoAtualizado)
        }
    }

    fun excluirAbastecimento(id: Long) {
        viewModelScope.launch {
            abastecimentoDao.excluir(id)
        }
    }

    fun salvarAbastecimento(
        veiculoId: Long,
        dataMillis: Long,
        valorGasto: Double,
        precoLitro: Double,
        odometroAtual: Int
    ) {
        viewModelScope.launch {
            // 1. Pega o último odômetro registrado no banco
            val ultimoOdometro = abastecimentoDao.ultimoOdometro(veiculoId)
                ?: veiculoDao.buscarPorId(veiculoId)?.odometroAtual ?: 0

            // 2. Calcula os valores
            val litros = if (precoLitro > 0) valorGasto / precoLitro else 0.0
            val kmRodados = odometroAtual - ultimoOdometro

            // A média só é calculada se rodou mais de 0 km (ou seja, a partir do 2º abastecimento)
            val consumoMedio = if (kmRodados > 0 && litros > 0) kmRodados / litros else 0.0

            // 3. Cria o objeto e salva
            val novoAbastecimento = Abastecimento(
                veiculoId = veiculoId,
                data = dataMillis,
                valorGasto = valorGasto,
                precoLitro = precoLitro,
                odometro = odometroAtual,
                litros = litros,
                kmRodados = kmRodados,
                consumoMedio = consumoMedio
            )
            abastecimentoDao.inserir(novoAbastecimento)

            // 4. Atualiza o odômetro atual do veículo para o próximo abastecimento
            val veiculo = veiculoDao.buscarPorId(veiculoId)
            if (veiculo != null) {
                veiculoDao.atualizar(veiculo.copy(odometroAtual = odometroAtual)) // <--- O CERTO É ATUALIZAR
            }
        }
    }
}