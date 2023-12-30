package com.example.proiectdiploma

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class SymptomsPage : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private lateinit var inputSymptomEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms_page)

        resultTextView = findViewById(R.id.resultTextView)
        inputSymptomEditText = findViewById(R.id.inputSymptom)
        val analyzeButton: Button = findViewById(R.id.analyzeButton)

        analyzeButton.setOnClickListener {
            // Obține simptomele introduse de utilizator
            val userSymptoms = inputSymptomEditText.text.toString()

            // Verifică dacă utilizatorul a introdus un simptom
            if (!userSymptoms.isEmpty()) {
                // Simpla cerere de exemplu
                SymptomAnalyzer().execute(userSymptoms)
            } else {
                // Dacă nu s-a introdus niciun simptom, afișează un mesaj de eroare
                resultTextView.text = "Please enter a symptom."
            }
        }
    }

    private inner class SymptomAnalyzer : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg symptoms: String): String {
            return try {
                // Construiește URL-ul cererii HTTP
                val apiUrl = "https://symptom-checker4.p.rapidapi.com/analyze"
                val url = URL(apiUrl)

                // Deschide conexiunea HTTP
                val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                urlConnection.setRequestProperty("content-type", "application/json")
                urlConnection.setRequestProperty("X-RapidAPI-Key", "3805f589d9msh0dd1ef9116386fdp179a17jsnb1d6e94282e9")
                urlConnection.setRequestProperty("X-RapidAPI-Host", "symptom-checker4.p.rapidapi.com")
                urlConnection.requestMethod = "POST"

                // Construiește corpul cererii din simptomele primite
                val body = "{\"symptoms\": \"${symptoms[0]}\"}"

                // Trimite corpul cererii
                urlConnection.doOutput = true
                val os: OutputStream = urlConnection.outputStream
                os.write(body.toByteArray(charset("utf-8")))
                os.close()

                // Citeste raspunsul
                val `in` = BufferedReader(InputStreamReader(urlConnection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (`in`.readLine().also { line = it } != null) {
                    response.append(line)
                }
                `in`.close()

                // Returneaza raspunsul
                response.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                "Error occurred."
            }
        }

        override fun onPostExecute(result: String) {
            // Afișează rezultatul în TextView
            resultTextView.text = result
        }
    }
}
