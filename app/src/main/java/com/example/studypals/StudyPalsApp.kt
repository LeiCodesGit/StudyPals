package com.example.studypals

import android.app.Application
import com.google.firebase.FirebaseApp

class StudyPalsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}