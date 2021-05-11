package com.bignerdranch.android.criminalintent

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment

class CrimeFragment: Fragment() {
    private lateinit var crime: Crime
    //wiring the widgets into the fragment
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox

    /*
    overriding the onCreate function, since it will be called from an Activity to run
    functions and display content to the UI.
    also linking the controller (fragment) with the data class
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
    }

    /*
    linking the view file manually to the controller since it does not inflate it
    automatically like the activity.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container,false)

        //initiating the widgets inside the fragment View.
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox

        dateButton.apply {
            text = crime.date.toString()
            isEnabled = false
        }

        return view
    }

    /*We added the TextWatcher after onCreateView, so that the UI will be restored, then
    this function works. meanwhile if we add them to onCreate, the TextWatcher will start before
    the view is restored, either after rotation or being called.
     */


    override fun onStart() {
        super.onStart()
        /*
        Creating a listener class to the user text input,
        TextWatcher has three functions, before, on and afterTextChange, we need only one.
        we convert the text CharSequence into string to be used in the title.

        At the end, we link the titlefield with the listener class using
        an addTextChangeListener

        actually setting all the listeners inside onStart/
         */
        val titleWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?,
                                           start: Int,
                                           count: Int,
                                           after: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?,
                                       start: Int,
                                       before: Int,
                                       count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                TODO("Not yet implemented")
            }
        }
        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }
    }

}