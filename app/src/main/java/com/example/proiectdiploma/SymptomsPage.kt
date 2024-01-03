package com.example.proiectdiploma

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class SymptomsPage : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private lateinit var inputSymptomEditText: EditText
    private lateinit var inputNpiEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms_page)

        resultTextView = findViewById(R.id.resultTextView)
        inputSymptomEditText = findViewById(R.id.inputSymptom)
        inputNpiEditText = findViewById(R.id.inputNpi)

        val analyzeButton: Button = findViewById(R.id.analyzeButton)
        val searchDoctorButton: Button = findViewById(R.id.searchDoctorButton)
        val logoutButton: Button = findViewById(R.id.logoutButton)

        analyzeButton.setOnClickListener {
            val userSymptoms = inputSymptomEditText.text.toString()

            if (!userSymptoms.isEmpty()) {
                SymptomAnalyzer().execute(userSymptoms)
            } else {
                resultTextView.text = "Please enter a symptom."
            }
        }

        searchDoctorButton.setOnClickListener {
            val userNpi = inputNpiEditText.text.toString()

            if (!userNpi.isEmpty()) {
                SearchDoctorTask().execute(userNpi)
            } else {
                resultTextView.text = "Please enter an NPI."
            }
        }

        logoutButton.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }

    private inner class SymptomAnalyzer : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg symptoms: String): String {
            return try {
                val apiUrl = "https://symptom-checker4.p.rapidapi.com/analyze"
                val url = URL(apiUrl)

                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.setRequestProperty("content-type", "application/json")
                urlConnection.setRequestProperty("X-RapidAPI-Key", "3805f589d9msh0dd1ef9116386fdp179a17jsnb1d6e94282e9")
                urlConnection.setRequestProperty("X-RapidAPI-Host", "symptom-checker4.p.rapidapi.com")
                urlConnection.requestMethod = "POST"

                val body = "{\"symptoms\": \"${symptoms[0]}\"}"

                urlConnection.doOutput = true
                val os: OutputStream = urlConnection.outputStream
                os.write(body.toByteArray(charset("utf-8")))
                os.close()

                val `in` = BufferedReader(InputStreamReader(urlConnection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (`in`.readLine().also { line = it } != null) {
                    response.append(line)
                }
                `in`.close()

                response.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                "Error occurred."
            }
        }

        override fun onPostExecute(result: String) {
            resultTextView.text = result
        }
    }

    private inner class SearchDoctorTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String): String {
            return try {
                val apiUrl = "https://us-doctors-and-medical-professionals.p.rapidapi.com/search_npi?npi=${params[0]}"
                val url = URL(apiUrl)

                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.setRequestProperty("X-RapidAPI-Key", "3805f589d9msh0dd1ef9116386fdp179a17jsnb1d6e94282e9")
                urlConnection.setRequestProperty("X-RapidAPI-Host", "us-doctors-and-medical-professionals.p.rapidapi.com")
                urlConnection.requestMethod = "GET"

                val `in` = BufferedReader(InputStreamReader(urlConnection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (`in`.readLine().also { line = it } != null) {
                    response.append(line)
                }
                `in`.close()

                response.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                "Error occurred."
            }
        }

        override fun onPostExecute(result: String) {
            resultTextView.text = result
        }
    }
}
