package com.upsa.europapress.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.upsa.europapress.R
import com.upsa.europapress.databinding.ActivityPasswordRecoveryBinding
import www.sanju.motiontoast.MotionToast

class PasswordRecoveryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordRecoveryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPasswordRecoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button3.setOnClickListener {
            val emailAddress = binding.emailEditText.text.toString()
            if (emailAddress.isNotEmpty()) {
                Firebase.auth.sendPasswordResetEmail(emailAddress).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        MotionToast.createColorToast(
                            this,
                            "Success",
                            "Email sent successfully, please check!",
                            MotionToast.TOAST_SUCCESS,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this, R.font.helvetica_regular)
                        )
                        Handler().postDelayed({ onBackPressed() }, 1000)
                    } else {
                        MotionToast.createColorToast(
                            this,
                            "Notice",
                            "Please fill in the correct email address!",
                            MotionToast.TOAST_WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this, R.font.helvetica_regular)
                        )
                    }
                }
            } else {
                MotionToast.createColorToast(
                    this,
                    "Notice",
                    "Please fill in the correct email address!",
                    MotionToast.TOAST_WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, R.font.helvetica_regular)
                )
            }
        }

        binding.goBack.setOnClickListener { onBackPressed() }
    }
}
