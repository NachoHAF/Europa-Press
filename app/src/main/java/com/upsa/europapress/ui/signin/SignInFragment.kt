package com.upsa.europapress.ui.signin

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.upsa.europapress.R
import com.upsa.europapress.activities.CheckEmailActivity
import com.upsa.europapress.activities.MainActivity
import com.upsa.europapress.activities.PasswordRecoveryActivity
import com.upsa.europapress.activities.SignUpActivity
import com.upsa.europapress.databinding.FragmentSignInBinding
import www.sanju.motiontoast.MotionToast


class SignInFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentSignInBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.window?.statusBarColor = activity?.getColor(R.color.signin)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.signInElement2.setOnClickListener {
            val mEmail = binding.editTextTextEmailAddress.text.toString()
            val mPassword = binding.editTextTextPassword.text.toString()

            when {
                mEmail.isEmpty() || mPassword.isEmpty() -> {
                    MotionToast.createColorToast(
                        requireActivity(),
                        "Notice",
                        "Authentication failed！",
                        MotionToast.TOAST_WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                    )
                }
                else -> {
                    signIn(mEmail, mPassword)
                }
            }
        }
        binding.signUp.setOnClickListener {
            val intent = Intent(requireContext(), SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.forgetPass.setOnClickListener {
            val intent = Intent(requireContext(), PasswordRecoveryActivity::class.java)
            startActivity(intent)
        }

        // 暗夜模式&开启应用判断
        val appSettingPrefs: SharedPreferences =
            requireActivity().getSharedPreferences("AppSettingPrefs", 0)
        val isNightModeOn: Boolean = appSettingPrefs.getBoolean("NightMode", false)
        if (isNightModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity()) {
                task ->
            if (task.isSuccessful) {
                if (auth.currentUser?.isEmailVerified == true) {
                    Log.d("TAG", "signInWithEmail:success")
                    reload()
                } else {
                    val intent = Intent(requireContext(), CheckEmailActivity::class.java)
                    this.startActivity(intent)
                    activity?.finish()
                }
            } else {
                MotionToast.createColorToast(
                    requireActivity(),
                    "Notice",
                    "Authentication failed！",
                    MotionToast.TOAST_WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                )
            }
        }
    }

    private fun reload() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        this.startActivity(intent)
        activity?.finish()
    }
}


