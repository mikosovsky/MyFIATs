package com.example.myfiats

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class LoginLayoutActivity : AppCompatActivity() {

    //Intents
    private lateinit var registerLayoutActivityIntent:Intent
    private lateinit var loggedLayoutActivityIntent:Intent
    // Buttons
    private lateinit var registerButton:Button
    private lateinit var loginButton:Button
    // EditText
    private lateinit var emailEditText:EditText
    private lateinit var passwordEditText:EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)
        setUp()
    }
    // Set up function (Here are all declarations
    private fun setUp() {
        supportActionBar?.hide()
        setUpViews()
        registerButtonOnClick()
    }
    // Functionality for registerButton
    private fun registerButtonOnClick() {
        registerButton.setOnClickListener {
            startActivity(registerLayoutActivityIntent)
        }
    }
    // Function to set up all views in login_layout.xml
    private fun setUpViews(){
        registerLayoutActivityIntent = Intent(this@LoginLayoutActivity, RegisterLayoutActivity::class.java)
        loggedLayoutActivityIntent = Intent(this@LoginLayoutActivity, LoggedLayoutActivity::class.java)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
    }
}