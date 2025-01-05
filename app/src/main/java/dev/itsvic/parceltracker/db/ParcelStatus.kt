package dev.itsvic.parceltracker.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Update
import dev.itsvic.parceltracker.api.Status
import java.time.Instant

@Entity
data class ParcelStatus(
    @PrimaryKey val parcelId: Int,
    val status: Status,
    val lastChange: Instant,
)

@Dao
interface ParcelStatusDao {
    @Insert
    suspend fun insert(status: ParcelStatus)

    @Update
    suspend fun update(status: ParcelStatus)
}
