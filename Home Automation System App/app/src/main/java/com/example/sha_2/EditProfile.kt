package com.example.sha_2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class EditProfile : AppCompatActivity() {

    private lateinit var newUsernameEditText: EditText
    private lateinit var newPhoneEditText: EditText
    private lateinit var newFullnameEditText: EditText
    private lateinit var saveButton: Button

    // Firebase Auth and Firestore instances
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        // Initialize UI components
        newUsernameEditText = findViewById(R.id.newusernameEditText)
        newPhoneEditText = findViewById(R.id.newphnoEditText)
        newFullnameEditText = findViewById(R.id.newFnameEditText)
        saveButton = findViewById(R.id.save_button)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set listener for the save button
        saveButton.setOnClickListener {
            updateUserProfile()
        }

        // Back button click listener.
        findViewById<ImageView>(R.id.editprofile_back_Button).setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
            finish()
        }
    }

    private fun updateUserProfile() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val userMap = hashMapOf<String, Any>()

            val newUsername = newUsernameEditText.text.toString().trim()
            val newFullName = newFullnameEditText.text.toString().trim()
            val newPhone = newPhoneEditText.text.toString().trim()

            // Validate inputs and add to userMap only if valid and not empty
            if (newUsername.isNotEmpty()) {
                userMap["username"] = newUsername
            }

            if (newFullName.isNotEmpty()) {
                userMap["name"] = newFullName
            }

            if (newPhone.isNotEmpty()) {
                if (isValidPhoneNumber(newPhone)) {
                    userMap["phone"] = newPhone
                } else {
                    Toast.makeText(this, "Invalid phone number format.", Toast.LENGTH_SHORT).show()
                    return // Stop the update
                }
            }

            // Only proceed if there's something to update
            if (userMap.isNotEmpty()) {
                // First attempt to update in Admin collection
                firestore.collection("Admin").document(userId).update(userMap)
                    .addOnSuccessListener {
                        showSuccessAndRedirect()
                    }
                    .addOnFailureListener { e ->
                        // If update fails, try Guest collection
                        updateGuestProfile(userId, userMap)
                    }
            } else {
                Toast.makeText(this, "No changes to update.", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(this, "No user is signed in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateGuestProfile(userId: String, userMap: HashMap<String, Any>) {
        firestore.collection("Guest").document(userId).update(userMap)
            .addOnSuccessListener {
                showSuccessAndRedirect()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val pattern = Pattern.compile("^\\d{10}$") // Example: 10-digit number
        return pattern.matcher(phoneNumber).matches()
    }

    private fun showSuccessAndRedirect() {
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, Profile::class.java))
        finish()
    }
}

