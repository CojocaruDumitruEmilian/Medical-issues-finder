package com.example.proiectdiploma

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
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
    private lateinit var doctorInfoTextView: TextView

    private var selectedNpi: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms_page)

        resultTextView = findViewById(R.id.resultTextView)
        inputSymptomEditText = findViewById(R.id.inputSymptom)
        inputNpiEditText = findViewById(R.id.inputNpi)
        doctorInfoTextView = findViewById(R.id.doctorInfoTextView)


        val analyzeButton: Button = findViewById(R.id.analyzeButton)
        val searchDoctorButton: Button = findViewById(R.id.searchDoctorButton)
        val logoutButton: Button = findViewById(R.id.logoutButton)

        analyzeButton.setOnClickListener {
            val userSymptoms = inputSymptomEditText.text.toString()

            if (!userSymptoms.isEmpty()) {
                SymptomAnalyzer().execute(userSymptoms)
                /*val selectedDoctor = getSpecialtyForSymptoms(userSymptoms)
                if (selectedDoctor.isNotEmpty()) {
                    resultTextView.text = "Recommended doctor for symptoms: $selectedDoctor"
                    SearchDoctorTask().execute(selectedDoctor)
                } else {
                    resultTextView.text = "No matching doctor found for the given symptoms."
                }*/


                //
            } else {
                resultTextView.text = "Please enter a symptom."
            }
        }

        searchDoctorButton.setOnClickListener {
            val userSymptoms = inputSymptomEditText.text.toString()
            var selectedNpi: String = ""

            if (!userSymptoms.isEmpty()) {
                val selectedDoctor = getSpecialtyForSymptoms(userSymptoms)
                if (selectedDoctor.second.isNotEmpty()) {
                    selectedNpi = selectedDoctor.second
                    SearchDoctorTask(selectedNpi).execute()
                } else {
                    resultTextView.text = "No matching doctor found for the given symptoms."
                }
            } else {
                resultTextView.text = "Please enter a symptom."
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

    private fun getSpecialtyForSymptoms(symptoms: String): Pair<String, String> {
        // Aici poți adăuga logica ta pentru a determina specialitatea doctorului și NPI-ul în funcție de simptome
        // În exemplul de mai jos, se face o simplă comparare cu un șir de simptome predefinit
        return when {
            symptoms.contains("Malaria", ignoreCase = true) -> Pair("Neurologist", "1528098985")
            symptoms.contains("Common cold", ignoreCase = true) -> Pair("doc", "1528098985")
            symptoms.contains("Muscle strain", ignoreCase = true) -> Pair("wassup", "1164008991")
            symptoms.contains("fever", ignoreCase = true) -> Pair("Internal Medicine", "1528098985")
            // Adaugă alte cazuri pentru alte simptome sau modifică după necesitate
            else -> Pair("", "")
        }
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
                val jsonObject = JSONObject(response.toString())
                val npi = jsonObject.optString("npi", "")

                // Returnează un Pair cu specialitatea și NPI-ul
                getSpecialtyForSymptoms(symptoms[0]).copy(second = npi)

                response.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                "Error occurred."

            }
        }

        override fun onPostExecute(result: String) {
            resultTextView.text = "Potential Causes:\n"

            try {
                // Parsează răspunsul JSON
                val jsonObject = JSONObject(result)

                // Verifică dacă cheia "potentialCauses" există în răspunsul JSON și este un array
                if (jsonObject.has("potentialCauses") && jsonObject["potentialCauses"] is JSONArray) {
                    // Extrage lista de potențiale cauze
                    val potentialCausesArray = jsonObject.getJSONArray("potentialCauses")

                    // Afișează lista de potențiale cauze în TextView
                    resultTextView.append("Potential Causes:\n")

                    for (i in 0 until potentialCausesArray.length()) {
                        val potentialCause = potentialCausesArray.getString(i)
                        resultTextView.append("${i + 1}: \"$potentialCause\"\n")
                    }

                    // Afișează o linie goală înainte de recomandare
                    resultTextView.append("\n")

                    // Verifică dacă există cel puțin o potențială cauză care corespunde cu specialitatea introdusă manual
                    for (i in 0 until potentialCausesArray.length()) {
                        val potentialCause = potentialCausesArray.getString(i)

                        // Verifică dacă potențiala cauză corespunde cu specialitatea introdusă manual
                        val (selectedSpecialty, selectedNpi) = getSpecialtyForSymptoms(potentialCause)

                        if (selectedSpecialty.isNotBlank()) {
                            resultTextView.append("Recommended doctor for matching cause \"$potentialCause\": $selectedSpecialty\nNPI: $selectedNpi\n")

                            // Execută SearchDoctorTask() aici, după ce ai găsit o potențială cauză care corespunde
                            SearchDoctorTask(selectedNpi).execute()

                            // Întrerupe bucla după ce ai găsit o potențială cauză care corespunde
                            break
                        }
                    }

                    // Afiseaza un mesaj dacă nu s-a găsit nicio potențială cauză care să corespundă
                    resultTextView.append("No matching doctor found for the given symptoms.\n")
                } else {
                    resultTextView.text = "Error: 'potentialCauses' key is missing or not an array in the JSON response."
                }

            } catch (e: JSONException) {
                e.printStackTrace()
                resultTextView.text = "Error parsing response: ${e.message}"
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
                // Parsează răspunsul JSON
                val jsonObject = JSONObject(result)

                // Verifică dacă cheia "Data" există în răspunsul JSON și nu este null
                if (jsonObject.has("Data") && !jsonObject.isNull("Data")) {
                    val dataObject = jsonObject.getJSONObject("Data")

                    // Extrage numele și prenumele
                    val providerFirstName = dataObject.getString("Provider_First_Name")
                    val providerLastName = dataObject.getString("ProviderLastName_Legal_Name")


                    // Afișează numele și prenumele în TextView
                    doctorInfoTextView.text = "Name: $providerFirstName $providerLastName"

                    doctorInfoTextView.visibility = View.VISIBLE
                } else {
                    resultTextView.text = "npiii: $selectedNpi"
                    doctorInfoTextView.text = ""
                    doctorInfoTextView.visibility = View.GONE
                    //  resultTextView.text = "No data available for the provided NPI."
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                doctorInfoTextView.text = "Error parsing response: ${e.message}"
                doctorInfoTextView.visibility = View.VISIBLE
            }
        }
    }

}
