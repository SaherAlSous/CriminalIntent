package com.bignerdranch.android.criminalintent.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.android.criminalintent.Crime
import java.io.File
import java.util.*
import java.util.concurrent.Executors

/*
Repository class encapsulates the logic for accessing data from single source or set of sources
it determines how to fetch and store particular set of data, weather locally in a DB or remote server

The UI code will request the data from the Repository. Repository take care of the data work not UI.

CrimeRepository is a singleton, it will have only one instance and stay
 in memory allover the app lifecycle.
 */

//Creating 2 properties to Store references
private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context){

    /*
    Room.databaseBuilder create concrete implementation of your abstract CrimeDataBase using
    - Application Context
    - Database class that we Room to create.
    - name of db file that Room will create for the app.
     */
    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2) //adding migration to db p.294
        .build()

    private val crimeDao = database.crimeDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir
    /*
    Adding and executor to create a thread to store the updated crime an insert a new one
    check page 257
     */

    /*
    Creating the functions that the app components can use to access database
    for each function in DAO we create a function here in Repository to link them
     */

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()
    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)
    /* adding livedata above after DAO*/


    fun updateCrime(crime: Crime){
        executor.execute {
            crimeDao.updateCrime(crime)
                    }
    }
    fun addCrime(crime: Crime){
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }
    /*
    adding an executor to update/create crime entries.
     */

    //saving photos into device, p.317
    fun getPhotoFile(crime: Crime): File = File(filesDir, crime.photoFileName)

    companion object{
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context){
            if (INSTANCE == null){
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get(): CrimeRepository{
            return INSTANCE ?:
            throw IllegalStateException("CrimeRepository must be initialized")

            /*
            learn more about singletons in page: 242
             */
        }
    }
}