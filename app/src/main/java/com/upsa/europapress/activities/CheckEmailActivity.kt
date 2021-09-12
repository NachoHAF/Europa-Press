package com.upsa.europapress.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.upsa.europapress.R
import com.upsa.europapress.databinding.ActivityCheckEmailBinding
import www.sanju.motiontoast.MotionToast

class CheckEmailActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityCheckEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize Firebase Auth
        auth = Firebase.auth

        val user = auth.currentUser
        binding.button2.setOnClickListener {
            val profileUpdates = userProfileChangeRequest {}

            user!!.updateProfile(profileUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (user.isEmailVerified) {
                        reload()
                    } else {
                        MotionToast.createColorToast(
                            this,
                            "Verification email sent!",
                            "Please go to the email to verify！",
                            MotionToast.TOAST_SUCCESS,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this, R.font.helvetica_regular)
                        )
                    }
                }
            }
        }

        binding.backIcon.setOnClickListener { signOut() }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                reload()
            } else {
                sendEmailVerfication()
            }
        }
    }

    private fun sendEmailVerfication() {
        val user = auth.currentUser
        user!!.sendEmailVerification().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                MotionToast.createColorToast(
                    this,
                    "Verification email sent!",
                    "Please go to the email to verify！",
                    MotionToast.TOAST_SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, R.font.helvetica_regular)
                )
            }
        }
    }

    private fun reload() {
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
        finish()
    }

    private fun signOut() {
        Firebase.auth.signOut()
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}