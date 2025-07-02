package com.lperez.appauthd2otp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lperez.appauthd2otp.android.screens.AuthRequestsScreen
import com.lperez.appauthd2otp.android.screens.FailedLinkScreen
import com.lperez.appauthd2otp.android.screens.FirstScreen
import com.lperez.appauthd2otp.android.screens.LoginScreen
import com.lperez.appauthd2otp.android.screens.RegisterScreen
import com.lperez.appauthd2otp.android.screens.RequestDetailScreen
import com.lperez.appauthd2otp.android.screens.SuccessfulRegistryScreen
import com.lperez.appauthd2otp.android.screens.SuccessfullLinkScreen
import com.lperez.appauthd2otp.android.screens.UnlinkedDeviceScreen
import com.lperez.appauthd2otp.android.screens.WelcomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "first") {
                    composable("first") { FirstScreen(navController) }
                    composable("welcome") { WelcomeScreen(navController) }
                    composable("registro") { RegisterScreen(navController) }
                    composable("exitoregistro") { SuccessfulRegistryScreen(navController) }
                    composable("login") { LoginScreen(navController) }
                    composable("unlinked/{userId}/{requestId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")
                        val requestId = backStackEntry.arguments?.getString("requestId")
                        UnlinkedDeviceScreen(navController,userId,requestId) }
                    composable("requests/{userId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")
                        AuthRequestsScreen(navController, userId)
                    }
                    composable("requesdetail/{userId}/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id")
                        val userId = backStackEntry.arguments?.getString("userId")
                        RequestDetailScreen(navController, userId, id)
                    }

                    composable("linkdenegado") { FailedLinkScreen(navController) }
                    composable("linkaceptado/{userId}/{requestId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")
                        val requestId = backStackEntry.arguments?.getString("requestId")
                        SuccessfullLinkScreen(navController, userId, requestId)
                    }
                }
            }
        }

    }
}

@Composable
fun GreetingView(text: String) {
    Text(text = text)
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        GreetingView("Hello, Android!")
    }
}
