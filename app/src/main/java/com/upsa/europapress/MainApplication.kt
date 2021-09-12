package com.upsa.europapress

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance(
            "https://europa-press-default-rtdb.europe-west1.firebasedatabase.app/"
        )
            .setPersistenceEnabled(true)
        Firebase.database.setPersistenceEnabled(true)
    }
}
