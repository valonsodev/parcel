package dev.itsvic.parceltracker.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Parcel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun parcelDao(): ParcelDao
}
