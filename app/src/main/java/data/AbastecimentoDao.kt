package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface AbastecimentoDao {

    @Insert
    suspend fun inserir(abastecimento: Abastecimento): Long

    @Query("SELECT * FROM abastecimentos WHERE veiculoId = :veiculoId ORDER BY data DESC")
    fun listarPorVeiculo(veiculoId: Long): Flow<List<Abastecimento>>

    @Query("SELECT odometro FROM abastecimentos WHERE veiculoId = :veiculoId ORDER BY data DESC LIMIT 1")
    suspend fun ultimoOdometro(veiculoId: Long): Int?

    @Query("DELETE FROM abastecimentos WHERE id = :id")
    suspend fun excluir(id: Long)

    @Update
    suspend fun atualizar(abastecimento: Abastecimento)

}