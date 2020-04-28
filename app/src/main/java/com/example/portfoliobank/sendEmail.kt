package com.example.portfoliobank

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_send_email.*
import java.lang.Exception

class sendEmail : AppCompatActivity() {

    private lateinit var UID: String
    private lateinit var recipient_email: String
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    private var currentUserID: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_email)
        supportActionBar?.setTitle(R.string.send_message)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser?.uid

        UID = intent?.getStringExtra("UID").toString()
        recipient_email = intent?.getStringExtra("recipient_email").toString()

        if( (recipient_email.isNotBlank() || !recipient_email.isEmpty()) && recipient_email.length > 4 ){
            sender_email.setText(recipient_email)
        }

        send_button.setOnClickListener {
            val msgIntent = Intent(Intent.ACTION_SEND)

            msgIntent.data = Uri.parse("mailto:")
            msgIntent.type = "text/plain"
            msgIntent.putExtra(Intent.EXTRA_EMAIL, recipient_email)
            msgIntent.putExtra(Intent.EXTRA_SUBJECT, subject_id.text.toString())
            msgIntent.putExtra(Intent.EXTRA_TEXT,body_msg.text.toString())

            try{
                startActivity(Intent.createChooser(msgIntent,"Choose any Email service"))
            } catch (e:Exception){
                Snackbar.make(it, e.message.toString(),Snackbar.LENGTH_LONG).show()
            }

        }
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, sendEmail::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

   override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        Log.v("XXX","curr: " +currentUserID)
        if(currentUserID.isNullOrEmpty() || currentUserID.toString().isBlank()){
            inflater.inflate(R.menu.login_menu, menu)
        }else{
            inflater.inflate(R.menu.logout_menu, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                val profileIntent = Intent(this, portfolio::class.java)
                profileIntent.putExtra("UID", UID)
                startActivity(profileIntent)
            }

            R.id.logout -> {
                startActivity(MainActivity.getLaunchIntent(this))
                FirebaseAuth.getInstance().signOut()
            }

            R.id.login -> {
                startActivity(MainActivity.getLaunchIntent(this))
            }

        }
        return super.onOptionsItemSelected(item)
    }
}
