package com.example.studypals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StudyPalsHomePage() {
    // Custom colors for a "study" vibe
    val primaryPurple = Color(0xFF6750A4)
    val lightBackground = Color(0xFFF8F9FA)
    val progressTrack = Color(0xFFEADDFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "StudyPals",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryPurple
                )
                Text(
                    text = "Lock in, Tune out, Grow together.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            // Simple Profile Icon Placeholder
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = progressTrack
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("👤")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Focus Pet Card
        ElevatedCard(
            modifier = Modifier
                .size(240.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                // Background Glow
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(
                            Brush.radialGradient(listOf(progressTrack, Color.Transparent)),
                            CircleShape
                        )
                )
                // The Pet
                Text(text = "🐣", fontSize = 90.sp) 
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. XP Progress Section
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Level 1: Egg", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "420 / 1000 XP", style = MaterialTheme.typography.labelLarge, color = primaryPurple)
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { 0.42f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape),
                color = primaryPurple,
                trackColor = progressTrack
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 4. Primary Action Button
        Button(
            onClick = { /* Navigate to Timer */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("START STUDY MODE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5. Tasks Preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Today's Tasks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = {}) {
                Text("View All", color = primaryPurple)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TaskPreviewItem("Math Homework", true)
        TaskPreviewItem("Biology Flashcards", false)
    }
}

@Composable
fun TaskPreviewItem(title: String, isDone: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = if (isDone) Color(0xFF4CAF50) else Color.LightGray
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                color = if (isDone) Color.Gray else Color.Unspecified
            )
        }
    }
}
