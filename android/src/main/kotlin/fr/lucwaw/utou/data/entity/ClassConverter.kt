package fr.lucwaw.utou.data.entity

import androidx.room.TypeConverter
import kotlin.time.Instant

class Converters {

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? =
        instant?.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(epochMillis: Long?): Instant? =
        epochMillis?.let { Instant.fromEpochMilliseconds(it) }
}
