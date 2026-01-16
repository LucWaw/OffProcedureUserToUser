package fr.lucwaw.utou.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.lucwaw.utou.data.dao.UserDao
import fr.lucwaw.utou.data.entity.Converters
import fr.lucwaw.utou.data.entity.UserEntity

@Database(entities = [UserEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}