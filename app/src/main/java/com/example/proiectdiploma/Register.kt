package com.example.proiectdiploma

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {

    //private lateinit var binding: ActivityMainBinding
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonReg: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var progressbar: ProgressBar
    private lateinit var textView: TextView

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)


        editTextEmail=findViewById(R.id.email)
        editTextPassword=findViewById(R.id.password)
        editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD  // Adaugă această linie

        buttonReg=findViewById(R.id.btn_register)
        progressbar=findViewById(R.id.progressBar)
        textView=findViewById(R.id.loginNow)

        textView.setOnClickListener{
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }

        buttonReg.setOnClickListener{
            progressbar.visibility = View.VISIBLE
            val email=editTextEmail.getText().toString()
            val password=editTextPassword.getText().toString()

            if(TextUtils.isEmpty(email)){
                Toast.makeText(this,"Enter email", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if(TextUtils.isEmpty(password)){
                Toast.makeText(this,"Enter password", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressbar.visibility = View.VISIBLE

                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Toast.makeText(
                            this,
                            "Account created",
                            Toast.LENGTH_SHORT,
                        ).show()

                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            this,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        Log.e(ContentValues.TAG, "Authentication failed", task.exception)

                    }
                }

        }



    }
}