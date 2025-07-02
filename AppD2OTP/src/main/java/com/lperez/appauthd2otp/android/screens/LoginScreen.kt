package com.lperez.appauthd2otp.android.screens

import android.util.Log
import com.lperez.appauthd2otp.android.network.ApiClient
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lperez.appauthd2otp.android.utils.ClientData
import com.lperez.appauthd2otp.android.utils.CryptoSigner
import com.lperez.appauthd2otp.android.utils.DeviceIdProvider
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val apiClient = remember { ApiClient() }
    val context = LocalContext.current
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
                text = "Iniciar Sesión",
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") }
            )

            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("PIN") }
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
                    isButtonEnabled = false
                    coroutineScope.launch {
                        try {

                            if(username.length<4) {
                                errorMessage =
                                    "El usuario debe de ser de al menos 4 valores alfanuméricos"
                            }else if (pin.length<4) {
                                errorMessage =
                                    "El pin debe de ser de al menos 4 valores alfanuméricos"
                            }else {

                                CryptoSigner.generarYEnviarM1Login(
                                    username = username,
                                    pin = pin,
                                    context = context,
                                    onSuccess = { IdAM2, IdBM2, mensajeM2, CCM2, VAM2, CCM3, VAM3, timestamp ->


                                        CryptoSigner.procesarM2YEnviarM3Login(
                                            mensajeM2 = mensajeM2,
                                            vam2 = VAM2,
                                            vam3 = VAM3,
                                            ccm2 = CCM2,
                                            ccm3 = CCM3,
                                            timestampM1 = timestamp,
                                            context = context,
                                            onSuccess = { usuarioId, deviceIdCall, newccab ->
                                                clientData.storeCCAB(newccab)
                                                if (deviceIdCall != deviceId) {
                                                    CryptoSigner.generarYEnviarM1SolicitudVinculacion(
                                                        username = username,
                                                        pin = pin,
                                                        deviceId=deviceId,
                                                        context = context,
                                                        onSuccess = { IdAM2, IdBM2, mensajeM2, CCM2, VAM2, CCM3, VAM3, timestamp ->

                                                            CryptoSigner.procesarM2YEnviarM3SolicitudVinculacion(
                                                                mensajeM2 = mensajeM2,
                                                                vam2 = VAM2,
                                                                vam3 = VAM3,
                                                                ccm2 = CCM2,
                                                                ccm3 = CCM3,
                                                                context = context,
                                                                timestampM1 = timestamp,
                                                                onSuccess = { userId, requestId , newccab  ->
                                                                    clientData.storeCCAB(newccab)
                                                                    navController.navigate("unlinked/${userId}/${requestId}")

                                                                },
                                                                onError = { error ->
                                                                    errorMessage = "Error al enviar la solicitud"
                                                                }
                                                            )

                                                        },
                                                        onError = { error ->
                                                            errorMessage = "Error al procesar M3: $error"
                                                        }
                                                    )

                                                } else
                                                    navController.navigate("requests/${usuarioId}")

                                            },
                                            onError = { error ->
                                                errorMessage = "Error al procesar M3: $error"
                                            }
                                        )


                                    },
                                    onError = { error ->
                                        errorMessage = "Error en M1: $error"
                                    }
                                )
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error general: ${e.message}"
                            e.printStackTrace()
                        }finally {
                            delay(4000)
                            isButtonEnabled = true
                        }
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier.fillMaxWidth()

            ) {
                Text("Confirmar")
            }
        }
    }
}