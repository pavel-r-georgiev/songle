package me.pavelgeorgiev.songle

import com.google.firebase.database.FirebaseDatabase

class Application : android.app.Application(){
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}
