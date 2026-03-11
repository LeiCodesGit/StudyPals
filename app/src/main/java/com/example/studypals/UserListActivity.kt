package com.example.studypals

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserListActivity : AppCompatActivity() {

    private lateinit var rvUserList: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        rvUserList = findViewById(R.id.rvUserList)
        rvUserList.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        fetchUsers()
    }

    private fun fetchUsers() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users").get()
            .addOnSuccessListener { documents ->
                val users = mutableListOf<User>()
                for (doc in documents) {
                    val user = doc.toObject(User::class.java)
                    // Don't show current user in the list
                    if (user.uid != currentUserId) {
                        users.add(user)
                    }
                }
                rvUserList.adapter = UserListAdapter(users) { selectedUser ->
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("receiverId", selectedUser.uid)
                    intent.putExtra("receiverName", selectedUser.username)
                    startActivity(intent)
                }
            }
    }
}
