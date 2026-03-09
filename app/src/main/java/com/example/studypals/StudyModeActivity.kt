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
        // Load the array from strings.xml using your custom spinner_item layout
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
                val selected = modes[position]

                // Extract minutes from the string ("25" from "Traditional • 25 min")
                val timeValue = selected.substringAfter("•").trim().filter { it.isDigit() }.toLongOrNull() ?: 25

                timeLeftInMillis = if (selected.contains("s")) {
                    timeValue * 1000L // 10 seconds
                } else {
                    timeValue * 60000L // Minutes
                }
                updateCountDownText()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
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
        spinnerMode.isEnabled = false // Lock mode selection during focus
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

        val selected = spinnerMode.selectedItem.toString()
        // FIXED parsing logic
        val minutes = selected.substringAfter("•").trim().filter { it.isDigit() }.toIntOrNull() ?: 25
        timeLeftInMillis = minutes * 60000L
        updateCountDownText()
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        tvCountdown.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun handleSessionComplete() {
        // Calculate XP: 10 XP per study minute
        val selectedMode = spinnerMode.selectedItem.toString()
        val minutesEarned = selectedMode.substringAfter("•").trim().filter { it.isDigit() }.toLongOrNull() ?: 0
        val xpToGain = minutesEarned * 10

        userRepository.getUserData { user, _ ->
            user?.let {
                val newXp = it.currentXP + xpToGain
                val newTotalMinutes = it.totalFocusMinutes + minutesEarned

                // Growth Stages: Egg (1-5), Young (6-15), Adult (16+)
                // Level up every 1000 XP
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
                        showSuccessDialog(xpToGain) // Call the new dialog function
                    }
            }
        }
    }

    private fun updatePetVisual() {
        userRepository.getUserData { user, _ ->
            user?.let {
                // Set the Name and XP
                tvPetName.text = it.petName
                val progress = it.currentXP % 1000
                tvExpValue.text = "$progress / 1000 XP"
                pbExpBar.progress = progress.toInt()

                // Set the Level Label
                val stage = when {
                    it.level < 6 -> "Egg"
                    it.level < 16 -> "Young"
                    else -> "Adult"
                }
                tvLevelLabel.text = "Level ${it.level}: $stage"

                // Set the Image
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

    override fun onBackPressed() {
        if (isTimerRunning) {
            // Show a dialog asking if they want to quit and lose progress
            Toast.makeText(this, "Session in progress! Finish or Quit to leave.", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed() // This finishes StudyModeActivity and returns to Home
        }
    }

    private fun showSuccessDialog(xpGained: Long) {
        val builder = android.app.AlertDialog.Builder(this)

        // Set the Title and Message
        builder.setTitle("🎉 Congratulations!")
        builder.setMessage("You finished your focus session and earned +$xpGained XP for your Pal!")
        builder.setCancelable(false) // Prevents closing by clicking outside

        // Option 1: Study Again
        builder.setPositiveButton("Study Again") { dialog, _ ->
            resetTimer()
            dialog.dismiss()
        }

        // Option 2: Go Back to Homepage
        builder.setNegativeButton("Go Home") { _, _ ->
            finish() // This closes StudyMode and returns to HomeActivity
        }

        // Create and Show the dialog
        val dialog = builder.create()
        dialog.show()

        // Optional: Style the buttons to match your Purple/Navy theme
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.purple_700))
        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.black))
    }
}