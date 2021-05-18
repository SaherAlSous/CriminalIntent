package com.bignerdranch.android.criminalintent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.util.*


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() , CrimeListFragment.Callbacks {
    /*
    Implementing callbacks inside the hosting activity to change the fragment once needed.
     */


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)


            /*
      to add a fragment into activity in code, we call FragmentManager.
      we instantiate the fragment with the fragment manager. and set the condition to show it
      in case it is null, call fragment manager to start transaction and add the fragment container
      as the view and its code file CrimeFragment() in action, then commit them to OS.

      transactions are used to add, remove, attach, detach or replace a fragment
       */
            val currentFragment = //<--  When we need to retrieve the CrimeFragment from
                // the fragment manager you asked for it by container view ID
                supportFragmentManager.findFragmentById(R.id.fragment_container)

            if (currentFragment == null) {
                val fragment = CrimeListFragment.newInstance()
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
            }
        }
        override fun onCrimeSelected(crimeId: UUID) {
            val fragment = CrimeFragment.newInstance(crimeId) //after creating a new instance in crimefragment,
                                                             // we pass the user id through its argument
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // Making the transaction to be reversed to list.
                .commit()
        }
    }



