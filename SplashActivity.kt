package com.example.smarttaskmanager.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.smarttaskmanager.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val root = findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as ViewGroup

        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            child.alpha = 0f
            child.translationY = 100f
            
            child.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(1200)
                .setStartDelay((i * 200).toLong())
                .setInterpolator(OvershootInterpolator())
                .start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2500)
    }
}