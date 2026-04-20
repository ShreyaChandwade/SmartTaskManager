package com.example.smarttaskmanager

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class SmartTaskApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val prefs = getSharedPreferences("SmartTasksPrefs", Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", false)
        
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
