package dev.itsvic.parceltracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.itsvic.parceltracker.api.getParcel
import dev.itsvic.parceltracker.db.ParcelStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class NotificationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = ParcelApplication.db
        val parcelDao = db.parcelDao()
        val statusDao = db.parcelStatusDao()
        val zone = ZoneId.systemDefault()
        Log.d("NotificationWorker", "I ran!")

        withContext(Dispatchers.IO) {
            val parcels = parcelDao.getAllWithStatusAsync()
            Log.d("NotificationWorker", "Got parcels: $parcels")

            for (parcelWithStatus in parcels) {
                val parcel = parcelWithStatus.parcel
                val oldStatus = parcelWithStatus.status
                // fetch
                Log.d("NotificationWorker", "Fetching parcel status for $parcel")
                val apiParcel = try {
                    getParcel(parcel.parcelId, parcel.postalCode, parcel.service)
                } catch (e: Exception) {
                    Log.d("NotificationWorker", "Failed to fetch, skipping", e)
                    continue
                }

                val lastChange = apiParcel.history.first().time.atZone(zone).toInstant()

                if (oldStatus == null) {
                    Log.d(
                        "NotificationWorker",
                        "Parcel did not have a status before, will only add one."
                    )
                    statusDao.insert(ParcelStatus(parcel.id, apiParcel.currentStatus, lastChange))
                } else if (oldStatus.lastChange != lastChange) {
                    Log.d(
                        "NotificationWorker",
                        "Parcel has had updates since then, push a notification!"
                    )
                } else {
                    Log.d("NotificationWorker", "Parcel has not had any updates yet.")
                }
            }
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
