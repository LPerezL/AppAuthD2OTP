package com.lperez.appauthd2otp.android.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lperez.appauthd2otp.android.utils.ClientData
import com.lperez.appauthd2otp.android.utils.CryptoSigner
import com.lperez.appauthd2otp.android.utils.Solicitud
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@Composable
fun AuthRequestsScreen(navController: NavController, userId: String?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val solicitudes = remember { mutableStateListOf<Solicitud>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val clientData=ClientData(context)
    var isCooldownActive by remember { mutableStateOf(false) }


    LaunchedEffect(userId) {
        if (userId != null) {
            CryptoSigner.generarYEnviarM1getSolicitudes(
                userId=userId,
                context=context,
                onSuccess = { M2,CCM2,VAM2,CCM3,VAM3,timestamp ->

                    CryptoSigner.procesarM2YEnviarM3getSolicitudes(
                        mensajeM2 = M2,
                        vam2 = VAM2,
                        vam3 = VAM3,
                        ccm2 = CCM2,
                        ccm3 = CCM3,
                        timestampM1 = timestamp,
                        context = context,
                        onSuccess = { nuevasSolicitudes, newccab ->
                            clientData.storeCCAB(newccab)
                            solicitudes.clear()
                            if (nuevasSolicitudes!=null) {

                                solicitudes.addAll(nuevasSolicitudes)
                            }

                        },
                        onError = { error ->
                            errorMessage = "Error al procesar Solicitudes"
                        }
                    )

                },
                onError = {
                    errorMessage = "Error al obtener solicitudes"
                }
            )
        }
    }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Text(
            text = "Lista de solicitudes",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }



        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(solicitudes) { solicitud ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            coroutineScope.launch {


                                if (userId != null) {
                                    CryptoSigner.generarYEnviarM1getSolicitudes(
                                        userId = userId,
                                        context = context,
                                        onSuccess = { M2, CCM2, VAM2, CCM3, VAM3, timestamp ->
                                            CryptoSigner.procesarM2YEnviarM3getSolicitudes(
                                                mensajeM2 = M2,
                                                vam2 = VAM2,
                                                vam3 = VAM3,
                                                ccm2 = CCM2,
                                                ccm3 = CCM3,
                                                timestampM1 = timestamp,
                                                context = context,
                                                onSuccess = { nuevasSolicitudes, newccab ->
                                                    clientData.storeCCAB(newccab)
                                                    solicitudes.clear()
                                                    if (nuevasSolicitudes != null) {

                                                        solicitudes.addAll(nuevasSolicitudes)
                                                        if(solicitudes.contains(solicitud)){
                                                            navController.navigate("requesdetail/${userId}/${solicitud.id}")
                                                        }
                                                    }
                                                },
                                                onError = { error ->
                                                    errorMessage = "Error al procesar Solicitudes"
                                                }
                                            )
                                        },
                                        onError = {
                                            errorMessage = "Error al obtener solicitudes"
                                        }
                                    )
                                }

                                delay(3000)
                                isCooldownActive = false
                            }

                        }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color(0xFFE0E0E0))
                    ) {
                        Text(solicitud.mensaje)
                        Text("App: ${solicitud.aplicacion}")
                        Text("Fecha: ${solicitud.fecha}")
                        Text("[Ver →]")
                    }
                }
            }
        }

        Button(
            onClick = {
                if (isCooldownActive) return@Button
                isCooldownActive = true
                coroutineScope.launch {


                    if (userId != null) {
                        CryptoSigner.generarYEnviarM1getSolicitudes(
                            userId = userId,
                            context = context,
                            onSuccess = { M2, CCM2, VAM2, CCM3, VAM3, timestamp ->
                                CryptoSigner.procesarM2YEnviarM3getSolicitudes(
                                    mensajeM2 = M2,
                                    vam2 = VAM2,
                                    vam3 = VAM3,
                                    ccm2 = CCM2,
                                    ccm3 = CCM3,
                                    timestampM1 = timestamp,
                                    context = context,
                                    onSuccess = { nuevasSolicitudes, newccab ->
                                        clientData.storeCCAB(newccab)
                                        solicitudes.clear()
                                        if (nuevasSolicitudes != null) {

                                            solicitudes.addAll(nuevasSolicitudes)
                                        }
                                    },
                                    onError = { error ->
                                        errorMessage = "Error al procesar Solicitudes"
                                    }
                                )
                            },
                            onError = {
                                errorMessage = "Error al obtener solicitudes"
                            }
                        )
                    }

                    delay(3000)
                    isCooldownActive = false
                }
            },
            enabled = !isCooldownActive,
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
        ) {
            Text("Recargar lista de solicitudes")
        }


        BackHandler {
            navController.navigate("welcome") {
                popUpTo("requests/${userId}") { inclusive = true }
            }
        }
    }
}
