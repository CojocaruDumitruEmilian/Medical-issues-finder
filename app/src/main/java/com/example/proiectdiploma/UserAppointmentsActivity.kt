package com.example.proiectdiploma

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserAppointmentsActivity : AppCompatActivity() {

    private lateinit var appointmentsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_appointments)

        // Inițializează referința la nodul "appointments" pentru utilizatorul curent
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments/$uid")

            // Afișează toate programările pentru utilizatorul curent
            appointmentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val appointmentsList = mutableListOf<String>()

                    for (appointmentSnapshot in snapshot.children) {
                        val appointment = appointmentSnapshot.getValue(String::class.java)
                        appointment?.let {
                            appointmentsList.add(it)
                        }
                    }

                    val appointmentDetailsTextView: TextView = findViewById(R.id.appointmentDetailsTextView)
                    if (appointmentsList.isNotEmpty()) {
                        val allAppointmentsText = appointmentsList.joinToString("\n")
                        appointmentDetailsTextView.text = allAppointmentsText
                    } else {
                        appointmentDetailsTextView.text = "No appointments found."
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle errors here
                }
            })
        }

        val backToSymptomsButton: Button = findViewById(R.id.backToSymptomsButton)
        backToSymptomsButton.setOnClickListener {
            // Întoarce-te la pagina SymptomsPage
            val intent = Intent(this, SymptomsPage::class.java)
            startActivity(intent)
            finish()
        }
    }
}
