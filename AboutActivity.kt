package com.example.smarttaskmanager.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.smarttaskmanager.R

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val shareAppBtn = findViewById<Button>(R.id.shareAppBtn)

        shareAppBtn.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Smart Task Manager")
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hey! Check out Smart Task Manager. It's a great app to organize your life with reminders, voice notes, and more."
            )
            startActivity(Intent.createChooser(shareIntent, "Share App via"))
        }
    }
}
