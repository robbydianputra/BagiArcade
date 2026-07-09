package com.bagicode.games.chess

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bagicode.games.R
import com.bagicode.games.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("chess_prefs", MODE_PRIVATE)

        // Load current settings
        binding.mirrorSwitch.isChecked = prefs.getBoolean("mirror_mode", true)
        binding.predictionSwitch.isChecked = prefs.getBoolean("prediction_enabled", true)
        binding.timerSwitch.isChecked = prefs.getBoolean("timer_enabled", true)

        val currentDuration = prefs.getInt("timer_duration", 5)
        when (currentDuration) {
            5 -> binding.radio5.isChecked = true
            10 -> binding.radio10.isChecked = true
            15 -> binding.radio15.isChecked = true
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.saveButton.setOnClickListener {
            val editor = prefs.edit()
            editor.putBoolean("mirror_mode", binding.mirrorSwitch.isChecked)
            editor.putBoolean("prediction_enabled", binding.predictionSwitch.isChecked)
            editor.putBoolean("timer_enabled", binding.timerSwitch.isChecked)

            val selectedDuration = when (binding.timerDurationGroup.checkedRadioButtonId) {
                R.id.radio10 -> 10
                R.id.radio15 -> 15
                else -> 5
            }
            editor.putInt("timer_duration", selectedDuration)
            editor.apply()

            setResult(RESULT_OK)
            finish()
        }
    }
}
