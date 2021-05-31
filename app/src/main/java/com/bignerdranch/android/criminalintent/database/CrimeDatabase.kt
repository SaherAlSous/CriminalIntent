package com.bignerdranch.android.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.criminalintent.Crime

/*
Database annotation tells room that this class represents database in the app.
the entities tells room which entities (models) to use to run/create db.
versions is used to tell room if you made changes/modifications to entities from previous time.
 */
@Database(entities = [Crime::class], version = 3)
@TypeConverters(CrimeTypeConverters::class) /*To tell the room compiler which converters to use*/
abstract class CrimeDatabase: RoomDatabase(){

    abstract fun crimeDao() : CrimeDao
/* Registering DAO class (interface)
 with db class to be used in operations*/

    /*
    Since we updated the DB table (Crime) we have to tell Room to
    migrate the data to the new table. p.293
     */
}

val migration_1_2 = object : Migration(1,2){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''")
    }
}

val migration_2_3 = object : Migration(2,3){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Crime ADD COLUMN requirePolice INTEGER NOT NULL")
    }
}