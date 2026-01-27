package com.example.sha_2


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import javax.mail.*
import javax.mail.internet.*


class ForgotPassword : AppCompatActivity() {


    private lateinit var emailEditText: EditText
    private lateinit var otpEditText: EditText
    private lateinit var confirmPasswordText: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var verifyOtpButton: Button
    private lateinit var confirmPasswordButton: Button


    // Firebase Authentication and Firestore instances
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
        setContentView(R.layout.activity_forgot_password)


        emailEditText = findViewById(R.id.emailEditText)
        otpEditText = findViewById(R.id.otpEditText)
        sendOtpButton = findViewById(R.id.sendOtpButton)
        verifyOtpButton = findViewById(R.id.verifyOtpButton)
        confirmPasswordText = findViewById(R.id.ConfirmpassText)
        confirmPasswordButton = findViewById(R.id.ConfirmpasswordButton)


        sendOtpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (isValidEmail(email)) {
                // Check if the email exists in either collection and get UID
                CoroutineScope(Dispatchers.IO).launch {
                    val result = determineUserCollectionAndUid(email) // Returns Pair<CollectionName?, Uid?>
                    withContext(Dispatchers.Main) {
                        if (result.first != null && result.second != null) { // Both collection and UID found
                            generatedOtp = generateOTP(6) // Generate a 6-digit OTP
                            otpGenerationTime = System.currentTimeMillis() // Store current time


                            Log.d("OTP", "Generated OTP: $generatedOtp")


                            sendEmail(email, "Your OTP verification Code", "Your OTP code is: $generatedOtp valid for 5 mins")


                            Toast.makeText(this@ForgotPassword, "OTP sent to $email", Toast.LENGTH_SHORT).show()


                            otpEditText.visibility = View.VISIBLE
                            verifyOtpButton.visibility = View.VISIBLE
                        } else {
                            Toast.makeText(this@ForgotPassword, "Email not found in Admin or Guest", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }


        verifyOtpButton.setOnClickListener {
            val otp2 = otpEditText.text.toString().trim()
            Log.d("OTP", "Entered OTP: $otp2")


            if (otp2.isNotEmpty()) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - otpGenerationTime <= otpExpirationDuration) {
                    if (otp2 == generatedOtp) {
                        Toast.makeText(this, "OTP verified", Toast.LENGTH_SHORT).show()


                        confirmPasswordText.visibility = View.VISIBLE
                        confirmPasswordButton.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "OTP has expired. Please request a new one.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            }
        }


        confirmPasswordButton.setOnClickListener {
            val newPassword = confirmPasswordText.text.toString().trim()
            if (validateNewPassword(newPassword)) {
                updatePasswordInFirestore(newPassword)
            }
        }


        // Back button click listener.
        val backButton = findViewById<ImageView>(R.id.repass_back_Button)
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }


    // Function to determine which collection the email is from AND get the UID
    private suspend fun determineUserCollectionAndUid(email: String): Pair<String?, String?> = withContext(Dispatchers.IO) {
        var collectionName: String? = null
        var uid: String? = null


        // Check Admin collection
        try {
            val adminQuery = firestore.collection("Admin").whereEqualTo("email", email).get().await()
            if (!adminQuery.isEmpty) {
                collectionName = "Admin"
                uid = adminQuery.documents[0].id // Get UID (document ID)
                Log.d("ForgotPass", "Found in Admin, UID: $uid")
                return@withContext Pair(collectionName, uid)
            }
        } catch (e: Exception) {
            Log.e("ForgotPass", "Error checking Admin: ${e.message}")
        }


        // Check Guest collection
        try {
            val guestQuery = firestore.collection("Guest").whereEqualTo("email", email).get().await()
            if (!guestQuery.isEmpty) {
                collectionName = "Guest"
                uid = guestQuery.documents[0].id // Get UID (document ID)
                Log.d("ForgotPass", "Found in Guest, UID: $uid")
                return@withContext Pair(collectionName, uid)
            }
        } catch (e: Exception) {
            Log.e("ForgotPass", "Error checking Guest: ${e.message}")
        }


        Log.d("ForgotPass", "Not found in either collection")
        return@withContext Pair(null, null) // Not found
    }


    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    private fun generateOTP(length: Int): String {
        val allowedCharacters = "0123456789"
        val otp = StringBuilder()
        val random = Random()
        for (i in 0 until length) {
            otp.append(allowedCharacters[random.nextInt(allowedCharacters.length)])
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
                    Toast.makeText(this@ForgotPassword, "OTP sent successfully.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: MessagingException) {
                e.printStackTrace()
                Log.e("Email", "Error sending email: ${e.message}")


                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ForgotPassword, "Error sending email: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun validateNewPassword(password: String): Boolean {
        return when {
            password.isEmpty() -> {
                confirmPasswordText.error = "Password is required"
                false
            }
            password.length < 8 -> {
                confirmPasswordText.error = "Password must be at least 8 characters"
                false
            }
            else -> true // Password is valid
        }
    }


    private fun updatePasswordInFirestore(newPassword: String) {
        val email = emailEditText.text.toString().trim()


        CoroutineScope(Dispatchers.IO).launch {
            val result = determineUserCollectionAndUid(email)
            val collectionName = result.first
            val uid = result.second


            withContext(Dispatchers.Main) {
                if (collectionName != null && uid != null) {
                    // Proceed with password update using the UID
                    firestore.collection(collectionName).document(uid) // Use UID to find the doc
                        .update("password", newPassword)
                        .addOnSuccessListener {
                            Toast.makeText(this@ForgotPassword, "Password updated successfully in $collectionName", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@ForgotPassword, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@ForgotPassword, "Error updating password in $collectionName: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this@ForgotPassword, "Email/UID not found.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

