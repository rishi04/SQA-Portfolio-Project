package com.example.portfoliobank

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.proj_desc
import kotlinx.android.synthetic.main.activity_home.proj_name
import kotlinx.android.synthetic.main.activity_home.proj_skills
import kotlinx.android.synthetic.main.activity_portfolio.*

class portfolio : AppCompatActivity() {

    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    private var currentUserID: String? = null
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var about_myself: String
    private lateinit var email: String
    private lateinit var univ_name: String
    private lateinit var univ_start_date: String
    private lateinit var univ_end_date: String
    private lateinit var univ_deg: String
    private lateinit var univ_major: String
    private lateinit var proj_name: String
    private lateinit var proj_skills: String
    private lateinit var proj_desc: String
    private lateinit var mUsersDbRef: DatabaseReference
    private lateinit var mUserStorageRefs: StorageReference
    private lateinit var valueEventListener: ValueEventListener
    private lateinit var UID: String
    private var userType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_portfolio)
        supportActionBar?.setTitle(R.string.complete_portfolio)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser?.uid


        UID = intent?.getStringExtra("UID").toString()
        if ((UID.isNotBlank() || !UID.isEmpty()) && UID.length > 4) {
            currentUserID = UID
            send_email_button.visibility = View.VISIBLE
        } else {
            send_email_button.visibility = View.GONE
        }

        userType = intent?.getStringExtra("Admin").toString()
        if (userType.equals("Admin")) {
            delete_button.visibility = View.VISIBLE
        } else {
            delete_button.visibility = View.GONE
        }
        mUsersDbRef = mDatabase.reference.child("Users").child(currentUserID!!)
        mUserStorageRefs = FirebaseStorage.getInstance().reference.child("UserProfileImages")

        fetchUserDetailsFromFirebase()

        send_email_button.setOnClickListener {
            val emailIntent = Intent(this, sendEmail::class.java)
            emailIntent.putExtra("UID", currentUserID)
            emailIntent.putExtra("recipient_email", email)
            startActivity(emailIntent)

        }

        delete_button.setOnClickListener { deleteUser() }

    }


    fun deleteUser() {
        mUsersDbRef.removeValue()
        Toast.makeText(this, "User has been removed from the database", Toast.LENGTH_SHORT).show()
        startActivity(display_list.getLaunchIntent(this))
    }

    private fun fetchUserDetailsFromFirebase() {
        valueEventListener = mUsersDbRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                throw p0.toException()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    firstName = dataSnapshot.child("firstName").value.toString()
                    lastName = dataSnapshot.child("lastName").value.toString()
                    val username = firstName + lastName
                    user_profile_name.setText(username)

                    email = dataSnapshot.child("email").value.toString()
                    user_email.setText(email)

                    val profile_image = dataSnapshot.child("ProfileImage").value.toString()
                    Picasso.get()
                        .load(profile_image)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_profile_name)
                        .fit()
                        .into(user_profile)

                    if (!dataSnapshot.child("about_myself").exists()) {
                        about_myself = resources.getString(R.string.about_myself)
                        user_about_self.setText(R.string.describe_yourself)
                    } else {
                        about_myself = dataSnapshot.child("about_myself").value.toString()
                        user_about_self.setText(about_myself)
                    }

                    if (!dataSnapshot.child("univ_name").exists() ||
                        !dataSnapshot.child("univ_start_date").exists() ||
                        !dataSnapshot.child("univ_end_date").exists() ||
                        !dataSnapshot.child("univ_deg").exists() ||
                        !dataSnapshot.child("univ_major").exists() ||
                        !dataSnapshot.child("proj_name").exists() ||
                        !dataSnapshot.child("proj_skills").exists() ||
                        !dataSnapshot.child("proj_desc").exists()
                    ) {
                        univ_name = resources.getString(R.string.enter_education)
                        usr_univ_name.setText(R.string.univ_name)
                        univ_start_date = resources.getString(R.string.univ_start_date)
                        from.setText(R.string.univ_start_date)
                        univ_end_date = resources.getString(R.string.univ_end_date)
                        to.setText(R.string.univ_end_date)
                        univ_deg = resources.getString(R.string.univ_degree)
                        usr_univ_deg.setText(R.string.univ_degree)
                        univ_major = resources.getString(R.string.univ_major)
                        usr_univ_major.setText(R.string.univ_major)

                        proj_name = resources.getString(R.string.proj_name)
                        usr_proj_name.setText(R.string.proj_name)
                        proj_skills = resources.getString(R.string.skills)
                        usr_proj_skills.setText(R.string.skills)
                        proj_desc = resources.getString(R.string.proj_desc)
                        usr_proj_desc.setText(R.string.proj_desc)

                    } else {
                        univ_name = dataSnapshot.child("univ_name").value.toString()
                        usr_univ_name.setText(univ_name)
                        univ_start_date = dataSnapshot.child("univ_start_date").value.toString()
                        from.setText(univ_start_date)
                        univ_end_date = dataSnapshot.child("univ_end_date").value.toString()
                        to.setText(univ_end_date)
                        univ_deg = dataSnapshot.child("univ_deg").value.toString()
                        usr_univ_deg.setText(univ_deg)
                        univ_major = dataSnapshot.child("univ_major").value.toString()
                        usr_univ_major.setText(univ_major)


                        proj_name = dataSnapshot.child("proj_name").value.toString()
                        usr_proj_name.setText(proj_name)
                        proj_skills = dataSnapshot.child("proj_skills").value.toString()
                        usr_proj_skills.setText(proj_skills)
                        proj_desc = dataSnapshot.child("proj_desc").value.toString()
                        usr_proj_desc.setText(proj_desc)
                    }
                }
            }

        })
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, portfolio::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> startActivity(display_list.getLaunchIntent(this))
        }
        return super.onOptionsItemSelected(item)
    }
}