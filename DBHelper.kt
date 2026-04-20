package com.example.smarttaskmanager.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, "TaskDB", null, 5) { // Upgraded version to 5

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE tasks(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, desc TEXT, category TEXT, date TEXT, status TEXT, image_path TEXT, audio_path TEXT, tag TEXT)"
        )
        db.execSQL(
            "CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE tasks ADD COLUMN tag TEXT DEFAULT 'Personal'")
            } catch (e: Exception) {
                // If it fails for whatever reason, wipe and recreate (fallback safe mechanism)
                db.execSQL("DROP TABLE IF EXISTS tasks")
                db.execSQL("DROP TABLE IF EXISTS users")
                onCreate(db)
            }
        } else {
            db.execSQL("DROP TABLE IF EXISTS tasks")
            db.execSQL("DROP TABLE IF EXISTS users")
            onCreate(db)
        }
    }

    // --- USER AUTHENTICATION ---

    fun insertUser(user: String, pass: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put("username", user)
        cv.put("password", pass)

        val result = db.insert("users", null, cv)
        return result != -1L
    }

    fun checkUser(user: String, pass: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE username=? AND password=?", arrayOf(user, pass))
        val count = cursor.count
        cursor.close()
        return count > 0
    }

    // --- TASK MANAGEMENT ---

    fun insertTask(title: String, desc: String, category: String, date: String, imagePath: String, audioPath: String, tag: String = "Personal") {
        val db = writableDatabase

        val cv = ContentValues()
        cv.put("title", title)
        cv.put("desc", desc)
        cv.put("category", category)
        cv.put("date", date)
        cv.put("status", "pending")
        cv.put("image_path", imagePath)
        cv.put("audio_path", audioPath)
        cv.put("tag", tag)

        db.insert("tasks", null, cv)
    }

    fun getTasks(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM tasks", null)

        if (cursor.moveToFirst()) {
            do {
                val map = HashMap<String, String>()
                map["id"] = cursor.getString(0) ?: ""
                map["title"] = cursor.getString(1) ?: ""
                map["desc"] = cursor.getString(2) ?: ""
                map["category"] = cursor.getString(3) ?: ""
                map["date"] = cursor.getString(4) ?: ""
                map["status"] = cursor.getString(5) ?: ""
                map["image_path"] = cursor.getString(6) ?: ""
                map["audio_path"] = cursor.getString(7) ?: ""
                map["tag"] = if (cursor.columnCount > 8) cursor.getString(8) ?: "Personal" else "Personal"
                list.add(map)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    fun updateStatus(id: String, newStatus: String) {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put("status", newStatus)
        db.update("tasks", cv, "id=?", arrayOf(id))
    }

    fun clearCompletedTasks() {
        val db = writableDatabase
        db.delete("tasks", "status=?", arrayOf("done"))
    }

    fun deleteTask(id: String) {
        val db = writableDatabase
        db.delete("tasks", "id=?", arrayOf(id))
    }

    fun updateTask(id: String, title: String, desc: String, category: String, date: String, imagePath: String, audioPath: String, tag: String = "Personal") {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put("title", title)
        cv.put("desc", desc)
        cv.put("category", category)
        cv.put("date", date)
        cv.put("image_path", imagePath)
        cv.put("audio_path", audioPath)
        cv.put("tag", tag)
        db.update("tasks", cv, "id=?", arrayOf(id))
    }
}