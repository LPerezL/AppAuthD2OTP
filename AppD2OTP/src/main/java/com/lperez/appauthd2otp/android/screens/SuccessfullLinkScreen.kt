package com.lperez.appauthd2otp.android.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lperez.appauthd2otp.android.utils.ClientData
import com.lperez.appauthd2otp.android.utils.CryptoSigner
@Composable
fun SuccessfullLinkScreen(navController: NavHostController,userId:String?,requestId:String?) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val clientData= ClientData(context)
    LaunchedEffect(Unit) {
        if(userId!=null&&requestId!=null) {


            CryptoSigner.generarYEnviarM1cambioDevice(
                userId = userId,
                solicitudId = requestId,
                context = context,
                onSuccess = { IdAM2, IdBM2, mensajeM2, CCM2, VAM2, CCM3, VAM3, timestampM1 ->


                    CryptoSigner.procesarM2YEnviarM3CambioDevice(
                        mensajeM2 = mensajeM2,
                        vam2 = VAM2,
                        vam3 = VAM3,
                        ccm2 = CCM2,
                        ccm3 = CCM3,
                        timestampM1 = timestampM1,
                        context = context,
                        onSuccess = {ccab  ->
                            clientData.storeCCAB(ccab)

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
                text = "Vinculación exitosa. Ahora podrás iniciar sesión con tu usuario desde este dispositivo.",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = {
                    navController.navigate("welcome")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Confirmar")
            }
        }
    }
}