package com.example.smarttaskmanager.activities

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smarttaskmanager.R
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("SmartTasksPrefs", Context.MODE_PRIVATE)

        val shakeSwitch = findViewById<SwitchCompat>(R.id.shakeSwitch)
        val notifSwitch = findViewById<SwitchCompat>(R.id.notifSwitch)
        val darkModeSwitch = findViewById<SwitchCompat>(R.id.darkModeSwitch)

        shakeSwitch.isChecked = prefs.getBoolean("shake_enabled", true)
        notifSwitch.isChecked = prefs.getBoolean("notifs_enabled", true)
        darkModeSwitch.isChecked = prefs.getBoolean("dark_mode", false)

        shakeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("shake_enabled", isChecked).apply()
            Toast.makeText(this, "Shake setting updated", Toast.LENGTH_SHORT).show()
        }

        notifSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifs_enabled", isChecked).apply()
            Toast.makeText(this, "Notification setting updated", Toast.LENGTH_SHORT).show()
        }
        
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}
