package com.example.myfiats

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import org.checkerframework.checker.units.qual.Length
import java.text.SimpleDateFormat
import java.util.*

class RegisterLayoutActivity : AppCompatActivity() {

    //Intents
    private lateinit var loginLayoutActivityIntent: Intent

    //Buttons
    private lateinit var goBackButton: Button
    private lateinit var registerButton: Button

    // EditText
    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var birthdateEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText

    // TextView
    private lateinit var errorBirthdateTextView: TextView
    private lateinit var errorPasswordTextView: TextView
    private lateinit var errorConfirmPasswordTextView: TextView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_layout)
        setUp()
    }

    // Set up function (Here are all declarations)
    private fun setUp() {
        supportActionBar?.hide()
        auth = Firebase.auth
        database = Firebase.firestore
        setUpViews()
        goBackButtonOnClick()
        setUpPopUpDataPickerAtBirthdateEditText()
        registerButtonOnClick()
    }

    // Function to set up all views in register_layout.xml
    private fun setUpViews() {
        loginLayoutActivityIntent = Intent(this@RegisterLayoutActivity, LoginLayoutActivity::class.java)
        goBackButton = findViewById(R.id.goBackButton)
        registerButton = findViewById(R.id.registerButton)
        nameEditText = findViewById(R.id.nameEditText)
        surnameEditText = findViewById(R.id.surnameEditText)
        birthdateEditText = findViewById(R.id.birthadteEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        errorBirthdateTextView = findViewById(R.id.errorBirthdateTextView)
        errorPasswordTextView = findViewById(R.id.errorPasswordTextView)
        errorConfirmPasswordTextView = findViewById(R.id.errorConfirmPasswordTextView)
    }

    // Functionality for goBackButton (Go back to login_layout.xml)
    private fun goBackButtonOnClick() {
        goBackButton.setOnClickListener {
            startActivity(loginLayoutActivityIntent)
        }
    }

    // Functionality for registerButtonOnClick (register new user in firebase)
    private fun registerButtonOnClick() {
        registerButton.setOnClickListener {
            checkEditTextsForContentAndRegisterNewUser()
        }
    }

    // Function is responsible for checking the correct text in EditTexts
    private fun checkEditTextsForContentAndRegisterNewUser() {
        // Extract all important information from EditTexts to register new user
        val nameEditTextIsNotEmpty = nameEditText.text.isNotEmpty()
        val surnameEditTextIsNotEmpty = surnameEditText.text.isNotEmpty()
        val birthdateEditTextIsNotEmpty = birthdateEditText.text.isNotEmpty()
        val emailEditTextIsNotEmpty = emailEditText.text.isNotEmpty()
        val passwordEditTextIsNotEmpty = passwordEditText.text.isNotEmpty()
        val confirmPasswordEditTextIsNotEmpty = confirmPasswordEditText.text.isNotEmpty()
        val EditTextsAreNotEmpty =
            nameEditTextIsNotEmpty && surnameEditTextIsNotEmpty && birthdateEditTextIsNotEmpty && emailEditTextIsNotEmpty && passwordEditTextIsNotEmpty && confirmPasswordEditTextIsNotEmpty
        if (EditTextsAreNotEmpty) {
            val passwordString = passwordEditText.text.toString()
            val confirmPasswordString = confirmPasswordEditText.text.toString()
            val passwordLength = passwordString.length
            val birthdateString = birthdateEditText.text.toString()
            val emailString = emailEditText.text.toString()
            val birthdateDate = SimpleDateFormat("dd.MM.yyyy").parse(birthdateString)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -18)
            val minimumBirthdateDate = calendar.time
            // Later I'm going to make more restricts passwords like min 8 signs, 1 special sign, 1 numeric sign, 1 big letter
            if ( passwordString == confirmPasswordString && passwordLength >= 6 && birthdateDate <= minimumBirthdateDate) {
                setEditTextsBackgroundsBlue()
                createNewAccountInFirebase(emailString, passwordString, birthdateString)
            } else {
                setEditTextsBackgroundsBlue()
                // Here will be code for handle underage birthdate
                handleUnderageBirthdate(birthdateDate, minimumBirthdateDate)
                // Here will be code for handle not equal passwords or not enough length
                handleNotIdenticalOrTooShortPasswords(passwordString, confirmPasswordString, passwordLength)
            }
        } else {
            setEditTextsBackgroundsBlue()
            // Here will be code for handle not content in EditTexts
        }
    }

    // Function for PopUp DataPicker while click at birthdateEditText
    private fun setUpPopUpDataPickerAtBirthdateEditText() {
        birthdateEditText.setOnClickListener {
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

    // Function which create new account in Firebase
    private fun createNewAccountInFirebase(emailString: String, passwordString: String, birthdateString: String) {
        auth.createUserWithEmailAndPassword(emailString, passwordString)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseAuthLogNameString = getString(R.string.firebaseAuthLogName)
                    Log.d(firebaseAuthLogNameString, "createUserWithEmail:sucess")
                    val nameString = nameEditText.text.toString()
                    val surnameString = surnameEditText.text.toString()
                    val user = hashMapOf(
                        "email" to emailString,
                        "name" to nameString,
                        "surname" to surnameString,
                        "birthdate" to birthdateString,
                    )
                    database.collection("users")
                        .add(user)
                        .addOnSuccessListener { documentReference ->
                            val firebaseFirestoreLogNameString = getString(R.string.firebaseFirestoreLogName)
                            Log.d(
                                firebaseFirestoreLogNameString,
                                "DocumentSnapshot added with ID: ${documentReference.id}"
                            )
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
    }

    // Function is responsible for handling underage birthdate in birthdateEditText
    private fun handleUnderageBirthdate(birthdateDate: Date, minimumBirthdateDate: Date) {
        if (birthdateDate >= minimumBirthdateDate) {
            val birthdateUnderageError = getString(R.string.birthdateUnderageError)
            errorBirthdateTextView.text = birthdateUnderageError
            errorBirthdateTextView.visibility = View.VISIBLE
            birthdateEditText.background = getDrawable(R.drawable.error_rounded_corner_view)
        }
    }

    // Function is responsible for handling not identical passwords in passwordEditText and confirmPasswordEditText
    private fun handleNotIdenticalOrTooShortPasswords(passwordString: String, confirmPasswordString: String, passwordLength: Int) {
        val passwordNotEnoughLongError = getString(R.string.passwordNotEnoughLongError)
        val passwordsAreNotTheSame = getString(R.string.passwordsAreNotTheSame)
        if (passwordString == confirmPasswordString && passwordLength < 6) {
            setRedBackgroundOfEditTextAndShowError(passwordEditText, errorPasswordTextView, passwordNotEnoughLongError)
        } else if (passwordString != confirmPasswordString && passwordLength >= 6) {
            setRedBackgroundOfEditTextAndShowError(confirmPasswordEditText, errorConfirmPasswordTextView, passwordsAreNotTheSame)
        } else if (passwordString != confirmPasswordString && passwordLength < 6) {
            setRedBackgroundOfEditTextAndShowError(passwordEditText, errorPasswordTextView, passwordNotEnoughLongError)
            setRedBackgroundOfEditTextAndShowError(confirmPasswordEditText, errorConfirmPasswordTextView, passwordsAreNotTheSame)
        }
    }

    // Function is responsible for handling not filled up EditTexts
    private fun handleNotFilledUpEditTexts() {

    }
    // Function to set chosen EditText's background red and show error
    fun setRedBackgroundOfEditTextAndShowError(errorEditText: EditText, errorTextView: TextView, errorMessage: String) {
        errorEditText.background = getDrawable(R.drawable.error_rounded_corner_view)
        errorTextView.text = errorMessage
        errorTextView.visibility = View.VISIBLE
    }
    // Function to set all EditTexts' backgrounds blue
    fun setEditTextsBackgroundsBlue() {
        nameEditText.background = getDrawable(R.drawable.rounded_corner_view)
        surnameEditText.background = getDrawable(R.drawable.rounded_corner_view)
        birthdateEditText.background = getDrawable(R.drawable.rounded_corner_view)
        emailEditText.background = getDrawable(R.drawable.rounded_corner_view)
        passwordEditText.background = getDrawable(R.drawable.rounded_corner_view)
        confirmPasswordEditText.background = getDrawable(R.drawable.rounded_corner_view)
        errorBirthdateTextView.visibility = View.INVISIBLE
        errorPasswordTextView.visibility = View.INVISIBLE
        errorConfirmPasswordTextView.visibility = View.INVISIBLE
    }
}