package com.example.smarttaskmanager.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.smarttaskmanager.R
import com.example.smarttaskmanager.database.DBHelper

class LoginActivity : AppCompatActivity() {

    lateinit var db: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = DBHelper(this)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val signupLink = findViewById<TextView>(R.id.signupLink)

        loginBtn.setOnClickListener {
            val user = username.text.toString().trim()
            val pass = password.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (db.checkUser(user, pass)) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                val prefs = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putString("username", user).apply()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show()
            }
        }

        signupLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }
}