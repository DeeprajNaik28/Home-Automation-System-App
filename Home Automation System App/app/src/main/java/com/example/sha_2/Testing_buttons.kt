package com.example.sha_2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class Testing_buttons : AppCompatActivity(), View.OnClickListener {

    private lateinit var firestore: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing_buttons)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Initialize Buttons and set OnClickListener
        findViewById<Button>(R.id.btnVolumeUp).setOnClickListener(this)
        findViewById<Button>(R.id.btnOk).setOnClickListener(this)
        findViewById<Button>(R.id.btnVolumeDown).setOnClickListener(this)
        findViewById<Button>(R.id.btnfreeze).setOnClickListener(this)
        findViewById<Button>(R.id.btnMode).setOnClickListener(this)

        // Navigation: Move to LivingRoom activity
        val imageViewMoveToHome: ImageView = findViewById(R.id.Projector_back_Button)
        imageViewMoveToHome.setOnClickListener {
            val intent = Intent(this, LivingRoom::class.java)
            startActivity(intent)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnVolumeUp -> {
                updateProjectorCommand("volume_up")
            }
            R.id.btnOk -> {
                Toast.makeText(this, "OK Pressed", Toast.LENGTH_SHORT).show()
                updateProjectorCommand("ok") // send OK command immediately
            }
            R.id.btnVolumeDown -> {
                updateProjectorCommand("volume_down")
            }
            R.id.btnfreeze -> {
                updateProjectorCommand("freeze")
            }
            R.id.btnMode -> {
                updateProjectorCommand("mode")
            }
        }
    }

    // Function to update projector commands in Firestore
    private fun updateProjectorCommand(command: String) {
        val roomName = "Living_Room" // Specify the room name
        val fieldPathRooms2 = "command_projector" // The command field in Living Room
        val deviceName = "projector"  // The name of the projector (should match your Appliance document)

        // Update Firestore in Rooms2 collection, Living Room document
        firestore.collection("Rooms2").document(roomName).update(fieldPathRooms2, command)
            .addOnSuccessListener {
                Log.d("Firestore", "Rooms2 updated successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Rooms2 update failed: ${exception.message}")
                Toast.makeText(this, "Failed to update Rooms2: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

        // Update Firestore in Appliance collection, projector document
        firestore.collection("Appliance").document(deviceName).update("command", command)
            .addOnSuccessListener {
                Log.d("Firestore", "Appliance updated successfully")
                Toast.makeText(this, "Projector command $command sent successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Appliance update failed: ${exception.message}")
                Toast.makeText(this, "Failed to send command: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}




















//code for only room2
//package com.example.sha_2
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.firestore.FirebaseFirestore
//
//class Testing_buttons : AppCompatActivity(), View.OnClickListener {
//    private lateinit var firestore: FirebaseFirestore
//
//    @SuppressLint("MissingInflatedId")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_testing_buttons)
//
//        // Initialize Firestore
//        firestore = FirebaseFirestore.getInstance()
//
//        // Initialize Buttons and set OnClickListener
//        findViewById<Button>(R.id.btnVolumeUp).setOnClickListener(this)
//        findViewById<Button>(R.id.btnOk).setOnClickListener(this)
//        findViewById<Button>(R.id.btnVolumeDown).setOnClickListener(this)
//        findViewById<Button>(R.id.btnfreeze).setOnClickListener(this)
//        findViewById<Button>(R.id.btnMode).setOnClickListener(this)
//
//        // Navigation: Move to LivingRoom activity
//        val imageViewMoveToHome: ImageView = findViewById(R.id.Projector_back_Button)
//        imageViewMoveToHome.setOnClickListener {
//            val intent = Intent(this, LivingRoom::class.java)
//            startActivity(intent)
//        }
//    }
//
//    override fun onClick(view: View) {
//        when (view.id) {
//            R.id.btnVolumeUp -> {
//                updateProjectorCommand("volume_up")
//            }
//            R.id.btnOk -> {
//                Toast.makeText(this, "OK Pressed", Toast.LENGTH_SHORT).show()
//                updateProjectorCommand("ok") // send OK command
//            }
//            R.id.btnVolumeDown -> {
//                updateProjectorCommand("volume_down")
//            }
//            R.id.btnfreeze -> {
//                updateProjectorCommand("freeze")
//            }
//            R.id.btnMode -> {
//                updateProjectorCommand("mode")
//            }
//        }
//    }
//
//    // Updated function to update projector commands
//    private fun updateProjectorCommand(command: String) {
//        val roomName = "Living_Room" // Specify the room name
//        val fieldPath = "command_projector" // the command field in Living Room
//
//        // Update Firestore in Rooms2 collection, Living Room document
//        firestore.collection("Rooms2").document(roomName).update(fieldPath, command)
//            .addOnSuccessListener {
//                Toast.makeText(
//                    this,
//                    "Projector command $command sent successfully!",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//            .addOnFailureListener { exception ->
//                Toast.makeText(
//                    this,
//                    "Failed to send command: ${exception.message}",
//                    Toast.LENGTH_SHORT
//                ).show()
//                Log.e("FirestoreError", exception.message ?: "Unknown error")
//            }
//    }
//}
//
