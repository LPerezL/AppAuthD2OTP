package com.lperez.appauthd2otp.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.lperez.appauthd2otp.android.utils.ClientData
import com.lperez.appauthd2otp.android.utils.CryptoSigner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UnlinkedDeviceScreen(navController: NavHostController, userId:String?, requestId:String?) {
    val secondsLeft = remember { mutableStateOf(420) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clientData= ClientData(context)
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if(userId!=null&&requestId!=null) {
            while (secondsLeft.value > 0) {
                delay(1000L)
                secondsLeft.value--
            }

            CryptoSigner.generarYEnviarM1ComprobarVinculacion(
                userId = userId,
                requestId = requestId,
                context = context,
                onSuccess = { IdAM2, IdBM2, mensajeM2, CCM2, VAM2, CCM3, VAM3, timestampM1 ->


                    CryptoSigner.procesarM2YEnviarM3ComprobarVinculacion(
                        mensajeM2 = mensajeM2,
                        vam2 = VAM2,
                        vam3 = VAM3,
                        ccm2 = CCM2,
                        ccm3 = CCM3,
                        timestampM1 = timestampM1,
                        context = context,
                        onSuccess = {change,state,ccab  ->
                            clientData.storeCCAB(ccab)
                            if(change==true) {
                                if (state == true) {

                                    navController.navigate("linkaceptado/${userId}/${requestId}")
                                }else{
                                    navController.navigate("linkdenegado")
                                }
                            }else{
                                CryptoSigner.generarYEnviarM1Denegar(
                                    userId = userId,
                                    solicitudId = requestId,
                                    context = context,
                                    onSuccess = { IdAM2, IdBM2, mensajeM2, CCM2, VAM2, CCM3, VAM3, timestampM1 ->
                                        CryptoSigner.procesarM2AceptarODenegar(
                                            mensajeM2 = mensajeM2,
                                            vam2 = VAM2,
                                            vam3 = VAM3,
                                            ccm2 = CCM2,
                                            ccm3 = CCM3,
                                            timestampM1 = timestampM1,
                                            context = context,
                                            onSuccess = { newccba ->
                                                clientData.storeCCAB(newccba)
                                                navController.navigate("linkdenegado") {
                                                    popUpTo("unlinked") { inclusive = true }
                                                }
                                            },
                                            onError = { error ->
                                                errorMessage = "Error al procesar M3: $error"
                                            }
                                        )
                                    },
                                    onError = { error ->
                                        errorMessage = "Error al procesar M3: $error"
                                    }
                                )
                            }
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
    }

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
                text = "Este dispositivo no es el vinculado a su cuenta, deberá vincularlo con la misma y desvincular el antiguo",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Text(
                text = "Se ha enviado una solicitud de vinculación, aceptela en el dispositivo vinculado",
                fontSize = 22.sp,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )


            Text(
                text = "Esperando respuesta de solicitud...",
                fontSize = 22.sp,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )



            Spacer(modifier = Modifier.height(16.dp))

            Text("Cancelando solicitud de Login en ${secondsLeft.value} segundos...")
            Text("Si al acabar el tiempo no ha sido aceptada, volverás al inicio de la aplicación")

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (userId != null && requestId != null) {
                            try {
                                CryptoSigner.generarYEnviarM1ComprobarVinculacion(
                                    userId = userId,
                                    requestId = requestId,
                                    context = context,
                                    onSuccess = { IdAM2, IdBM2, mensajeM2, CCM2, VAM2, CCM3, VAM3, timestampM1 ->


                                        CryptoSigner.procesarM2YEnviarM3ComprobarVinculacion(
                                            mensajeM2 = mensajeM2,
                                            vam2 = VAM2,
                                            vam3 = VAM3,
                                            ccm2 = CCM2,
                                            ccm3 = CCM3,
                                            timestampM1 = timestampM1,
                                            context = context,
                                            onSuccess = { change, state, ccab ->
                                                clientData.storeCCAB(ccab)
                                                if (change == true) {
                                                    if (state == true) {
                                                        navController.navigate("linkaceptado/${userId}/${requestId}")
                                                    } else {
                                                        navController.navigate("linkdenegado")
                                                    }
                                                }
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

                            } catch (e: Exception) {
                                errorMessage = "Error general: ${e.message}"
                                e.printStackTrace()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()

            ) {
                Text("Ya he aceptado o denegado la solicitud")
            }
        }
    }
}

