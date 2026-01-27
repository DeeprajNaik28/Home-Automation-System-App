package com.example.sha_2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

// Data class to represent a Requested Admin
data class RequestedAdminData(val uid: String, val username: String)

class RequestedAdmin : AppCompatActivity() {

    private lateinit var requestedAdminRecyclerView: RecyclerView
    private lateinit var requestedAdminAdapter: AdminAdapter
    private val requestedAdminList = mutableListOf<RequestedAdminData>()

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // SMTP configuration for sending emails
    private val smtpHost = "smtp.gmail.com"
    private val smtpPort = "587"
    private val senderEmail = "rcsea07@gmail.com" // Replace with your email address
    private val senderPassword = "xbyd wzux vovr uldo" // Use your app password here

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requested_admin)

        // Initialize UI components
        requestedAdminRecyclerView = findViewById(R.id.RequestedAdminRecyclerView)
        requestedAdminRecyclerView.layoutManager = LinearLayoutManager(this)
        requestedAdminAdapter = AdminAdapter(requestedAdminList) { requestedAdmin, action ->
            when (action) {
                "approve" -> approveAdmin(requestedAdmin)
                "reject" -> rejectAdmin(requestedAdmin)
            }
        }
        requestedAdminRecyclerView.adapter = requestedAdminAdapter

        // Load admin Usernames
        loadAdminUsernames()

        // Back button click listener.
        findViewById<ImageView>(R.id.RequestedAdmin_back_Button).setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
            finish()
        }
    }

    private fun loadAdminUsernames() {
        firestore.collection("RequestedAdmin")
            .get()
            .addOnSuccessListener { result ->
                requestedAdminList.clear() // Clear the list before adding new data
                for (document in result) {
                    val uid = document.id // Document ID is the UID
                    val name = document.getString("name") ?: "No Username" // Use "username" instead of "name"
                    requestedAdminList.add(RequestedAdminData(uid, name))
                }
                requestedAdminAdapter.notifyDataSetChanged() // Refresh the RecyclerView
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading usernames: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun approveAdmin(requestedAdmin: RequestedAdminData) {
        // Get user data from RequestedAdmin collection
        firestore.collection("RequestedAdmin").document(requestedAdmin.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val email = document.getString("email") ?: ""
                    val name = document.getString("name") ?: ""

                    // Copy data to Admin collection
                    firestore.collection("Admin").document(requestedAdmin.uid)
                        .set(document.data!!)
                        .addOnSuccessListener {
                            // Delete from RequestedAdmin collection after successful copy
                            deleteFromRequestedAdmin(requestedAdmin)

                            sendEmail(email, "Your Request for Admin has been Approved", "You are now an Admin")

                            Toast.makeText(this, "Admin approved successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error copying data to Admin: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "User data not found in RequestedAdmin", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error getting user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectAdmin(requestedAdmin: RequestedAdminData) {
        // Get user data from RequestedAdmin collection
        firestore.collection("RequestedAdmin").document(requestedAdmin.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val email = document.getString("email") ?: ""

                    deleteFromRequestedAdmin(requestedAdmin)

                    sendEmail(email, "Your Request for Admin has been Rejected", "Your account is Terminated")

                    Toast.makeText(this, "Requested admin rejected successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "User data not found in RequestedAdmin", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error getting user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteFromRequestedAdmin(requestedAdmin: RequestedAdminData) {
        // Delete from Firestore "RequestedAdmin" collection
        firestore.collection("RequestedAdmin").document(requestedAdmin.uid)
            .delete()
            .addOnSuccessListener {
                // Remove the user from the list and update the RecyclerView
                requestedAdminList.remove(requestedAdmin)
                requestedAdminAdapter.notifyDataSetChanged()

                Toast.makeText(this, "User removed from RequestedAdmin", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error removing user from RequestedAdmin: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    // RecyclerView Adapter
    private inner class AdminAdapter(
        private val requestedAdminList: List<RequestedAdminData>,
        private val onActionClicked: (RequestedAdminData, String) -> Unit // Lambda for delete and approve
    ) : RecyclerView.Adapter<AdminAdapter.AdminViewHolder>() {

        inner class AdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            @SuppressLint("ResourceType")
            val usernameTextView: TextView = itemView.findViewById(1) // Assuming TextView's ID is 1
            @SuppressLint("ResourceType")
            val approveButton: Button = itemView.findViewById(2)   // Assuming Button's ID is 2
            @SuppressLint("ResourceType")
            val rejectButton: Button = itemView.findViewById(3)   // Assuming Button's ID is 3
        }

        @SuppressLint("ResourceType")
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
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

            // Create a Button for approving
            val approveButton = Button(parent.context).apply {
                id = 2 // Give it an ID
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = "Approve"
            }
            linearLayout.addView(approveButton)

            // Create a Button for rejecting
            val rejectButton = Button(parent.context).apply {
                id = 3 // Give it an ID
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = "Reject"
            }
            linearLayout.addView(rejectButton)

            return AdminViewHolder(linearLayout) // Pass the LinearLayout to the ViewHolder
        }

        override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
            val requestedAdmin = requestedAdminList[position]
            holder.usernameTextView.text = requestedAdmin.username

            holder.approveButton.setOnClickListener {
                onActionClicked(requestedAdmin, "approve") // Call the lambda for approve
            }

            holder.rejectButton.setOnClickListener {
                onActionClicked(requestedAdmin, "reject") // Call the lambda for reject
            }
        }

        override fun getItemCount() = requestedAdminList.size
    }

    private fun sendEmail(to: String, subject: String, body: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val properties = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.host", smtpHost)
                put("mail.smtp.port", smtpPort)
                put("mail.smtp.starttls.enable", "true")
            }

            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(senderEmail, senderPassword)
                }
            })

            try {
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(senderEmail))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                    this.subject = subject
                    this.setText(body)
                }
                Transport.send(message)
                Log.d("Email", "Email sent successfully.")

            } catch (e: MessagingException) {
                e.printStackTrace()
                Log.e("Email", "Error sending email: ${e.message}")
            }
        }
    }
}



























//working
//package com.example.sha_2
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//import androidx.core.view.setPadding
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.util.Properties
//import javax.mail.Authenticator
//import javax.mail.Message
//import javax.mail.MessagingException
//import javax.mail.PasswordAuthentication
//import javax.mail.Session
//import javax.mail.Transport
//import javax.mail.internet.InternetAddress
//import javax.mail.internet.MimeMessage
//
//// Data class to represent a Requested Admin
//data class RequestedAdminData(val uid: String, val username: String)
//
//class RequestedAdmin : AppCompatActivity() {
//
//    private lateinit var requestedAdminRecyclerView: RecyclerView
//    private lateinit var requestedAdminAdapter: AdminAdapter
//    private val requestedAdminList = mutableListOf<RequestedAdminData>()
//
//    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
//
//    // SMTP configuration for sending emails
//    private val smtpHost = "smtp.gmail.com"
//    private val smtpPort = "587"
//    private val senderEmail = "rcsea07@gmail.com" // Replace with your email address
//    private val senderPassword = "xbyd wzux vovr uldo" // Use your app password here
//
//    @SuppressLint("MissingInflatedId")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_requested_admin)
//
//        // Initialize UI components
//        requestedAdminRecyclerView = findViewById(R.id.RequestedAdminRecyclerView)
//        requestedAdminRecyclerView.layoutManager = LinearLayoutManager(this)
//        requestedAdminAdapter = AdminAdapter(requestedAdminList) { requestedAdmin, action ->
//            when (action) {
//                "approve" -> approveAdmin(requestedAdmin)
//                "reject" -> rejectAdmin(requestedAdmin)
//            }
//        }
//        requestedAdminRecyclerView.adapter = requestedAdminAdapter
//
//        // Load admin Usernames
//        loadAdminUsernames()
//
//        // Back button click listener.
//        findViewById<ImageView>(R.id.RequestedAdmin_back_Button).setOnClickListener {
//            startActivity(Intent(this, Profile::class.java))
//            finish()
//        }
//    }
//
//    private fun loadAdminUsernames() {
//        firestore.collection("RequestedAdmin")
//            .get()
//            .addOnSuccessListener { result ->
//                requestedAdminList.clear() // Clear the list before adding new data
//                for (document in result) {
//                    val uid = document.id // Document ID is the UID
//                    val name = document.getString("name") ?: "No Username" // Use "username" instead of "name"
//                    requestedAdminList.add(RequestedAdminData(uid, name))
//                }
//                requestedAdminAdapter.notifyDataSetChanged() // Refresh the RecyclerView
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error loading usernames: ${e.message}", Toast.LENGTH_SHORT)
//                    .show()
//            }
//    }
//
//    private fun approveAdmin(requestedAdmin: RequestedAdminData) {
//        // Get user data from RequestedAdmin collection
//        firestore.collection("RequestedAdmin").document(requestedAdmin.uid)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    val email = document.getString("email") ?: ""
//                    val name = document.getString("name") ?: ""
//
//                    // Copy data to Admin collection
//                    firestore.collection("Admin").document(requestedAdmin.uid)
//                        .set(document.data!!)
//                        .addOnSuccessListener {
//                            Toast.makeText(this, "Admin approved successfully", Toast.LENGTH_SHORT).show()
//
//                            sendEmail(email, "Your Request for Admin has been Approved", "You are now an Admin")
//                        }
//                        .addOnFailureListener { e ->
//                            Toast.makeText(this, "Error copying data to Admin: ${e.message}", Toast.LENGTH_SHORT).show()
//                        }
//                } else {
//                    Toast.makeText(this, "User data not found in RequestedAdmin", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error getting user data: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    private fun rejectAdmin(requestedAdmin: RequestedAdminData) {
//        // Get user data from RequestedAdmin collection
//        firestore.collection("RequestedAdmin").document(requestedAdmin.uid)
//            .get()
//
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    val email = document.getString("email") ?: ""
//
//                    Toast.makeText(this, "Requested admin rejected successfully", Toast.LENGTH_SHORT).show()
//
//                    sendEmail(email, "Your Request for Admin has been Rejected", "Your account is Terminated")
//                } else {
//                    Toast.makeText(this, "User data not found in RequestedAdmin", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error getting user data: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    // RecyclerView Adapter
//    private inner class AdminAdapter(
//        private val requestedAdminList: List<RequestedAdminData>,
//        private val onActionClicked: (RequestedAdminData, String) -> Unit // Lambda for delete and approve
//    ) : RecyclerView.Adapter<AdminAdapter.AdminViewHolder>() {
//
//        inner class AdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            @SuppressLint("ResourceType")
//            val usernameTextView: TextView = itemView.findViewById(1) // Assuming TextView's ID is 1
//            @SuppressLint("ResourceType")
//            val approveButton: Button = itemView.findViewById(2)   // Assuming Button's ID is 2
//            @SuppressLint("ResourceType")
//            val rejectButton: Button = itemView.findViewById(3)   // Assuming Button's ID is 3
//        }
//
//        @SuppressLint("ResourceType")
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
//            // Create a LinearLayout to hold the TextView and Button
//            val linearLayout = LinearLayout(parent.context).apply {
//                layoutParams = LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//                )
//                orientation = LinearLayout.HORIZONTAL
//                setPadding(16)
//            }
//
//            // Create a TextView for the username
//            val usernameTextView = TextView(parent.context).apply {
//                id = 1 // Give it an ID
//                layoutParams = LinearLayout.LayoutParams(
//                    0,
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    1f  // Weight of 1 to take up most of the space
//                )
//                textSize = 18f
//                setTextColor(ContextCompat.getColor(parent.context, android.R.color.black))
//            }
//            linearLayout.addView(usernameTextView)
//
//            // Create a Button for approving
//            val approveButton = Button(parent.context).apply {
//                id = 2 // Give it an ID
//                layoutParams = LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//                )
//                text = "Approve"
//            }
//            linearLayout.addView(approveButton)
//
//            // Create a Button for rejecting
//            val rejectButton = Button(parent.context).apply {
//                id = 3 // Give it an ID
//                layoutParams = LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//                )
//                text = "Reject"
//            }
//            linearLayout.addView(rejectButton)
//
//            return AdminViewHolder(linearLayout) // Pass the LinearLayout to the ViewHolder
//        }
//
//        override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
//            val requestedAdmin = requestedAdminList[position]
//            holder.usernameTextView.text = requestedAdmin.username
//
//            holder.approveButton.setOnClickListener {
//                onActionClicked(requestedAdmin, "approve") // Call the lambda for approve
//            }
//
//            holder.rejectButton.setOnClickListener {
//                onActionClicked(requestedAdmin, "reject") // Call the lambda for reject
//            }
//        }
//
//        override fun getItemCount() = requestedAdminList.size
//    }
//
//    private fun sendEmail(to: String, subject: String, body: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val properties = Properties().apply {
//                put("mail.smtp.auth", "true")
//                put("mail.smtp.host", smtpHost)
//                put("mail.smtp.port", smtpPort)
//                put("mail.smtp.starttls.enable", "true")
//            }
//
//            val session = Session.getInstance(properties, object : Authenticator() {
//                override fun getPasswordAuthentication(): PasswordAuthentication {
//                    return PasswordAuthentication(senderEmail, senderPassword)
//                }
//            })
//
//            try {
//                val message = MimeMessage(session).apply {
//                    setFrom(InternetAddress(senderEmail))
//                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
//                    this.subject = subject
//                    this.setText(body)
//                }
//                Transport.send(message)
//                Log.d("Email", "Email sent successfully.")
//
//            } catch (e: MessagingException) {
//                e.printStackTrace()
//                Log.e("Email", "Error sending email: ${e.message}")
//            }
//        }
//    }
//}