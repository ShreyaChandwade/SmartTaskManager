package com.example.smarttaskmanager.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.hardware.SensorManager
import android.hardware.SensorEventListener
import androidx.appcompat.app.AppCompatActivity
import com.example.smarttaskmanager.R
import com.example.smarttaskmanager.database.DBHelper

class MainActivity : AppCompatActivity() {

    lateinit var db: DBHelper
    lateinit var listView: ListView
    lateinit var addBtn: View

    lateinit var sensorManager: SensorManager
    var accelerometer: android.hardware.Sensor? = null

    var lastUpdate: Long = 0
    var last_x = 0f
    var last_y = 0f
    var last_z = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val prefs = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        val username = prefs.getString("username", "User") ?: "User"

        supportActionBar?.title = "Smart Tasks"

        val headerTitle = findViewById<TextView>(R.id.headerTitle)
        headerTitle?.text = "Welcome, $username!"

        invalidateOptionsMenu()

        db = DBHelper(this)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)

        listView = findViewById(R.id.listView)
        addBtn = findViewById(R.id.addBtn)

        addBtn.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }

        loadTasks()

        // CLICK → MARK COMPLETE / UNCOMPLETE
        listView.setOnItemClickListener { _, _, position, _ ->
            val task = db.getTasks()[position]
            val id = task["id"]
            val currentStatus = task["status"]

            val newStatus = if (currentStatus == "done") "pending" else "done"

            db.updateStatus(id!!, newStatus)
            loadTasks()
        }
    }

    fun loadTasks() {

        val data = db.getTasks()
        
        val emptyState = findViewById<LinearLayout>(R.id.emptyStateLayout)
        if (data.isEmpty()) {
            listView.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            listView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }

        val adapter = object : ArrayAdapter<HashMap<String, String>>(
            this,
            R.layout.task_item,
            data
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

                val view = layoutInflater.inflate(R.layout.task_item, parent, false)

                val title = view.findViewById<TextView>(R.id.taskTitle)
                val dot = view.findViewById<View>(R.id.priorityDot)
                val menuBtn = view.findViewById<ImageButton>(R.id.menuBtn)

                val task = data[position]
                val id = task["id"]

// ✅ DEFINE FIRST
                val taskTitle = task["title"] ?: ""
                val priority = task["category"] ?: ""

// ✅ THEN USE
                val display = when (priority) {
                    "High" -> "🔴 $taskTitle"
                    "Medium" -> "🟡 $taskTitle"
                    "Low" -> "🟢 $taskTitle"
                    else -> taskTitle
                }

                title.text = display

// ✅ NOW THIS WORKS
                when (priority) {
                    "High" -> dot.setBackgroundColor(android.graphics.Color.RED)
                    "Medium" -> dot.setBackgroundColor(android.graphics.Color.YELLOW)
                    "Low" -> dot.setBackgroundColor(android.graphics.Color.GREEN)
                }

                // strike-through
                if (task["status"] == "done") {
                    title.paintFlags =
                        title.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    title.paintFlags =
                        title.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }

                val taskImageView = view.findViewById<ImageView>(R.id.taskImageView)
                val iconAudio = view.findViewById<TextView>(R.id.iconAudio)
                val taskTagView = view.findViewById<TextView>(R.id.taskTag)
                
                val imgPath = task["image_path"] ?: ""
                val audPath = task["audio_path"] ?: ""
                val currentTag = task["tag"] ?: "Personal"
                
                if (currentTag.isNotEmpty()) {
                    taskTagView.text = currentTag
                    taskTagView.visibility = View.VISIBLE
                }

                if (imgPath.isNotEmpty()) {
                    taskImageView.visibility = View.VISIBLE
                    try {
                        taskImageView.setImageURI(android.net.Uri.parse(imgPath))
                    } catch (e: Exception) {
                        taskImageView.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                } else {
                    taskImageView.visibility = View.GONE
                }

                iconAudio.visibility = if (audPath.isNotEmpty()) View.VISIBLE else View.GONE

                iconAudio.setOnClickListener {
                    try {
                        android.media.MediaPlayer().apply {
                            setDataSource(audPath)
                            prepare()
                            start()
                        }
                        Toast.makeText(this@MainActivity, "Playing...", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "No audio found", Toast.LENGTH_SHORT).show()
                    }
                }

                // 📌 POPUP MENU
                menuBtn.setOnClickListener { anchorView ->
                    val popup = PopupMenu(this@MainActivity, anchorView)
                    popup.menuInflater.inflate(R.menu.task_menu, popup.menu)
                    
                    popup.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.menu_edit -> {
                                val intent = Intent(this@MainActivity, AddTaskActivity::class.java).apply {
                                    putExtra("id", id)
                                    putExtra("title", task["title"])
                                    putExtra("category", task["category"])
                                    putExtra("date", task["date"])
                                    putExtra("image_path", task["image_path"])
                                    putExtra("audio_path", task["audio_path"])
                                    putExtra("tag", task["tag"])
                                }
                                startActivity(intent)
                            }
                            R.id.menu_delete -> {
                                db.deleteTask(id!!)
                                loadTasks()
                            }
                            R.id.menu_complete -> {
                                val newStatus = if (task["status"] == "done") "pending" else "done"
                                db.updateStatus(id!!, newStatus)
                                loadTasks()
                            }
                        }
                        true
                    }
                    popup.show()
                }

                return view
            }
        }

        listView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadTasks()

        accelerometer?.also {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorListener)
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {

        when (item.itemId) {
            
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }

            R.id.menu_calculator -> {
                startActivity(Intent(this, CalculatorActivity::class.java))
                return true
            }

            R.id.menu_contacts -> {
                startActivity(Intent(this, ContactsActivity::class.java))
                return true
            }

            R.id.menu_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }

            R.id.menu_logout -> {
                val prefs = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().clear().apply()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // SENSOR (SHAKE)
    val sensorListener = object : SensorEventListener {

        override fun onSensorChanged(event: android.hardware.SensorEvent?) {

            event?.let {

                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                val currentTime = System.currentTimeMillis()

                if ((currentTime - lastUpdate) > 200) {

                    val diffTime = currentTime - lastUpdate
                    lastUpdate = currentTime

                    val speed =
                        Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000

                    if (speed > 800) {
                        Toast.makeText(
                            applicationContext,
                            "Sweep! Completed Tasks Cleared 🧹",
                            Toast.LENGTH_SHORT
                        ).show()
                        db.clearCompletedTasks()
                        loadTasks()
                    }

                    last_x = x
                    last_y = y
                    last_z = z
                }
            }
        }

        override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
    }
}