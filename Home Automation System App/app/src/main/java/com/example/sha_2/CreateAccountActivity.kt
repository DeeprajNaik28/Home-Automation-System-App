package com.example.sha_2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import java.util.Random
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var create_otp: EditText

    private lateinit var signUpButtonAdmin: Button
    private lateinit var signUpButtonGuest: Button

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // SMTP configuration for sending emails
    private val smtpHost = "smtp.gmail.com"
    private val smtpPort = "587"
    private val senderEmail = "rcsea07@gmail.com" // Replace with your email address
    private val senderPassword = "xbyd wzux vovr uldo" // Use your app password here

    // Store the generated OTP and its generation time
    private var generatedOtp: String? = null
    private var otpGenerationTime: Long = 0 // Store generation time in milliseconds

    // Define OTP expiration duration (e.g., 5 minutes)
    private val otpExpirationDuration: Long = 5 * 60 * 1000 // 5 minutes in milliseconds

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        create_otp = findViewById(R.id.create_otp)

        // Initialize UI elements
        usernameEditText = findViewById(R.id.username)
        phoneEditText = findViewById(R.id.phone)
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        nameEditText = findViewById(R.id.name)
        signUpButtonAdmin = findViewById(R.id.signUpButton_admin)
        signUpButtonGuest = findViewById(R.id.signUpButton_guest)

        // Set listeners for the sign-up button
        signUpButtonAdmin.setOnClickListener {
            handleAdminSignUp()

        }
        signUpButtonGuest.setOnClickListener { handleGuestSignUp() }

        // To move from create page to login page
        val textViewMoveToNext: TextView = findViewById(R.id.loginlink)
        textViewMoveToNext.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)  // Go to Login Activity
            startActivity(intent)
        }

        // Initially hide OTP field
        create_otp.visibility = View.GONE
    }

    // for admin
    private fun handleAdminSignUp() {
        if (validateInputs()) {
            if (generatedOtp == null) {
                sendAdminOtp()
            } else {
                verifyAdminOtp()
            }
        }

    }

    // for guest
    private fun handleGuestSignUp() {
        if (validateInputs()) {
            if (generatedOtp == null) {
                sendGuestOtp()
            } else {
                verifyGuestOtp()
            }
        }
    }

    private fun sendAdminOtp() {
        val email = emailEditText.text.toString().trim()
        generatedOtp = generateOTP(6) // Generate a 6-digit OTP
        otpGenerationTime = System.currentTimeMillis() // Store current time

        Log.d("OTP", "Generated OTP: $generatedOtp") // Log the generated OTP

        sendEmail(email, "Your OTP verification Code", "Your OTP code is: $generatedOtp valid for 5 mins") // Send the OTP via email

        Toast.makeText(this, "OTP sent to $email", Toast.LENGTH_SHORT).show()

        create_otp.visibility = View.VISIBLE // Show OTP input field after OTP is sent
    }

    private fun sendGuestOtp() {
        val email = emailEditText.text.toString().trim()
        generatedOtp = generateOTP(6) // Generate a 6-digit OTP
        otpGenerationTime = System.currentTimeMillis() // Store current time

        Log.d("OTP", "Generated OTP: $generatedOtp") // Log the generated OTP

        sendEmail(email, "Your OTP verification Code", "Your OTP code is: $generatedOtp valid for 5 mins") // Send the OTP via email

        Toast.makeText(this, "OTP sent to $email", Toast.LENGTH_SHORT).show()

        create_otp.visibility = View.VISIBLE // Show OTP input field after OTP is sent
    }

    private fun verifyAdminOtp() {
        val otp2 = create_otp.text.toString().trim()
        Log.d("OTP", "Entered OTP: $otp2") // Log the entered OTP

        if (otp2.isNotEmpty()) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - otpGenerationTime <= otpExpirationDuration) { // Check if OTP is still valid
                if (otp2 == generatedOtp) { // Validate the entered OTP
                    Toast.makeText(this, "OTP verified", Toast.LENGTH_SHORT).show()
                    createAdminAccount() // Proceed to account creation after successful verification
                } else {
                    Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "OTP has expired. Please request a new one.", Toast.LENGTH_SHORT).show()
                generatedOtp = null // Reset the generated OTP since it has expired
                create_otp.visibility = View.GONE // Hide the OTP input field
            }
        } else {
            Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyGuestOtp() {
        val otp2 = create_otp.text.toString().trim()
        Log.d("OTP", "Entered OTP: $otp2") // Log the entered OTP

        if (otp2.isNotEmpty()) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - otpGenerationTime <= otpExpirationDuration) { // Check if OTP is still valid
                if (otp2 == generatedOtp) { // Validate the entered OTP
                    Toast.makeText(this, "OTP verified", Toast.LENGTH_SHORT).show()
                    createGuestAccount() // Proceed to account creation after successful verification
                } else {
                    Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "OTP has expired. Please request a new one.", Toast.LENGTH_SHORT).show()
                generatedOtp = null // Reset the generated OTP since it has expired
                create_otp.visibility = View.GONE // Hide the OTP input field
            }
        } else {
            Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
        }
    }

    // creating account for admin
    private fun createAdminAccount() {
        if (validateInputs()) {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveAdminData()
                    } else {
                        Toast.makeText(this, "Account creation failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // creating account for guest
    private fun createGuestAccount() {
        if (validateInputs()) {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveGuestData()
                    } else {
                        Toast.makeText(this, "Account creation failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // saving admin data
    private fun saveAdminData() {
        val userId = auth.currentUser?.uid ?: return

        val userMap = hashMapOf(
            "userId" to userId,
            "name" to nameEditText.text.toString().trim(),
            "phone" to phoneEditText.text.toString().trim(),
            "email" to emailEditText.text.toString().trim(),
            "username" to usernameEditText.text.toString().trim(),
            "password" to passwordEditText.text.toString().trim()
        )

        firestore.collection("RequestedAdmin").document(userId).set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Request sent ! wait for approval", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))  // Redirect to Home Activity
                finish()  // Close this activity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // saving guest data
    private fun saveGuestData() {
        val userId = auth.currentUser?.uid ?: return

        val userMap = hashMapOf(
            "userId" to userId,
            "name" to nameEditText.text.toString().trim(),
            "phone" to phoneEditText.text.toString().trim(),
            "email" to emailEditText.text.toString().trim(),
            "username" to usernameEditText.text.toString().trim(),
            "password" to passwordEditText.text.toString().trim()
        )

        firestore.collection("Guest").document(userId).set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Home::class.java))  // Redirect to Home Activity
                finish()  // Close this activity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateInputs(): Boolean {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val name = nameEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()

        // Validate name
        if (name.isEmpty()) {
            nameEditText.error = "Name is required"
            return false
        }

        // Validate email
        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return false
        }

        if (!isValidEmail(email)) {
            emailEditText.error = "Invalid email format"
            return false
        }

        // Validate phone number
        if (phone.isEmpty()) {
            phoneEditText.error = "Phone number is required"
            return false
        }

        if (phone.length != 10 || !phone.all { it.isDigit() }) {
            phoneEditText.error = "Phone number must be 10 digits"
            return false
        }

        // Validate username
        if (username.isEmpty()) {
            usernameEditText.error = "Username is required"
            return false
        }

        if (username.length < 6) {
            usernameEditText.error = "Username must be at least 6 characters"
            return false
        }

        // Validate password
        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return false
        }

        if (password.length < 8) {
            passwordEditText.error = "Password must be at least 8 characters"
            return false
        }

        return true  // All validations passed
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun generateOTP(length: Int): String {
        val random = Random()
        val otp = StringBuilder()

        for (i in 0 until length) {
            otp.append(random.nextInt(10))  // Generate a random digit between 0 and 9
        }

        return otp.toString()
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
                Log.d("Email", "OTP sent successfully.")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreateAccountActivity, "OTP sent successfully.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: MessagingException) {
                e.printStackTrace()
                Log.e("Email", "Error sending email: ${e.message}")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreateAccountActivity, "Error sending email: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}































//package com.example.sha_2
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.util.Patterns
//import android.view.View
//import android.widget.Button
//import android.widget.EditText
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.material.textfield.TextInputEditText
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.util.Properties
//import java.util.Random
//import javax.mail.Authenticator
//import javax.mail.Message
//import javax.mail.MessagingException
//import javax.mail.PasswordAuthentication
//import javax.mail.Session
//import javax.mail.Transport
//import javax.mail.internet.InternetAddress
//import javax.mail.internet.MimeMessage
//
//class CreateAccountActivity : AppCompatActivity() {
//
//    private lateinit var usernameEditText: EditText
//    private lateinit var emailEditText: EditText
//    private lateinit var passwordEditText: EditText
//    private lateinit var nameEditText: EditText
//    private lateinit var phoneEditText: EditText
//    private lateinit var create_otp: EditText
//
//    private lateinit var signUpButtonAdmin: Button
//    private lateinit var signUpButtonGuest: Button
//
//    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
//    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
//
//    // SMTP configuration for sending emails
//    private val smtpHost = "smtp.gmail.com"
//    private val smtpPort = "587"
//    private val senderEmail = "rcsea07@gmail.com" // Replace with your email address
//    private val senderPassword = "xbyd wzux vovr uldo" // Use your app password here
//
//    // Store the generated OTP and its generation time
//    private var generatedOtp: String? = null
//    private var otpGenerationTime: Long = 0 // Store generation time in milliseconds
//
//    // Define OTP expiration duration (e.g., 5 minutes)
//    private val otpExpirationDuration: Long = 5 * 60 * 1000 // 5 minutes in milliseconds
//
//    @SuppressLint("MissingInflatedId")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_create_account)
//
//        create_otp = findViewById(R.id.create_otp)
//
//        // Initialize UI elements
//        usernameEditText = findViewById(R.id.username)
//        phoneEditText = findViewById(R.id.phone)
//        emailEditText = findViewById(R.id.email)
//        passwordEditText = findViewById(R.id.password)
//        nameEditText = findViewById(R.id.name)
//        signUpButtonAdmin = findViewById(R.id.signUpButton_admin)
//        signUpButtonGuest = findViewById(R.id.signUpButton_guest)
//
//        // Set listeners for the sign-up button
//        signUpButtonAdmin.setOnClickListener {
//            handleAdminSignUp()
//            if (validateInputs()) {
//                val name = findViewById<TextInputEditText>(R.id.name).text.toString().trim()
//                val intent = Intent(this, RequestedAdmin::class.java)
//                intent.putExtra("userName", name)
//                startActivity(intent)
//            }
//        }
//        signUpButtonGuest.setOnClickListener { handleGuestSignUp() }
//
//        // To move from create page to login page
//        val textViewMoveToNext: TextView = findViewById(R.id.loginlink)
//        textViewMoveToNext.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)  // Go to Login Activity
//            startActivity(intent)
//        }
//
//        // Initially hide OTP field
//        create_otp.visibility = View.GONE
//    }
//
//    // for admin
//    private fun handleAdminSignUp() {
//        if (validateInputs()) {
//            if (generatedOtp == null) {
//                sendAdminOtp()
//            } else {
//                verifyAdminOtp()
//            }
//        }
//
//    }
//
//    // for guest
//    private fun handleGuestSignUp() {
//        if (validateInputs()) {
//            if (generatedOtp == null) {
//                sendGuestOtp()
//            } else {
//                verifyGuestOtp()
//            }
//        }
//    }
//
//    private fun sendAdminOtp() {
//        val email = emailEditText.text.toString().trim()
//        generatedOtp = generateOTP(6) // Generate a 6-digit OTP
//        otpGenerationTime = System.currentTimeMillis() // Store current time
//
//        Log.d("OTP", "Generated OTP: $generatedOtp") // Log the generated OTP
//
//        sendEmail(email, "Your OTP verification Code", "Your OTP code is: $generatedOtp valid for 5 mins") // Send the OTP via email
//
//        Toast.makeText(this, "OTP sent to $email", Toast.LENGTH_SHORT).show()
//
//        create_otp.visibility = View.VISIBLE // Show OTP input field after OTP is sent
//    }
//
//    private fun sendGuestOtp() {
//        val email = emailEditText.text.toString().trim()
//        generatedOtp = generateOTP(6) // Generate a 6-digit OTP
//        otpGenerationTime = System.currentTimeMillis() // Store current time
//
//        Log.d("OTP", "Generated OTP: $generatedOtp") // Log the generated OTP
//
//        sendEmail(email, "Your OTP verification Code", "Your OTP code is: $generatedOtp valid for 5 mins") // Send the OTP via email
//
//        Toast.makeText(this, "OTP sent to $email", Toast.LENGTH_SHORT).show()
//
//        create_otp.visibility = View.VISIBLE // Show OTP input field after OTP is sent
//    }
//
//    private fun verifyAdminOtp() {
//        val otp2 = create_otp.text.toString().trim()
//        Log.d("OTP", "Entered OTP: $otp2") // Log the entered OTP
//
//        if (otp2.isNotEmpty()) {
//            val currentTime = System.currentTimeMillis()
//            if (currentTime - otpGenerationTime <= otpExpirationDuration) { // Check if OTP is still valid
//                if (otp2 == generatedOtp) { // Validate the entered OTP
//                    Toast.makeText(this, "OTP verified", Toast.LENGTH_SHORT).show()
//                    createAdminAccount() // Proceed to account creation after successful verification
//                } else {
//                    Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Toast.makeText(this, "OTP has expired. Please request a new one.", Toast.LENGTH_SHORT).show()
//                generatedOtp = null // Reset the generated OTP since it has expired
//                create_otp.visibility = View.GONE // Hide the OTP input field
//            }
//        } else {
//            Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun verifyGuestOtp() {
//        val otp2 = create_otp.text.toString().trim()
//        Log.d("OTP", "Entered OTP: $otp2") // Log the entered OTP
//
//        if (otp2.isNotEmpty()) {
//            val currentTime = System.currentTimeMillis()
//            if (currentTime - otpGenerationTime <= otpExpirationDuration) { // Check if OTP is still valid
//                if (otp2 == generatedOtp) { // Validate the entered OTP
//                    Toast.makeText(this, "OTP verified", Toast.LENGTH_SHORT).show()
//                    createGuestAccount() // Proceed to account creation after successful verification
//                } else {
//                    Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Toast.makeText(this, "OTP has expired. Please request a new one.", Toast.LENGTH_SHORT).show()
//                generatedOtp = null // Reset the generated OTP since it has expired
//                create_otp.visibility = View.GONE // Hide the OTP input field
//            }
//        } else {
//            Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    // creating account for admin
//    private fun createAdminAccount() {
//        if (validateInputs()) {
//            val email = emailEditText.text.toString().trim()
//            val password = passwordEditText.text.toString().trim()
//
//            auth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        saveAdminData()
//                    } else {
//                        Toast.makeText(this, "Account creation failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//        }
//    }
//
//    // creating account for guest
//    private fun createGuestAccount() {
//        if (validateInputs()) {
//            val email = emailEditText.text.toString().trim()
//            val password = passwordEditText.text.toString().trim()
//
//            auth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        saveGuestData()
//                    } else {
//                        Toast.makeText(this, "Account creation failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//        }
//    }
//
//    // saving admin data
//    private fun saveAdminData() {
//        val userId = auth.currentUser?.uid ?: return
//
//        val userMap = hashMapOf(
//            "userId" to userId,
//            "name" to nameEditText.text.toString().trim(),
//            "phone" to phoneEditText.text.toString().trim(),
//            "email" to emailEditText.text.toString().trim(),
//            "username" to usernameEditText.text.toString().trim(),
//            "password" to passwordEditText.text.toString().trim()
//        )
//
//        firestore.collection("Admin").document(userId).set(userMap)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show()
//                startActivity(Intent(this, Home::class.java))  // Redirect to Home Activity
//                finish()  // Close this activity
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    // saving guest data
//    private fun saveGuestData() {
//        val userId = auth.currentUser?.uid ?: return
//
//        val userMap = hashMapOf(
//            "userId" to userId,
//            "name" to nameEditText.text.toString().trim(),
//            "phone" to phoneEditText.text.toString().trim(),
//            "email" to emailEditText.text.toString().trim(),
//            "username" to usernameEditText.text.toString().trim(),
//            "password" to passwordEditText.text.toString().trim()
//        )
//
//        firestore.collection("Guest").document(userId).set(userMap)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show()
//                startActivity(Intent(this, Home::class.java))  // Redirect to Home Activity
//                finish()  // Close this activity
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    private fun validateInputs(): Boolean {
//        val username = usernameEditText.text.toString().trim()
//        val email = emailEditText.text.toString().trim()
//        val password = passwordEditText.text.toString().trim()
//        val name = nameEditText.text.toString().trim()
//        val phone = phoneEditText.text.toString().trim()
//
//        // Validate name
//        if (name.isEmpty()) {
//            nameEditText.error = "Name is required"
//            return false
//        }
//
//        // Validate email
//        if (email.isEmpty()) {
//            emailEditText.error = "Email is required"
//            return false
//        }
//
//        if (!isValidEmail(email)) {
//            emailEditText.error = "Invalid email format"
//            return false
//        }
//
//        // Validate phone number
//        if (phone.isEmpty()) {
//            phoneEditText.error = "Phone number is required"
//            return false
//        }
//
//        if (phone.length != 10 || !phone.all { it.isDigit() }) {
//            phoneEditText.error = "Phone number must be 10 digits"
//            return false
//        }
//
//        // Validate username
//        if (username.isEmpty()) {
//            usernameEditText.error = "Username is required"
//            return false
//        }
//
//        if (username.length < 6) {
//            usernameEditText.error = "Username must be at least 6 characters"
//            return false
//        }
//
//        // Validate password
//        if (password.isEmpty()) {
//            passwordEditText.error = "Password is required"
//            return false
//        }
//
//        if (password.length < 8) {
//            passwordEditText.error = "Password must be at least 8 characters"
//            return false
//        }
//
//        return true  // All validations passed
//    }
//
//    private fun isValidEmail(email: String): Boolean {
//        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
//    }
//
//    private fun generateOTP(length: Int): String {
//        val random = Random()
//        val otp = StringBuilder()
//
//        for (i in 0 until length) {
//            otp.append(random.nextInt(10))  // Generate a random digit between 0 and 9
//        }
//
//        return otp.toString()
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
//                Log.d("Email", "OTP sent successfully.")
//
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@CreateAccountActivity, "OTP sent successfully.", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: MessagingException) {
//                e.printStackTrace()
//                Log.e("Email", "Error sending email: ${e.message}")
//
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@CreateAccountActivity, "Error sending email: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//
//}
//
//
