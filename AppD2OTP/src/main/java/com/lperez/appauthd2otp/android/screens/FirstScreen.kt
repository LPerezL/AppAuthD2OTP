package com.lperez.appauthd2otp.android.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.lperez.appauthd2otp.android.network.ApiClient
import com.lperez.appauthd2otp.android.utils.ClientData
import kotlinx.coroutines.launch

@Composable
fun FirstScreen(navController: NavHostController) {

    val coroutineScope = rememberCoroutineScope()
    val apiClient = remember { ApiClient() }
    val context = LocalContext.current
    val clientData = remember {ClientData(context)}


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Text(
                text = "App D2OTP",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                    apiClient.registrarAppBackend(context,clientData.idA, clientData.ccab)
                    navController.navigate("welcome")

                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Entrar")
            }


        }
    }
}
