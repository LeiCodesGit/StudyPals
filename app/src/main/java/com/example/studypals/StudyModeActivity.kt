package com.example.studypals

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class StudyModeActivity : AppCompatActivity() {

    private lateinit var tvCountdown: TextView
    private lateinit var btnStartFocus: Button
    private lateinit var btnQuitSession: Button
    private lateinit var studyPet: ImageView
    private lateinit var spinnerMode: Spinner

    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis: Long = 0

    private lateinit var tvPetName: TextView
    private lateinit var tvLevelLabel: TextView
    private lateinit var tvExpValue: TextView
    private lateinit var pbExpBar: ProgressBar

    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.studymode)

        // Initialize UI Components
        tvCountdown = findViewById(R.id.tvCountdown)
        btnStartFocus = findViewById(R.id.btnStartFocus)
        btnQuitSession = findViewById(R.id.btnQuitSession)
        studyPet = findViewById(R.id.studyPet)
        spinnerMode = findViewById(R.id.spinnerPomodoroMode)

        tvPetName = findViewById(R.id.tvPetName)
        tvLevelLabel = findViewById(R.id.tvLevelLabel)
        tvExpValue = findViewById(R.id.tvExpValue)
        pbExpBar = findViewById(R.id.pbExpBar)

        setupSpinner()
        updatePetVisual()

        btnStartFocus.setOnClickListener {
            if (isTimerRunning) pauseTimer() else startTimer()
        }

        btnQuitSession.setOnClickListener {
            resetTimer()
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.pomodoro_modes,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMode.adapter = adapter

        spinnerMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val modes = resources.getStringArray(R.array.pomodoro_modes)

                // Safety check to prevent IndexOutOfBounds
                if (position >= 0 && position < modes.size) {
                    val selected = modes[position]
                    parseSelectedTime(selected)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun parseSelectedTime(selected: String) {
        // Extract numbers after the bullet point from strings.xml
        val timeValue = selected.substringAfter("•").trim().filter { it.isDigit() }.toLongOrNull() ?: 25

        // Logic for seconds (s) or minutes (m)
        timeLeftInMillis = when {
            selected.contains("s", ignoreCase = true) -> timeValue * 1000L
            else -> timeValue * 60000L
        }
        updateCountDownText()
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                isTimerRunning = false
                btnStartFocus.text = "START FOCUS"
                handleSessionComplete()
            }
        }.start()

        isTimerRunning = true
        btnStartFocus.text = "PAUSE"
        spinnerMode.isEnabled = false
    }

    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false
        btnStartFocus.text = "RESUME"
    }

    private fun resetTimer() {
        timer?.cancel()
        isTimerRunning = false
        btnStartFocus.text = "START FOCUS"
        spinnerMode.isEnabled = true

        val selected = spinnerMode.selectedItem?.toString() ?: ""
        parseSelectedTime(selected)
    }

    private fun updateCountDownText() {
        val totalSeconds = timeLeftInMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        tvCountdown.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun handleSessionComplete() {
        val selectedMode = spinnerMode.selectedItem.toString()
        val minutesEarned = selectedMode.substringAfter("•").trim().filter { it.isDigit() }.toLongOrNull() ?: 0
        val xpToGain = minutesEarned * 10

        userRepository.getUserData { user, _ ->
            user?.let {
                val newXp = it.currentXP + xpToGain
                val newTotalMinutes = it.totalFocusMinutes + minutesEarned
                val newLevel = (newXp / 1000).toInt() + 1

                val updatedUser = it.copy(
                    currentXP = newXp,
                    level = if (newLevel > it.level) newLevel else it.level,
                    totalFocusMinutes = newTotalMinutes
                )

                FirebaseFirestore.getInstance().collection("users")
                    .document(it.uid).set(updatedUser)
                    .addOnSuccessListener {
                        updatePetVisual()
                        showSuccessDialog(xpToGain)
                    }
            }
        }
    }

    private fun updatePetVisual() {
        userRepository.getUserData { user, _ ->
            user?.let {
                tvPetName.text = it.petName
                val progress = it.currentXP % 1000
                tvExpValue.text = "$progress / 1000 XP"
                pbExpBar.progress = progress.toInt()

                val stage = when {
                    it.level < 6 -> "Egg"
                    it.level < 16 -> "Young"
                    else -> "Adult"
                }
                tvLevelLabel.text = "Level ${it.level}: $stage"

                val petResId = when (it.petType) {
                    "British Shorthair" -> when {
                        it.level >= 16 -> R.drawable.adult_british
                        it.level >= 6 -> R.drawable.baby_british
                        else -> R.drawable.egg_british
                    }
                    "Golden Retriever" -> when {
                        it.level >= 16 -> R.drawable.adult_golden
                        it.level >= 6 -> R.drawable.baby_golden
                        else -> R.drawable.egg_golden
                    }
                    "Maine Coon" -> when {
                        it.level >= 16 -> R.drawable.adult_mainecoon
                        it.level >= 6 -> R.drawable.baby_mainecoon
                        else -> R.drawable.egg_mainecoon
                    }
                    else -> R.drawable.egg_british
                }
                studyPet.setImageResource(petResId)
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (isTimerRunning) {
            Toast.makeText(this, "Session in progress! Finish or Quit to leave.", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }

    private fun showSuccessDialog(xpGained: Long) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("🎉 Congratulations!")
        builder.setMessage("You finished your focus session and earned +$xpGained XP for your Pal!")
        builder.setCancelable(false)

        builder.setPositiveButton("Study Again") { dialog, _ ->
            resetTimer()
            dialog.dismiss()
        }

        builder.setNegativeButton("Go Home") { _, _ ->
            finish()
        }

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.purple_700))
        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.black))
    }
}