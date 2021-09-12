package com.upsa.europapress.ui.signin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.upsa.europapress.R
import com.upsa.europapress.activities.MainActivity


class SplashFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.statusBarColor = activity?.getColor(R.color.splash)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize Firebase Auth
        auth = Firebase.auth
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                Handler(Looper.getMainLooper()).postDelayed({ reload() }, 2500)
            }
        } else {
            Handler(Looper.getMainLooper())
                .postDelayed(
                    {
                        if (onBoardingFinished()) {
                            findNavController()
                                .navigate(R.id.action_splashFragment_to_signInFragment)
                        } else {
                            findNavController()
                                .navigate(R.id.action_splashFragment_to_viewPagerFragment)
                        }
                    },
                    2500
                )
        }
    }

    private fun onBoardingFinished(): Boolean {
        val sharedPref = requireContext().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("Finished", false)
    }

    private fun reload() {
        if (isAdded) {
            val intent = Intent(requireContext(), MainActivity::class.java)
            this.startActivity(intent)
            activity?.finish()
        }
    }
}