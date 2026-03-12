package com.example.studypals

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var tvChatTitle: TextView
    private lateinit var chatAdapter: ChatAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    private var currentUserName: String = "Anonymous"
    private var chatId: String? = null
    private var receiverId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat)

        rvChat = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        tvChatTitle = findViewById(R.id.tvChatTitle)
        val btnAddFriend = findViewById<ImageButton>(R.id.btnAddFriend)

        // Get extras for private chat
        receiverId = intent.getStringExtra("receiverId")
        val receiverName = intent.getStringExtra("receiverName")

        if (receiverId != null && receiverName != null) {
            tvChatTitle.text = receiverName
            chatId = generateChatId(auth.currentUser?.uid ?: "", receiverId!!)
            btnAddFriend.visibility = android.view.View.GONE // Hide add friend when already in private chat
        } else {
            tvChatTitle.text = "Global Chat"
            chatId = "global"
            btnAddFriend.visibility = android.view.View.VISIBLE
        }

        // Setup RecyclerView
        chatAdapter = ChatAdapter(emptyList())
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        rvChat.layoutManager = layoutManager
        rvChat.adapter = chatAdapter

        userRepository.getUserData { user, _ ->
            if (user != null) {
                currentUserName = user.username
            }
        }

        btnSend.setOnClickListener {
            sendMessage()
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnAddFriend.setOnClickListener {
            val intent = Intent(this, UserListActivity::class.java)
            startActivity(intent)
        }

        listenForMessages()
    }

    private fun generateChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        val user = auth.currentUser ?: return

        if (text.isEmpty()) return

        val message = hashMapOf(
            "senderId" to user.uid,
            "senderName" to currentUserName,
            "text" to text,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        etMessage.setText("")

        val ref = if (chatId == "global") {
            db.collection("chats").document("global").collection("messages")
        } else {
            db.collection("chats").document(chatId!!).collection("messages")
        }

        ref.add(message)
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listenForMessages() {
        val ref = if (chatId == "global") {
            db.collection("chats").document("global").collection("messages")
        } else {
            db.collection("chats").document(chatId!!).collection("messages")
        }

        ref.orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                val messagesList = value?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                } ?: emptyList()

                chatAdapter.updateMessages(messagesList)
                if (messagesList.isNotEmpty()) {
                    rvChat.smoothScrollToPosition(messagesList.size - 1)
                }
            }
    }
}
