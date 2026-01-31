package com.example.studypals

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// AppCompatActivity is required to use XML layouts like login.xml
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This line tells the app to display your login.xml file
        setContentView(R.layout.login)
    }
}