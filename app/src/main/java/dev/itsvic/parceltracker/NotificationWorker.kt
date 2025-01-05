package dev.itsvic.parceltracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class NotificationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = ParcelApplication.db
        Log.d("NotificationWorker", "I ran!")

        withContext(Dispatchers.IO) {
            val parcels = db.parcelDao().getAllAsync()
            Log.d("NotificationWorker", "Got parcels: $parcels")
        }

        return Result.success()
    }
}

fun Context.enqueueNotificationWorker() {
    val request = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
        .build()

    WorkManager.getInstance(this)
        .enqueueUniquePeriodicWork(
            "ParcelTrackerNotificationWorker",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
}
