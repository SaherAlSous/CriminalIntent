package com.bignerdranch.android.criminalintent

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/*
annotation to let room create a database
 table for it, with defining the table primary key. p-:223

 - Entity classes define the structure of database tables (rows -> entries, columns -> properties)
 */

@Entity
data class Crime(@PrimaryKey val id : UUID = UUID.randomUUID(),
                 var title: String ="",
                 var date: Date = Date(),
                 var isSolved: Boolean = false,
                 var suspect: String = ""){

    //designating a picture location p.317
    val photoFileName
        get() = "IMG_$id.jpg"
}
