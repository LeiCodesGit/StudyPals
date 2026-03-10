package com.example.studypals

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : AppCompatActivity() {

    private lateinit var rvUserAdmin: RecyclerView
    private lateinit var adapter: UserAdminAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // DEBUG: Check your UID in Logcat
        val currentUid = auth.currentUser?.uid
        Log.d("AdminActivity", "Your current UID: $currentUid")

        rvUserAdmin = findViewById(R.id.rvUserAdmin)
        rvUserAdmin.layoutManager = LinearLayoutManager(this)
        
        adapter = UserAdminAdapter(userList)
        rvUserAdmin.adapter = adapter

        fetchUsers()
    }

    private fun fetchUsers() {
        db.collection("users")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Admin Error: ${error.message}", Toast.LENGTH_LONG).show()
                    Log.e("AdminActivity", "Firestore Error: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (value != null) {
                    userList.clear()
                    for (doc in value) {
                        val user = doc.toObject(User::class.java)
                        if (user != null) {
                            val userWithId = user.copy(uid = doc.id)
                            userList.add(userWithId)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    
                    if (userList.isEmpty()) {
                        Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}
