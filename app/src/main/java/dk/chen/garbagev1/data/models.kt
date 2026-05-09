package dk.chen.garbagev1.data

import androidx.compose.ui.graphics.Color
import dk.chen.garbagev1.domain.Item
import dk.chen.garbagev1.domain.Bin
import java.util.UUID
import androidx.core.graphics.toColorInt
import dk.chen.garbagev1.domain.RecyclingStation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    val id: String = UUID.randomUUID().toString(),
    val what: String = "",
    val where: String = "",
    val photoPath: String? = null
)

@Serializable
data class BinDto(
    val name: String = "",
    val imageUrl: String = "",
    val binColor: String = "",
    val lastPickupTime: Long = 0L
)

@Serializable
data class RecyclingStationResponse(
    val result: RecyclingResultDto
)

@Serializable
data class RecyclingResultDto(
    val records: List<RecyclingStationDto>
)

@Serializable
data class RecyclingStationDto(
    @SerialName("_id") val id: Int,
    @SerialName("navn") val name: String = "Unknown Station",
    @SerialName("kategori") val category: String = "General",
    @SerialName("adresse") val address: String = "No address available",
    @SerialName("status") val status: String = "Unknown",
    val bins: List<String> = emptyList(),
    @SerialName("latitude") val latitude: Double = 0.0,
    @SerialName("longitude") val longitude: Double = 0.0
)

fun RecyclingStationDto.toDomain(): RecyclingStation {
    var lat = this.latitude
    var lon = this.longitude

    if (lat == 0.0) {
        when {
            this.name.contains("Bispeengen Genbrugsstation") -> { lat = 55.6983; lon = 12.5222 }
            this.name.contains("Sydhavn Genbrugsstation") -> { lat = 55.6416; lon = 12.5298 }
            this.name.contains("Vermlandsgades Genbrugsstation") -> { lat = 55.6631; lon = 12.6033 }
            this.name.contains("Kulbanevej Genbrugsstation") -> { lat = 55.6514; lon = 12.4969 }
            this.name.contains("Borgervænget Genbrugsstation") -> { lat = 55.7132; lon = 12.5831 }
            this.name.contains("Hørgården Nærgenbrugsstation") -> { lat = 55.6591; lon = 12.5901 }
            this.name.contains("Gartnergade Nærgenbrugsstation") -> { lat = 55.6881; lon = 12.5539 }
            this.name.contains("Nordhavn Nærgenbrugsstation") -> { lat = 55.7077; lon = 12.5977 }
            this.name.contains("Møllegade Nærgenbrugsstation") -> { lat = 55.6905; lon = 12.5568 }
            this.name.contains("Christiania Nærgenbrugsstation") -> { lat = 55.6728; lon = 12.6001 }
            this.name.contains("Enghave Nærgenbrugsstation") -> { lat = 55.6653; lon = 12.5448 }
            this.name.contains("Tingbjerg Nærgenbrugsstation") -> { lat = 55.7171; lon = 12.4839 }
            this.name.contains("Haraldsgade Nærgenbrugsstation") -> { lat = 55.7049; lon = 12.5546 }
            this.name.contains("Annexstræde") -> { lat = 55.6635; lon = 12.5135 }
            this.name.contains("Asger Jorns Allè") -> { lat = 55.6322; lon = 12.5694 }
            this.name.contains("Kulturhuset Pilegården") -> { lat = 55.7024; lon = 12.5029 }
            this.name.contains("Byttestation Tingbjerg Forum") -> { lat = 55.7185; lon = 12.4815 }
            this.name.contains("Rentemestervej") -> { lat = 55.7061; lon = 12.5312 }
            this.name.contains("Aksel Sandemoses Plads") -> { lat = 55.6908; lon = 12.5401 }
            this.name.contains("Remiseparken") -> { lat = 55.6534; lon = 12.6039 }
            else -> { lat = 55.6761; lon = 12.5683 }
        }
    }

    val finalBins = if (this.name.contains("Bispeengen")) {
        listOf("plastic", "paper", "metal")
    } else {
        listOf("plastic", "paper")
    }

    return RecyclingStation(
        id = this.id.toString(),
        name = this.name,
        category = this.category,
        address = this.address,
        status = this.status,
        bins = finalBins,
        latitude = lat,
        longitude = lon
    )
}

fun BinDto.toBin(): Bin = Bin(
    name = this.name,
    imageUrl = this.imageUrl,
    binColor = Color(color = this.binColor.toColorInt()),
    lastPickupTime = this.lastPickupTime
)

fun ItemDto.toItem(): Item = Item(
    id = this.id,
    what = this.what,
    where = this.where,
    photoPath = this.photoPath
)
