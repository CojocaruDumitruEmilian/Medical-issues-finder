package com.example.proiectdiploma

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener

class SymptomsPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var resultTextView: TextView
    private lateinit var inputSymptomEditText: EditText
    private lateinit var doctorInfoTextView: TextView
    private var providerFirstName: String = ""
    private var providerLastName: String = ""
    private var selectedNpi: String = ""
       var potentialCausesArray: JSONArray?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms_page)

        resultTextView = findViewById(R.id.resultTextView)
        inputSymptomEditText = findViewById(R.id.inputSymptom)
        doctorInfoTextView = findViewById(R.id.doctorInfoTextView)

        val analyzeButton: Button = findViewById(R.id.analyzeButton)
        val logoutButton: Button = findViewById(R.id.logoutButton)
        val viewAppointmentsButton: Button = findViewById(R.id.viewAppointmentsButton)

        analyzeButton.setOnClickListener {
            val userSymptoms = inputSymptomEditText.text.toString()
            if (!userSymptoms.isEmpty()) {
                SymptomAnalyzer().execute(userSymptoms)
            } else {
                resultTextView.text = "Please enter a symptom."
            }
        }

        logoutButton.setOnClickListener {
            signOut()
        }

        doctorInfoTextView.setOnClickListener {
            val intent = Intent(this, AppointmentActivity::class.java)
            intent.putExtra("DoctorFirstName", providerFirstName)
            intent.putExtra("DoctorLastName", providerLastName)
            intent.putExtra("Npi", selectedNpi)
            Log.d("SymptomsPage", "ProviderFirstName: $providerFirstName, ProviderLastName: $providerLastName, Npi: $selectedNpi")
            startActivity(intent)
        }

        viewAppointmentsButton.setOnClickListener {
            // Deschide activitatea pentru afișarea programărilor utilizatorului
            val intent = Intent(this, UserAppointmentsActivity::class.java)


            startActivity(intent)
        }

        val voiceInputButton: Button = findViewById(R.id.voiceInputButton)

        voiceInputButton.setOnClickListener {
            startVoiceRecognition()
        }


    }

    private fun startVoiceRecognition() {
        Log.d("SymptomsPage", "startVoiceRecognition() called")

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US") // Setează limba după nevoie

        val recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SymptomsPage", "onReadyForSpeech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("SymptomsPage", "onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {
                Log.d("SymptomsPage", "onRmsChanged: $rmsdB")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d("SymptomsPage", "onBufferReceived")
            }

            override fun onEndOfSpeech() {
                Log.d("SymptomsPage", "onEndOfSpeech")
            }

            override fun onError(error: Int) {
                Log.e("SymptomsPage", "onError: $error")
            }

            override fun onResults(results: Bundle?) {
                Log.d("SymptomsPage", "onResults")
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val userSymptoms = matches[0]
                    Log.d("SymptomsPage", "Recognized: $userSymptoms")
                    inputSymptomEditText.setText(userSymptoms)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.d("SymptomsPage", "onPartialResults")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d("SymptomsPage", "onEvent: $eventType")
            }
        })

        recognizer.startListening(intent)
    }


    private fun getSpecialtyForSymptoms(symptoms: String): Pair<String, String> {
        return when {
            symptoms.contains("Malaria", ignoreCase = true) -> Pair("Neurologist", "1528098985")
            symptoms.contains("Common cold", ignoreCase = true) -> Pair("Internist", "1528098985")
            symptoms.contains("Muscle strain", ignoreCase = true) -> Pair("Orthopedic", "1164008991")
            symptoms.contains("Fever", ignoreCase = true) -> Pair("Internal Medicine", "1528098985")
            symptoms.contains("Skin", ignoreCase = true) -> Pair("Dermatologist", "1639380884")
            symptoms.contains("Lung", ignoreCase = true) -> Pair("Pulmonologist", "1013302090")
            symptoms.contains("Liver", ignoreCase = true) -> Pair("Hepatologist", "1346415700")
            else -> Pair("", "")
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
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                currentUser?.let { user ->
                    val potentialCauses = parseSymptomsResult(result)
                    addSymptomsToFirestore(user.uid, potentialCauses)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                resultTextView.text = "Error parsing response: ${e.message}"
            }
        }



        private fun parseSymptomsResult(result: String): List<String> {
            val potentialCausesList = mutableListOf<String>()

            try {
                val jsonObject = JSONObject(result)

                if (jsonObject.has("potentialCauses") && jsonObject["potentialCauses"] is JSONArray) {
                    val potentialCausesArray = jsonObject.getJSONArray("potentialCauses")

                    resultTextView.text = "Potential Causes:\n"

                    for (i in 0 until potentialCausesArray.length()) {
                        val potentialCause = potentialCausesArray.getString(i)
                        resultTextView.append("${i + 1}: \"$potentialCause\"\n")
                        potentialCausesList.add(potentialCause)
                    }

                    resultTextView.append("\n")

                    this@SymptomsPage.potentialCausesArray = potentialCausesArray

                    for (i in 0 until potentialCausesArray.length()) {
                        val potentialCause = potentialCausesArray.getString(i)

                        val (selectedSpecialty, selectedNpi) = getSpecialtyForSymptoms(potentialCause)

                        if (selectedSpecialty.isNotBlank()) {
                            resultTextView.append("\nNPI: $selectedNpi\n")
                            SearchDoctorTask(selectedNpi).execute()
                            break
                        }
                    }
                } else {
                    resultTextView.text = "Error: 'potentialCauses' key is missing or not an array in the JSON response."
                }

            } catch (e: JSONException) {
                e.printStackTrace()
                resultTextView.text = "Error parsing response: ${e.message}"
            }

            return potentialCausesList
        }
    }

    private fun addSymptomsToFirestore(uid: String, potentialCauses: List<String>) {
        val userDocument = FirebaseFirestore.getInstance().collection("users").document(uid)

        userDocument.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        // Document existent, actualizați lista de cauze potențiale
                        userDocument.update("potentialCauses", FieldValue.arrayUnion(*potentialCauses.toTypedArray()))
                            .addOnSuccessListener {
                                // resultTextView.text = "Potential Causes added to Firestore."
                            }
                            .addOnFailureListener { e ->
                                //resultTextView.text = "Error adding Potential Causes to Firestore: ${e.message}"
                            }
                    } else {
                        // Document inexistent, creați documentul și adăugați lista de cauze potențiale
                        userDocument.set(mapOf("potentialCauses" to potentialCauses))
                            .addOnSuccessListener {
                                // resultTextView.text = "Potential Causes added to Firestore."
                            }
                            .addOnFailureListener { e ->
                                //  resultTextView.text = "Error adding Potential Causes to Firestore: ${e.message}"
                            }
                    }
                } else {
                    resultTextView.text = "Error checking Firestore document: ${task.exception?.message}"
                }
            }
    }

    private inner class SearchDoctorTask(private val selectedNpi: String) : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void): String {
            return try {
                val apiUrl = "https://us-doctors-and-medical-professionals.p.rapidapi.com/search_npi?npi=$selectedNpi"
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
            try {
                parseDoctorInfo(result)
            } catch (e: JSONException) {
                e.printStackTrace()
                doctorInfoTextView.text = "Error parsing response: ${e.message}"
                doctorInfoTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun parseDoctorInfo(result: String) {
        try {
            val jsonObject = JSONObject(result)

            if (jsonObject.has("Data") && !jsonObject.isNull("Data")) {
                val dataObject = jsonObject.getJSONObject("Data")
                providerFirstName = dataObject.getString("Provider_First_Name")
                providerLastName = dataObject.getString("ProviderLastName_Legal_Name")

                doctorInfoTextView.text = "Name: $providerFirstName $providerLastName"
                doctorInfoTextView.visibility = View.VISIBLE
            } else {
                doctorInfoTextView.text = "No data available for the provided NPI."
                doctorInfoTextView.visibility = View.VISIBLE
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            doctorInfoTextView.text = "Error parsing response: ${e.message}"
            doctorInfoTextView.visibility = View.VISIBLE
        }
    }
}
