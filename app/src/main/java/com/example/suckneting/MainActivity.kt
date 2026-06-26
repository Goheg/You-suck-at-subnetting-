package com.example.suckneting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.suckneting.ui.screens.FlsmScreen
import com.example.suckneting.ui.screens.QuizScreen
import com.example.suckneting.ui.screens.VlsmScreen
import com.example.suckneting.ui.theme.SucknetingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SucknetingTheme {
                // Testing the VLSM feature
                // VlsmScreen
                // FlsmScreen()
                QuizScreen()
            }
        }
    }
}
