package dev.itsvic.parceltracker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.itsvic.parceltracker.api.ParcelHistoryItem
import dev.itsvic.parceltracker.api.Status
import dev.itsvic.parceltracker.api.statusToHumanString
import dev.itsvic.parceltracker.db.Parcel

const val CHANNEL_ID = "ParcelTrackerEvents"

fun Context.sendNotification(parcel: Parcel, status: Status, event: ParcelHistoryItem) {
    val context = this
    val statusString = getString(statusToHumanString[status]!!)
    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.package_2)
        .setContentTitle("${parcel.humanName}: $statusString")
        .setContentText(event.description)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        // TODO: intent to open the full parcel view
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(this)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notify(parcel.id, builder.build())
    }
}

fun Context.createNotificationChannel() {
    val name = getString(R.string.channel_name)
    val description = getString(R.string.channel_description)
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel = NotificationChannel(CHANNEL_ID, name, importance)
    channel.description = description
    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}
