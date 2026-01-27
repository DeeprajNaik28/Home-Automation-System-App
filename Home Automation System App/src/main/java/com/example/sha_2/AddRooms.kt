package com.example.sha_2

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import java.util.HashMap

class AddRooms : AppCompatActivity() {

    private lateinit var addroomBackButton: ImageView
    private lateinit var roomEditText: EditText
    private lateinit var deviceEditText: EditText
    private lateinit var adddeviceButton: ImageView
    private lateinit var addroomButton: MaterialButton
    private lateinit var db: FirebaseFirestore // Firebase Firestore instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_rooms)

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize views
        addroomBackButton = findViewById(R.id.addroom_back_Button)
        roomEditText = findViewById(R.id.roomEditText)
        deviceEditText = findViewById(R.id.deviceEditText)
        adddeviceButton = findViewById(R.id.adddeviceButton)
        addroomButton = findViewById(R.id.addroombutton)

        // Set onClickListeners
        addroomBackButton.setOnClickListener { finish() } // Go back

        addroomButton.setOnClickListener {
            addRoomToFirebase()
        }
    }

    private fun addRoomToFirebase() {
        val roomName = roomEditText.text.toString().trim()
        val deviceName = deviceEditText.text.toString().trim()

        if (roomName.isEmpty() || deviceName.isEmpty()) {
            Toast.makeText(this, "Please enter both room and device names", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a map to store the device
        val deviceData = HashMap<String, Any>()
        deviceData[deviceName] = true // You can put any value here. Using 'true' as a simple example.

        // Check if the room document already exists
        db.collection("Rooms2")
            .document(roomName)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Room exists, update it by adding the new device
                    val existingData = document.data
                    if (existingData != null) {
                        val devicesMap = existingData as MutableMap<String, Any>
                        devicesMap[deviceName] = true
                        db.collection("Rooms2")
                            .document(roomName)
                            .set(devicesMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Device added to existing room", Toast.LENGTH_SHORT).show()
                                Log.d("AddRooms", "Device added to existing room")
                                roomEditText.text.clear() // Clear the input fields
                                deviceEditText.text.clear()
                            }
                            .addOnFailureListener { e ->
                                Log.w("AddRooms", "Error updating document", e)
                                Toast.makeText(this, "Error adding device to room: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Room does not exist, create it with the device
                    db.collection("Rooms2")
                        .document(roomName)
                        .set(deviceData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Room and device added successfully", Toast.LENGTH_SHORT).show()
                            Log.d("AddRooms", "Room and device added successfully")
                            roomEditText.text.clear() // Clear the input fields
                            deviceEditText.text.clear()
                        }
                        .addOnFailureListener { e ->
                            Log.w("AddRooms", "Error adding document", e)
                            Toast.makeText(this, "Error adding room and device: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("AddRooms", "Error checking document", e)
                Toast.makeText(this, "Error checking room existence: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
















//package com.example.sha_2
//
//import android.os.Bundle
//import android.util.Log
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.material.button.MaterialButton
//import com.google.firebase.firestore.FirebaseFirestore
//import java.util.HashMap
//
//
//
//class AddRooms : AppCompatActivity() {
//
//    private lateinit var addroomBackButton: ImageView
//    private lateinit var roomEditText: EditText
//    private lateinit var deviceEditText: EditText
//    private lateinit var adddeviceButton: ImageView
//    private lateinit var addroomButton: MaterialButton
//    private lateinit var db: FirebaseFirestore // Firebase Firestore instance
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_add_rooms)
//
//        // Initialize Firebase Firestore
//        db = FirebaseFirestore.getInstance()
//
//        // Initialize views
//        addroomBackButton = findViewById(R.id.addroom_back_Button)
//        roomEditText = findViewById(R.id.roomEditText)
//        deviceEditText = findViewById(R.id.deviceEditText)
//        adddeviceButton = findViewById(R.id.adddeviceButton)
//        addroomButton = findViewById(R.id.addroombutton)
//
//        // Set onClickListeners
//        addroomBackButton.setOnClickListener { finish() } // Go back
//
//        addroomButton.setOnClickListener {
//            addRoomToFirebase()
//        }
//    }
//
//    private fun addRoomToFirebase() {
//        val roomName = roomEditText.text.toString().trim()
//        val deviceName = deviceEditText.text.toString().trim()
//
//        if (roomName.isEmpty() || deviceName.isEmpty()) {
//            Toast.makeText(this, "Please enter both room and device names", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Create a map to store the room data
//        val roomData = HashMap<String, Any>()
//        roomData["roomName"] = roomName
//        roomData["devices"] = mutableListOf(deviceName) // Store devices in a list
//
//        // Add the room (document) to the "Rooms2" collection with a generated ID
//        db.collection("Rooms2")
//            .add(roomData)
//            .addOnSuccessListener { documentReference ->
//                Toast.makeText(this, "Room and device added successfully", Toast.LENGTH_SHORT).show()
//                Log.d("AddRooms", "Room and device added successfully")
//                roomEditText.text.clear() // Clear the input fields
//                deviceEditText.text.clear()
//            }
//            .addOnFailureListener { e ->
//                Log.w("AddRooms", "Error adding document", e)
//                Toast.makeText(this, "Error adding room and device: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
//}
