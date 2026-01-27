package com.example.sha_2

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.content.ContextCompat

// Data class to represent a Guest
data class Guest(val uid: String, val username: String)

class DeleteGuest : AppCompatActivity() {

    private lateinit var guestRecyclerView: RecyclerView
    private lateinit var guestAdapter: GuestAdapter
    private lateinit var userIdTextView: TextView
    private val guestList = mutableListOf<Guest>()

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_guest)

        // Initialize UI components
        guestRecyclerView = findViewById(R.id.guestRecyclerView)
        guestRecyclerView.layoutManager = LinearLayoutManager(this)
        guestAdapter = GuestAdapter(guestList) { guest ->  // Set the delete listener
            deleteGuest(guest)
        }
        guestRecyclerView.adapter = guestAdapter


        // Load Guest Usernames
        loadGuestUsernames()

        // Back button click listener.
        findViewById<ImageView>(R.id.deleteGuest_back_Button).setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
            finish()
        }
    }

    private fun loadGuestUsernames() {
        firestore.collection("Guest")
            .get()
            .addOnSuccessListener { result ->
                guestList.clear() // Clear the list before adding new data
                for (document in result) {
                    val uid = document.id // Document ID is the UID
                    val name = document.getString("name")
                        ?: "No Username" // Replace "name" with your field name
                    guestList.add(Guest(uid, name))
                }
                guestAdapter.notifyDataSetChanged() // Refresh the RecyclerView
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading usernames: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun deleteGuest(guest: Guest) {
        // Delete from Firestore "Guest" collection
        firestore.collection("Guest").document(guest.uid)
            .delete()
            .addOnSuccessListener {
                // Optionally, delete the user from Firebase Authentication (if applicable)
                deleteAuthUser(guest.uid) //  See deleteAuthUser function below

                //Remove the user from the list and update the RecyclerView
                guestList.remove(guest)
                guestAdapter.notifyDataSetChanged()

                Toast.makeText(this, "Guest user deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting guest user: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun deleteAuthUser(uid: String) {
        // [START delete_user]
        FirebaseFirestore.getInstance().collection("Admin").document(uid)
            .delete()
            .addOnSuccessListener {
                //Remove the user from the list and update the RecyclerView
                Toast.makeText(this, "User Authentication deleted successfully", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error deleting user Authentication: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // RecyclerView Adapter
    private inner class GuestAdapter(
        private val guestList: List<Guest>,
        private val onDeleteClick: (Guest) -> Unit // Lambda for delete
    ) : RecyclerView.Adapter<GuestAdapter.GuestViewHolder>() {

        inner class GuestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            @SuppressLint("ResourceType")
            val usernameTextView: TextView = itemView.findViewById(1) // Assuming TextView's ID is 1

            @SuppressLint("ResourceType")
            val deleteButton: Button = itemView.findViewById(2)   // Assuming Button's ID is 2
        }

        @SuppressLint("ResourceType")
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
            // Create a LinearLayout to hold the TextView and Button
            val linearLayout = LinearLayout(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                setPadding(16)
            }

            // Create a TextView for the username
            val usernameTextView = TextView(parent.context).apply {
                id = 1 // Give it an ID
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f  // Weight of 1 to take up most of the space
                )
                textSize = 18f
                setTextColor(ContextCompat.getColor(parent.context, android.R.color.black))
            }
            linearLayout.addView(usernameTextView)

            // Create a Button for deleting the guest
            val deleteButton = Button(parent.context).apply {
                id = 2 // Give it an ID
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = "Delete"
            }
            linearLayout.addView(deleteButton)

            return GuestViewHolder(linearLayout) // Pass the LinearLayout to the ViewHolder
        }


        override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {
            val guest = guestList[position]
            holder.usernameTextView.text = guest.username
            holder.deleteButton.setOnClickListener {
                onDeleteClick(guest) // Call the lambda when delete is clicked
            }
        }

        override fun getItemCount() = guestList.size
    }
}

