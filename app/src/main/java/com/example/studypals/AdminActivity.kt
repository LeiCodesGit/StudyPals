package com.example.studypals

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin) // Links to your header layout

        val rvUserAdmin = findViewById<RecyclerView>(R.id.rvUserAdmin)

        // Your combined user list
        val userList = mutableListOf(
            User(uid = "1", username = "Sami", email = "sam@gmail.com"),
            User(uid = "2", username = "John Doe", email = "johndoe@example.com")
        )

        // Attaching the fixed adapter
        val adapter = UserAdminAdapter(userList)
        rvUserAdmin.layoutManager = LinearLayoutManager(this)
        rvUserAdmin.adapter = adapter
    }
}