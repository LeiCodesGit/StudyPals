package com.example.studypals

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class StudyModeActivity : AppCompatActivity() {

    private lateinit var etMinutes: EditText
    private lateinit var tvSeconds: TextView
    private lateinit var btnStartFocus: Button
    private lateinit var btnQuitSession: Button
    private lateinit var studyPet: ImageView
    private lateinit var appLogo: ImageView

    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis: Long = 25 * 60 * 1000 // Default 25 mins

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.studymode)

        // Initialize Views
        etMinutes = findViewById(R.id.etMinutes)
        tvSeconds = findViewById(R.id.tvSeconds)
        btnStartFocus = findViewById(R.id.btnStartFocus)
        btnQuitSession = findViewById(R.id.btnQuitSession)
        studyPet = findViewById(R.id.studyPet)
        appLogo = findViewById(R.id.appLogo)

        // Load Pet Data
        loadUserData()

        // Button Listeners
        btnStartFocus.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        btnQuitSession.setOnClickListener {
            resetTimer()
        }

        appLogo.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserData() {
        userRepository.getUserData { user, error ->
            if (user != null) {
                val petResId = when (user.petType) {
                    "British Shorthair" -> when {
                        user.level >= 16 -> R.drawable.adult_british
                        user.level >= 6 -> R.drawable.baby_british
                        else -> R.drawable.egg_british
                    }
                    "Golden Retriever" -> when {
                        user.level >= 16 -> R.drawable.adult_golden
                        user.level >= 6 -> R.drawable.baby_golden
                        else -> R.drawable.egg_golden
                    }
                    "Maine Coon" -> when {
                        user.level >= 16 -> R.drawable.adult_mainecoon
                        user.level >= 6 -> R.drawable.baby_mainecoon
                        else -> R.drawable.egg_mainecoon
                    }
                    else -> R.drawable.egg_british
                }
                studyPet.setImageResource(petResId)
            }
        }
    }

    private fun startTimer() {
        // If not running and just starting, get the value from EditText
        if (countDownTimer == null) {
            val input = etMinutes.text.toString()
            if (input.isEmpty()) {
                Toast.makeText(this, "Please enter minutes", Toast.LENGTH_SHORT).show()
                return
            }
            val minutesInput = input.toLong()
            if (minutesInput == 0L) {
                Toast.makeText(this, "Please enter a value greater than 0", Toast.LENGTH_SHORT).show()
                return
            }
            timeLeftInMillis = minutesInput * 60 * 1000
        }

        etMinutes.isEnabled = false // Lock editing while running
        
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                isTimerRunning = false
                btnStartFocus.text = "START FOCUS"
                etMinutes.isEnabled = true
                handleSessionComplete()
            }
        }.start()

        isTimerRunning = true
        btnStartFocus.text = "PAUSE"
        btnQuitSession.visibility = View.VISIBLE
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        btnStartFocus.text = "RESUME"
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        isTimerRunning = false
        etMinutes.isEnabled = true
        
        // Revert to what's in the EditText or default 25
        val input = etMinutes.text.toString()
        val minutes = if (input.isNotEmpty()) input.toLong() else 25L
        timeLeftInMillis = minutes * 60 * 1000
        
        updateCountDownText()
        btnStartFocus.text = "START FOCUS"
        btnQuitSession.visibility = View.INVISIBLE
    }

    private fun updateCountDownText() {
        val totalSeconds = timeLeftInMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        etMinutes.setText(minutes.toString())
        tvSeconds.text = String.format(":%02d", seconds)
    }

    private fun handleSessionComplete() {
        val userId = auth.currentUser?.uid ?: return
        val input = etMinutes.text.toString()
        val focusMinutes = if (input.isNotEmpty()) input.toLong() else 25L
        
        // Reward proportional to time (e.g., 2 XP per minute)
        val xpReward = focusMinutes * 2

        val userRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentXP = snapshot.getLong("currentXP") ?: 0L
            val currentLevel = snapshot.getLong("level")?.toInt() ?: 1
            
            var newXP = currentXP + xpReward
            var newLevel = currentLevel

            if (newXP >= 1000) {
                newXP -= 1000
                newLevel++
            }

            transaction.update(userRef, "currentXP", newXP)
            transaction.update(userRef, "level", newLevel)
            transaction.update(userRef, "totalFocusMinutes", FieldValue.increment(focusMinutes))
            transaction.update(userRef, "lastStudyDate", FieldValue.serverTimestamp())

            null
        }.addOnSuccessListener {
            Toast.makeText(this, "Session Complete! +$xpReward XP", Toast.LENGTH_LONG).show()
            resetTimer()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to save progress", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
