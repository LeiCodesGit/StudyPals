package com.example.studypals

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class UserAdminAdapter(private var userList: MutableList<User>) :
    RecyclerView.Adapter<UserAdminAdapter.UserViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.tvAdminUsername)
        val emailText: TextView = view.findViewById(R.id.tvAdminUserEmail)
        val editBtn: ImageButton = view.findViewById(R.id.btnEditUser)
        val deleteBtn: ImageButton = view.findViewById(R.id.btnDeleteUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_admin, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.nameText.text = user.username
        holder.emailText.text = user.email

        holder.editBtn.setOnClickListener {
            showEditDialog(holder.itemView.context, user, position)
        }

        holder.deleteBtn.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete User")
                .setMessage("Delete ${user.username}?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteUserFromFirestore(holder.itemView.context, user, position)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun getItemCount() = userList.size

    private fun showEditDialog(context: Context, user: User, position: Int) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_edit)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etUsername = dialog.findViewById<EditText>(R.id.etEditUsername)
        val etFirstName = dialog.findViewById<EditText>(R.id.etEditFirstName)
        val etLastName = dialog.findViewById<EditText>(R.id.etEditLastName)
        val btnSave = dialog.findViewById<Button>(R.id.btnUpdateUser)

        etUsername.setText(user.username)
        etFirstName.setText(user.firstName)
        etLastName.setText(user.lastName)

        btnSave.setOnClickListener {
            val updatedUsername = etUsername.text.toString()
            val updatedFirstName = etFirstName.text.toString()
            val updatedLastName = etLastName.text.toString()

            val updates = mapOf(
                "username" to updatedUsername,
                "firstName" to updatedFirstName,
                "lastName" to updatedLastName
            )

            db.collection("users").document(user.uid)
                .update(updates)
                .addOnSuccessListener {
                    val updatedUser = user.copy(
                        username = updatedUsername,
                        firstName = updatedFirstName,
                        lastName = updatedLastName
                    )
                    userList[position] = updatedUser
                    notifyItemChanged(position)
                    Toast.makeText(context, "Changes Saved to Database", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Log.e("UserAdminAdapter", "Error updating user", e)
                    Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    private fun deleteUserFromFirestore(context: Context, user: User, position: Int) {
        db.collection("users").document(user.uid)
            .delete()
            .addOnSuccessListener {
                // The snapshot listener in AdminActivity will handle the UI update automatically,
                // but if not using a listener, we remove manually.
                // Since AdminActivity uses a listener, the list might refresh itself.
                Toast.makeText(context, "User Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("UserAdminAdapter", "Error deleting user", e)
                Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
