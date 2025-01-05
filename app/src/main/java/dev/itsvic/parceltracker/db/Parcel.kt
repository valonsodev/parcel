package dev.itsvic.parceltracker.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
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

data class ParcelWithStatus(
    @Embedded val parcel: Parcel,
    @Relation(
        parentColumn = "id",
        entityColumn = "parcelId"
    ) val status: ParcelStatus?,
)

@Dao
interface ParcelDao {
    @Query("SELECT * FROM parcel")
    fun getAll(): Flow<List<Parcel>>

    @Transaction
    @Query("SELECT * FROM parcel")
    fun getAllWithStatus(): Flow<List<ParcelWithStatus>>

    @Transaction
    @Query("SELECT * FROM parcel")
    suspend fun getAllWithStatusAsync(): List<ParcelWithStatus>

    @Query("SELECT * FROM parcel WHERE id=:id LIMIT 1")
    fun getById(id: Int): Flow<Parcel>

    @Insert
    suspend fun insert(parcel: Parcel): Long

    @Update
    suspend fun update(parcel: Parcel)

    @Delete
    suspend fun delete(parcel: Parcel)
}
