package com.example.proiectdiploma

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker

class AppointmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment)

        val doctorFirstName = intent.getStringExtra("DoctorFirstName")
        val doctorLastName = intent.getStringExtra("DoctorLastName")
        val npi = intent.getStringExtra("Npi")

        val doctorNameTextView: TextView = findViewById(R.id.doctorsNameTextView)
        val datePicker: DatePicker = findViewById(R.id.datePicker)
        val timePicker: TimePicker = findViewById(R.id.timePicker)
        val confirmAppointmentButton: Button = findViewById(R.id.confirmAppointmentButton)

        doctorNameTextView.text = "Doctor's Name: $doctorFirstName $doctorLastName"

        confirmAppointmentButton.setOnClickListener {
            val selectedDate = "${datePicker.year}-${datePicker.month + 1}-${datePicker.dayOfMonth}"
            val selectedTime = "${timePicker.hour}:${timePicker.minute}"

            val appointmentDetails = "Appointment with $doctorFirstName $doctorLastName on $selectedDate at $selectedTime"

            // Afiseaza detalii într-un TextView sau în alt mod dorit
            val appointmentDetailsTextView: TextView = findViewById(R.id.appointmentDetailsTextView)
            appointmentDetailsTextView.text = appointmentDetails

            val intent = Intent(this, UserAppointmentsActivity::class.java)
            intent.putExtra("DoctorFirstName", doctorFirstName)
            intent.putExtra("DoctorLastName", doctorLastName)
            intent.putExtra("SelectedDate", selectedDate)
            intent.putExtra("SelectedTime", selectedTime)
            startActivity(intent)
            finish()
        }
    }
}
