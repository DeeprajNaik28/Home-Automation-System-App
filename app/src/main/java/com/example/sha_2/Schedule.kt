package com.example.sha_2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Schedule : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        // Initialize Firestore and FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        enableEdgeToEdge()
        setContentView(R.layout.activity_schedule)



        // Navigation to Home
        val imageViewMoveTo_home: ImageView = findViewById(R.id.homeIcon)
        imageViewMoveTo_home.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        // Navigation to Profile
        val imageViewMoveTo_Profile: ImageView = findViewById(R.id.profileIcon)
        imageViewMoveTo_Profile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        // Navigation to Add Schedule
        val imageViewMoveTo_Addschedule: ImageView = findViewById(R.id.addScheduleButton)
        imageViewMoveTo_Addschedule.setOnClickListener {
            val intent = Intent(this, Add_Schedule::class.java)
            startActivity(intent)
        }

        // Fetch and display the latest schedule data
        fetchLatestScheduleData()
    }

    private fun fetchLatestScheduleData() {
        firestore.collection("Schedules")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Order by timestamp, assuming you have such a field
            .limit(1) // Limit to the latest document
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    clearScheduleDetails() // Clear details if no documents found
                } else {
                    // Get the first document in the result set (which is the latest due to ordering)
                    val lastDocument = result.documents.first()

                    // Extract fields from the last document
                    val RoomInfo = lastDocument.getString("room") ?: "N/A"
                    val deviceRoomInfo = lastDocument.getString("device") ?: "Unknown Device"
                    val CommandInfo = lastDocument.getString("control") ?: "N/A"
                    val startTime = lastDocument.getString("starttime") ?: "N/A"
                   // val endTime = lastDocument.getString("endtime") ?: "N/A"

                    updateScheduleDetails(deviceRoomInfo, RoomInfo,CommandInfo, startTime)
                }
            }
            .addOnFailureListener { exception ->
                // Handle error here (e.g., show a Toast or log the error)
                clearScheduleDetails() // Clear details on failure as well
            }
    }

    private fun clearScheduleDetails() {
        val RoomTextView: TextView = findViewById(R.id.room2)
        val deviceRoomTextView: TextView = findViewById(R.id.device_room2)
        val CommandTextView: TextView = findViewById(R.id.command_room2)
        val onTimeTextView: TextView = findViewById(R.id.onTime2)
      // val offTimeTextView: TextView = findViewById(R.id.offTime2)

        RoomTextView.text = " "
        deviceRoomTextView.text = ""
        CommandTextView.text = " "
        onTimeTextView.text = ""
      // offTimeTextView.text = ""
    }

    private fun updateScheduleDetails(deviceRoomInfo: String, RoomInfo: String,CommandInfo:String, startTime: String) {
        val RoomTextView: TextView = findViewById(R.id.room2)
        val deviceRoomTextView: TextView = findViewById(R.id.device_room2)
        val CommandTextView: TextView = findViewById(R.id.command_room2)
        val onTimeTextView: TextView = findViewById(R.id.onTime2)
      // val offTimeTextView: TextView = findViewById(R.id.offTime2)

        RoomTextView.text = RoomInfo
        deviceRoomTextView.text =   "Device: $deviceRoomInfo "
        CommandTextView.text = "Control: $CommandInfo "
        onTimeTextView.text = "ON\n$startTime "
       // offTimeTextView.text = "OFF\n$endTime"
    }
}
