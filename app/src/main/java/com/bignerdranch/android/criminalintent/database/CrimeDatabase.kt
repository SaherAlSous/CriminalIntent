package com.bignerdranch.android.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bignerdranch.android.criminalintent.Crime

/*
Database annotation tells room that this class represents database in the app.
the entities tells room which entities (models) to use to run/create db.
versions is used to tell room if you made changes/modifications to entities from previous time.
 */
@Database(entities = [Crime::class], version = 1)
@TypeConverters(CrimeTypeConverters::class) /*To tell the room compiler which converters to use*/
abstract class CrimeDatabase: RoomDatabase(){

    abstract fun crimeDao() : CrimeDao
/* Registering DAO class (interface)
 with db class to be used in operations*/

}
