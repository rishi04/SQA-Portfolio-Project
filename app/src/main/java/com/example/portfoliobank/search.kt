package com.example.portfoliobank

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.activity_search.search_can_name
import kotlinx.android.synthetic.main.create_account.spinner_state

class search : AppCompatActivity() {

    private var stateSelected: String? = null
    private var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        supportActionBar?.setTitle(R.string.app_name)

        setSpinner()
        search_button.setOnClickListener {
            name = search_can_name.text.toString()

            val searchIntent = Intent(this, display_list::class.java)

            if (!TextUtils.isEmpty(name) || !name.isNullOrEmpty()) {
                searchIntent.putExtra("name", name)
                startActivity(searchIntent)
            } else if (stateSelected != null) {
                searchIntent.putExtra("state", stateSelected)
                startActivity(searchIntent)
            } else {
                Snackbar.make(it, "Please select either a name or state", Snackbar.LENGTH_LONG)
                    .show()
            }

        }
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
        inflater.inflate(R.menu.login_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.login -> {
                startActivity(MainActivity.getLaunchIntent(this))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, search::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

}
