package dk.chen.garbagev1.domain

import androidx.compose.ui.graphics.Color
import dk.chen.garbagev1.data.ItemDto
import dk.chen.garbagev1.data.BinDto
import java.util.UUID

data class Item(
    val id: String = UUID.randomUUID().toString(),
    val what: String,
    val where: String,
    val photoPath: String? = null
)

data class Bin(
    val name: String,
    val imageUrl: String,
    val binColor: Color,
    val lastPickupTime: Long = 0L
)

enum class Theme {
    SYSTEM,
    LIGHT,
    DARK
}

data class RecyclingStation(
    val id: String,
    val name: String,
    val category: String,
    val address: String,
    val status: String,
    val bins: List<String>,
    val latitude: Double,
    val longitude: Double,
)

fun Item.toDto(): ItemDto = ItemDto(id = this.id, what = this.what, where = this.where, photoPath = this.photoPath)
fun Bin.toDto(): BinDto = BinDto(name = this.name, imageUrl = this.imageUrl, binColor = this.binColor.value.toString())

fun Item.fullDescription(): String = "${this.what.lowercase()} should be placed in: ${this.where}"