package com.bignerdranch.android.criminalintent.database

import androidx.room.TypeConverter
import java.util.*


/*
   Over here we will add type converter. since Room cant store Date & UUID in Crime by default.

   a type converter tells room how to convert specific type to the format it needs to
   store it in db.

   we need 2 functions annotated with TypeConverter
   - to convert the item to store it into db
   - to convert from db back to original type.

    */

class CrimeTypeConverters {

    @TypeConverter
    fun fromDate(date: Date?) :Long?{
        return date?.time
    }

    @TypeConverter
    fun toDate(MillisSinceEpoch : Long?) : Date? {
        return MillisSinceEpoch?.let {
            Date(it)
        }
    }

    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return UUID.fromString(uuid)
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?) : String? {
        return uuid?.toString()
    }

}