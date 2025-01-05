package dev.itsvic.parceltracker.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    version = 2,
    entities = [Parcel::class, ParcelStatus::class],
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun parcelDao(): ParcelDao
    abstract fun parcelStatusDao(): ParcelStatusDao
}
