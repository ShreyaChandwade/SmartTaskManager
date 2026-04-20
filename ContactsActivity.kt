package com.example.smarttaskmanager.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smarttaskmanager.R

class ContactsActivity : AppCompatActivity() {

    lateinit var listView: ListView
    val contactsList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        listView = findViewById(R.id.contactsListView)

        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 100)
        } else {
            loadContacts()
        }
        
        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, AddTaskActivity::class.java)
            // The purpose of this contact picker is to quickly create a task out of it
            intent.putExtra("title", "Call ${contactsList[position]}")
            startActivity(intent)
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadContacts() {
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )

        cursor?.let {
            while (it.moveToNext()) {
                val namePos = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                if (namePos >= 0) {
                    val name = it.getString(namePos)
                    if (!contactsList.contains(name)) {
                        contactsList.add(name)
                    }
                }
            }
            it.close()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contactsList)
        listView.adapter = adapter
    }
}
