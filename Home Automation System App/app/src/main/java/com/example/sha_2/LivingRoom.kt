package com.example.sha_2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class LivingRoom : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_living_room)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navigation: Move to Home activity
        val imageViewMoveToHome: ImageView = findViewById(R.id.LivingRoom_back_Button)
        imageViewMoveToHome.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        // Navigation: Move to buttons activity
        val textViewMoveTotesting: TextView = findViewById(R.id.projector_cardview)
        textViewMoveTotesting.setOnClickListener {
            val intent = Intent(this, Testing_buttons::class.java)
            startActivity(intent)
        }

        // Reference the Switch buttons
        val switchLight: Switch = findViewById(R.id.switch_Light)
        val switchMonitor: Switch = findViewById(R.id.switch_monitor)
        val switchProjector: Switch = findViewById(R.id.switch_Projector)
        val switchfan: Switch = findViewById(R.id.switch_Fan)

        // Set colors programmatically (optional)
        val thumbColor = ContextCompat.getColorStateList(this, R.color.thumb_color)

        switchLight.thumbTintList = thumbColor
        switchMonitor.thumbTintList = thumbColor
        switchProjector.thumbTintList = thumbColor
        switchfan.thumbTintList = thumbColor

        // Add listeners for each switch
        switchLight.setOnCheckedChangeListener { _, isChecked ->
            Log.d("SwitchLight", "Light switched ${if (isChecked) "ON" else "OFF"}")
            updateApplianceState("light", isChecked)
        }

        switchMonitor.setOnCheckedChangeListener { _, isChecked ->
            Log.d("switchMonitor", "switch Monitor ${if (isChecked) "ON" else "OFF"}")
            updateApplianceState("light", isChecked) // Ensure this matches Firestore field name
        }

        switchProjector.setOnCheckedChangeListener { _, isChecked ->
            Log.d("SwitchProjector", "Projector switched ${if (isChecked) "ON" else "OFF"}")
            updateApplianceState("projector", isChecked)
        }

        switchfan.setOnCheckedChangeListener { _, isChecked ->
            Log.d("SwitchFan", "Fan switched ${if (isChecked) "ON" else "OFF"}")
            updateApplianceState("light", isChecked)
        }
    }

    private fun updateApplianceState(appliance: String, isOn: Boolean) {
        val roomName = "Living_Room" // Specify the room name
        val command = if (isOn) "on" else "off"

        // Construct the command field name based on the appliance type
        val commandFieldNameRooms2 = when (appliance) {
            "light" -> "command_light"
            "switchMonitor" -> "switch Monitor" // Ensure this matches your Firestore structure
            "projector" -> "command_projector"
            "Fan" -> "command_Fan"
            else -> return // If appliance type is unrecognized, return early
        }

        // Update Firestore in Rooms2 collection, Living Room document
        firestore.collection("Rooms2").document(roomName).update(commandFieldNameRooms2, command)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "$appliance turned $command successfully in $roomName!",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("Firestore", "$appliance updated successfully in Rooms2")

                // Update Firestore in Appliance collection as well
                updateApplianceCommand(appliance, command)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to send command for $appliance: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("FirestoreError", exception.message ?: "Unknown error")
            }
    }

    private fun updateApplianceCommand(appliance: String, command: String) {
        // Update Firestore in Appliance collection based on appliance type
        firestore.collection("Appliance").document(appliance).update("command", command)
            .addOnSuccessListener {
                Log.d("Firestore", "$appliance updated successfully in Appliance collection")
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Appliance update failed: ${exception.message}")
                Toast.makeText(this, "Failed to send command for $appliance: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
























//not woiking for sensor
//package com.example.sha_2
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.widget.ImageView
//import android.widget.Switch
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.google.firebase.firestore.FirebaseFirestore
//
//class LivingRoom : AppCompatActivity() {
//    private lateinit var firestore: FirebaseFirestore
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_living_room)
//
//        // Initialize Firestore
//        firestore = FirebaseFirestore.getInstance()
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        // Navigation: Move to Home activity
//        val imageViewMoveToHome: ImageView = findViewById(R.id.LivingRoom_back_Button)
//        imageViewMoveToHome.setOnClickListener {
//            val intent = Intent(this, Home::class.java)
//            startActivity(intent)
//        }
//
//        // Navigation: Move to buttons activity
//        val textViewMoveTotesting: TextView = findViewById(R.id.projector_cardview)
//        textViewMoveTotesting.setOnClickListener {
//            val intent = Intent(this, Testing_buttons::class.java)
//            startActivity(intent)
//        }
//
//        // Reference the Switch buttons
//        val switchLight: Switch = findViewById(R.id.switch_Light)
//        val switchMotion: Switch = findViewById(R.id.switch_motion)
//        val switchProjector: Switch = findViewById(R.id.switch_Projector)
//        val switchAC: Switch = findViewById(R.id.switch_AC)
//
//        // Set colors programmatically (optional)
//        val thumbColor = ContextCompat.getColorStateList(this, R.color.thumb_color)
//
//        switchLight.thumbTintList = thumbColor
//        switchMotion.thumbTintList = thumbColor
//        switchProjector.thumbTintList = thumbColor
//        switchAC.thumbTintList = thumbColor
//
//        // Add listeners for each switch
//        switchLight.setOnCheckedChangeListener { _, isChecked ->
//            Log.d("SwitchLight", "Light switched ${if (isChecked) "ON" else "OFF"}")
//            updateApplianceState("light", isChecked)
//        }
//
//        switchMotion.setOnCheckedChangeListener { _, isChecked ->
//            Log.d("switchMotion", "Motion switched ${if (isChecked) "ON" else "OFF"}")
//            updateApplianceState("motion", isChecked) // Fixed key to match appliance name
//        }
//
//        switchProjector.setOnCheckedChangeListener { _, isChecked ->
//            Log.d("SwitchProjector", "Projector switched ${if (isChecked) "ON" else "OFF"}")
//            updateApplianceState("projector", isChecked)
//        }
//
//        switchAC.setOnCheckedChangeListener { _, isChecked ->
//            Log.d("SwitchAC", "AC switched ${if (isChecked) "ON" else "OFF"}")
//            updateApplianceState("ac", isChecked)
//        }
//    }
//
//    private fun updateApplianceState(appliance: String, isOn: Boolean) {
//        val roomName = "Living_Room" // Specify the room name
//        val command = if (isOn) "on" else "off"
//
//        // Construct the command field name based on the appliance type
//        val commandFieldNameRooms2 = when (appliance) {
//            "light" -> "command_light"
//            "motion_sensor" -> "motion_detected"
//            "projector" -> "command_projector"
//            "ac" -> "command_ac"
//            else -> return // If appliance type is unrecognized, return early
//        }
//
//        // Update Firestore in Rooms2 collection, Living Room document
//        firestore.collection("Rooms2").document(roomName).update(commandFieldNameRooms2, command)
//            .addOnSuccessListener {
//                Toast.makeText(
//                    this,
//                    "$appliance turned $command successfully in $roomName!",
//                    Toast.LENGTH_SHORT
//                ).show()
//                Log.d("Firestore", "$appliance updated successfully in Rooms2")
//
//                // Update Firestore in Appliance collection as well
//                    updateApplianceCommand(appliance, command)
//            }
//            .addOnFailureListener { exception ->
//                Toast.makeText(
//                    this,
//                    "Failed to send command for $appliance: ${exception.message}",
//                    Toast.LENGTH_SHORT
//                ).show()
//                Log.e("FirestoreError", exception.message ?: "Unknown error")
//            }
//    }
//
//    private fun updateApplianceCommand(appliance: String, command: String) {
//        // Update Firestore in Appliance collection based on appliance type
//        firestore.collection("Appliance").document(appliance).update("command", command)
//            .addOnSuccessListener {
//                Log.d("Firestore", "$appliance updated successfully in Appliance collection")
//            }
//            .addOnFailureListener { exception ->
//                Log.e("FirestoreError", "Appliance update failed: ${exception.message}")
//                Toast.makeText(this, "Failed to send command for $appliance: ${exception.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
//}
//























//code only for Rooms2
//package com.example.sha_2
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.widget.ImageView
//import android.widget.Switch
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.google.firebase.firestore.FirebaseFirestore
//
//class LivingRoom : AppCompatActivity() {
//    private lateinit var firestore: FirebaseFirestore
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_living_room)
//
//        // Initialize Firestore
//        firestore = FirebaseFirestore.getInstance()
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        // Navigation: Move to Home activity
//        val imageViewMoveToHome: ImageView = findViewById(R.id.LivingRoom_back_Button)
//        imageViewMoveToHome.setOnClickListener {
//            val intent = Intent(this, Home::class.java)
//            startActivity(intent)
//        }
//
//        // Navigation: Move to buttons activity
//        val textViewMoveTotesting: TextView = findViewById(R.id.projector_cardview)
//        textViewMoveTotesting.setOnClickListener {
//            val intent = Intent(this, Testing_buttons::class.java)
//            startActivity(intent)
//        }
//
//        // Reference the Switch buttons
//        val switchLight: Switch = findViewById(R.id.switch_Light)
//        val switchMotion: Switch = findViewById(R.id.switch_motion)
//        val switchProjector: Switch = findViewById(R.id.switch_Projector)
//        val switchAC: Switch = findViewById(R.id.switch_AC)
//
//        // Set colors programmatically (optional)
//        val thumbColor = ContextCompat.getColorStateList(this, R.color.thumb_color)
//
//        switchLight.thumbTintList = thumbColor
//        switchMotion.thumbTintList = thumbColor
//        switchProjector.thumbTintList = thumbColor
//        switchAC.thumbTintList = thumbColor
//
//        // Add listeners for each switch
//        switchLight.setOnCheckedChangeListener { _, isChecked ->
//            Log.d("SwitchLight", "Light switched ${if (isChecked) "ON" else "OFF"}")
//            updateApplianceState("light", isChecked)
//        }
//
//        switchMotion.setOnCheckedChangeListener { _, isChecked ->
//            Log.d("switchMotion", "Motion switched ${if (isChecked) "ON" else "OFF"}")
//            updateApplianceState("Motion", isChecked)
//        }
//
//        switchProjector.setOnCheckedChangeListener { _, isChecked ->
//            Log.d("SwitchProjector", "Projector switched ${if (isChecked) "ON" else "OFF"}")
//            updateApplianceState("projector", isChecked)
//        }
//
//        switchAC.setOnCheckedChangeListener { _, isChecked ->
//            Log.d("SwitchAC", "AC switched ${if (isChecked) "ON" else "OFF"}")
//            updateApplianceState("ac", isChecked)
//        }
//    }
//
//    private fun updateApplianceState(appliance: String, isOn: Boolean) {
//        val roomName = "Living_Room" // Specify the room name
//        val command = if (isOn) "on" else "off"
//
//        // Construct the command field name based on the appliance type
//        val commandFieldName = when (appliance) {
//            "light" -> "command_light"
//            "Motion" -> "motion_detected"
//            "projector" -> "command_projector"
//            "ac" -> "command_ac"
//            else -> return // If appliance type is unrecognized, return early
//        }
//
//        // Update Firestore in Rooms2 collection, Living Room document
//        firestore.collection("Rooms2").document(roomName).update(commandFieldName, command)
//            .addOnSuccessListener {
//                Toast.makeText(
//                    this,
//                    "$appliance turned $command successfully in $roomName!",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//            .addOnFailureListener { exception ->
//                Toast.makeText(
//                    this,
//                    "Failed to send command for $appliance: ${exception.message}",
//                    Toast.LENGTH_SHORT
//                ).show()
//                Log.e("FirestoreError", exception.message ?: "Unknown error")
//            }
//    }
//}
//
//
//
