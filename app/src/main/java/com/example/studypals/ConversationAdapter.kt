package com.example.studypals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ConversationAdapter(
    private val conversations: List<Conversation>,
    private val onClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvFriendName)
        val tvLastMsg: TextView = view.findViewById(R.id.tvLastMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the conversation item layout, NOT the admin one
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val convo = conversations[position]
        holder.tvName.text = convo.friendName
        holder.tvLastMsg.text = "${convo.lastMessage} • ${formatTime(convo.timestamp)}"

        holder.itemView.setOnClickListener { onClick(convo) }
    }

    override fun getItemCount(): Int = conversations.size

    private fun formatTime(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val diff = (System.currentTimeMillis() - timestamp) / 1000
        return when {
            diff < 60 -> "Just now"
            diff < 3600 -> "${diff / 60}m ago"
            diff < 86400 -> "${diff / 3600}h ago"
            else -> "${diff / 86400}d ago"
        }
    }
}