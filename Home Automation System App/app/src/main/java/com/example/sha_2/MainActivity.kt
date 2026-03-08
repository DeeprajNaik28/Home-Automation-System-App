package com.example.sha_2


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {


    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        // Set the content view (activity layout)
        setContentView(R.layout.activity_main)


        // Initialize UI elements
        usernameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)


        // Set listener for the login button
        loginButton.setOnClickListener {
            try {
                if (validateInputs()) {
                    login()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()  // Print the error to Logcat
            }
        }


        // Set listener to move to Create Account page when the link is clicked
        val textViewMoveToCreateAcc: TextView = findViewById(R.id.signUplink)
        textViewMoveToCreateAcc.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }


//navigation
        val forgotPasswordLink: TextView = findViewById(R.id.forgotPasswordLink)
        forgotPasswordLink.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }
    }


    private fun validateInputs(): Boolean {
        val email = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()


        // Validate email
        if (email.isEmpty()) {
            usernameEditText.error = "Email is required"
            return false
        }
        if (!isValidEmail(email)) {
            usernameEditText.error = "Invalid email format"
            return false
        }


        // Validate Password
        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return false
        } else if (password.length < 8) {
            passwordEditText.error = "Password must be at least 8 characters"
            return false
        }


        return true
    }


    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    private fun login() {
        val email = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()


        // Sign in with email and password
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login success, fetch user data
                    fetchUserData(email)
                } else {
                    // Login failed
                    Log.e("Login", "Login failed: ${task.exception?.message}")
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun fetchUserData(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Retrieve the UID based on the email (query the "Admin" collection)
                val adminQuerySnapshot = db.collection("Admin")
                    .whereEqualTo("email", email)
                    .get()
                    .await()


                // 2. Check if the email exists in the "Admin" collection
                if (!adminQuerySnapshot.isEmpty) {
                    // Email exists in "Admin"
                    val adminUid = adminQuerySnapshot.documents[0].id
                    withContext(Dispatchers.Main) {
                        // Proceed to the next activity or update UI as needed
                        val intent = Intent(this@MainActivity, Home::class.java)
                        intent.putExtra("userType", "admin")
                        startActivity(intent)
                        finish()
                    }
                } else {
                    // 3. If not in "Admin", query the "Guest" collection
                    val guestQuerySnapshot = db.collection("Guest")
                        .whereEqualTo("email", email)
                        .get()
                        .await()


                    // 4. Check if the email exists in the "Guest" collection
                    if (!guestQuerySnapshot.isEmpty) {
                        // Email exists in "Guest"
                        val guestUid = guestQuerySnapshot.documents[0].id
                        withContext(Dispatchers.Main) {
                            // Proceed to the next activity
                            val intent = Intent(this@MainActivity, Home::class.java)
                            intent.putExtra("userType", "guest")
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        // 5. If not found in either collection, show an error message
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("Login", "Error fetching user data: ${e.message}")
                    Toast.makeText(this@MainActivity, "Error fetching user data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


