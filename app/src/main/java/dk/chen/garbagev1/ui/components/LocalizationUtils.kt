package dk.chen.garbagev1.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dk.chen.garbagev1.R

import android.content.Context

@Composable
fun getBinDisplayName(binName: String): String {
    val context = androidx.compose.ui.platform.LocalContext.current
    return getBinDisplayName(context, binName)
}

fun getBinDisplayName(context: Context, binName: String): String {
    val resId = when (binName) {
        "Batteries" -> R.string.category_batteries
        "Plastic" -> R.string.category_plastic
        "Cardboard" -> R.string.category_cardboard
        "Glass" -> R.string.category_glass
        "Paper" -> R.string.category_paper
        "Metal" -> R.string.category_metal
        "Food" -> R.string.category_food
        "Daily Waste" -> R.string.category_daily_waste
        "Electronics" -> R.string.category_electronics
        "Bulky Waste" -> R.string.category_bulky_waste
        "Wood" -> R.string.category_wood
        "Chemical" -> R.string.category_chemical
        "Textile Waste" -> R.string.category_textile_waste
        "Other" -> R.string.category_other
        else -> null
    }
    return if (resId != null) context.getString(resId) else binName
}
