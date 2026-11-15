package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VeiculoDao {

    //Salvar um novo veiculo
    @Insert
    suspend fun salvar(veiculo: Veiculo): Long

    //Listar todos os veículos em ordem alfabética
    @Query("SELECT * FROM veiculos ORDER BY nome ASC")
    fun listarTodos(): Flow<List<Veiculo>>

    // 3. Buscar um veículo específico pelo ID
    @Query("SELECT * FROM veiculos WHERE id = :id")
    suspend fun buscarPorId(id: Long): Veiculo?

    @Update
    suspend fun atualizar(veiculo: Veiculo)
}