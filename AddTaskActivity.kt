package com.example.smarttaskmanager.activities

import android.media.MediaRecorder
import android.media.MediaPlayer
import android.app.Activity
import android.net.Uri
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.smarttaskmanager.R
import com.example.smarttaskmanager.database.DBHelper

class AddTaskActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var db: DBHelper
    var taskId: String? = null
    var imageUri: Uri? = null

    var recorder: MediaRecorder? = null
    var player: MediaPlayer? = null
    lateinit var filePath: String

    var selectedYear = -1
    var selectedMonth = -1
    var selectedDay = -1
    var selectedHour = -1
    var selectedMinute = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        db = DBHelper(this)

        val title = findViewById<EditText>(R.id.title)
        val priority = findViewById<AutoCompleteTextView>(R.id.priority)
        val tagSpinner = findViewById<AutoCompleteTextView>(R.id.tag)
        val date = findViewById<EditText>(R.id.date)
        val time = findViewById<EditText>(R.id.time)
        val saveBtn = findViewById<Button>(R.id.saveBtn)
        val imageBtn = findViewById<Button>(R.id.imageBtn)
        val recordBtn = findViewById<Button>(R.id.recordBtn)
        val playBtn = findViewById<Button>(R.id.playBtn)
        val cameraBtn = findViewById<Button>(R.id.cameraBtn)

        imageView = findViewById(R.id.imageView)   // ✅ FIXED

        // 📸 CAMERA
        cameraBtn.setOnClickListener {

            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)

            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, 100)
            } else {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
            }
        }

        if (checkSelfPermission(android.Manifest.permission.CAMERA)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO), 101)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 103)
            }
        }

        // 📅 DATE PICKER FIX
        date.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            android.app.DatePickerDialog(this, { _, y, m, d ->
                selectedYear = y
                selectedMonth = m
                selectedDay = d
                date.setText("$d/${m + 1}/$y")
            }, year, month, day).show()
        }

        // ⏰ TIME PICKER
        time.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = calendar.get(java.util.Calendar.MINUTE)

            android.app.TimePickerDialog(this, { _, h, min ->
                selectedHour = h
                selectedMinute = min
                time.setText(String.format("%02d:%02d", h, min))
            }, hour, minute, false).show()
        }

        // 🖼️ GALLERY
        imageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        // 🎤 AUDIO
        filePath = filesDir.absolutePath + "/audio.3gp"
        var isRecording = false

        recordBtn.setOnClickListener {

            if (!isRecording) {
                if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Enable Audio Permission", Toast.LENGTH_SHORT).show()
                    requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 102)
                    return@setOnClickListener
                }
                recorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setOutputFile(filePath)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    prepare()
                    start()
                }

                recordBtn.text = "Stop Recording"
                Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show()
                isRecording = true

            } else {
                recorder?.stop()
                recorder?.release()
                recorder = null

                recordBtn.text = "Record Voice 🎤"
                Toast.makeText(this, "Recording Saved", Toast.LENGTH_SHORT).show()
                playBtn.isEnabled = true
                isRecording = false
            }
        }

        playBtn.setOnClickListener {
            try {
                player = MediaPlayer().apply {
                    setDataSource(filePath)
                    prepare()
                    start()
                }
                Toast.makeText(this, "Playing...", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this, "No recording found", Toast.LENGTH_SHORT).show()
            }
        }

        // 🎯 SPINNER
        val options = arrayOf("High", "Medium", "Low")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options)
        priority.setAdapter(adapter)

        val tagOptions = arrayOf("Personal", "Work", "School", "Home", "Other")
        val tagAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tagOptions)
        tagSpinner.setAdapter(tagAdapter)

        taskId = intent.getStringExtra("id")

        if (taskId != null) {
            title.setText(intent.getStringExtra("title"))
            date.setText(intent.getStringExtra("date"))
            
            val category = intent.getStringExtra("category")
            if (category != null) {
                priority.setText(category, false)
            }
            
            val imgPathStr = intent.getStringExtra("image_path")
            if (!imgPathStr.isNullOrEmpty()) {
                try {
                    imageUri = android.net.Uri.parse(imgPathStr)
                    imageView.setImageURI(imageUri)
                    imageView.visibility = android.view.View.VISIBLE
                } catch (e: Exception) {}
            }
            
            val audPathStr = intent.getStringExtra("audio_path")
            if (!audPathStr.isNullOrEmpty() && java.io.File(audPathStr).exists()) {
                filePath = audPathStr
                playBtn.isEnabled = true
            }
            
            val tagValue = intent.getStringExtra("tag") ?: "Personal"
            tagSpinner.setText(tagValue, false)

            saveBtn.text = "Update Task"
        }

        // 💾 SAVE TASK
        saveBtn.setOnClickListener {

            val t = title.text.toString().trim()
            val p = priority.text.toString()
            val dt = date.text.toString()

            if (t.isEmpty()) {
                Toast.makeText(this, "Enter Title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val imgPaths = imageUri?.toString() ?: ""
                val audPaths = if (java.io.File(filePath).exists()) filePath else ""
                val selectedTag = tagSpinner.text.toString()

                if (taskId != null) {
                    db.updateTask(taskId!!, t, "", p, dt, imgPaths, audPaths, selectedTag)
                    Toast.makeText(this, "Task Updated", Toast.LENGTH_SHORT).show()
                } else {
                    db.insertTask(t, "", p, dt, imgPaths, audPaths, selectedTag)
                    Toast.makeText(this, "Task Added", Toast.LENGTH_SHORT).show()
                    showNotification(t)
                    setReminder(t)
                }

                finish()

            } catch (e: Exception) {
                Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // 🔔 NOTIFICATION
    fun showNotification(title: String) {

        val channelId = "task_channel"

        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Task Notifications",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val builder = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setContentTitle("Task Added")
            .setContentText(title)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    // ⏰ REMINDER
    fun setReminder(title: String) {
        if (selectedYear == -1 || selectedHour == -1) {
            Toast.makeText(this, "Reminder skipped: No specific date/time chosen", Toast.LENGTH_SHORT).show()
            return
        }

        val calendar = java.util.Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0)
        
        val triggerTime = calendar.timeInMillis
        if (triggerTime < System.currentTimeMillis()) {
            Toast.makeText(this, "Reminder skipped: Time is in the past", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, com.example.smarttaskmanager.ReminderReceiver::class.java)
        intent.putExtra("title", title)

        val pendingIntent = android.app.PendingIntent.getBroadcast(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager

        var canScheduleExact = true
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            canScheduleExact = alarmManager.canScheduleExactAlarms()
        }

        if (canScheduleExact) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                alarmManager.set(
                    android.app.AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.set(
                android.app.AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }

        Toast.makeText(this, "Reminder Set Successfully", Toast.LENGTH_SHORT).show()
    }

    // 📸 RESULT HANDLER (FIXED)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            // Gallery
            if (requestCode == 1) {
                imageUri = data?.data
                imageView.setImageURI(imageUri)
                imageView.visibility = android.view.View.VISIBLE
            }

            // Camera
            if (requestCode == 100) {
                val image = data?.extras?.get("data") as? android.graphics.Bitmap

                if (image != null) {
                    imageView.setImageBitmap(image)
                    imageView.visibility = android.view.View.VISIBLE
                } else {
                    Toast.makeText(this, "Image capture failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}