//package com.example.sha_2
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//
//
//class Home : AppCompatActivity() {
//
//    private lateinit var firestore: FirebaseFirestore
//    private lateinit var auth: FirebaseAuth
//
//    @SuppressLint("MissingInflatedId")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Initialize Firestore and FirebaseAuth
//        firestore = FirebaseFirestore.getInstance()
//        auth = FirebaseAuth.getInstance()
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_home)
//
//        // Find the TextView for the user greeting
//        val userGreetingTextView: TextView = findViewById(R.id.userGreetingTextView)
//
//        // Get current user's UID
//        val userId = auth.currentUser?.uid
//
//        // Fetch user's name from Firestore based on their role (Admin or Guest)
//        if (userId != null) {
//            fetchUsername(userId, userGreetingTextView)
//        } else {
//            userGreetingTextView.text = "No User Signed In"
//        }
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        // Fetch and display rooms
//        fetchRooms()
//
//        // Set up button listeners for navigation (Living Room, Profile, Schedule)
//        setupNavigationButtons()
//    }
//
//    private fun fetchUsername(userId: String, userGreetingTextView: TextView) {
//        // First check Admin collection
//        firestore.collection("Admin").document(userId)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    val name = document.getString("name")
//                    updateUsernameUI(name, userGreetingTextView)
//                } else {
//                    // If not found in Admin, check Guest collection
//                    fetchGuestUsername(userId, userGreetingTextView)
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("FirestoreError", "Failed to fetch Admin data.", e)
//                fetchGuestUsername(userId, userGreetingTextView) // Try fetching from Guest if Admin fails
//            }
//    }
//
//    private fun fetchGuestUsername(userId: String, userGreetingTextView: TextView) {
//        firestore.collection("Guest").document(userId)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    val name = document.getString("name")
//                    updateUsernameUI(name, userGreetingTextView)
//                } else {
//                    updateUsernameUI(null, userGreetingTextView) // Handle case where no document exists in both collections.
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("FirestoreError", "Failed to fetch Guest data.", e)
//                updateUsernameUI(null, userGreetingTextView) // Handle error case.
//            }
//    }
//
//    private fun updateUsernameUI(username: String?, userGreetingTextView: TextView) {
//        userGreetingTextView.text = username?.let { "Hi, $it" } ?: "No Username Available."
//    }
//
//    private fun setupNavigationButtons() {
//
//        val livingRoomButton: Button = findViewById(R.id.LivingRoom_button)
//        livingRoomButton.setOnClickListener {
//            startActivity(Intent(this, LivingRoom::class.java))
//        }
//
//        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
//            startActivity(Intent(this, Profile::class.java))
//        }
//
//        findViewById<ImageView>(R.id.scheduleIcon).setOnClickListener {
//            startActivity(Intent(this, Schedule::class.java))
//        }
//
//        findViewById<ImageView>(R.id.homeIcon).setOnClickListener {
//            startActivity(Intent(this, Home::class.java))
//        }
//
//        findViewById<ImageView>(R.id.addroomButton).setOnClickListener {
//            startActivity(Intent(this, AddRooms::class.java))
//        }
//    }
//
//    private fun fetchRooms() {
//        firestore.collection("Rooms2")
//            .get()
//            .addOnSuccessListener { result ->
//                val buttonContainer: LinearLayout = findViewById(R.id.buttonContainer)
//                buttonContainer.removeAllViews() // Clear any existing buttons
//
//                for (document in result) {
//                    val roomName = document.id // Document ID is the room name
//                    if (roomName != "Living Room") { // Exclude "Living Room"
//                        Log.d("Home", "Room Name: $roomName")
//                        createRoomButton(roomName, buttonContainer)
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.w("Home", "Error getting documents: ", e)
//            }
//    }
//
//    private fun createRoomButton(roomName: String, buttonContainer: LinearLayout) {
//        val button = Button(this)
//        button.text = roomName
//
//        // Apply the same style as the "Living Room" button
//        button.layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT
//        )
//        val marginParams = button.layoutParams as LinearLayout.LayoutParams
//        marginParams.setMargins(
//            resources.getDimensionPixelSize(R.dimen.button_margin_horizontal),
//            resources.getDimensionPixelSize(R.dimen.button_margin_top),
//            resources.getDimensionPixelSize(R.dimen.button_margin_horizontal),
//            0
//        )
//        button.setPadding(0, resources.getDimensionPixelSize(R.dimen.button_padding_vertical).toInt(), 0, resources.getDimensionPixelSize(R.d.dimens.button_padding_vertical).toInt())
//        button.height = resources.getDimensionPixelSize(R.dimen.button_height) // If you have defined a height
//        button.width = LinearLayout.LayoutParams.MATCH_PARENT // Match parent width
//
//        // Add the button to the layout
//        buttonContainer.addView(button)
//
//        // Set onClickListener for the button (example)
//        button.setOnClickListener {
//            // Handle room button click
//            Log.d("Home", "Clicked on room: $roomName")
//            // Perform any other desired action here
//        }
//    }
//
//}





















//
//
//package com.example.sha_2
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.os.Build.VERSION_CODES.R
//import android.os.Bundle
//import android.util.Log
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//
//class Home : AppCompatActivity() {
//
//    private lateinit var firestore: FirebaseFirestore
//    private lateinit var auth: FirebaseAuth
//
//    @SuppressLint("MissingInflatedId")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Initialize Firestore and FirebaseAuth
//        firestore = FirebaseFirestore.getInstance()
//        auth = FirebaseAuth.getInstance()
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_home)
//
//        // Find the TextView for the user greeting
//        val userGreetingTextView: TextView = findViewById(R.id.userGreetingTextView)
//
//        // Get current user's UID
//        val userId = auth.currentUser?.uid
//
//        // Fetch user's name from Firestore based on their role (Admin or Guest)
//        if (userId != null) {
//            fetchUsername(userId, userGreetingTextView)
//        } else {
//            userGreetingTextView.text = "No User Signed In"
//        }
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        // Set up button listeners for navigation (Living Room, Profile, Schedule)
//        setupNavigationButtons()
//    }
//
//
//    private fun fetchUsername(userId: String, userGreetingTextView: TextView) {
//        // First check Admin collection
//        firestore.collection("Admin").document(userId)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    val name = document.getString("name")
//                    updateUsernameUI(name, userGreetingTextView)
//                } else {
//                    // If not found in Admin, check Guest collection
//                    fetchGuestUsername(userId, userGreetingTextView)
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("FirestoreError", "Failed to fetch Admin data.", e)
//                fetchGuestUsername(userId, userGreetingTextView) // Try fetching from Guest if Admin fails
//            }
//    }
//
//    private fun fetchGuestUsername(userId: String, userGreetingTextView: TextView) {
//        firestore.collection("Guest").document(userId)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    val name = document.getString("name")
//                    updateUsernameUI(name, userGreetingTextView)
//                } else {
//                    updateUsernameUI(null, userGreetingTextView) // Handle case where no document exists in both collections.
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("FirestoreError", "Failed to fetch Guest data.", e)
//                updateUsernameUI(null, userGreetingTextView) // Handle error case.
//            }
//    }
//
//    private fun updateUsernameUI(username: String?, userGreetingTextView: TextView) {
//        userGreetingTextView.text = username?.let { "Hi, $it" } ?: "No Username Available."
//    }
//
//    private fun setupNavigationButtons() {
//        val livingRoomButton: Button = findViewById(R.id.LivingRoom_button)
//        livingRoomButton.setOnClickListener {
//            startActivity(Intent(this, LivingRoom::class.java))
//        }
//
//        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
//            startActivity(Intent(this, Profile::class.java))
//        }
//
//        findViewById<ImageView>(R.id.scheduleIcon).setOnClickListener {
//            startActivity(Intent(this, Schedule::class.java))
//        }
//
//        findViewById<ImageView>(R.id.homeIcon).setOnClickListener {
//            startActivity(Intent(this, Home::class.java))
//        }
//
//        findViewById<ImageView>(R.id.addroomButton).setOnClickListener {
//            startActivity(Intent(this, AddRooms::class.java))
//        }
//
//    }
//}


































//curent working code
package com.example.sha_2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class Home : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firestore and FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Find the TextView for the user greeting
        val userGreetingTextView: TextView = findViewById(R.id.userGreetingTextView)

        // Get current user's UID
        val userId = auth.currentUser?.uid

        // Fetch user's name from Firestore based on their role (Admin or Guest)
        if (userId != null) {
            fetchUsername(userId, userGreetingTextView)
        } else {
            userGreetingTextView.text = "No User Signed In"
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        // Set up button listeners for navigation (Living Room, Profile, Schedule)
        setupNavigationButtons()
    }

    private fun fetchUsername(userId: String, userGreetingTextView: TextView) {
        // First check Admin collection
        firestore.collection("Admin").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name")
                    updateUsernameUI(name, userGreetingTextView)
                } else {
                    // If not found in Admin, check Guest collection
                    fetchGuestUsername(userId, userGreetingTextView)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to fetch Admin data.", e)
                fetchGuestUsername(userId, userGreetingTextView) // Try fetching from Guest if Admin fails
            }
    }

    private fun fetchGuestUsername(userId: String, userGreetingTextView: TextView) {
        firestore.collection("Guest").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name")
                    updateUsernameUI(name, userGreetingTextView)
                } else {
                    updateUsernameUI(null, userGreetingTextView) // Handle case where no document exists in both collections.
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to fetch Guest data.", e)
                updateUsernameUI(null, userGreetingTextView) // Handle error case.
            }
    }

    private fun updateUsernameUI(username: String?, userGreetingTextView: TextView) {
        userGreetingTextView.text = username?.let { "Hi, $it" } ?: "No Username Available."
    }

    private fun setupNavigationButtons() {

        val livingRoomButton: Button = findViewById(R.id.LivingRoom_button)
        livingRoomButton.setOnClickListener {
            startActivity(Intent(this, LivingRoom::class.java))
        }

        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }

        findViewById<ImageView>(R.id.scheduleIcon).setOnClickListener {
            startActivity(Intent(this, Schedule::class.java))
        }

        findViewById<ImageView>(R.id.homeIcon).setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }

//        findViewById<ImageView>(R.id.addroomButton).setOnClickListener {
//            startActivity(Intent(this, AddRooms::class.java))
//        }
    }





}

