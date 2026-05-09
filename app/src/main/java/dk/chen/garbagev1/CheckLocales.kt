package dk.chen.garbagev1
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
fun checkLocales() {
    Log.d("CheckLocales", "Locales: " + AppCompatDelegate.getApplicationLocales().toLanguageTags())
}
