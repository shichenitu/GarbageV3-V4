package dk.chen.garbagev1.domain

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import dk.chen.garbagev1.data.ItemDto
import dk.chen.garbagev1.data.BinDto
import dk.chen.garbagev1.R
import java.util.UUID

data class Item(
    val id: String = UUID.randomUUID().toString(),
    val what: String,
    val where: String,
    val photoPath: String? = null
)

enum class BinCategory(
    val englishName: String,
    @StringRes val stringRes: Int
) {
    BATTERIES("Batteries", R.string.category_batteries),
    PLASTIC("Plastic", R.string.category_plastic),
    CARDBOARD("Cardboard", R.string.category_cardboard),
    GLASS("Glass", R.string.category_glass),
    PAPER("Paper", R.string.category_paper),
    METAL("Metal", R.string.category_metal),
    FOOD("Food", R.string.category_food),
    DAILY_WASTE("Daily Waste", R.string.category_daily_waste),
    ELECTRONICS("Electronics", R.string.category_electronics),
    BULKY_WASTE("Bulky Waste", R.string.category_bulky_waste),
    WOOD("Wood", R.string.category_wood),
    CHEMICAL("Chemical", R.string.category_chemical),
    TEXTILE_WASTE("Textile Waste", R.string.category_textile_waste),
    OTHER("Other", R.string.category_other);

    companion object {
        fun fromName(name: String): BinCategory? {
            return entries.find { it.englishName.equals(name, ignoreCase = true) }
        }
    }
}

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

fun Bin.getDisplayNameRes(): Int {
    return BinCategory.fromName(this.name)?.stringRes ?: R.string.category_other
}