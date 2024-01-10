package com.example.proiectdiploma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class UserAppointmentsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_appointments)

        // Primiți detalii despre programare din Intent
        val doctorFirstName = intent.getStringExtra("DoctorFirstName")
        val doctorLastName = intent.getStringExtra("DoctorLastName")
        val selectedDate = intent.getStringExtra("SelectedDate")
        val selectedTime = intent.getStringExtra("SelectedTime")

        // Afiseaza detalii pe ecran
        val appointmentDetailsTextView: TextView = findViewById(R.id.appointmentDetailsTextView)
        appointmentDetailsTextView.text = "Appointment with Dr. $doctorFirstName $doctorLastName on $selectedDate at $selectedTime"
    }
}
