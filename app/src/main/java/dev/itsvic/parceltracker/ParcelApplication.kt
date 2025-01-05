package dev.itsvic.parceltracker

import android.app.Application
import androidx.room.Room
import dev.itsvic.parceltracker.db.AppDatabase

class ParcelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "parcel-tracker")
            .build()

        applicationContext.enqueueNotificationWorker()
    }

    companion object {
        lateinit var db: AppDatabase
    }
}
