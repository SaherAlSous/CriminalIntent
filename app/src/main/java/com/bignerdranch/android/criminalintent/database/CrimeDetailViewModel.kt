package com.bignerdranch.android.criminalintent.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.bignerdranch.android.criminalintent.Crime
import java.util.*

/*
check page 252/3 for explanation!!!
 */

class CrimeDetailViewModel: ViewModel() {
    private val crimeRepository = CrimeRepository.get()
    private val crimeIdLiveData = MutableLiveData<UUID>()

    var crimeLiveData : LiveData<Crime?> =
        Transformations.switchMap(crimeIdLiveData) { crimeId ->
            crimeRepository.getCrime(crimeId)
        }

    fun loadCrime(crimeId : UUID){
        crimeIdLiveData.value = crimeId
    }
    fun saveCrime(crime: Crime){
        crimeRepository.updateCrime(crime)
    }
}