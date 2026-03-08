package com.example.sha_2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var usernameTextView: TextView
    private lateinit var fullNameTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var deleteGuestButton: Button // Declare the delete button
    private lateinit var requestedAdminbutton: Button


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize UI elements
        usernameTextView = findViewById(R.id.usernameTextView)
        fullNameTextView = findViewById(R.id.profile_fullname)
        phoneTextView = findViewById(R.id.profile_ph)
        emailTextView = findViewById(R.id.profile_email)
        logoutButton = findViewById(R.id.Logout_button)
        deleteGuestButton = findViewById(R.id.deleteGuestbutton) // Initialize the delete button
        requestedAdminbutton = findViewById(R.id.requestedAdminbutton)


        // Back button click listener
        findViewById<ImageView>(R.id.profile_back_Button).setOnClickListener {
            finish()
        }

        // Edit profile click listener
        findViewById<TextView>(R.id.editProfile).setOnClickListener {
            startActivity(Intent(this, EditProfile::class.java))
        }

        // Logout button click listener
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java)) // Redirect to login
            finish()
        }

        // Delete Guest Click Listener
        deleteGuestButton.setOnClickListener {
            startActivity(Intent(this, DeleteGuest::class.java))
        }

        // Delete Guest Click Listener
        requestedAdminbutton.setOnClickListener {
            startActivity(Intent(this, RequestedAdmin::class.java))
        }

        // Fetch user data and determine user type (Admin or Guest)
        fetchUserData()
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Check if the user exists in the "Admin" collection
            firestore.collection("Admin").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // User is an Admin
                        displayAdminProfile(userId)
                    } else {
                        // User is likely a Guest
                        displayGuestProfile(userId)
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Profile", "Error checking Admin collection", e)
                    // Assume Guest if there's an error checking Admin
                    displayGuestProfile(userId)
                }
        } else {
            // No user is signed in
            usernameTextView.text = "No user signed in"
        }
    }

    private fun displayAdminProfile(userId: String) {
        // Show the delete button
        deleteGuestButton.visibility = Button.VISIBLE // Or View.VISIBLE
        requestedAdminbutton.visibility = Button.VISIBLE // Or View.VISIBLE


        // Fetch and display admin profile data
        firestore.collection("Admin").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val username = document.getString("username") ?: "N/A"
                    val fullName = document.getString("name") ?: "N/A"
                    val phone = document.getString("phone") ?: "N/A"
                    val email = auth.currentUser?.email ?: "N/A"

                    usernameTextView.text = username
                    fullNameTextView.text = fullName
                    phoneTextView.text = phone
                    emailTextView.text = email
                } else {
                    Log.d("Profile", "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.w("Profile", "Error getting document: ", e)
            }
    }

    private fun displayGuestProfile(userId: String) {
        // Hide the delete button
        deleteGuestButton.visibility = Button.GONE // Or View.GONE
        requestedAdminbutton.visibility = Button.GONE // Or View.gone


        // Fetch and display guest profile data
        firestore.collection("Guest").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val username = document.getString("username") ?: "N/A"
                    val fullName = document.getString("name") ?: "N/A"
                    val phone = document.getString("phone") ?: "N/A"
                    val email = auth.currentUser?.email ?: "N/A" // Assuming email is in Authentication

                    usernameTextView.text = username
                    fullNameTextView.text = fullName
                    phoneTextView.text = phone
                    emailTextView.text = email
                } else {
                    Log.d("Profile", "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.w("Profile", "Error getting document: ", e)
            }
    }
}

