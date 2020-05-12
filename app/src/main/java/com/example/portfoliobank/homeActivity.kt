package com.example.portfoliobank

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

import kotlinx.android.synthetic.main.activity_home.*
import java.util.*
import kotlin.collections.HashMap

class homeActivity : AppCompatActivity() {
    private val TAG = "editProfileActivity"
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID: String
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var about_myself: String
    private lateinit var univ_name: String
    private lateinit var univ_start_date: String
    private lateinit var univ_end_date: String
    private lateinit var univ_deg: String
    private lateinit var univ_major: String
    private lateinit var usr_proj_name: String
    private lateinit var usr_proj_skills: String
    private lateinit var usr_proj_desc: String
    private lateinit var mUsersDbRef: DatabaseReference
    private lateinit var mUserStorageRefs: StorageReference
    private lateinit var valueEventListener: ValueEventListener
    var ImageUri: Uri? = null
    var mStartDateListener: DatePickerDialog.OnDateSetListener? = null
    var mEndDateListener: DatePickerDialog.OnDateSetListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.setTitle(R.string.profile)
        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser!!.uid
        mUsersDbRef = mDatabase.reference.child("Users").child(currentUserID)
        mUserStorageRefs = FirebaseStorage.getInstance().reference.child("UserProfileImages")

        // Allows the user to edit his/her details and is updated to Firebase.
        fetchUserDetailsFromFirebase()

        edit_profile_username_button.setOnClickListener {
            addViewsToEditUsername()
        }

        edit_profile_about_myself_button.setOnClickListener {
            addViewsToEditAboutMyself()
        }

        edit_edu_button.setOnClickListener {
            addViewToEditEducation()
        }
        edit_profile_image.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this)
        }

        edit_proj_button.setOnClickListener {
            addViewtoEditProjectDetails()
        }


        view_port_button.setOnClickListener {
            startActivity(portfolio.getLaunchIntent(this))
        }

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
                    edit_profile_username_2.setText(username)

                    val email = dataSnapshot.child("email").value.toString()
                    edit_profile_email_2.setText(email)

                    val profile_image = dataSnapshot.child("ProfileImage").value.toString()
                    Picasso.get()
                        .load(profile_image)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_profile_name)
                        .fit()
                        .into(edit_profile_image)

                    if (!dataSnapshot.child("about_myself").exists()) {
                        about_myself = resources.getString(R.string.about_myself)
                        edit_profile_about_myself_2.setText(R.string.describe_yourself)
                    } else {
                        about_myself = dataSnapshot.child("about_myself").value.toString()
                        edit_profile_about_myself_2.setText(about_myself)
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
                        edit_univ.setText(R.string.univ_name)
                        univ_start_date = resources.getString(R.string.univ_start_date)
                        edit_univ_start_date.setText(R.string.univ_start_date)
                        univ_end_date = resources.getString(R.string.univ_end_date)
                        edit_univ_end_date.setText(R.string.univ_end_date)
                        univ_deg = resources.getString(R.string.univ_degree)
                        edit_univ_degree.setText(R.string.univ_degree)
                        univ_major = resources.getString(R.string.univ_major)
                        edit_univ_major.setText(R.string.univ_major)

                        usr_proj_name = resources.getString(R.string.proj_name)
                        proj_name.setText(R.string.proj_name)
                        usr_proj_skills = resources.getString(R.string.skills)
                        proj_skills.setText(R.string.skills)
                        usr_proj_desc = resources.getString(R.string.proj_desc)
                        proj_desc.setText(R.string.proj_desc)

                    } else {
                        univ_name = dataSnapshot.child("univ_name").value.toString()
                        edit_univ.setText(univ_name)
                        univ_start_date = dataSnapshot.child("univ_start_date").value.toString()
                        edit_univ_start_date.setText(univ_start_date)
                        univ_end_date = dataSnapshot.child("univ_end_date").value.toString()
                        edit_univ_end_date.setText(univ_end_date)
                        univ_deg = dataSnapshot.child("univ_deg").value.toString()
                        edit_univ_degree.setText(univ_deg)
                        univ_major = dataSnapshot.child("univ_major").value.toString()
                        edit_univ_major.setText(univ_major)


                        usr_proj_name = dataSnapshot.child("proj_name").value.toString()
                        proj_name.setText(usr_proj_name)
                        usr_proj_skills = dataSnapshot.child("proj_skills").value.toString()
                        proj_skills.setText(usr_proj_skills)
                        usr_proj_desc = dataSnapshot.child("proj_desc").value.toString()
                        proj_desc.setText(usr_proj_desc)
                    }
                }
            }

        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                ImageUri = result.uri
                Picasso.get()
                    .load(ImageUri)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_profile_name)
                    .fit()
                    .into(edit_profile_image)
                uploadProfileImagetoFirebase(ImageUri)
            }
        }


    }


    fun uploadProfileImagetoFirebase(resultUri: Uri?) {
        if (resultUri == null) {
            Log.v(TAG, "post_it_image")
            val snackbar = Snackbar
                .make(relative_layout_head, "Please select an image", Snackbar.LENGTH_LONG)
                .setAction("OK", object : View.OnClickListener {
                    override fun onClick(view: View) {}
                })
            snackbar.setActionTextColor(Color.WHITE)
            snackbar.show()
        } else {
            val filePath: StorageReference =
                mUserStorageRefs.child(UUID.randomUUID().toString() + ".jpg")
            filePath.putFile(ImageUri!!).addOnSuccessListener {
                Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")
                filePath.downloadUrl.addOnSuccessListener {
                    mUsersDbRef.child("ProfileImage").setValue(it.toString())
                }
            }
        }


    }

    private fun addViewsToEditAboutMyself() {

        edit_profile_about_myself_2.visibility = View.INVISIBLE

        val edit_about_myself = EditText(this)
        edit_about_myself.id = ViewCompat.generateViewId()
        edit_about_myself.setBackgroundResource(R.drawable.text_bckgnd)
        edit_about_myself.setHint(about_myself)
        val layoutParams1 = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams1.addRule(RelativeLayout.BELOW, edit_profile_about_myself_2.id)
        layoutParams1.addRule(RelativeLayout.ALIGN_LEFT, edit_profile_about_myself_2.id)
        edit_about_myself.setPadding(20, 40, 20, 20)
        edit_profile_myself_layout.addView(edit_about_myself, layoutParams1)


        // Dynamically created a SAVE button to save the user's About myself value.
        val save_button = Button(this)
        save_button.id = ViewCompat.generateViewId()
        save_button.setText(R.string.save_text)
        val layoutParams2 = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams2.addRule(RelativeLayout.BELOW, edit_about_myself.id)
        layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, edit_about_myself.id)
        save_button.setPadding(20, 20, 20, 20)
        edit_profile_myself_layout.addView(save_button, layoutParams2)

        //CANCEL button to cancel the dynamically created views
        val cancel_button = Button(this)
        cancel_button.setText(R.string.cancel_text)
        val layoutParams3 = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams3.addRule(RelativeLayout.BELOW, edit_about_myself.id)
        layoutParams3.addRule(RelativeLayout.START_OF, save_button.id)
        cancel_button.setPadding(20, 20, 20, 20)
        edit_profile_myself_layout.addView(cancel_button, layoutParams3)

        cancel_button.setOnClickListener {
            edit_profile_about_myself_2.visibility = View.VISIBLE
            edit_profile_myself_layout.removeView(save_button)
            edit_profile_myself_layout.removeView(edit_about_myself)
            edit_profile_myself_layout.removeView(cancel_button)
        }

        // SAVE the edited About_myself value in Firebase Database.
        save_button.setOnClickListener {

            val about_myself = edit_about_myself.text.toString()
            if (TextUtils.isEmpty(about_myself)) {
                Toast.makeText(
                    this,
                    "About Myself field cannot be saved as blank",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val userinfo_updates_hashMap = HashMap<String, Any>()
                userinfo_updates_hashMap.put("about_myself", about_myself)

                mUsersDbRef.updateChildren(userinfo_updates_hashMap)
                    .addOnSuccessListener {
                        edit_profile_about_myself_2.setText(about_myself)
                        edit_profile_about_myself_2.visibility = View.VISIBLE
                        edit_profile_myself_layout.removeView(save_button)
                        edit_profile_myself_layout.removeView(edit_about_myself)
                        edit_profile_myself_layout.removeView(cancel_button)
                    }
                    .addOnFailureListener {
                        Log.v(TAG, it.printStackTrace().toString())
                    }
            }
        }
    }

    fun startDatePicker() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(
            this, AlertDialog.THEME_HOLO_DARK, mStartDateListener,
            year, month, day
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    fun endDatePicker() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(
            this, AlertDialog.THEME_HOLO_DARK, mEndDateListener,
            year, month, day
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    fun addViewToEditEducation() {

        edit_univ.visibility = View.INVISIBLE
        edit_univ_degree.visibility = View.INVISIBLE
        edit_univ_major.visibility = View.INVISIBLE

        // Edit university name
        val edit_university_name = EditText(this)
        edit_university_name.id = ViewCompat.generateViewId()
        edit_university_name.setHint(univ_name)
        edit_university_name.setBackgroundResource(R.drawable.text_bckgnd)
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.addRule(RelativeLayout.BELOW, edit_edu_1.id)
        edit_university_name.setPadding(20, 40, 20, 20)
        edit_edu_layout.addView(edit_university_name, layoutParams)

        //University Degree
        val edit_university_degree = EditText(this)
        edit_university_degree.id = ViewCompat.generateViewId()
        edit_university_degree.setHint(univ_deg)
        edit_university_degree.setBackgroundResource(R.drawable.text_bckgnd)
        val layoutParams5 = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams5.addRule(RelativeLayout.BELOW, edit_university_name.id)
        edit_university_degree.setPadding(20, 40, 20, 20)
        edit_edu_layout.addView(edit_university_degree, layoutParams5)


        //University Major
        val edit_university_major = EditText(this)
        edit_university_major.id = ViewCompat.generateViewId()
        edit_university_major.setHint(univ_major)
        edit_university_major.setBackgroundResource(R.drawable.text_bckgnd)
        val layoutParams6 = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams6.addRule(RelativeLayout.BELOW, edit_university_degree.id)
        edit_university_major.setPadding(20, 40, 20, 20)
        edit_edu_layout.addView(edit_university_major, layoutParams6)

        // Add save button
        val save_univ_button = Button(this)
        save_univ_button.id = ViewCompat.generateViewId()
        save_univ_button.setText(R.string.save_text)
        val layoutParams4 = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams4.addRule(RelativeLayout.BELOW, edit_univ_end_date.id)
        layoutParams4.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, edit_univ_end_date.id)
        save_univ_button.setPadding(20, 20, 20, 20)
        edit_edu_layout.addView(save_univ_button, layoutParams4)

        // Add Cancel button
        val cancel_button = Button(this)
        cancel_button.setText(R.string.cancel_text)
        val layoutParams3 = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams3.addRule(RelativeLayout.BELOW, edit_univ_end_date.id)
        layoutParams3.addRule(RelativeLayout.START_OF, save_univ_button.id)
        cancel_button.setPadding(20, 20, 20, 20)
        edit_edu_layout.addView(cancel_button, layoutParams3)

        cancel_button.setOnClickListener {
            edit_univ.visibility = View.VISIBLE
            edit_univ_major.visibility = View.VISIBLE
            edit_univ_degree.visibility = View.VISIBLE
            edit_edu_layout.removeView(save_univ_button)
            edit_edu_layout.removeView(edit_university_name)
            edit_edu_layout.removeView(cancel_button)
            edit_edu_layout.removeView(edit_university_degree)
            edit_edu_layout.removeView(edit_university_major)
        }

        edit_univ_start_date.setOnClickListener {
            startDatePicker()
            mStartDateListener =
                DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                    val date = "" + month + "/" + day + "/" + year
                    edit_univ_start_date.setText(date)
                }
        }


        edit_univ_end_date.setOnClickListener {
            endDatePicker()
            mEndDateListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                val date = "" + month + "/" + day + "/" + year
                edit_univ_end_date.setText(date)
            }
        }


        // SAVE the edited University values in Firebase Database.
        save_univ_button.setOnClickListener {
            var edited_univ_name = edit_university_name.text.toString()
            val edited_start_date = edit_univ_start_date.text.toString()
            val edited_end_date = edit_univ_end_date.text.toString()
            var edited_deg = edit_university_degree.text.toString()
            var edited_major = edit_university_major.text.toString()


            /*if (edited_start_date.compareTo(edited_end_date) > 0) {
                Toast.makeText(this, "Start date must be less than end date", Toast.LENGTH_SHORT)
                    .show()
            } else*/
            if (TextUtils.isEmpty(univ_name) && TextUtils.isEmpty(edited_univ_name)) {
                Toast.makeText(this, "University name cannot be blank", Toast.LENGTH_SHORT).show()
            } else if (TextUtils.isEmpty(univ_deg) && TextUtils.isEmpty(edited_deg)) {
                Toast.makeText(this, "Degree name cannot be blank", Toast.LENGTH_SHORT).show()
            } else if (TextUtils.isEmpty(univ_major) && TextUtils.isEmpty(edited_major)) {
                Toast.makeText(this, "Major cannot be blank", Toast.LENGTH_SHORT).show()
            } else {
                if (TextUtils.isEmpty(edited_univ_name)) {
                    Log.v("Home-3", "hi")
                    edited_univ_name = univ_name
                }

                if (TextUtils.isEmpty(edited_deg)) {
                    edited_deg = univ_deg
                }

                if (TextUtils.isEmpty(edited_major)) {
                    edited_major = univ_major
                }

                /*edit_univ.setText(edited_univ_name)
                edit_univ_degree.setText(edited_deg)
                edit_univ_major.setText(edited_major)*/

                val userinfo_updates_hashMap = HashMap<String, Any>()
                userinfo_updates_hashMap.put("univ_name", edited_univ_name)
                userinfo_updates_hashMap.put("univ_start_date", edited_start_date)
                userinfo_updates_hashMap.put("univ_end_date", edited_end_date)
                userinfo_updates_hashMap.put("univ_major", edited_major)
                userinfo_updates_hashMap.put("univ_deg", edited_deg)
                mUsersDbRef.updateChildren(userinfo_updates_hashMap)
                    .addOnSuccessListener {
                        edit_univ.setText(edited_univ_name)
                        edit_univ.visibility = View.VISIBLE
                        edit_univ_degree.setText(edited_deg)
                        edit_univ_degree.visibility = View.VISIBLE
                        edit_univ_major.setText(edited_major)
                        edit_univ_major.visibility = View.VISIBLE
                        edit_univ_start_date.setText(edited_start_date)
                        edit_univ_end_date.setText(edited_end_date)
                        edit_edu_layout.removeView(save_univ_button)
                        edit_edu_layout.removeView(edit_university_name)
                        edit_edu_layout.removeView(cancel_button)
                        edit_edu_layout.removeView(edit_university_degree)
                        edit_edu_layout.removeView(edit_university_major)
                    }
                    .addOnFailureListener {
                        Log.v(TAG, it.printStackTrace().toString())
                    }
            }
        }
    }


    fun addViewtoEditProjectDetails() {


        proj_name.visibility = View.INVISIBLE
        proj_skills.visibility = View.INVISIBLE
        proj_desc.visibility = View.INVISIBLE

        // Edit project name
        val edit_proj_name = EditText(this)
        edit_proj_name.id = ViewCompat.generateViewId()
        edit_proj_name.setHint(usr_proj_name)
        edit_proj_name.setBackgroundResource(R.drawable.text_bckgnd)
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.addRule(RelativeLayout.BELOW, proj_details.id)
        edit_proj_name.setPadding(20, 40, 20, 20)
        edit_proj_layout.addView(edit_proj_name, layoutParams)


        //Project skills
        val edit_proj_skills = EditText(this)
        edit_proj_skills.id = ViewCompat.generateViewId()
        edit_proj_skills.setHint(usr_proj_skills)
        edit_proj_skills.setBackgroundResource(R.drawable.text_bckgnd)
        val layoutParams5 = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams5.addRule(RelativeLayout.BELOW, edit_proj_name.id)
        edit_proj_skills.setPadding(20, 40, 20, 20)
        edit_proj_layout.addView(edit_proj_skills, layoutParams5)


        //Project desc
        val edit_proj_desc = EditText(this)
        edit_proj_desc.id = ViewCompat.generateViewId()
        edit_proj_desc.setHint(usr_proj_desc)
        edit_proj_desc.setBackgroundResource(R.drawable.text_bckgnd)
        val layoutParams6 = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams6.addRule(RelativeLayout.BELOW, edit_proj_skills.id)
        edit_proj_desc.setPadding(20, 40, 20, 20)
        edit_proj_layout.addView(edit_proj_desc, layoutParams6)

        // Add save button
        val save_project_button = Button(this)
        save_project_button.id = ViewCompat.generateViewId()
        save_project_button.setText(R.string.save_text)
        val layoutParams7 = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams7.addRule(RelativeLayout.BELOW, edit_proj_desc.id)
        layoutParams7.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, edit_proj_desc.id)
        save_project_button.setPadding(20, 20, 20, 20)
        edit_proj_layout.addView(save_project_button, layoutParams7)


        // Add Cancel button
        val cancel_button = Button(this)
        cancel_button.setText(R.string.cancel_text)
        val layoutParams3 = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams3.addRule(RelativeLayout.BELOW, edit_proj_desc.id)
        layoutParams3.addRule(RelativeLayout.START_OF, save_project_button.id)
        cancel_button.setPadding(20, 20, 20, 20)
        edit_proj_layout.addView(cancel_button, layoutParams3)

        cancel_button.setOnClickListener {
            proj_name.visibility = View.VISIBLE
            proj_skills.visibility = View.VISIBLE
            proj_desc.visibility = View.VISIBLE
            edit_proj_layout.removeView(save_project_button)
            edit_proj_layout.removeView(edit_proj_name)
            edit_proj_layout.removeView(cancel_button)
            edit_proj_layout.removeView(edit_proj_desc)
            edit_proj_layout.removeView(edit_proj_skills)
        }

        // SAVE the edited Username value in Firebase Database.
        save_project_button.setOnClickListener {
            var edited_proj_name = edit_proj_name.text.toString()
            var edited_proj_skills = edit_proj_skills.text.toString()
            var edited_proj_desc = edit_proj_desc.text.toString()

            if (TextUtils.isEmpty(usr_proj_name) && TextUtils.isEmpty(edited_proj_name)) {
                Toast.makeText(this, "Project name cannot be blank", Toast.LENGTH_SHORT).show()
            } else if (TextUtils.isEmpty(usr_proj_desc) && TextUtils.isEmpty(edited_proj_desc)) {
                Toast.makeText(this, "Project degree cannot be blank", Toast.LENGTH_SHORT).show()
            } else if (TextUtils.isEmpty(usr_proj_skills) && TextUtils.isEmpty(edited_proj_skills)) {
                Toast.makeText(this, "Project skills cannot be blank", Toast.LENGTH_SHORT).show()
            } else {
                if (TextUtils.isEmpty(edited_proj_name)) {
                    edited_proj_name = usr_proj_name
                }

                if (TextUtils.isEmpty(edited_proj_skills)) {
                    edited_proj_skills = usr_proj_skills
                }

                if (TextUtils.isEmpty(edited_proj_desc)) {
                    edited_proj_desc = usr_proj_desc
                }

                proj_name.setText(edited_proj_name)
                proj_skills.setText(edited_proj_skills)
                proj_desc.setText(edited_proj_desc)

                val userinfo_updates_hashMap = HashMap<String, Any>()
                userinfo_updates_hashMap.put("proj_name", edited_proj_name)
                userinfo_updates_hashMap.put("proj_skills", edited_proj_skills)
                userinfo_updates_hashMap.put("proj_desc", edited_proj_desc)
                mUsersDbRef.updateChildren(userinfo_updates_hashMap)
                    .addOnSuccessListener {

                        proj_name.visibility = View.VISIBLE
                        proj_skills.visibility = View.VISIBLE
                        proj_desc.visibility = View.VISIBLE

                        edit_proj_layout.removeView(save_project_button)
                        edit_proj_layout.removeView(edit_proj_name)
                        edit_proj_layout.removeView(cancel_button)
                        edit_proj_layout.removeView(edit_proj_desc)
                        edit_proj_layout.removeView(edit_proj_skills)
                    }
                    .addOnFailureListener {
                        Log.v(TAG, it.printStackTrace().toString())
                    }
            }
        }
    }

    // Added multiple views using Dynamic layout to edit Username
    fun addViewsToEditUsername() {
        edit_profile_username_2.visibility = View.INVISIBLE

        // Edit first name
        val edit_first_name = EditText(this)
        edit_first_name.id = ViewCompat.generateViewId()
        edit_first_name.setHint(firstName)
        edit_first_name.setBackgroundResource(R.drawable.text_bckgnd)
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.addRule(RelativeLayout.BELOW, edit_profile_username_1.id)
        edit_first_name.setPadding(20, 40, 20, 20)
        edit_profile_relative_layout.addView(edit_first_name, layoutParams)

        // Edit last name
        val edit_last_name = EditText(this)
        edit_last_name.id = ViewCompat.generateViewId()
        edit_last_name.setBackgroundResource(R.drawable.text_bckgnd)
        edit_last_name.setHint(lastName)
        val layoutParams1 = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams1.addRule(RelativeLayout.BELOW, edit_first_name.id)
        edit_last_name.setPadding(20, 40, 20, 20)
        edit_profile_relative_layout.addView(edit_last_name, layoutParams1)

        // Add save button
        val save_button = Button(this)
        save_button.id = ViewCompat.generateViewId()
        save_button.setText(R.string.save_text)
        val layoutParams2 = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams2.addRule(RelativeLayout.BELOW, edit_last_name.id)
        layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, edit_last_name.id)
        save_button.setPadding(20, 20, 20, 20)
        edit_profile_relative_layout.addView(save_button, layoutParams2)

        // Add Cancel button
        val cancel_button = Button(this)
        cancel_button.setText(R.string.cancel_text)
        val layoutParams3 = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams3.addRule(RelativeLayout.BELOW, edit_last_name.id)
        layoutParams3.addRule(RelativeLayout.START_OF, save_button.id)
        cancel_button.setPadding(20, 20, 20, 20)
        edit_profile_relative_layout.addView(cancel_button, layoutParams3)

        cancel_button.setOnClickListener {
            edit_profile_username_2.visibility = View.VISIBLE
            edit_profile_relative_layout.removeView(save_button)
            edit_profile_relative_layout.removeView(edit_first_name)
            edit_profile_relative_layout.removeView(edit_last_name)
            edit_profile_relative_layout.removeView(cancel_button)
        }


        // SAVE the edited Username value in Firebase Database.
        save_button.setOnClickListener {
            if (TextUtils.isEmpty(edit_first_name.text.toString()) || TextUtils.isEmpty(
                    edit_last_name.text.toString()
                )
            ) {
                Toast.makeText(this, "User name cannot be blank", Toast.LENGTH_SHORT).show()
            } else {

                val edited_firstName = edit_first_name.text.toString()
                val edited_lastName = edit_last_name.text.toString()
                val userinfo_updates_hashMap = HashMap<String, Any>()
                userinfo_updates_hashMap.put("firstName", edited_firstName)
                userinfo_updates_hashMap.put("lastName", edited_lastName)

                val edited_username = edited_firstName + edited_lastName
                mUsersDbRef.updateChildren(userinfo_updates_hashMap)
                    .addOnSuccessListener {
                        edit_profile_username_2.setText(edited_username)
                        edit_profile_username_2.visibility = View.VISIBLE
                        edit_profile_relative_layout.removeView(save_button)
                        edit_profile_relative_layout.removeView(edit_first_name)
                        edit_profile_relative_layout.removeView(edit_last_name)
                        edit_profile_relative_layout.removeView(cancel_button)
                    }
                    .addOnFailureListener {
                        Log.v(TAG, it.printStackTrace().toString())
                    }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mUsersDbRef.removeEventListener(valueEventListener)
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, homeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.logout_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.logout -> {
                startActivity(MainActivity.getLaunchIntent(this))
                FirebaseAuth.getInstance().signOut()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
