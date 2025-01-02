package dev.itsvic.parceltracker.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import dev.itsvic.parceltracker.api.Service
import kotlinx.coroutines.flow.Flow

@Entity
data class Parcel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val humanName: String,
    val parcelId: String,
    val postalCode: String?,
    val service: Service,
)

@Dao
interface ParcelDao {
    @Query("SELECT * FROM parcel")
    fun getAll(): Flow<List<Parcel>>

    @Query("SELECT * FROM parcel WHERE id=:id LIMIT 1")
    fun getById(id: Int): Flow<Parcel>

    @Insert
    suspend fun insert(parcel: Parcel): Long

    @Delete
    suspend fun delete(parcel: Parcel)
}
