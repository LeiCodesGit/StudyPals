package com.example.studypals

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class UserListActivity : AppCompatActivity() {

    private lateinit var rvUserList: RecyclerView
    private lateinit var etSearchEmail: EditText
    private lateinit var btnSearch: ImageButton
    private lateinit var llNoUser: LinearLayout

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var snapshotListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        rvUserList = findViewById(R.id.rvUserList)
        etSearchEmail = findViewById(R.id.etSearchEmail)
        btnSearch = findViewById(R.id.btnSearch)
        llNoUser = findViewById(R.id.llNoUser)

        rvUserList.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        btnSearch.setOnClickListener {
            val emailQuery = etSearchEmail.text.toString().trim()
            if (emailQuery.isNotEmpty()) {
                searchUserByEmail(emailQuery)
            } else {
                startActivity(Intent(this, ChatActivity::class.java))
            }
        }

        fetchMyConversations()
    }

    private fun fetchMyConversations() {
        val currentUserId = auth.currentUser?.uid ?: return
        snapshotListener?.remove()

        // Added error logging to help identify missing indices or permission issues
        snapshotListener = db.collection("friendships")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Log.e("UserListActivity", "Firestore Error: ${error.message}", error)
                    Toast.makeText(this, "Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                    showEmptyState(true)
                    return@addSnapshotListener
                }

                if (documents == null || documents.isEmpty) {
                    showEmptyState(true)
                    return@addSnapshotListener
                }

                val conversationsList = mutableListOf<Conversation>()
                val totalDocs = documents.size()
                var processedCount = 0

                for (doc in documents) {
                    val participants = doc.get("participants") as? List<String>
                    val friendId = participants?.find { it != currentUserId }

                    if (friendId == null) {
                        processedCount++
                        if (processedCount == totalDocs) updateConversationUI(conversationsList)
                        continue
                    }

                    val lastMsg = doc.getString("lastMessage") ?: "No messages yet"
                    val time = doc.getLong("timestamp") ?: 0L

                    db.collection("users").document(friendId).get()
                        .addOnSuccessListener { userDoc ->
                            val friendName = userDoc.getString("username") ?: "Unknown User"
                            synchronized(conversationsList) {
                                conversationsList.add(Conversation(friendId, friendName, lastMsg, time))
                            }
                        }
                        .addOnCompleteListener {
                            processedCount++
                            if (processedCount == totalDocs) updateConversationUI(conversationsList)
                        }
                }
            }
    }

    private fun updateConversationUI(list: List<Conversation>) {
        if (list.isEmpty()) {
            showEmptyState(true)
        } else {
            showEmptyState(false)
            val sortedList = list.sortedByDescending { it.timestamp }
            rvUserList.adapter = ConversationAdapter(sortedList) { convo ->
                openChat(convo.friendId, convo.friendName)
            }
        }
    }

    private fun searchUserByEmail(email: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.toObjects(User::class.java).filter { it.uid != currentUserId }
                if (users.isEmpty()) {
                    Toast.makeText(this, "No user found", Toast.LENGTH_SHORT).show()
                } else {
                    showEmptyState(false)
                    rvUserList.adapter = UserListAdapter(users) { selectedUser ->
                        openChat(selectedUser.uid, selectedUser.username)
                    }
                }
            }
    }

    private fun openChat(receiverId: String, receiverName: String) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("receiverId", receiverId)
            putExtra("receiverName", receiverName)
        }
        startActivity(intent)
    }

    private fun showEmptyState(isEmpty: Boolean) {
        rvUserList.visibility = if (isEmpty) View.GONE else View.VISIBLE
        llNoUser.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotListener?.remove()
    }
}
