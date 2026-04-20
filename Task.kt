package com.example.smarttaskmanager.model

data class Task(
    var id: Int,
    var title: String,
    var description: String,
    var category: String,
    var date: String
)