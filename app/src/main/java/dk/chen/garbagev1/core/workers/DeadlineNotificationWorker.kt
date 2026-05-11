package dk.chen.garbagev1.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dk.chen.garbagev1.core.NotificationHelper
import dk.chen.garbagev1.domain.BinRepository
import kotlinx.coroutines.flow.first
import kotlin.collections.filter
import kotlin.collections.forEach

@HiltWorker
class DeadlineNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val binRepository: BinRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext = context, params = workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val bins = binRepository.getBins().first()
            val currentTime = System.currentTimeMillis()
            val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L

            val overdueBins = bins.filter { bin ->
                bin.lastPickupTime != 0L && (currentTime - bin.lastPickupTime) > sevenDaysInMillis
            }

            overdueBins.forEach { bin ->
                notificationHelper.showNotification(
                    context = applicationContext,
                    title = "Time to recycle!",
                    message = "The ${bin.name} bin hasn't been emptied for over 7 days.",
                    notificationId = bin.name.hashCode()
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}