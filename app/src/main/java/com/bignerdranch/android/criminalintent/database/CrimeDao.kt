package com.bignerdranch.android.criminalintent.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.bignerdranch.android.criminalintent.Crime
import java.util.*

/*
Data Access Object: to interact with database, it contains function for each database
operation you want to perform. in this app we will use 2 types of operations
- getting for list of crimes
- get a single crime based on UUID

@Query means its a db operation. here it is pulling data from db.

--> for more db codes check www.sqlite.org
 */
@Dao
interface CrimeDao {

    @Query("Select * FROM crime")
    fun getCrimes() : LiveData<List<Crime>> //adding livedata to move room operation to background

    @Query("SELECT * FROM crime WHERE id=(:id)")
    fun getCrime(id: UUID) : LiveData<Crime?> //if room remains in main thread, app crashes.

}