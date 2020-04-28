package com.example.portfoliobank

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.create_account.*
import kotlinx.android.synthetic.main.create_account.view.*
import java.io.IOException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class createAccount_Activity : AppCompatActivity() {

    private val TAG = "createAccount_activity"
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUserStorageRefs: StorageReference
    private var resultUri: Uri? = null
    private var stateSelected: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_account)
        supportActionBar?.setTitle(R.string.create_account)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase.reference.child("Users")
        mUserStorageRefs = FirebaseStorage.getInstance().reference.child("UserProfileImages")
        mAuth = FirebaseAuth.getInstance()

        create_account_button.setOnClickListener {
            createUserAccount()
        }

        // Profile image - Crop the image.
        create_account_profile_button.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this)
        }

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
                // another interface callback
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                resultUri = result.uri
                create_account_profile_button.alpha = 0f
                Picasso.get()
                    .load(resultUri)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_profile_name)
                    .fit()
                    .into(create_account_profile_image)

            } else {
                Toast.makeText(
                    this@createAccount_Activity,
                    "Error while cropping image. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    // Upload the image to Firebase Storage
    fun uploadProfileImagetoFirebase(resultUri: Uri) {
        val filePath: StorageReference =
            mUserStorageRefs.child(UUID.randomUUID().toString() + ".jpg")
        filePath.putFile(resultUri).addOnSuccessListener {
            Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")
            filePath.downloadUrl.addOnSuccessListener {
                val currentUserDb = mDatabaseReference.child(mAuth.currentUser!!.uid)
                currentUserDb.child("ProfileImage").setValue(it.toString())
            }
        }

    }

    // To validate password
    fun isValidPassword(password: String): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)
        return matcher.matches()
    }

    //  Password validation condition checks - Should not be less than 8 characters. Both the passwords must match.
    fun createUserAccount() {
        val first_password = password.text.toString().trim()
        val second_password = sec_password.text.toString().trim()
        if (!TextUtils.isEmpty(first_name?.text.toString()) && !TextUtils.isEmpty(last_name?.text.toString())
            && !TextUtils.isEmpty(email?.text.toString()) && !TextUtils.isEmpty(first_password)
            && !TextUtils.isEmpty(second_password)
            && !TextUtils.isEmpty(stateSelected)
        ) {
            if ((first_password.length < 8 || second_password.length < 8) && (!isValidPassword(
                    first_password
                ) || !isValidPassword(second_password))
            ) {
                Toast.makeText(
                    this,
                    "Password length is too short. Please enter a minimum of 8 characters.",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (first_password.equals(second_password)) createUser()
            else {
                Toast.makeText(
                    this,
                    "Mis-match between passwords!. Please re-try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                "Please enter all details to create an account",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // To fetch the user data and save it in Firebase database.
    fun createUser() {
        if (resultUri == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show()
        } else {
            mAuth.createUserWithEmailAndPassword(
                email.text.toString().trim(),
                password.text.toString().trim()
            )
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        verifyEmail()
                        val currentUserDb = mDatabaseReference.child(mAuth.currentUser!!.uid)
                        currentUserDb.child("firstName").setValue(first_name.text.toString().trim())
                        currentUserDb.child("lastName").setValue(last_name.text.toString().trim())
                        currentUserDb.child("email").setValue(email.text.toString().trim())
                        currentUserDb.child("password").setValue(password.text.toString())
                        currentUserDb.child("state").setValue(stateSelected)
                        currentUserDb.child("userType").setValue("User")
                        currentUserDb.child("timestamp").setValue(ServerValue.TIMESTAMP)
                        uploadProfileImagetoFirebase(resultUri!!)
                        updateUserInfoAndUI()
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            this, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun verifyEmail() {
        val mUser = mAuth.currentUser
        Log.v(TAG, "email-1: " + mUser?.email)
        mUser!!.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.v(TAG, "email: " + mUser.email)
                    Toast.makeText(
                        this,
                        "Account vertification was successful.Verification email sent to " + mUser.email,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e("createAccount_activity", "sendEmailVerification", task.exception)
                    Toast.makeText(
                        this,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun updateUserInfoAndUI() {
        val intent = Intent(this, homeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    companion object {
        fun getLaunchIntent(from: Context) =
            Intent(from, createAccount_Activity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> startActivity(MainActivity.getLaunchIntent(this))
        }
        return super.onOptionsItemSelected(item)
    }
}