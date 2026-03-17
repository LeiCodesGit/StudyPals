package com.example.studypals

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class StudyModeActivity : AppCompatActivity() {

    private lateinit var tvCountdown: TextView
    private lateinit var btnStartFocus: Button
    private lateinit var btnQuitSession: Button
    private lateinit var spinnerMode: Spinner

    // Host UI
    private lateinit var tvHostUserName: TextView
    private lateinit var tvHostPetName: TextView
    private lateinit var tvHostLevelLabel: TextView
    private lateinit var tvHostExpValue: TextView
    private lateinit var pbHostExpBar: ProgressBar
    private lateinit var hostPetImg: ImageView

    // Partner UI
    private lateinit var partnerDashboard: LinearLayout
    private lateinit var tvPartnerUserName: TextView
    private lateinit var tvPartnerPetName: TextView
    private lateinit var tvPartnerLevelLabel: TextView
    private lateinit var tvPartnerExpValue: TextView
    private lateinit var pbPartnerExpBar: ProgressBar
    private lateinit var partnerPetImg: ImageView

    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis: Long = 0
    private var isSuccessDialogShowing = false

    private val userRepository = UserRepository()
    private var currentSessionId: String? = null
    private var sessionListener: ListenerRegistration? = null
    private var isHost = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.studymode)

        tvCountdown = findViewById(R.id.tvCountdown)
        btnStartFocus = findViewById(R.id.btnStartFocus)
        btnQuitSession = findViewById(R.id.btnQuitSession)
        spinnerMode = findViewById(R.id.spinnerPomodoroMode)

        tvHostUserName = findViewById(R.id.tvHostUserName)
        tvHostPetName = findViewById(R.id.tvPetName)
        tvHostLevelLabel = findViewById(R.id.tvLevelLabel)
        tvHostExpValue = findViewById(R.id.tvExpValue)
        pbHostExpBar = findViewById(R.id.pbExpBar)
        hostPetImg = findViewById(R.id.studyPet)

        partnerDashboard = findViewById(R.id.partnerDashboard)
        tvPartnerUserName = findViewById(R.id.tvPartnerUserName)
        tvPartnerPetName = findViewById(R.id.tvPartnerPetName)
        tvPartnerLevelLabel = findViewById(R.id.tvPartnerLevelLabel)
        tvPartnerExpValue = findViewById(R.id.tvPartnerExpValue)
        pbPartnerExpBar = findViewById(R.id.pbPartnerExpBar)
        partnerPetImg = findViewById(R.id.partnerPet)

        setupSpinner()
        updateLocalPetVisual()

        btnStartFocus.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
                updateSessionTimerStatus(false)
            } else {
                startTimer()
                updateSessionTimerStatus(true)
            }
        }

        btnQuitSession.setOnClickListener {
            handleQuit()
        }

        findViewById<View>(R.id.fabMultiplayer).setOnClickListener {
            showMultiplayerOptions()
        }
    }

    private fun handleQuit() {
        val message = if (isHost) {
            "Ending the session will disconnect everyone. Are you sure you want to give up?"
        } else {
            "Are you sure you want to leave the study session?"
        }

        AlertDialog.Builder(this)
            .setTitle("Give Up?")
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                if (isHost && currentSessionId != null) {
                    // Update status to finished/cancelled so guests know it's over
                    FirebaseFirestore.getInstance().collection("sessions")
                        .document(currentSessionId!!)
                        .update("status", "finished")
                        .addOnCompleteListener {
                            exitToHome()
                        }
                } else if (!isHost && currentSessionId != null) {
                    // Just leave the session (remove partner data)
                    FirebaseFirestore.getInstance().collection("sessions")
                        .document(currentSessionId!!)
                        .update(
                            "partnerId", null,
                            "partnerName", null,
                            "partnerPetName", null,
                            "partnerPetType", null,
                            "partnerPetLevel", null,
                            "partnerPetXP", null,
                            "status", "waiting"
                        ).addOnCompleteListener {
                            exitToHome()
                        }
                } else {
                    exitToHome()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exitToHome() {
        timer?.cancel()
        sessionListener?.remove()
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
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
                if (position >= 0 && position < modes.size) {
                    val selected = modes[position]
                    parseSelectedTime(selected)
                    
                    if (isHost && currentSessionId != null) {
                        FirebaseFirestore.getInstance().collection("sessions")
                            .document(currentSessionId!!)
                            .update("selectedMode", selected)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun parseSelectedTime(selected: String) {
        val timeValue = selected.substringAfter("•").trim().filter { it.isDigit() }.toLongOrNull() ?: 25
        timeLeftInMillis = when {
            selected.contains("s", ignoreCase = true) -> timeValue * 1000L
            else -> timeValue * 60000L
        }
        updateCountDownText()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                isTimerRunning = false
                btnStartFocus.text = "START FOCUS"
                handleSessionComplete()
                if (isHost && currentSessionId != null) {
                    updateSessionTimerStatus(false)
                }
            }
        }.start()

        isTimerRunning = true
        btnStartFocus.text = "PAUSE"
        spinnerMode.isEnabled = false
    }

    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false
        btnStartFocus.text = if (timeLeftInMillis > 0) "RESUME" else "START FOCUS"
    }

    private fun resetTimer() {
        timer?.cancel()
        isTimerRunning = false
        btnStartFocus.text = "START FOCUS"
        spinnerMode.isEnabled = isHost || currentSessionId == null

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
        if (isSuccessDialogShowing) return
        
        val selectedMode = spinnerMode.selectedItem.toString()
        val minutesEarned = selectedMode.substringAfter("•").trim().filter { it.isDigit() }.toLongOrNull() ?: 0
        val xpToGain = if (selectedMode.contains("s", true)) 10L else minutesEarned * 10

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
                        updateLocalPetVisual()
                        showSuccessDialog(xpToGain)
                        
                        if (currentSessionId != null) {
                            val updateMap = if (isHost) {
                                mapOf("hostPetXP" to newXp, "hostPetLevel" to updatedUser.level)
                            } else {
                                mapOf("partnerPetXP" to newXp, "partnerPetLevel" to updatedUser.level)
                            }
                            FirebaseFirestore.getInstance().collection("sessions")
                                .document(currentSessionId!!).update(updateMap)
                        }
                    }
            }
        }
    }

    private fun updateLocalPetVisual() {
        userRepository.getUserData { user, _ ->
            user?.let {
                if (currentSessionId == null || isHost) {
                    tvHostUserName.text = it.username
                    tvHostPetName.text = it.petName
                    val progress = (it.currentXP % 1000).toInt()
                    tvHostExpValue.text = "$progress / 1000 XP"
                    pbHostExpBar.max = 1000
                    pbHostExpBar.progress = progress

                    val stage = getStage(it.level)
                    tvHostLevelLabel.text = "Level ${it.level}: $stage"
                    hostPetImg.setImageResource(getPetResource(it.petType, it.level))
                } else {
                    // If guest, local pet is the Partner Dashboard
                    tvPartnerUserName.text = it.username
                    tvPartnerPetName.text = it.petName
                    val progress = (it.currentXP % 1000).toInt()
                    tvPartnerExpValue.text = "$progress / 1000 XP"
                    pbPartnerExpBar.max = 1000
                    pbPartnerExpBar.progress = progress
                    tvPartnerLevelLabel.text = "Level ${it.level}: ${getStage(it.level)}"
                    partnerPetImg.setImageResource(getPetResource(it.petType, it.level))
                }
            }
        }
    }

    private fun getPetResource(type: String, level: Int): Int {
        return when (type) {
            "British Shorthair" -> when {
                level >= 16 -> R.drawable.adult_british
                level >= 6 -> R.drawable.baby_british
                else -> R.drawable.egg_british
            }
            "Golden Retriever" -> when {
                level >= 16 -> R.drawable.adult_golden
                level >= 6 -> R.drawable.baby_golden
                else -> R.drawable.egg_golden
            }
            "Maine Coon" -> when {
                level >= 16 -> R.drawable.adult_mainecoon
                level >= 6 -> R.drawable.baby_mainecoon
                else -> R.drawable.egg_mainecoon
            }
            else -> R.drawable.egg_british
        }
    }

    private fun showSuccessDialog(xpGained: Long) {
        if (isSuccessDialogShowing) return
        isSuccessDialogShowing = true
        
        val builder = AlertDialog.Builder(this)
        builder.setTitle("🎉 Congratulations!")
        builder.setMessage("You earned +$xpGained XP for your Pal!")
        builder.setPositiveButton("OK") { d, _ -> 
            d.dismiss()
            isSuccessDialogShowing = false
            resetTimer()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun showMultiplayerOptions() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Multiplayer Study")
        val options = arrayOf("Host Session", "Join Session")
        builder.setItems(options) { _, which ->
            if (which == 0) hostSession((100000..999999).random().toString())
            else showJoinDialog()
        }
        builder.show()
    }

    private fun hostSession(code: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        userRepository.getUserData { user, _ ->
            if (user == null) return@getUserData
            isHost = true
            val session = Session(
                roomCode = code,
                hostId = uid,
                hostName = user.username,
                hostPetName = user.petName,
                hostPetType = user.petType,
                hostPetLevel = user.level,
                hostPetXP = user.currentXP,
                selectedMode = spinnerMode.selectedItem.toString(),
                status = "waiting"
            )
            val sessionRef = FirebaseFirestore.getInstance().collection("sessions").document()
            currentSessionId = sessionRef.id
            sessionRef.set(session.copy(sessionId = currentSessionId!!)).addOnSuccessListener {
                AlertDialog.Builder(this).setTitle("Room Code").setMessage("Share this code: $code").show()
                startSessionListener(currentSessionId!!)
            }
        }
    }

    private fun showJoinDialog() {
        val input = EditText(this)
        input.hint = "6-digit code"
        AlertDialog.Builder(this).setTitle("Join Session").setView(input).setPositiveButton("Join") { _, _ ->
            joinSession(input.text.toString())
        }.show()
    }

    private fun joinSession(code: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        userRepository.getUserData { user, _ ->
            if (user == null) return@getUserData
            isHost = false
            FirebaseFirestore.getInstance().collection("sessions")
                .whereEqualTo("roomCode", code).whereEqualTo("status", "waiting")
                .get().addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        val doc = docs.documents[0]
                        currentSessionId = doc.id
                        doc.reference.update(
                            "partnerId", uid,
                            "partnerName", user.username,
                            "partnerPetName", user.petName,
                            "partnerPetType", user.petType,
                            "partnerPetLevel", user.level,
                            "partnerPetXP", user.currentXP,
                            "status", "active"
                        ).addOnSuccessListener { startSessionListener(currentSessionId!!) }
                    } else {
                        Toast.makeText(this, "Session not found", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun updateSessionTimerStatus(running: Boolean) {
        if (isHost && currentSessionId != null) {
            FirebaseFirestore.getInstance().collection("sessions")
                .document(currentSessionId!!)
                .update("timerRunning", running)
        }
    }

    private fun startSessionListener(sessionId: String) {
        sessionListener?.remove()
        sessionListener = FirebaseFirestore.getInstance().collection("sessions")
            .document(sessionId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val session = snapshot.toObject(Session::class.java) ?: return@addSnapshotListener

                // If session ended by host
                if (session.status == "finished") {
                    Toast.makeText(this, "The host has ended the session.", Toast.LENGTH_LONG).show()
                    exitToHome()
                    return@addSnapshotListener
                }

                if (!isHost) {
                    syncModeAndTimer(session)
                    btnStartFocus.isEnabled = false
                    btnQuitSession.isEnabled = true // Guests can choose to leave independently too
                    spinnerMode.isEnabled = false
                }

                // ALWAYS Show Host Info in Host Dashboard
                tvHostUserName.text = session.hostName
                tvHostPetName.text = session.hostPetName
                val hXp = (session.hostPetXP % 1000).toInt()
                tvHostExpValue.text = "$hXp / 1000 XP"
                pbHostExpBar.max = 1000
                pbHostExpBar.progress = hXp
                tvHostLevelLabel.text = "Level ${session.hostPetLevel}: ${getStage(session.hostPetLevel)}"
                hostPetImg.setImageResource(getPetResource(session.hostPetType, session.hostPetLevel))

                // ALWAYS Show Partner Info in Partner Dashboard
                if (session.partnerId != null) {
                    partnerDashboard.visibility = View.VISIBLE
                    tvPartnerUserName.text = session.partnerName
                    tvPartnerPetName.text = session.partnerPetName
                    val pXp = ((session.partnerPetXP ?: 0) % 1000).toInt()
                    val pLvl = session.partnerPetLevel ?: 1
                    tvPartnerExpValue.text = "$pXp / 1000 XP"
                    pbPartnerExpBar.max = 1000
                    pbPartnerExpBar.progress = pXp
                    tvPartnerLevelLabel.text = "Level $pLvl: ${getStage(pLvl)}"
                    partnerPetImg.setImageResource(getPetResource(session.partnerPetType ?: "Default", pLvl))
                } else {
                    partnerDashboard.visibility = View.GONE
                }
            }
    }

    private fun getStage(level: Int) = when {
        level < 6 -> "Egg"
        level < 16 -> "Young"
        else -> "Adult"
    }

    private fun syncModeAndTimer(session: Session) {
        val modes = resources.getStringArray(R.array.pomodoro_modes)
        val index = modes.indexOf(session.selectedMode)
        if (index != -1 && spinnerMode.selectedItemPosition != index) {
            spinnerMode.setSelection(index)
        }
        
        if (session.timerRunning && !isTimerRunning) {
            startTimer()
        } else if (!session.timerRunning && isTimerRunning) {
            // Force finish if close to zero, otherwise pause
            if (timeLeftInMillis < 5000) {
                timer?.onFinish()
                timer?.cancel()
            } else {
                pauseTimer()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionListener?.remove()
    }
}
