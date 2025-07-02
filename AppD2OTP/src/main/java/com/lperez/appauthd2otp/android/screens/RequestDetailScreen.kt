package com.lperez.appauthd2otp.android.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.lperez.appauthd2otp.android.utils.ClientData
import com.lperez.appauthd2otp.android.utils.CryptoSigner
import com.lperez.appauthd2otp.android.utils.Solicitud
import kotlinx.coroutines.launch

@Composable
fun RequestDetailScreen(
    navController: NavController,
    userId: String?,
    solicitudId: String?
) {
    val solicitud = remember {
        mutableStateOf<Solicitud?>(null)
    }
    val coroutineScope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val clientData= ClientData(context)

    LaunchedEffect(userId, solicitudId) {
        if (userId != null && solicitudId != null) {
            try {
                CryptoSigner.generarYEnviarM1ObtenerSolicitud(
                    userId=userId,
                    requestId=solicitudId,
                    context=context,
                    onSuccess = {M2,CCM2,VAM2,CCM3,VAM3,timestamp ->

                        CryptoSigner.procesarM2ObtenerSolicitud(
                            mensajeM2 = M2,
                            vam2 = VAM2,
                            vam3 = VAM3,
                            ccm2 = CCM2,
                            ccm3 = CCM3,
                            timestampM1 = timestamp,
                            context = context,
                            onSuccess = { solicitudObtenida, newccab ->
                                clientData.storeCCAB(newccab)
                                solicitud.value= solicitudObtenida

                            },
                            onError = { error ->
                                errorMessage = "Error al procesar Solicitudes"
                            }
                        )

                    },
                    onError = {

                    }

                    )

            } catch (e: Exception) {
                errorMessage = "Error al obtener solicitud: ${e.message}"
            }
        }
    }
    errorMessage?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp)
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "🔐 Solicitud de autenticación",
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (solicitud.value == null) {
                Text("Cargando solicitud...")
            } else {
                val data = solicitud.value!!
                Text("Aplicación: ${data.aplicacion ?: "N/A"}")
                Text("Fecha: ${data.fecha ?: "N/A"}")
                Text("Operación: ${data.mensaje ?: "N/A"}")
                Text("ID transacción: ${data.id  ?: "N/A"}")

                Spacer(Modifier.height(24.dp))
                Text("¿Deseas aprobar esta operación?")
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (!userId.isNullOrBlank() && !solicitudId.isNullOrBlank())
                                    try {
                                        CryptoSigner.generarYEnviarM1Aceptar(
                                            userId = userId,
                                            solicitudId = solicitudId,
                                            context = context,
                                            onSuccess = { IdAM2, IdBM2, mensajeM2, CCM2, VAM2, CCM3, VAM3,timestampM1 ->
                                                CryptoSigner.procesarM2AceptarODenegar(
                                                    mensajeM2=mensajeM2,
                                                    vam2 = VAM2,
                                                    vam3 = VAM3,
                                                    ccm2 = CCM2,
                                                    ccm3 = CCM3,
                                                    timestampM1 = timestampM1,
                                                    context = context,
                                                    onSuccess = { newccba ->
                                                        clientData.storeCCAB(newccba)
                                                        navController.navigate("welcome") {
                                                            popUpTo("requestdetail/${userId}/${solicitudId}") { inclusive = true }
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


                                    } catch (e: Exception) {
                                        errorMessage = "Error general: ${e.message}"
                                        e.printStackTrace()
                                    }
                            }
                        }
                    ) {
                        Text("Aceptar")
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (!userId.isNullOrBlank() && !solicitudId.isNullOrBlank())
                                    try {
                                        CryptoSigner.generarYEnviarM1Denegar(
                                            userId = userId,
                                            solicitudId = solicitudId,
                                            context = context,
                                            onSuccess = { IdAM2, IdBM2, mensajeM2, CCM2, VAM2, CCM3, VAM3,timestampM1 ->
                                                CryptoSigner.procesarM2AceptarODenegar(
                                                    mensajeM2=mensajeM2,
                                                    vam2 = VAM2,
                                                    vam3 = VAM3,
                                                    ccm2 = CCM2,
                                                    ccm3 = CCM3,
                                                    timestampM1 = timestampM1,
                                                    context = context,
                                                    onSuccess = { newccba ->
                                                        clientData.storeCCAB(newccba)
                                                        navController.navigate("welcome") {
                                                            popUpTo("requestdetail/${userId}/${solicitudId}") { inclusive = true }
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


                                    } catch (e: Exception) {
                                        errorMessage = "Error general: ${e.message}"
                                        e.printStackTrace()
                                    }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}