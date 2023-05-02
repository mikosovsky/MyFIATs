package com.example.myfiats

import android.app.DatePickerDialog
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import java.text.SimpleDateFormat
import java.util.*

class RegisterLayoutActivity : AppCompatActivity() {

    //Intents
    private lateinit var loginLayoutActivityIntent: Intent
    //Buttons
    private lateinit var goBackButton:Button
    private lateinit var registerButton: Button
    // EditText
    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var birthdateEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_layout)
        setUp()
    }
    // Set up function (Here are all declarations)
    private fun setUp(){
        supportActionBar?.hide()
        auth = Firebase.auth
        database = Firebase.firestore
        setUpViews()
        goBackButtonOnClick()
        setUpPopUpDataPickerAtBirthdateEditText()
        registerButtonOnClick()
    }
    // Function to set up all views in register_layout.xml
    private fun setUpViews(){
        loginLayoutActivityIntent = Intent(this@RegisterLayoutActivity, LoginLayoutActivity::class.java)
        goBackButton = findViewById(R.id.goBackButton)
        registerButton = findViewById(R.id.registerButton)
        nameEditText = findViewById(R.id.nameEditText)
        surnameEditText = findViewById(R.id.surnameEditText)
        birthdateEditText = findViewById(R.id.birthadteEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
    }
    // Functionality for goBackButton (Go back to login_layout.xml)
    private fun goBackButtonOnClick(){
        goBackButton.setOnClickListener {
            startActivity(loginLayoutActivityIntent)
        }
    }
    // Functionality for registerButtonOnClick (register new user in firebase)
    private fun registerButtonOnClick(){
        registerButton.setOnClickListener {
            checkEditTextsForContent()
        }
    }
    // Function is responsible for checking the correct text in EditTexts
    private fun checkEditTextsForContent(){
        val nameEditTextIsNotEmpty = nameEditText.text.isNotEmpty()
        val surnameEditTextIsNotEmpty = surnameEditText.text.isNotEmpty()
        val birthdateEditTextIsNotEmpty = birthdateEditText.text.isNotEmpty()
        val emailEditTextIsNotEmpty = emailEditText.text.isNotEmpty()
        val passwordEditTextIsNotEmpty = passwordEditText.text.isNotEmpty()
        val confirmPasswordEditTextIsNotEmpty = confirmPasswordEditText.text.isNotEmpty()
        if (nameEditTextIsNotEmpty && surnameEditTextIsNotEmpty && birthdateEditTextIsNotEmpty && emailEditTextIsNotEmpty && passwordEditTextIsNotEmpty && confirmPasswordEditTextIsNotEmpty) {
            val passwordString = passwordEditText.text.toString()
            val confirmPasswordString = confirmPasswordEditText.text.toString()
            val passwordLength = passwordString.length
            // Later I'm going to make more restricts passwords like min 8 signs, 1 special sign, 1 numeric sign, 1 big letter
            if (passwordString == confirmPasswordString && passwordLength >= 6) {
                val birthdateString = birthdateEditText.text.toString()
                val birthdateDate = SimpleDateFormat("dd.MM.yyyy").parse(birthdateString)
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.YEAR,-18)
                val minimumBirthdateDate = calendar.time
                if (birthdateDate <= minimumBirthdateDate) {
                    // Firebase sign up code to refactor
                    val emailString = emailEditText.text.toString()
                    auth.createUserWithEmailAndPassword(emailString,passwordString)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val firebaseAuthLogNameString = getString(R.string.firebaseAuthLogName)
                                Log.d(firebaseAuthLogNameString,"createUserWithEmail:sucess")
                                val nameString = nameEditText.text.toString()
                                val surnameString = surnameEditText.text.toString()
                                val user = hashMapOf(
                                    "email" to emailString,
                                    "name" to nameString,
                                    "surname" to surnameString,
                                    "birthdate" to birthdateString,
                                )
                                print(user)
                                database.collection("users")
                                    .add(user)
                                    .addOnSuccessListener { documentReference ->
                                        val firebaseFirestoreLogNameString = getString(R.string.firebaseFirestoreLogName)
                                        Log.d(firebaseFirestoreLogNameString, "DocumentSnapshot added with ID: ${documentReference.id}")
                                        auth.signOut()
                                        startActivity(loginLayoutActivityIntent)
                                    }
                                    .addOnFailureListener { e ->
                                        val firebaseFirestoreLogNameString = getString(R.string.firebaseFirestoreLogName)
                                        Log.w(firebaseFirestoreLogNameString, "Error adding document", e)
                                    }

                            } else {
                                val firebaseAuthLogNameString = getString(R.string.firebaseAuthLogName)
                                Log.w(firebaseAuthLogNameString, "createUserWithEmail:failure", task.exception)
                                Toast.makeText(
                                    baseContext,
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                } else{
                    // Here will be code for handle underage birthdate
                }
            }else {
                // Here will be code for handle not equal passwords
            }
        } else {
            // Here will be code for handle not content in EditTexts
        }

    }

    // Function for PopUp DataPicker while click at birthdateEditText
    private fun setUpPopUpDataPickerAtBirthdateEditText(){
        birthdateEditText.setOnClickListener{
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                this,
                { view, year, monthOfYear, dayOfMonth ->
                    val date = (dayOfMonth.toString() + "." + (monthOfYear + 1) + "." + year)
                    birthdateEditText.setText(date)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }
}