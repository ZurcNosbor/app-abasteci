package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "veiculos")
data class Veiculo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val tipo: String,
    val ano: String, // <--- NOVO CAMPO AQUI
    val odometroAtual: Int
    )
