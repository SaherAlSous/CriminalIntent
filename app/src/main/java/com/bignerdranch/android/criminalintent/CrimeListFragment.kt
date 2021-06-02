package com.bignerdranch.android.criminalintent

import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.text.Layout
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.criminalintent.database.CrimeRepository
import java.util.*


private const val TAG = "CrimeListFragment"
private lateinit var crimeRecyclerView : RecyclerView


class CrimeListFragment: Fragment() {

    /*
    Required interface for hosting activities.
    Changing the fragment is only for the Hosting Activity to do,
    not the fragment business. that is why we callback to the hosting activity.
     */
    interface Callbacks {
        fun onCrimeSelected(crimeId : UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var imageButton : ImageButton


    //creating the adapter value that will link the Model with the View
    private var adapter : CrimeAdapter? = CrimeAdapter(emptyList())
    //preparing the adapter to take from livedata

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
    }

    /*
    onAttach for the fragment, once it is created and attached to the activity.
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    /* The Context object passed here is Activity Instance that is holding the fragment.
    Activity is sub class to context.
     */

    }
/*
telling fragment manager that crimeListFragment needs to receive menu callbacks
 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

  }

    //responding to menu items selection based on Item ID. p. 283/4
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }

        /*
    We link the fragment view (fragment crime list) with the code, and define the Recycler view
    then we give the Recycler view to a layout manager to work. otherwise it will crash
    because RecyclerView does not position items on the screen itself, it delegates that job
    to the LayoutManager.
    inside LayoutManager there are several built-in managers, for now we are using LinearLayout
    to list the items vertically.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

            val view= inflater.inflate(R.layout.fragment_crime_list, container, false)
              crimeRecyclerView =
                view?.findViewById(R.id.crime_recycler_view) as RecyclerView
            crimeRecyclerView.layoutManager= LinearLayoutManager(context)


//            crimeListViewModel.crimeListLiveData.observe(viewLifecycleOwner) { crimes ->
//                if (crimes.isNullOrEmpty()) {
//                   val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
//                    crimeRecyclerView =
//                        view.findViewById(R.id.crime_recycler_view) as RecyclerView
//                    crimeRecyclerView.layoutManager = LinearLayoutManager(context)
//
//                } else {
//                    val view = inflater.inflate(R.layout.empty_layout, container, false)
//                    imageButton = view.findViewById(R.id.imageButton) as ImageButton
//
//                    imageButton.setOnClickListener {
//                        val crime = Crime()
//                        crimeListViewModel.addCrime(crime)
//                        callbacks?.onCrimeSelected(crime.id)
//                    }
//                }
//            }

return view
        }
/*
updating the code to take LiveData
 */
    private fun updateUI(crimes: List<Crime>) {

//    adapter = CrimeAdapter(crimes)
//    crimeRecyclerView.adapter = adapter
//
    if (crimes.isEmpty()){
        imageButton = view?.findViewById(R.id.imageButton) as ImageButton
        imageButton.setVisibility(View.VISIBLE)
        imageButton.setOnClickListener{
                    val crime = Crime()
                    crimeListViewModel.addCrime(crime)
                    callbacks?.onCrimeSelected(crime.id)
        }
    } else{
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }

}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            Observer { crimes ->
                crimes?.let {
                    Log.i(TAG, "Got Crimes ${crimes.size}")
                    updateUI(crimes)
                }
            }
        )
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    /*
    Inflating the menu into the view (actionbar)
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }


    /*
    RecyclerView expects an item view to be wrapped in an instance of ViewHolder.
    ViewHolder stores a reference to an item's view (and sometimes references to specific
     widgets within that view)

     (Below) Crimeholder constructor we take in the view to hold on, then we pass it as the argument
     to the RecyclerView.ViewHolder constructor. The base ViewHolder class will then hold on to
     the view in a property named itemView.

     a RecyclerView never creates a Views by themselves, they creates ViewHolders, which brings
     their itemViews along for the ride.
     */
    private inner class CrimeHolder (view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener { //we added a click listener to each holder
        /*
        We want to make the info more private by taking it away from the adapter and keeping
        it cleaner.
        it is better to cleanly separate the concerns between view holder and adapter.
        the adapter should know as little as  possible about the inner workings and details of the
        view holder.
        Therefore it is better to encapsulate the work of (Binding data to view) inside ViewHolder.
         */

        private lateinit var crime : Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView : TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView : ImageView = itemView.findViewById(R.id.crime_solved_image)

        init { //initiating the listener
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime){
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = DateFormat.format("EEE dd MMM yyyy, hh:mm", this.crime.date)
            //image visibility
            solvedImageView.visibility = if (crime.isSolved) View.VISIBLE else View.GONE
        }

        /*
        In this example, the CrimeHolder is implementing the click listener interface by itself.
        on the itemview (the View of the entire row), the crimeholder is set as the receiver of the
        click.
         */

        override fun onClick(v: View?) {
            /*
            once the crime is clicked in the view, we send a callback to hosting activity
            with the id of the crime clicked.
             */
            callbacks?.onCrimeSelected(crime.id)
        }
    }
    /*
    RecyclerView doesn't create a ViewHolder itself, it asks an adapter to do so
    an adapter is a controller object that sits between the RecyclerView and the data set that
    the RecyclerView should display.

    The Adapter is responsible for:
    - creating the necessary ViewHolder when asked
    - Binding Viewholders to data from the model layer when asked

    Recycler View responsible for:
    - asking the adapter to create a new ViewHolder
    - asking the adapter to bind ViewHolder to the item from the backing data at a given position.
     */

    /*
    Crime adapter constructor takes data from crimes list, and links it
    to the view of CrimeHolder inner class: (it contains page: 186)
    - onCreateViewHolder: displays, wraps the view in view holder inside list_item_view.xml
                        and pass the resulting view to the new instance CrimeHolder.
    - onBindViewHolder: it populates / fill a given holder with the crime from a given position
                        from the crimes list.
                        Then we use the title and data from that crime list (at that position) to
                        set the text in the corresponding text view.
    - getItemCount: when the recycler view needs to know how many items in the data set backing it, \
                    it asks this fun.
                    Check figure 9.9 for adapter page 187.+++
     */

    private inner class CrimeAdapter(var crimes: List<Crime>)
        :RecyclerView.Adapter<CrimeHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            return if (viewType == 1){
                val view = layoutInflater.inflate(R.layout.list_item_crime2, parent,false)
                CrimeHolder(view) //Crimeholder is for single set of title and date. <---
            }else{
                val view = layoutInflater.inflate(R.layout.list_item_crime, parent,false)
                CrimeHolder(view) //Crimeholder is for single set of title and date. <---
            }

        }
        override fun getItemCount() = crimes.size

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            /*
            after making the binding action within the ViewHOlder, we remove this code and add
            the function we created there.
            holder.apply {
              titleTextView.text = crime.title
                dateTextView.text = crime.date.toString()
            }
             */

            holder.bind(crime)
    }

        override fun getItemViewType(position: Int): Int {
            return when(crimes[position].requirePolice){
                true -> 1
                else -> 2
            }
        }
}


    /*Activities can use this function like newIntent(), the call it to get an instance of the fragment
    you can link it with any code using CrimeListFragment.newInstance()
     */
    companion object{
        fun newInstance() : CrimeListFragment{
            return CrimeListFragment()
        }
    }
}