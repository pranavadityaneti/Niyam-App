package com.myniyam.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.myniyam.app.ui.AppNavHost
import com.myniyam.app.ui.theme.NiyamTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NiyamTheme {
                AppNavHost()
            }
        }
    }
}
