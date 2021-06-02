package com.bignerdranch.android.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import java.sql.Time
import java.util.*

private const val ARG_DATE = "date"

class DatePickerFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = arguments?.getSerializable(ARG_DATE) as Date //getting the date from CrimeFragment
        val calendar = Calendar.getInstance(Locale.FRANCE) //creating an instance with the calendar in the library
        calendar.time = date //setting the calendar date from CrimeFragment Argument. if null goes to current date.

        val initialYear = calendar.get(Calendar.YEAR) //Initializing calendar with current date
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        /*
        creating a date listener that submit the new date to the callbacks to crime fragment. p.273
         */
        val dateListener = DatePickerDialog.OnDateSetListener{
            _: DatePicker, year: Int, month: Int, day: Int ->
            val resultDate: Date = GregorianCalendar(year, month, day).time
            targetFragment?.let { fragment ->
                (fragment as Callbacks).onDateSelected(resultDate)
            }
        }


        return DatePickerDialog( // for explanation check page 265 BNR
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay,

        )
    }

    /*
    passing data between 2 fragments p.269
     */

    companion object{
        fun newInstance(date: Date): DatePickerFragment{
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }

    //Creating an interface for the crimefragment to use and get the date picked by user
    interface Callbacks {
        fun onDateSelected(date: Date)
    }
}