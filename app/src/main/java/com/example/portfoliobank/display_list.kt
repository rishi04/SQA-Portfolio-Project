package com.example.portfoliobank

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_display_list.*
import kotlinx.android.synthetic.main.activity_display_list.search_can_name
import kotlinx.android.synthetic.main.create_account.spinner_state


class display_list : AppCompatActivity() {

    private var stateSelected: String? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: FirebaseDatabase
    private var currentUserID: String? = null
    private lateinit var mUsersDbRef: DatabaseReference
    private lateinit var mUserStorageRefs: StorageReference
    private var name: String? = null
    private var userType: String? = null
    var searchQuery: Query? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_list)
        supportActionBar?.setTitle(R.string.app_name)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser?.uid
        mUsersDbRef = mDatabase.reference.child("Users")
        mUserStorageRefs = FirebaseStorage.getInstance().reference.child("UserProfileImages")


        search_results.visibility = View.GONE

        userType = intent?.getStringExtra("Admin").toString()
        if (userType.equals("Admin")) {
            searchQuery = mUsersDbRef.orderByChild("timestamp").limitToLast(2)
            fetchData(searchQuery!!)
        } else {

            name = intent?.getStringExtra("name").toString()
            stateSelected = intent?.getStringExtra("state").toString()
            if (name!!.length > 4) {
                search_results.visibility = View.VISIBLE
                searchQuery =
                    mUsersDbRef.orderByChild("firstName").startAt(name).endAt(name + "uf8ff")
                fetchData(searchQuery!!)
            } else if (stateSelected != null) {
                searchQuery = mUsersDbRef.orderByChild("state").startAt(stateSelected)
                    .endAt(stateSelected + "uf8ff")
                fetchData(searchQuery!!)
            }
        }
        result_list.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        val decoration = DividerItemDecoration(
            result_list.getContext(), linearLayoutManager.getOrientation()
        )
        result_list.addItemDecoration(decoration)
        result_list.layoutManager = linearLayoutManager

        setSpinner()

        query_button.setOnClickListener {
            search_results.visibility = View.VISIBLE
            name = search_can_name.text.toString()
            if (!name.isNullOrEmpty()) {
                search_results.visibility = View.VISIBLE
                searchQuery =
                    mUsersDbRef.orderByChild("firstName").startAt(name).endAt(name + "uf8ff")
                fetchData(searchQuery!!)
            } else if (stateSelected != null) {
                searchQuery = mUsersDbRef.orderByChild("state").startAt(stateSelected)
                    .endAt(stateSelected + "uf8ff")
                fetchData(searchQuery!!)
            } else {
                Snackbar.make(it, "Please select either a name or state", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun fetchData(searchQuery: Query) {
        val option = FirebaseRecyclerOptions.Builder<Users>()
            .setQuery(searchQuery, Users::class.java)
            .setLifecycleOwner(this)
            .build()

        val firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<Users, allUsersHolder>(option) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): allUsersHolder {
                    return allUsersHolder(
                        (LayoutInflater.from(parent.context)
                            .inflate(R.layout.display_search_list, parent, false))
                    )
                }

                override fun onBindViewHolder(holder: allUsersHolder, position: Int, model: Users) {
                    val placeid = getRef(position).key.toString()

                    mUsersDbRef.child(placeid)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {}
                            override fun onDataChange(p0: DataSnapshot) {
                                val fullName = model.firstName + model.lastName
                                holder.user_fullName.text = fullName
                                Picasso.get().load(model.ProfileImage)
                                    .into(holder.user_profileImage)
                                val email = model.email
                                holder.user_email.text = email
                            }
                        })
                    holder.itemView.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(p0: View?) {
                            val UID = getRef(position).key
                            val profileIntent = Intent(this@display_list, portfolio::class.java)
                            if (userType.equals("Admin")) {
                                profileIntent.putExtra("Admin", userType)
                            }
                            profileIntent.putExtra("UID", UID)
                            startActivity(profileIntent)

                        }

                    })
                }

            }
        result_list.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

    }

    class allUsersHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var user_fullName = itemView.findViewById<TextView>(R.id.s_profile_name)
        internal var user_profileImage = itemView.findViewById<ImageView>(R.id.s_profile_image)
        internal var user_email = itemView.findViewById<TextView>(R.id.s_profile_email)
    }

    fun setSpinner() {
        val list = mutableListOf(
            "Select your state",
            "New York",
            "Connecticut",
            "California",
            "Florida",
            "Michigan",
            "Georgia",
            "Texas",
            "Virgina",
            "Massachusetts",
            "Arizona",
            "Vermont",
            "Maryland",
            "North Carolina",
            "South Carolina",
            "Arkansas"
        )


        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_dropdown_item, list
        ) {
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: TextView =
                    super.getDropDownView(position, convertView, parent) as TextView
                view.setTypeface(view.typeface, Typeface.BOLD)

                if (position == spinner_state.selectedItemPosition && position != 0) {
                    view.background = ColorDrawable(Color.parseColor("#F7E7CE"))
                    view.setTextColor(Color.parseColor("#333399"))
                }

                if (position == 0) {
                    view.setTextColor(Color.LTGRAY)
                }

                return view
            }

            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }

        spinner_state.adapter = adapter

        spinner_state.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    stateSelected = parent.getItemAtPosition(position).toString()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // callback
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        if (currentUserID.isNullOrEmpty() || currentUserID.toString().isBlank()) {
            inflater.inflate(R.menu.login_menu, menu)
        } else {
            inflater.inflate(R.menu.logout_menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.login -> {
                startActivity(MainActivity.getLaunchIntent(this))
            }

            android.R.id.home -> {
                startActivity(search.getLaunchIntent(this))
            }

            R.id.logout -> {
                startActivity(MainActivity.getLaunchIntent(this))
                FirebaseAuth.getInstance().signOut()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, display_list::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }
}
