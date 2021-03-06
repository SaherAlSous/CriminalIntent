package com.bignerdranch.android.criminalintent

import androidx.lifecycle.ViewModel
import com.bignerdranch.android.criminalintent.database.CrimeRepository

class CrimeListViewModel: ViewModel() {

/*
val crimes = mutableListOf<Crime>()
    init {
    for (i in 0 until 100){
        val crime = Crime()
        crime.title = "Crime#$i"
        crime.isSolved = i % 2 ==0
        crimes += crime
    }
    }

 */
    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()

    //allow app to add crimes into db
    fun addCrime(crime: Crime){
        crimeRepository.addCrime(crime)
    }
}