package com.example.studypals

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AdminActivity : AppCompatActivity() {

    private lateinit var rvUserAdmin: RecyclerView
    private lateinit var adapter: UserAdminAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userList = mutableListOf<User>()
    private var usersListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val currentUid = auth.currentUser?.uid
        Log.d("AdminActivity", "Your current UID: $currentUid")

        rvUserAdmin = findViewById(R.id.rvUserAdmin)
        rvUserAdmin.layoutManager = LinearLayoutManager(this)

        adapter = UserAdminAdapter(userList)
        rvUserAdmin.adapter = adapter

        val btnLogoutAdmin = findViewById<ImageButton>(R.id.btnLogoutAdmin)
        btnLogoutAdmin.setOnClickListener {
            // FIX: Remove the listener BEFORE signing out to avoid PERMISSION_DENIED toast
            usersListener?.remove()
            auth.signOut()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        fetchUsers()
    }

    private fun fetchUsers() {
        // Store the listener registration so we can remove it later
        usersListener = db.collection("users")
            .addSnapshotListener { value, error ->
                // If we are logging out, auth.currentUser will be null
                if (auth.currentUser == null) return@addSnapshotListener

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

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup listener when activity is destroyed
        usersListener?.remove()
    }
}