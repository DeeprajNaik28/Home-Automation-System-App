package com.example.sha_2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Add_Schedule : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var roomSpinner: Spinner
    private lateinit var deviceSpinner: Spinner
    private lateinit var controlSpinner: Spinner
    private lateinit var whentoon_date: TextInputEditText
    private lateinit var whentoend_date: TextInputEditText
    private lateinit var whentoon_time: EditText
    private lateinit var whentoend_time: EditText
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    lateinit var txtTimeStart: EditText
    lateinit var txtTimeEnd: EditText
    private var mHour: Int = 0
    private var mMinute: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_schedule)

        // Initialize UI elements
        txtTimeStart = findViewById(R.id.whentoon_time)
        txtTimeEnd = findViewById(R.id.whentoend_time)

        // Event listener function display & save time for start time picker
        txtTimeStart.setOnClickListener {
            // Get Current Time
            val c = Calendar.getInstance()
            mHour = c.get(Calendar.HOUR_OF_DAY)
            mMinute = c.get(Calendar.MINUTE)

            // Launch Time Picker Dialog for start time
            val timePickerDialog = TimePickerDialog(this,
                { _, hourOfDay, minute ->
                    val selectedTime = Calendar.getInstance()
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedTime.set(Calendar.MINUTE, minute)

                    // Check if selected time is earlier than current time on the same day
                    if (selectedTime.timeInMillis < Calendar.getInstance().timeInMillis) {
                        Toast.makeText(this, "Please select future time", Toast.LENGTH_SHORT).show()
                        return@TimePickerDialog
                    }

                    txtTimeStart.setText("$hourOfDay:$minute")
                }, mHour, mMinute, false)
            timePickerDialog.show()
        }

        // Event listener function display & save time for end time picker
        txtTimeEnd.setOnClickListener {
            // Get Current Time
            val c = Calendar.getInstance()
            mHour = c.get(Calendar.HOUR_OF_DAY)
            mMinute = c.get(Calendar.MINUTE)

            // Launch Time Picker Dialog for end time
            val timePickerDialog = TimePickerDialog(this,
                { _, hourOfDay, minute ->
                    val selectedTime = Calendar.getInstance()
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedTime.set(Calendar.MINUTE, minute)

                    // Check if selected time is earlier than current time on the same day or before start time.
                    if (selectedTime.timeInMillis < Calendar.getInstance().timeInMillis) {
                        Toast.makeText(this, "Please select future time", Toast.LENGTH_SHORT).show()
                        return@TimePickerDialog
                    }

                    txtTimeEnd.setText("$hourOfDay:$minute")
                }, mHour, mMinute, false)
            timePickerDialog.show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements for spinners and date fields
        roomSpinner = findViewById(R.id.mySpinner)
        deviceSpinner = findViewById(R.id.mySpinner2)
        controlSpinner = findViewById(R.id.mySpinner3)

        whentoon_date = findViewById(R.id.whentoon_date)
        whentoend_date = findViewById(R.id.whentoend_date)
        whentoon_time = findViewById(R.id.whentoon_time)
        whentoend_time = findViewById(R.id.whentoend_time)

        // Make date fields not focusable to prevent keyboard from showing up
        whentoon_date.inputType = android.text.InputType.TYPE_NULL
        whentoend_date.inputType = android.text.InputType.TYPE_NULL
        whentoon_date.isFocusable = false
        whentoend_date.isFocusable = false

        // Set up Date Pickers with validation for past dates.
        whentoon_date.setOnClickListener {
            showDatePickerDialog(whentoon_date)
        }

        whentoend_date.setOnClickListener {
            showDatePickerDialog(whentoend_date)
        }

        // Navigation: Move to schedule activity
        val imageViewMoveToSchedule: ImageView = findViewById(R.id.Add_Schedule_back_Button)
        imageViewMoveToSchedule.setOnClickListener {
            startActivity(Intent(this, Schedule::class.java))
        }

        // Fetch rooms from Firestore
        fetchRooms()

        findViewById<MaterialButton>(R.id.Add_ScheduleButton).setOnClickListener {
            if (validateInputs()) {
                saveScheduleToFirestore()
            }
        }
    }

    private fun showDatePickerDialog(editText: TextInputEditText) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,
            { _, year, monthOfYear, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, monthOfYear, dayOfMonth)

                // Check if selected date is earlier than today.
                if (selectedCalendar.timeInMillis < Calendar.getInstance().timeInMillis) {
                    Toast.makeText(this, "Please select a future date", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog  // Exit if the date is invalid.
                }

                val formattedDate = dateFormat.format(selectedCalendar.time)
                editText.setText(formattedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun fetchRooms() {
        db.collection("Rooms2").get()
            .addOnSuccessListener { result ->
                val roomList = result.map { it.id }
                if (roomList.isNotEmpty()) {
                    setupRoomSpinner(roomList)
                } else {
                    Toast.makeText(this, "No rooms found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching rooms", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRoomSpinner(roomList: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roomList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roomSpinner.adapter = adapter

        roomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedRoom = parent.getItemAtPosition(position).toString()
                fetchDevicesForRoom(selectedRoom)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchDevicesForRoom(roomName: String) {
        db.collection("Rooms2").document(roomName).get()
            .addOnSuccessListener { document ->
                val devices = document.data?.keys?.filter { key -> document.get(key) is Map<*, *> }
                    ?: emptyList()
                if (devices.isNotEmpty()) {
                    setupDeviceSpinner(devices)
                } else {
                    setupDeviceSpinner(emptyList())
                    Toast.makeText(
                        this,
                        "No devices found in $roomName",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching devices", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupDeviceSpinner(deviceList: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, deviceList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        deviceSpinner.adapter = adapter

        deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedDevice = parent.getItemAtPosition(position).toString()
                val selectedRoom = roomSpinner.selectedItem.toString()
                fetchControlsForDevice(selectedRoom, selectedDevice)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchControlsForDevice(roomName: String, deviceName: String) {
        db.collection("Rooms2").document(roomName).get()
            .addOnSuccessListener { document ->
                val controlsMap = document.get(deviceName) as? Map<*, *>
                val controlList = controlsMap?.keys?.map { it.toString() } ?: emptyList()
                if (controlList.isNotEmpty()) {
                    setupControlSpinner(controlList)
                } else {
                    setupControlSpinner(emptyList())
                    Toast.makeText(
                        this,
                        "No controls found for $deviceName",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching controls", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupControlSpinner(controlList: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, controlList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        controlSpinner.adapter = adapter
    }

    private fun saveScheduleToFirestore() {
        val selectedRoom = roomSpinner.selectedItem?.toString()
        val selectedDevice = deviceSpinner.selectedItem?.toString()
        val selectedControl = controlSpinner.selectedItem?.toString()

        // Check if all selections are valid.
        if (selectedRoom.isNullOrEmpty() || selectedDevice.isNullOrEmpty() || selectedControl.isNullOrEmpty()) {
            Toast.makeText(this, "Please select a room, device, and control", Toast.LENGTH_SHORT).show()
            return
        }

        // Create schedule data using values from EditTexts.
        val scheduleData = hashMapOf(
            "room" to selectedRoom,
            "device" to selectedDevice,
            "control" to selectedControl,
            "startdt" to whentoon_date.text.toString(),
            "enddt" to whentoend_date.text.toString(),
            "starttime" to whentoon_time.text.toString(),
            "endtime" to whentoend_time.text.toString(),
            "timestamp" to FieldValue.serverTimestamp(),
            "executed" to false  // Mark as not executed yet.
        )

        // Save to Firestore under 'Schedules' collection.
        db.collection("Schedules")
            .add(scheduleData)
            .addOnSuccessListener { documentReference ->
                Log.d("AddSchedule", "DocumentSnapshot added with ID: ${documentReference.id}")
                Toast.makeText(this, "Schedule saved successfully", Toast.LENGTH_SHORT).show()

                // After saving the schedule execute the command after a delay.
                executeCommandAfterDelay(
                    selectedRoom,
                    selectedDevice,
                    selectedControl,
                    whentoon_time.text.toString()
                )

                startActivity(Intent(this, Schedule::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Log.w("AddSchedule", "Error adding document", e)
                Toast.makeText(this, "Failed to save schedule", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateDeviceCommand(roomName: String, deviceName: String, selectedControl: String) {
        val roomRef = db.collection("Rooms2").document(roomName)

        // Get current command before updating.
        roomRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentCommand = document.getString("command_$deviceName") ?: ""
                val updates = hashMapOf<String, Any>()

                // Save the current command to the previous command field.
                updates["prev_command_$deviceName"] = currentCommand

                // Update the command field with the selected control.
                updates["command_$deviceName"] = selectedControl

                roomRef.update(updates)
                    .addOnSuccessListener {
                        Log.d("UpdateCommand", "Command updated successfully for $deviceName in $roomName")
                        Toast.makeText(this, "Device command updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("UpdateCommand", "Error updating command", e)
                        Toast.makeText(this, "Failed to update device command", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Log.d("UpdateCommand", "Room $roomName not found")
                Toast.makeText(this, "Room not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.w("UpdateCommand", "Error getting document", e)
            Toast.makeText(this, "Error accessing room data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateApplianceCommand(deviceName: String, selectedControl: String) {
        val applianceRef = db.collection("Appliance").document(deviceName)

        // Get current command before updating.
        applianceRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Document exists; update the command.
                val currentCommand = document.getString("command") ?: ""
                val updates = hashMapOf<String, Any>()

                // Save the current command to the previous command field.
                updates["prev_command"] = currentCommand

                // Update the command field with the selected control.
                updates["command"] = selectedControl

                applianceRef.update(updates)
                    .addOnSuccessListener {
                        Log.d("UpdateApplianceCommand", "Command updated successfully for $deviceName")
                        Toast.makeText(this, "Appliance command updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("UpdateApplianceCommand", "Error updating command", e)
                        Toast.makeText(
                            this,
                            "Failed to update appliance command",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                // Document doesn't exist; create a new document with the deviceName as ID.
                val initialData = hashMapOf(
                    "command" to selectedControl,
                    "prev_command" to ""  // Initialize previous command to an empty string.
                )

                applianceRef.set(initialData)  // Use .set() to create the document.
                    .addOnSuccessListener {
                        Log.d("UpdateApplianceCommand", "New appliance document created: $deviceName")
                        Toast.makeText(
                            this,
                            "New appliance command created",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("UpdateApplianceCommand", "Error creating new appliance", e)
                        Toast.makeText(
                            this,
                            "Failed to create new appliance document",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }.addOnFailureListener { e ->
            Log.w("UpdateApplianceCommand", "Error getting document", e)
            Toast.makeText(this, "Error accessing appliance data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun executeCommandAfterDelay(
        roomName: String,
        deviceName: String,
        selectedControl: String,
        time: String
    ) {
        // Convert the time string from whentoon_time into a delay in milliseconds.
        val delayInMillis = calculateDelay(time)

        Log.d(
            "ExecuteCommand",
            "Setting delay for $deviceName in $roomName for $delayInMillis milliseconds."
        )

        // Launch a coroutine in the IO dispatcher.
        CoroutineScope(Dispatchers.IO).launch {
            // Delay for the calculated time.
            delay(delayInMillis)

            Log.d("ExecuteCommand", "Executing command for $deviceName in $roomName after delay.")

            // After the delay update the device command.
            updateDeviceCommand(roomName, deviceName, selectedControl)
            updateApplianceCommand(deviceName, selectedControl)
        }
    }

    private fun calculateDelay(time: String): Long {
        val parts = time.split(":")
        if (parts.size == 2) {
            try {
                val hours = parts[0].toInt()
                val minutes = parts[1].toInt()

                val currentCalendar = Calendar.getInstance()
                val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = currentCalendar.get(Calendar.MINUTE)

                val targetTimeInMinutes =(hours * 60 + minutes).toLong()
                val currentTimeInMinutes =(currentHour * 60 + currentMinute).toLong()

                var delayInMinutes= targetTimeInMinutes - currentTimeInMinutes

                if (delayInMinutes < 0) {
                    delayInMinutes += 24 * 60
                }

                return delayInMinutes * 60 * 1000
            } catch (e: NumberFormatException) {
                Log.e("CalculateDelay", "Invalid number format in time string.")
            }
        } else {
            Log.e("CalculateDelay", "Invalid time format:$time")
        }
        return 0
    }

        private fun validateInputs(): Boolean {
        // Use different variable names to avoid shadowing.
        val startDate = whentoon_date.text.toString().trim()
        val endDate = whentoend_date.text.toString().trim()
        val startTime = whentoon_time.text.toString().trim()
        val endTime = whentoend_time.text.toString().trim()

        // Validate start date.
        if (startDate.isEmpty()) {
            whentoon_date.error = "Start date is required"  // Set error on EditText.
            return false
        }

        // Validate end date.
        if (endDate.isEmpty()) {
            whentoend_date.error = "End date is required"   // Set error on EditText.
            return false
        }

        // Validate start time.
        if (startTime.isEmpty()) {
            whentoon_time.error = "Start time is required"   // Set error on EditText.
            return false
        }

        // Validate end time.
        if (endTime.isEmpty()) {
            whentoend_time.error = "End time is required"   // Set error on EditText.
            return false
        }

        // Validate that end date is not earlier than start date
        val startCalendar = Calendar.getInstance()
        val endCalendar = Calendar.getInstance()
        val startParts = startDate.split("-")
        val endParts = endDate.split("-")
        startCalendar.set(startParts[2].toInt(), startParts[1].toInt() - 1, startParts[0].toInt())
        endCalendar.set(endParts[2].toInt(), endParts[1].toInt() - 1, endParts[0].toInt())

        if (endCalendar.timeInMillis < startCalendar.timeInMillis) {
            whentoend_date.error = "End date must be after start date"
            return false
        }

        // If dates are the same, validate that end time is not earlier than start time
        if (startCalendar.timeInMillis == endCalendar.timeInMillis) {
            val startHour = startTime.split(":")[0].toInt()
            val startMinute = startTime.split(":")[1].toInt()
            val endHour = endTime.split(":")[0].toInt()
            val endMinute = endTime.split(":")[1].toInt()

            if (endHour < startHour || (endHour == startHour && endMinute < startMinute)) {
                whentoend_time.error = "End time must be after start time"
                return false
            }
        }

        return true  // All validations passed.
    }

}



































////fully working code , only past dates are shown
//package com.example.sha_2
//
//import android.app.DatePickerDialog
//import android.app.TimePickerDialog
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.*
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.google.android.material.button.MaterialButton
//import com.google.android.material.textfield.TextInputEditText
//import com.google.firebase.firestore.FieldValue
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.*
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Locale
//
//class Add_Schedule : AppCompatActivity() {
//
//    private lateinit var db: FirebaseFirestore
//    private lateinit var roomSpinner: Spinner
//    private lateinit var deviceSpinner: Spinner
//    private lateinit var controlSpinner: Spinner
//    private lateinit var whentoon_date: TextInputEditText
//    private lateinit var whentoend_date: TextInputEditText
//    private lateinit var whentoon_time: EditText
//    private lateinit var whentoend_time: EditText
//    private val calendar = Calendar.getInstance()
//    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
//
//    lateinit var txtTimeStart: EditText
//    lateinit var txtTimeEnd: EditText
//    private var mHour: Int = 0
//    private var mMinute: Int = 0
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_add_schedule)
//
//        // Initialize UI elements
//        txtTimeStart = findViewById(R.id.whentoon_time)
//        txtTimeEnd = findViewById(R.id.whentoend_time)
//
//        // Event listener function display & save time for start time picker
//        txtTimeStart.setOnClickListener {
//            // Get Current Time
//            val c = Calendar.getInstance()
//            mHour = c.get(Calendar.HOUR_OF_DAY)
//            mMinute = c.get(Calendar.MINUTE)
//
//            // Launch Time Picker Dialog for start time
//            val timePickerDialog = TimePickerDialog(this,
//                { _, hourOfDay, minute ->
//                    txtTimeStart.setText("$hourOfDay:$minute")
//                }, mHour, mMinute, false)
//            timePickerDialog.show()
//        }
//
//        // Event listener function display & save time for end time picker
//        txtTimeEnd.setOnClickListener {
//            // Get Current Time
//            val c = Calendar.getInstance()
//            mHour = c.get(Calendar.HOUR_OF_DAY)
//            mMinute = c.get(Calendar.MINUTE)
//
//            // Launch Time Picker Dialog for end time
//            val timePickerDialog = TimePickerDialog(this,
//                { _, hourOfDay, minute ->
//                    txtTimeEnd.setText("$hourOfDay:$minute")
//                }, mHour, mMinute, false)
//            timePickerDialog.show()
//        }
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        // Initialize Firestore
//        db = FirebaseFirestore.getInstance()
//
//        // Initialize UI elements for spinners and date fields
//        roomSpinner = findViewById(R.id.mySpinner)
//        deviceSpinner = findViewById(R.id.mySpinner2)
//        controlSpinner = findViewById(R.id.mySpinner3)
//
//        whentoon_date = findViewById(R.id.whentoon_date)
//        whentoend_date = findViewById(R.id.whentoend_date)
//        whentoon_time = findViewById(R.id.whentoon_time)
//        whentoend_time = findViewById(R.id.whentoend_time)
//
//        // Make date fields not focusable to prevent keyboard from showing up
//        whentoon_date.inputType = android.text.InputType.TYPE_NULL
//        whentoend_date.inputType = android.text.InputType.TYPE_NULL
//        whentoon_date.isFocusable = false
//        whentoend_date.isFocusable = false
//
//        // Set up Date Pickers
//        whentoon_date.setOnClickListener {
//            showDatePickerDialog(whentoon_date)
//        }
//
//        whentoend_date.setOnClickListener {
//            showDatePickerDialog(whentoend_date)
//        }
//
//        // Navigation: Move to schedule activity
//        val imageViewMoveToSchedule: ImageView = findViewById(R.id.Add_Schedule_back_Button)
//        imageViewMoveToSchedule.setOnClickListener {
//            startActivity(Intent(this, Schedule::class.java))
//        }
//
//        // Fetch rooms from Firestore
//        fetchRooms()
//
//        findViewById<MaterialButton>(R.id.Add_ScheduleButton).setOnClickListener {
//            if (validateInputs()) {
//                saveScheduleToFirestore()
//            }
//        }
//    }
//
//    private fun showDatePickerDialog(editText: TextInputEditText) {
//        val year = calendar.get(Calendar.YEAR)
//        val month = calendar.get(Calendar.MONTH)
//        val day = calendar.get(Calendar.DAY_OF_MONTH)
//
//        val datePickerDialog = DatePickerDialog(this,
//            { _, year, monthOfYear, dayOfMonth ->
//                val selectedCalendar = Calendar.getInstance()
//                selectedCalendar.set(year, monthOfYear, dayOfMonth)
//                val formattedDate = dateFormat.format(selectedCalendar.time)
//                editText.setText(formattedDate)
//            }, year, month, day)
//
//        datePickerDialog.show()
//    }
//
//    private fun fetchRooms() {
//        db.collection("Rooms2").get() // Changed to Rooms2
//            .addOnSuccessListener { result ->
//                val roomList = result.map { it.id }
//                if (roomList.isNotEmpty()) {
//                    setupRoomSpinner(roomList)
//                } else {
//                    Toast.makeText(this, "No rooms found", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Error fetching rooms", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    private fun setupRoomSpinner(roomList: List<String>) {
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roomList)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        roomSpinner.adapter = adapter
//
//        roomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                val selectedRoom = parent.getItemAtPosition(position).toString()
//                fetchDevicesForRoom(selectedRoom)
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }
//    }
//
//    private fun fetchDevicesForRoom(roomName: String) {
//        db.collection("Rooms2").document(roomName).get()
//            .addOnSuccessListener { document ->
//                val devices = document.data?.keys?.filter { key -> document.get(key) is Map<*, *> }
//                    ?: emptyList()
//                if (devices.isNotEmpty()) {
//                    setupDeviceSpinner(devices)
//                } else {
//                    setupDeviceSpinner(emptyList())
//                    Toast.makeText(
//                        this,
//                        "No devices found in $roomName",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Error fetching devices", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    private fun setupDeviceSpinner(deviceList: List<String>) {
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, deviceList)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        deviceSpinner.adapter = adapter
//
//        deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                val selectedDevice = parent.getItemAtPosition(position).toString()
//                val selectedRoom = roomSpinner.selectedItem.toString()
//                fetchControlsForDevice(selectedRoom, selectedDevice)
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }
//    }
//
//    private fun fetchControlsForDevice(roomName: String, deviceName: String) {
//        db.collection("Rooms2").document(roomName).get()
//            .addOnSuccessListener { document ->
//                val controlsMap = document.get(deviceName) as? Map<*, *>
//                val controlList = controlsMap?.keys?.map { it.toString() } ?: emptyList()
//                if (controlList.isNotEmpty()) {
//                    setupControlSpinner(controlList)
//                } else {
//                    setupControlSpinner(emptyList())
//                    Toast.makeText(
//                        this,
//                        "No controls found for $deviceName",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Error fetching controls", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    private fun setupControlSpinner(controlList: List<String>) {
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, controlList)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        controlSpinner.adapter = adapter
//    }
//
//    private fun saveScheduleToFirestore() {
//        val selectedRoom = roomSpinner.selectedItem?.toString()
//        val selectedDevice = deviceSpinner.selectedItem?.toString()
//        val selectedControl = controlSpinner.selectedItem?.toString()
//
//        // Check if all selections are valid
//        if (selectedRoom.isNullOrEmpty() || selectedDevice.isNullOrEmpty() || selectedControl.isNullOrEmpty()) {
//            Toast.makeText(this, "Please select a room, device, and control", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Create schedule data using values from EditTexts
//        val scheduleData = hashMapOf(
//            "room" to selectedRoom,
//            "device" to selectedDevice,
//            "control" to selectedControl,
//            "startdt" to whentoon_date.text.toString(),
//            "enddt" to whentoend_date.text.toString(),
//            "starttime" to whentoon_time.text.toString(),
//            "endtime" to whentoend_time.text.toString(),
//            "timestamp" to FieldValue.serverTimestamp(),
//            "executed" to false  // Mark as not executed yet
//        )
//
//        // Save to Firestore under 'Schedules' collection
//        db.collection("Schedules")
//            .add(scheduleData)
//            .addOnSuccessListener { documentReference ->
//                Log.d("AddSchedule", "DocumentSnapshot added with ID: ${documentReference.id}")
//                Toast.makeText(this, "Schedule saved successfully", Toast.LENGTH_SHORT).show()
//
//                // After saving the schedule, execute the command after a delay
//                executeCommandAfterDelay(
//                    selectedRoom,
//                    selectedDevice,
//                    selectedControl,
//                    whentoon_time.text.toString()
//                )
//
//                startActivity(Intent(this, Schedule::class.java))
//                finish()
//            }
//            .addOnFailureListener { e ->
//                Log.w("AddSchedule", "Error adding document", e)
//                Toast.makeText(this, "Failed to save schedule", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    private fun updateDeviceCommand(roomName: String, deviceName: String, selectedControl: String) {
//        val roomRef = db.collection("Rooms2").document(roomName)
//
//        // Get current command before updating.
//        roomRef.get().addOnSuccessListener { document ->
//            if (document.exists()) {
//                val currentCommand = document.getString("command_$deviceName") ?: ""
//                val updates = hashMapOf<String, Any>()
//
//                // Save the current command to the previous command field.
//                updates["prev_command_$deviceName"] = currentCommand
//
//                // Update the command field with the selected control.
//                updates["command_$deviceName"] = selectedControl
//
//                roomRef.update(updates)
//                    .addOnSuccessListener {
//                        Log.d("UpdateCommand", "Command updated successfully for $deviceName in $roomName")
//                        Toast.makeText(this, "Device command updated", Toast.LENGTH_SHORT).show()
//                    }
//                    .addOnFailureListener { e ->
//                        Log.w("UpdateCommand", "Error updating command", e)
//                        Toast.makeText(this, "Failed to update device command", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//            } else {
//                Log.d("UpdateCommand", "Room $roomName not found")
//                Toast.makeText(this, "Room not found", Toast.LENGTH_SHORT).show()
//            }
//        }.addOnFailureListener { e ->
//            Log.w("UpdateCommand", "Error getting document", e)
//            Toast.makeText(this, "Error accessing room data", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun updateApplianceCommand(deviceName: String, selectedControl: String) {
//        val applianceRef = db.collection("Appliance").document(deviceName)
//
//        // Get current command before updating
//        applianceRef.get().addOnSuccessListener { document ->
//            if (document.exists()) {
//                // Document exists, update the command
//                val currentCommand = document.getString("command") ?: ""
//                val updates = hashMapOf<String, Any>()
//
//                // Save the current command to the previous command field
//                updates["prev_command"] = currentCommand
//
//                // Update the command field with the selected control
//                updates["command"] = selectedControl
//
//                applianceRef.update(updates)
//                    .addOnSuccessListener {
//                        Log.d("UpdateApplianceCommand", "Command updated successfully for $deviceName")
//                        Toast.makeText(this, "Appliance command updated", Toast.LENGTH_SHORT).show()
//                    }
//                    .addOnFailureListener { e ->
//                        Log.w("UpdateApplianceCommand", "Error updating command", e)
//                        Toast.makeText(
//                            this,
//                            "Failed to update appliance command",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//            } else {
//                // Document doesn't exist, create a new document with the deviceName as ID
//                val initialData = hashMapOf(
//                    "command" to selectedControl,
//                    "prev_command" to ""  // Initialize previous command to an empty string
//                )
//
//                applianceRef.set(initialData) // Use .set() to create the document
//                    .addOnSuccessListener {
//                        Log.d("UpdateApplianceCommand", "New appliance document created: $deviceName")
//                        Toast.makeText(
//                            this,
//                            "New appliance command created",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                    .addOnFailureListener { e ->
//                        Log.w("UpdateApplianceCommand", "Error creating new appliance", e)
//                        Toast.makeText(
//                            this,
//                            "Failed to create new appliance document",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//            }
//        }.addOnFailureListener { e ->
//            Log.w("UpdateApplianceCommand", "Error getting document", e)
//            Toast.makeText(this, "Error accessing appliance data", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun executeCommandAfterDelay(
//        roomName: String,
//        deviceName: String,
//        selectedControl: String,
//        time: String
//    ) {
//        // Convert the time string from whentoon_time into a delay in milliseconds.
//        val delayInMillis = calculateDelay(time)
//
//        Log.d(
//            "ExecuteCommand",
//            "Setting delay for $deviceName in $roomName for $delayInMillis milliseconds."
//        )
//
//        // Launch a coroutine in the IO dispatcher.
//        CoroutineScope(Dispatchers.IO).launch {
//            // Delay for the calculated time.
//            delay(delayInMillis)
//
//            Log.d("ExecuteCommand", "Executing command for $deviceName in $roomName after delay.")
//
//            // After the delay update the device command.
//            updateDeviceCommand(roomName, deviceName, selectedControl)
//            updateApplianceCommand(deviceName, selectedControl)
//        }
//    }
//
//    private fun calculateDelay(time: String): Long {
//        val parts = time.split(":")
//        if (parts.size == 2) {
//            try {
//                val hours = parts[0].toInt()
//                val minutes = parts[1].toInt()
//
//                val currentCalendar = Calendar.getInstance()
//                val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
//                val currentMinute = currentCalendar.get(Calendar.MINUTE)
//
//                val targetTime = (hours * 60 + minutes).toLong()
//                val currentTime = (currentHour * 60 + currentMinute).toLong()
//
//                var delayInMinutes = targetTime - currentTime
//
//                if (delayInMinutes < 0) {
//                    delayInMinutes += 24 * 60  // If the time has already passed today schedule for tomorrow.
//                }
//
//                return delayInMinutes * 60 * 1000  // Convert to milliseconds.
//            } catch (e: NumberFormatException) {
//                Log.e("CalculateDelay", "Invalid number format in time string.")
//            }
//        } else {
//            Log.e("CalculateDelay", "Invalid time format:$time")
//        }
//        return 0  // Return 0 if the time format is incorrect.
//    }
//
//    private fun validateInputs(): Boolean {
//        // Use different variable names to avoid shadowing.
//        val startDate = whentoon_date.text.toString().trim()
//        val endDate = whentoend_date.text.toString().trim()
//        val startTime = whentoon_time.text.toString().trim()
//        val endTime = whentoend_time.text.toString().trim()
//
//        // Validate start date.
//        if (startDate.isEmpty()) {
//            whentoon_date.error = "Start date is required"  // Set error on EditText.
//            return false
//        }
//
//        // Validate end date.
//        if (endDate.isEmpty()) {
//            whentoend_date.error = "End date is required"   // Set error on EditText.
//            return false
//        }
//
//        // Validate start time.
//        if (startTime.isEmpty()) {
//            whentoon_time.error = "Start time is required"   // Set error on EditText.
//            return false
//        }
//
//        // Validate end time.
//        if (endTime.isEmpty()) {
//            whentoend_time.error = "End time is required"   // Set error on EditText.
//            return false
//        }
//
//        return true  // All validations passed.
//    }
//}
//
//
//
//
