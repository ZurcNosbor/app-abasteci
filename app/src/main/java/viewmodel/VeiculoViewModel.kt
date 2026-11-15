package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Veiculo
import kotlinx.coroutines.launch

class VeiculoViewModel(application: Application) : AndroidViewModel(application) {

    // Pega a instância do banco de dados
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.veiculoDao()

    // A tela vai "observar" essa lista. Se salvar um novo, ela atualiza sozinha.
    val listaVeiculos = dao.listarTodos()

    // Função chamada quando o usuário clica em "Salvar"
    fun salvarVeiculo(nome: String, tipo: String, ano: String, odometroInicial: Int) {
        viewModelScope.launch {
            val novoVeiculo = Veiculo(nome = nome, tipo = tipo, ano = ano, odometroAtual = odometroInicial)
            dao.salvar(novoVeiculo)
        }
    }

}