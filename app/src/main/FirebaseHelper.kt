package com.example.sha_2


    import com.google.firebase.database.DatabaseReference
    import com.google.firebase.database.FirebaseDatabase
    import kotlinx.coroutines.tasks.await

    class FirebaseHelper {
        private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

        suspend fun fetchCommand(appliance: String): String {
            return try {
                val commandSnapshot = database.child("appliance").child(appliance).child("commands").get().await()
                commandSnapshot.value.toString()
            } catch (e: Exception) {
                ""
            }
        }

        suspend fun fetchIRCodes(appliance: String, command: String): List<String> {
            return try {
                val irCodesSnapshot = database.child("appliance").child(appliance).child(command).get().await()
                irCodesSnapshot.children.map { it.value.toString() }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
