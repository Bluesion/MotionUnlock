package com.gpillusion.motionunlock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings, rootKey)

            val sharedPrefs = activity!!.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            val dark = findPreference<SwitchPreference>("dark")
            dark!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
                if (dark.isChecked) {
                    editor.putBoolean("dark", false)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else {
                    editor.putBoolean("dark", true)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                editor.apply()
                true
            }

            val about = findPreference<Preference>("about")
            about!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(activity, AboutActivity::class.java))
                true
            }
        }
    }
}