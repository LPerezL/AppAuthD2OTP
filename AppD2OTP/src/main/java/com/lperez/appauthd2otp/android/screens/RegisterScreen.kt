package com.lperez.appauthd2otp.android.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lperez.appauthd2otp.android.network.ApiClient
import com.lperez.appauthd2otp.android.utils.ClientData
import com.lperez.appauthd2otp.android.utils.CryptoSigner
import com.lperez.appauthd2otp.android.utils.DeviceIdProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiClient = remember { ApiClient() }
    val deviceId = remember { DeviceIdProvider.getOrCreateDeviceId(context) }
    val clientData= ClientData(context)
    var isButtonEnabled by remember { mutableStateOf(true) }
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
                text = "Crear cuenta",
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("PIN") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirmPin,
                onValueChange = { confirmPin = it },
                label = { Text("Confirmar PIN") },
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (!isButtonEnabled) return@Button
                    if (username.length < 4){
                        errorMessage = "El usuario debe de ser de al menos 4 valores alfanuméricos"
                    }else if (pin.length<4) {
                        errorMessage = "El pin debe de ser de al menos 4 valores alfanuméricos"
                    }else if (pin != confirmPin) {
                        errorMessage = "Los PIN no coinciden"
                    } else if (username.isBlank() || pin.isBlank()) {
                        errorMessage = "Todos los campos son obligatorios"
                    } else {
                        isButtonEnabled = false
                        coroutineScope.launch {
                            try {

                                val result = CryptoSigner.generarYEnviarM1Registry(
                                    username,
                                    pin,
                                    deviceId,
                                    context,

                                    onSuccess = { IdAM2, IdBM2, mensajeM2, CCM2, VAM2, CCM3, VAM3,timestampM1 ->
                                        CryptoSigner.procesarM2YEnviarM3Registry(
                                            mensajeM2 = mensajeM2,
                                            vam2 = VAM2,
                                            vam3 = VAM3,
                                            ccm2 = CCM2,
                                            ccm3 = CCM3,
                                            timestampM1=timestampM1,
                                            context = context,
                                            onSuccess = { usuarioId, newccab ->
                                                clientData.storeCCAB(newccab)
                                                navController.navigate("exitoregistro")

                                            },
                                            onError = { error ->
                                                errorMessage = "Error al procesar M3: $error"
                                            }
                                        )
                                    },
                                    onError = { error ->
                                        errorMessage = "Error: ${error}"
                                    }
                                )

                            } catch (e: Exception) {
                                errorMessage = "Error general: ${e.message}"
                                e.printStackTrace()
                            }finally {
                                delay(3000)
                                isButtonEnabled = true
                            }
                        }
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
            ) {
                Text("Confirmar")
            }
        }
    }
}
