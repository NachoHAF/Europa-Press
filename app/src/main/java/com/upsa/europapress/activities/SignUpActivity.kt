package com.upsa.europapress.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.upsa.europapress.R
import com.upsa.europapress.databinding.ActivitySignUpBinding
import www.sanju.motiontoast.MotionToast
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.signup)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth
        databaseReference =
            FirebaseDatabase.getInstance(
                "https://europa-press-default-rtdb.europe-west1.firebasedatabase.app/"
            )
                .getReference("Users")

        binding.signUpElement2.setOnClickListener {
            val mUserName = binding.editTextTextUserName.text.toString()
            val mEmail = binding.editTextTextEmailAddress.text.toString()
            val mPassword = binding.editTextTextPassword.text.toString()
            val mRepeatPassword = binding.repeatTextTextPassword.text.toString()

            val passwordRegex = Pattern.compile("^" + "(?=.*[-@#$%^&+=])" + ".{6,}" + "$")

            if (mUserName.isEmpty()) {
                MotionToast.createColorToast(
                    this,
                    "Notice",
                    "Please enter a valid username！",
                    MotionToast.TOAST_WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, R.font.helvetica_regular)
                )
            } else if (mEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {
                MotionToast.createColorToast(
                    this,
                    "Notice",
                    "Please enter a valid email address！",
                    MotionToast.TOAST_WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, R.font.helvetica_regular)
                )
            } else if (mPassword.isEmpty() || !passwordRegex.matcher(mPassword).matches()) {
                MotionToast.createColorToast(
                    this,
                    "Notice",
                    " More than 6 characters and contain at least one special character!",
                    MotionToast.TOAST_WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, R.font.helvetica_regular)
                )
            } else if (mPassword != mRepeatPassword) {
                MotionToast.createColorToast(
                    this,
                    "Notice",
                    "Inconsistent password twice！",
                    MotionToast.TOAST_WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, R.font.helvetica_regular)
                )
            } else {
                createAccount(mEmail, mPassword)
            }
        }

        binding.signIn.setOnClickListener { onBackPressed() }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val currentUser = auth.currentUser?.uid
                if (currentUser != null) {
                    databaseReference
                        .child(currentUser)
                        .child("username")
                        .setValue(binding.editTextTextUserName.text.toString())
                }

                intent = Intent(this, CheckEmailActivity::class.java)
                startActivity(intent)
            } else {
                MotionToast.createColorToast(
                    this,
                    "Notice",
                    "Email has been registered, please Sign in directly！",
                    MotionToast.TOAST_WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, R.font.helvetica_regular)
                )
            }
        }
    }
}
