package com.example.suckneting.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.suckneting.ui.viewmodel.QuizViewModel

// Reuse the professional dark palette
private val DarkBackground = Color(0xFF050C16)
private val CardBackground = Color(0xFF0D1625)
private val AccentPurple = Color(0xFF5D5FEF)
private val TextSecondary = Color(0xFF94A3B8)
private val SuccessGreen = Color(0xFF4ADE80)
private val BorderColor = Color(0xFF1E293B)
private val TipTeal = Color(0xFF2DD4BF)

@Composable
fun QuizScreen(
    viewModel: QuizViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val question = uiState.currentQuestion

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Hub, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("SubnetPro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = TextSecondary)
                    }
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AccentPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("JD", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column {
                        Text("Quiz Master", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("Master IP subnetting with rapid-fire questions.", color = TextSecondary, fontSize = 14.sp)
                    }
                }

                // Stats Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuizStatCard(
                            label = "SCORE",
                            value = uiState.score.toString(),
                            modifier = Modifier.weight(1f),
                            extra = if (uiState.lastPointsGained > 0) "+${uiState.lastPointsGained} pts" else null
                        )
                        QuizStatCard(
                            label = "STREAK",
                            value = uiState.streak.toString(),
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.LocalFireDepartment,
                            iconColor = TipTeal
                        )
                    }
                }

                // Question Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("IPV4 FUNDAMENTALS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text("Q${uiState.questionNumber} of ${uiState.totalQuestions}", color = TextSecondary.copy(alpha = 0.6f), fontSize = 10.sp)
                            }
                            Spacer(Modifier.height(16.dp))
                            
                            val annotatedString = buildAnnotatedString {
                                val questionText = question.theQuestion
                                // Basic logic to highlight IP/CIDR pattern
                                val regex = Regex("""\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}/\d{1,2}""")
                                val match = regex.find(questionText)
                                
                                if (match != null) {
                                    append(questionText.substring(0, match.range.first))
                                    withStyle(style = SpanStyle(background = Color.White.copy(alpha = 0.1f), fontWeight = FontWeight.ExtraBold)) {
                                        append(match.value)
                                    }
                                    append(questionText.substring(match.range.last + 1))
                                } else {
                                    append(questionText)
                                }
                            }

                            Text(
                                text = annotatedString,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp
                            )
                        }
                    }
                }

                // Options
                items(question.options.size) { index ->
                    val label = ('A' + index).toString()
                    val isSelected = uiState.selectedOptionIndex == index
                    val isCorrect = index == question.correctOptionIndex
                    val isChecked = uiState.isAnswerChecked

                    OptionCard(
                        label = label,
                        text = question.options[index],
                        isSelected = isSelected,
                        isCorrect = if (isChecked) isCorrect else null,
                        onClick = { viewModel.selectOption(index) }
                    )
                }

                item {
                    Button(
                        onClick = {
                            if (uiState.isAnswerChecked) {
                                viewModel.nextQuestion()
                            } else {
                                viewModel.checkAnswer()
                            }
                        },
                        enabled = uiState.selectedOptionIndex != null,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (uiState.isAnswerChecked) "Next Question" else "Check Answer",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Explanation
                item {
                    AnimatedVisibility(visible = uiState.isAnswerChecked) {
                        ExplanationCard(question.explanation)
                    }
                }
            }
        }
    }
}

@Composable
fun QuizStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    extra: String? = null,
    icon: ImageVector? = null,
    iconColor: Color = Color.White
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                if (extra != null) {
                    Spacer(Modifier.width(6.dp))
                    Text(extra, color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 4.dp))
                }
                if (icon != null) {
                    Spacer(Modifier.width(6.dp))
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp).padding(bottom = 4.dp))
                }
            }
        }
    }
}

@Composable
fun OptionCard(
    label: String,
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean?,
    onClick: () -> Unit
) {
    val borderColor = when {
        isCorrect == true -> SuccessGreen
        isCorrect == false && isSelected -> Color.Red.copy(alpha = 0.8f)
        isSelected -> AccentPurple
        else -> BorderColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isCorrect == null) { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected || isCorrect == true) AccentPurple else BorderColor),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(16.dp))
            Text(text, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
            if (isCorrect == true) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
            } else if (isCorrect == false && isSelected) {
                Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Red.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun ExplanationCard(explanation: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkBackground.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BorderColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Step-by-Step Explanation", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = explanation,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
