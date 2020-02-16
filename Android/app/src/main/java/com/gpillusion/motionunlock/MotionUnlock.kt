package com.gpillusion.motionunlock

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class MotionUnlock : Application() {

    override fun onCreate() {
        super.onCreate()
        val sharedPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (sharedPrefs.getBoolean("dark", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}