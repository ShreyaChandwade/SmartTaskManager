package com.example.smarttaskmanager.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.smarttaskmanager.R

class CalculatorActivity : AppCompatActivity() {

    lateinit var display: TextView
    var current = ""
    var operator = ""
    var firstNumber = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "Calculator Opened", Toast.LENGTH_SHORT).show()

        setContentView(R.layout.calculator_activity)   // ✅ VERY IMPORTANT

        display = findViewById(R.id.display)

        val grid = findViewById<GridLayout>(R.id.gridLayout)

        // ✅ SAFE LOOP (NO CRASH)
        for (i in 0 until grid.childCount) {

            val view = grid.getChildAt(i)

            if (view is Button) {

                view.setOnClickListener {

                    val text = view.text.toString()

                    when (text) {

                        "C" -> {
                            current = ""
                            operator = ""
                            firstNumber = 0.0
                            display.text = "0"
                        }

                        "+", "-", "*", "/" -> {

                            if (current.isNotEmpty()) {
                                firstNumber = current.toDouble()
                                operator = text
                                current = ""
                            }
                        }

                        "=" -> {

                            if (current.isNotEmpty() && operator.isNotEmpty()) {

                                val second = current.toDouble()

                                val result = when (operator) {
                                    "+" -> firstNumber + second
                                    "-" -> firstNumber - second
                                    "*" -> firstNumber * second
                                    "/" -> if (second != 0.0) firstNumber / second else 0.0
                                    else -> 0.0
                                }

                                display.text = result.toString()

                                // ✅ reset for next calculation
                                current = result.toString()
                                operator = ""
                            }
                        }

                        "." -> {
                            if (!current.contains(".")) {
                                current += "."
                                display.text = current
                            }
                        }

                        else -> {
                            current += text
                            display.text = current
                        }
                    }
                }
            }
        }
    }
}