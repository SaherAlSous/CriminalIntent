package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bignerdranch.android.criminalintent.database.CrimeDetailViewModel
import java.util.*
import androidx.lifecycle.Observer

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_CODE = 0
private const val Date_Format = "EEE, MMM, dd"
private const val REQUEST_CONTACT = 1

class CrimeFragment: Fragment() , DatePickerFragment.Callbacks {
    private lateinit var crime: Crime
    //wiring the widgets into the fragment
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    /*
    Hooking the crimedetailviewmodel with crimefragment to use crimeId
     */
    private val crimeDetailViewModel : CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    /*
    overriding the onCreate function, since it will be called from an Activity to run
    functions and display content to the UI.
    also linking the controller (fragment) with the data class
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        /*
        Retrieving the userID from fragment argument.
         */
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        /*
            Loading crime using crimeId on create
         */
        crimeDetailViewModel.loadCrime(crimeId)
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
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button

       /* dateButton.apply {
            text = crime.date.toString()
            isEnabled = false
        }
        Using the button to date picker dialog
        */

        return view
    }

    /*We added the TextWatcher after onCreateView, so that the UI will be restored, then
    this function works. meanwhile if we add them to onCreate, the TextWatcher will start before
    the view is restored, either after rotation or being called.
     */

    /*
    creating an observer for livedata to update UI once data is received.
     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
                    updateUI()
                    /*
                    for details on how this controller works, check page 254
                     */
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        /*
        Creating a listener class to the user text input,
        TextWatcher has three functions, before, on and afterTextChange, we need only one.
        we convert the text CharSequence into string to be used in the title.

        At the end, we link the title field with the listener class using
        an addTextChangeListener

        actually setting all the listeners inside onStart/
         */
        val titleWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?,
                                           start: Int,
                                           count: Int,
                                           after: Int) {
            }

            override fun onTextChanged(s: CharSequence?,
                                       start: Int,
                                       before: Int,
                                       count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }
        dateButton.setOnClickListener {
            /* passing the crime date from this fragment to dialog fragment using instance*/
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_CODE) // making this fragment
                                                                            // a target fragment for
                                                                            //the dialog to get the
                                                                            //picked date by user
                show(this@CrimeFragment.getParentFragmentManager(), DIALOG_DATE)
            }
        }

        /*
        Creating an implicit intent to send the report as text, with subject and body. p.298+
         */
        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooseIntent = Intent.createChooser(intent,getString(R.string.send_report))
                startActivity(chooseIntent)
            }
        }

        /*
        Creating an implicit request to pick up a contact from the phone contacts. p 302
         */
        suspectButton.apply{
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }
            /*
     in case there is no contacts app. we let the device(package manager) to bring a list of
     apps that has similar work -> checking for responding activities p. 307
      */
            val packageManager : PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent ,
                    PackageManager.MATCH_DEFAULT_ONLY)

            if (resolvedActivity==null) {
                isEnabled = false
            }
        }
    }

    /*
    once we are done with onStart. we update the UI
     */
    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotBlank()){
            suspectButton.text = crime.suspect
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       /*
       getting data from contact list. p. 304+
        */

        when{
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactURI : Uri? = data.data
                //Specify which fields you want your query to return values for
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                //Perform Your Query - the ContactURI is like a "where" clause here
                val cursor = contactURI?.let {
                    requireActivity().contentResolver
                        .query(it, queryFields, null, null, null)
                }

                cursor.use {
                    //Verify Cursor contacts at least one result
                    if (it?.count == 0) {
                        return
                    }

                    /*
                    Pull out the first column of the first row of data -
                    that is your suspect name.
                     */
                    it?.moveToFirst()
                    val suspect = it?.getString(0)
                    if (suspect != null) {
                        crime.suspect= suspect
                    }
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text=suspect
                }
            }
        }
    }

    /*
    Creating a string message made several variables
     */

    private fun getCrimeReport() : String{
        val solvedString = if (crime.isSolved){
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(Date_Format, crime.date).toString()
        var suspect = if (crime.suspect.isBlank()){
            getString(R.string.crime_report_no_suspect)
        }else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
            /*returning the complete msg p. 295 */
    }


    /*
    creating an instance for the fragment to hold the argument needed to pass the user id for this
    fragment.
     */
    companion object{
        fun newInstance(crimeId: UUID):CrimeFragment{
            val args = Bundle().apply{
                putSerializable(ARG_CRIME_ID,crimeId)
                /* we can pass any argument and put it in Bundle,
                using the instance from parent activity
                 */
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

    //saving the changes into the db once fragment stops.
    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    //getting a callback from date picker dialog for picked date and updating the UI
    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }
}