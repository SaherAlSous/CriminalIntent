package com.bignerdranch.android.criminalintent.database

import android.app.Application

class CriminalIntentApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)
    }
}

/*
Creating an APPLICATION subclass to allow accessing the lifecycle information about
the application itself.
this will initialize the Repository once the application is ready.
 */