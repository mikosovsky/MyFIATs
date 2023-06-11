package com.example.myfiats

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginLayoutActivity : AppCompatActivity() {

    //Intents
    private lateinit var registerLayoutActivityIntent: Intent
    private lateinit var loggedLayoutActivityIntent: Intent

    // Buttons
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button

    // EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    // Firebase
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)
        setUp()
    }

    // Set up function (Here are all declarations)
    private fun setUp() {
        supportActionBar?.hide()
        auth = Firebase.auth
        setUpViews()
        registerButtonOnClick()
        loginButtonOnClick()
    }

    // Functionality for registerButton (Go to register_layout.xml)
    private fun registerButtonOnClick() {
        registerButton.setOnClickListener {
            startActivity(registerLayoutActivityIntent)
        }
    }

    // Functionality for loginButton (Login user and go to logged_layout.xml)
    private fun loginButtonOnClick() {
        loginButton.setOnClickListener {
            val emailEditTextIsNotEmpty = emailEditText.text.isNotEmpty()
            val passwordEditTextIsNotEmpty = passwordEditText.text.isNotEmpty()
            if (emailEditTextIsNotEmpty && passwordEditTextIsNotEmpty) {
                val emailString = emailEditText.text.toString()
                val passwordString = passwordEditText.text.toString()
                auth.signInWithEmailAndPassword(emailString, passwordString)
                    .addOnCompleteListener(this) { task ->
                        val TAG = getString(R.string.firebaseAuthLogName)
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            loggedLayoutActivityIntent.putExtra("email",emailString)
                            startActivity(loggedLayoutActivityIntent)

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }

            } else {

            }
        }
    }

    // Function to set up all views in login_layout.xml
    private fun setUpViews() {
        registerLayoutActivityIntent = Intent(this@LoginLayoutActivity, RegisterLayoutActivity::class.java)
        loggedLayoutActivityIntent = Intent(this@LoginLayoutActivity, LoggedLayoutActivity::class.java)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
    }
}