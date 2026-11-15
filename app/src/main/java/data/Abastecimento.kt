package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "abastecimentos",
    foreignKeys = [ForeignKey(
        entity = Veiculo::class,
        parentColumns = ["id"],
        childColumns = ["veiculoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("veiculoId")]
)
data class Abastecimento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val veiculoId: Long,
    val data: Long,
    val valorGasto: Double,
    val precoLitro: Double,
    val odometro: Int,
    val litros: Double,
    val kmRodados: Int,
    val consumoMedio: Double
)