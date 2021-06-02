package com.bignerdranch.android.criminalintent

import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bignerdranch.android.criminalintent.database.CrimeDetailViewModel
import java.util.*
import androidx.lifecycle.Observer
import com.bignerdranch.android.criminalintent.database.CrimeRepository
import java.io.File
import kotlin.properties.Delegates

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_CODE = 0
private const val Date_Format = "EEE, MMM, dd"
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2
private const val REQUEST_PHONE = 999

class CrimeFragment: Fragment() , DatePickerFragment.Callbacks {
    private lateinit var crime: Crime
    //wiring the widgets into the fragment
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var crimePhoto: ImageView
    private lateinit var photoFile: File
    private lateinit var photoUri : Uri
    private lateinit var requirePolice : CheckBox
    private lateinit var crimeTime : Button
    private lateinit var callSuspect : Button
    private lateinit var treeObserver: ViewTreeObserver
    private var viewWidth by Delegates.notNull<Int>()
    private var viewHeight by Delegates.notNull<Int>()


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
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        crimePhoto = view.findViewById(R.id.crime_photo) as ImageView
        requirePolice = view.findViewById(R.id.require_police) as CheckBox
        crimeTime = view.findViewById(R.id.crime_time) as Button
        callSuspect = view.findViewById(R.id.call_suspect) as Button


       /* dateButton.apply {
            text = crime.date.toString()
            isEnabled = false
        }
        Using the button to date picker dialog
        */
        treeObserver = crimePhoto.viewTreeObserver
        treeObserver.addOnGlobalLayoutListener {
            viewWidth = crimePhoto.width
            viewHeight = crimePhoto.height
        }
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
                    /*
                    taking and saving a full resolution photo. p 318+
                     */
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    photoFile)
                    updateUI()
                    /*
                    for details on how this controller works, check page 254
                     */
                }
            }
        )
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.N)
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
        requirePolice.apply{
            setOnCheckedChangeListener { _, isChecked ->
                crime.requirePolice = isChecked
            }
        }

        dateButton.setOnClickListener {
            /* passing the crime date from this fragment to dialog fragment using instance*/
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_CODE) // making this fragment
                                                                            // a target fragment for
                                                                            //the dialog to get the
                                                                            //picked date by user
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        /**
         * Adding time Picker
         */
        crimeTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timeListener = TimePickerDialog.OnTimeSetListener { timePicker: TimePicker , hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                crimeTime.text = java.text.SimpleDateFormat("HH:mm").format(calendar.time)
            }
            TimePickerDialog(context, timeListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()

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
            /**
             * Getting contact Phone URI to fetch the number
             */
            callSuspect.apply {
                val pickPhoneIntent = Intent(Intent.ACTION_PICK,ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                setOnClickListener {
                    startActivityForResult(pickPhoneIntent, REQUEST_PHONE)
                }
            }


            /*
     in case there is no contacts app. we let the device(package manager) to bring a list of
     apps that has similar work -> checking for responding activities p. 307
      */
            val packageManager : PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent ,
                    PackageManager.MATCH_DEFAULT_ONLY)
          //  if (resolvedActivity==null) isEnabled = false //<-- IT IS ALWAYS DISABLED.
        }

        /**
         * creating the imagebutton to start the fragmentdialog and display the photo
         */
        crimePhoto.setOnClickListener {
            val fragment = PhotoDialogFragment(photoFile)
            fragment.show(childFragmentManager, "PhotoFragmentDialog")

        }

/*
creating a camera intent to take photos... p. 320
 */
        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
            // if (resolvedActivity==null) isEnabled = false

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY)
                for (cameraActivity in cameraActivities){
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
                startActivityForResult(captureImage, REQUEST_PHOTO)
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
        updatePhotoView(viewWidth,viewHeight)
    }
    /*
    displaying current photo p 324
     */
    private fun updatePhotoView(width: Int, height: Int){
        if(photoFile.exists()){
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            crimePhoto.setImageBitmap(bitmap)
        }else {
            crimePhoto.setImageDrawable(null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       /*
       getting data from contact list. p. 304+
        */

        when{
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactURI = data.data
                //Specify which fields you want your query to return values for
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                //Perform Your Query - the ContactURI is like a "where" clause here
                val cursor =
                    requireActivity().contentResolver
                        .query(contactURI!!, queryFields, null, null, null)



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
            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView(viewWidth,viewHeight)
            }
            /**
             * Get the Phone Number
             */
            requestCode == REQUEST_PHONE && data != null -> {
                val contactURI : Uri? = data.data
                //Specify which fields you want your query to return values for
                val queryFields = ContactsContract.CommonDataKinds.Phone._ID
                //Perform Your Query - the ContactURI is like a "where" clause here
                val cursor =
                   requireActivity().contentResolver
                        .query(contactURI!!, null, queryFields, null, null)


//                    val cursorPhone : Cursor? = requireActivity().contentResolver
//                        .query(
//                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                            null,
//                            ContactsContract.CommonDataKinds.Phone._ID,
//                            null,
//                            null
//                        )
//
//                cursorPhone.use {
//                    if (it?.count == 0) {
//                        println("There is no result")
//                        return
//                        }
//                    it?.moveToFirst()
//
//                    val contactNumber = it?.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//                    callSuspect.text=contactNumber
//                    println("I got $contactNumber")
//
//                }
//                cursorPhone?.close()

                   cursor.use {
                        if (it?.count == 0) {
                            return
                        }
                        it?.moveToFirst()
                        val number = it?.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                       val dialNumber = Intent(Intent.ACTION_DIAL)
                       dialNumber.data = Uri.parse("tel: $number")
                       startActivity(dialNumber)
                    }
                cursor?.close()
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
        val df = DateFormat.getBestDateTimePattern(Locale.FRANCE, Date_Format)
        val dateString = df.format(Date_Format ,crime.date)
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

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }
}