package com.example.portfoliobank

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.login.*


//LOGIN ACTIVITY
class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private var userType: String? = null
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        admin_verify_login_button.visibility = View.GONE
        firebaseAuth = FirebaseAuth.getInstance()

        initializeForCreateAccount()
        login_button.setOnClickListener { loginWithEmailandPassword() }

        login_pin_text.visibility = View.GONE
        // Show/Hide password
        checkbox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
                if (isChecked) {
                    // hide password
                    checkbox.text = "Hide password"
                    login_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                } else {
                    // show password
                    checkbox.text = "Show password"
                    login_password.setTransformationMethod(PasswordTransformationMethod.getInstance())
                }
            }
        })


        admin_button.setOnClickListener {
            login_pin_text.visibility = View.VISIBLE
            create_account_button.visibility = View.GONE
            login_button.visibility = View.GONE
            checkbox.visibility = View.GONE
            admin_button.visibility = View.GONE
            admin_verify_login_button.visibility = View.VISIBLE

        }

        admin_verify_login_button.setOnClickListener {
            val admin_pin = login_pin.text.toString()
            if (admin_pin.equals("Admin@1230")) {
                userType = "Admin"
                loginWithEmailandPassword()
            } else {
                Snackbar.make(it, "Authentication failed.", Snackbar.LENGTH_LONG).show()
            }
        }

    }


    private fun initializeForCreateAccount() {
        create_account_button.setOnClickListener {
            startActivity(createAccount_Activity.getLaunchIntent(this))
        }

    }


    private fun loginWithEmailandPassword() {
        val email = login_email?.text.toString()
        val password = login_password?.text.toString()
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        if (userType == "Admin") {
                            val displayIntent = Intent(this, display_list::class.java)
                            displayIntent.putExtra("Admin", userType)
                            startActivity(displayIntent)
                            //startActivity(Intent(this, display_list::class.java))
                        } else {
                            startActivity(Intent(this, homeActivity::class.java))
                        }
                    } else {
                        Toast.makeText(
                            this, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            startActivity(homeActivity.getLaunchIntent(this))
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> startActivity(search.getLaunchIntent(this))
        }
        return super.onOptionsItemSelected(item)
    }
}
