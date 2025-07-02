package com.lperez.appauthd2otp.android.utils

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lperez.appauthd2otp.android.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import com.lperez.libreriad2otpkotlin.libreriaD2OTP
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class Solicitud(
    val id: String,
    val aplicacion: String,
    val fecha: String,
    val mensaje: String
)

object CryptoSigner {
    val lib=libreriaD2OTP()



    fun generarYEnviarM1Registry(username: String, pin: String,deviceID:String, context: Context, onSuccess: (String,String, String, String, String, String, String, String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val clientData= ClientData(context)
                val backend = BackendIdentityManager(context)


                val timestamp = System.currentTimeMillis().toString()
                val idA = clientData.idA
                val pinh= lib.sha256(pin)
                var idB = backend.getIdB()
                if(idB.isNullOrBlank())
                    idB = "null"
                val CCM2 = lib.generateRandomValue()
                val VAM2 = lib.generateRandomValue()
                val CCM3 = lib.generateRandomValue()
                val VAM3 = lib.generateRandomValue()
                val VRMI: String
                if(idB!= "null")
                    VRMI = lib.generateVRMI3M(idA,idB,timestamp,CCM2,VAM2,CCM3,VAM3,username+pinh+deviceID)
                else
                    VRMI = "null"
                val datos = "$timestamp|$CCM2|$VAM2|$CCM3|$VAM3|$username|$pinh|$deviceID|$VRMI"
                val ccab = clientData.ccab
                val m1Cifrado = lib.aesEncrypt(datos, ccab)

                val api = ApiClient()
                val response = api.registrarUsuarioConClave(idA, idB, m1Cifrado)

                onSuccess(response.idA, response.idB ,response.M2,CCM2,VAM2,CCM3,VAM3,timestamp)

            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error generando o enviando M1: ${e.message}")
            }
        }
    }


    fun procesarM2YEnviarM3Registry(mensajeM2: String, context: Context, ccm2:String, vam2:String, ccm3:String, vam3:String,timestampM1:String, onSuccess: ( String,String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val clientData= ClientData(context)
            try {



                val ccab = lib.generateRandomValue()



                val mensajeM2Texto = lib.aesDecrypt(mensajeM2, ccm2)

                val partes = mensajeM2Texto.split("|")
                if (partes.size < 4) {
                    return@launch onError("Formato de M2 inválido")
                }

                val timestampM2 = partes[0]
                val vam2 = partes[1]
                val usuarioId = partes[2]
                val vrm2 = partes[3]


//                if(timestampM1.toLong()>timestampM2.toLong())
//                   throw Exception("Integridad de M2 comprometida: timestamps incorrectos")

                val idA = clientData.idA
                val backend = BackendIdentityManager(context)
                val idB = backend.getIdB() ?: "null"

                val textoParaHash = "$idA$idB$timestampM2$vam2$usuarioId"
                val digest = MessageDigest.getInstance("SHA-256").digest(textoParaHash.toByteArray(Charsets.UTF_8))
                val hashCalculado = Base64.encodeToString(digest, Base64.NO_WRAP)

                if (vrm2.trim() != hashCalculado.trim()) {
                    return@launch onError("Integridad de M2 comprometida: VRM2 no válido")
                }

                val ts3 = System.currentTimeMillis().toString()
                val datos3 = ccab
                val VRM3 = lib.generateVRM3(idA,idB,ts3,vam3,datos3)
                val datos = "$ts3|$vam3|$datos3|$VRM3"
                val M3= lib.aesEncrypt(datos,ccm3)
                val api = ApiClient()
                val respuesta = api.enviarM3Registry(idA,idB,M3)





                withContext(Dispatchers.Main) {
                    onSuccess(usuarioId,ccab)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Error procesando M2 o enviando M3: ${e.message}")
                }
            }
        }
    }

    fun generarYEnviarM1Login(username: String, pin: String, context: Context, onSuccess: (String, String, String, String, String, String, String, String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val clientData= ClientData(context)
                val backend = BackendIdentityManager(context)
                val timestamp = System.currentTimeMillis().toString()
                val idA = clientData.idA
                val pinh= lib.sha256(pin)
                var idB = backend.getIdB()
                if(idB.isNullOrBlank())
                    idB = "null"
                val CCM2 = lib.generateRandomValue()
                val VAM2 = lib.generateRandomValue()
                val CCM3 = lib.generateRandomValue()
                val VAM3 = lib.generateRandomValue()
                val VRMI: String
                if(idB!= "null")
                     VRMI = lib.generateVRMI3M(idA,idB,timestamp,CCM2,VAM2,CCM3,VAM3,username + pinh)
                else
                     VRMI = "null"
                val datos = "$timestamp|$CCM2|$VAM2|$CCM3|$VAM3|$username|$pinh|$VRMI"
                val ccab = clientData.ccab
                val m1Cifrado = lib.aesEncrypt(datos, ccab)

                val api = ApiClient()
                val response = api.enviarLoginD2OTP(idA, idB, m1Cifrado)


                onSuccess(response.idA, response.idB ,response.M2,CCM2,VAM2,CCM3,VAM3,timestamp)

            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error generando o enviando M1: ${e.message}")
            }
        }
    }

    fun procesarM2YEnviarM3Login(mensajeM2: String, context: Context, ccm2:String, vam2:String, ccm3:String, vam3:String,timestampM1:String, onSuccess: (String, String,String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val clientData= ClientData(context)
            try {



                val ccab = lib.generateRandomValue()




                val mensajeM2Texto = lib.aesDecrypt(mensajeM2, ccm2)

                val partes = mensajeM2Texto.split("|")
                if (partes.size < 5) {
                    return@launch onError("Formato de M2 inválido")
                }

                val timestampM2 = partes[0]
                val vam2 = partes[1]
                val usuarioId = partes[2]
                val deviceId = partes[3]
                val vrm2 = partes[4]
//                if(timestampM1.toLong()>timestampM2.toLong())
//                    throw Exception()
                val idA = clientData.idA
                val backend = BackendIdentityManager(context)
                val idB = backend.getIdB() ?: "null"

                val hashCalculado =  lib.generateVRM2(idA,idB,timestampM2,vam2, usuarioId+deviceId)
                if (vrm2.trim() != hashCalculado.trim()) {
                    return@launch onError("Integridad de M2 comprometida: VRM2 no válido")
                }

                val ts3 = System.currentTimeMillis().toString()
                val datos3 = ccab
                val VRM3 = lib.generateVRM3(idA,idB,ts3,vam3,datos3)
                val datos = "$ts3|$vam3|$datos3|$VRM3"
                val M3= lib.aesEncrypt(datos,ccm3)
                val api = ApiClient()
                val respuesta = api.enviarM3Login(idA,idB,M3)





                withContext(Dispatchers.Main) {
                    onSuccess(usuarioId,deviceId,ccab)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Error procesando M2 o enviando M3: ${e.message}")
                }
            }
        }
    }

    fun generarYEnviarM1getSolicitudes(userId: String, context: Context, onSuccess: (String, String, String, String, String, String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val clientData= ClientData(context)
                val backend = BackendIdentityManager(context)
                val timestamp = System.currentTimeMillis().toString()
                val idA = clientData.idA
                var idB = backend.getIdB()
                if(idB.isNullOrBlank())
                    idB = "null"
                val CCM2 = lib.generateRandomValue()
                val VAM2 = lib.generateRandomValue()
                val CCM3 = lib.generateRandomValue()
                val VAM3 = lib.generateRandomValue()
                val VRMI: String
                if(idB!= "null")
                    VRMI = lib.generateVRMI3M(idA,idB,timestamp,CCM2,VAM2,CCM3,VAM3,userId)
                else
                    VRMI = "null"
                val datos = "$timestamp|$CCM2|$VAM2|$CCM3|$VAM3|$userId|$VRMI"
                val ccab = clientData.ccab
                val m1Cifrado = lib.aesEncrypt(datos, ccab)

                val api = ApiClient()
                val response = api.getSolicitudes(idA, idB, m1Cifrado)


                onSuccess(response.M2,CCM2,VAM2,CCM3,VAM3,timestamp)

            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error generando o enviando M1: ${e.message}")
            }
        }
    }

    fun procesarM2YEnviarM3getSolicitudes(mensajeM2: String, context: Context, ccm2:String, vam2:String, ccm3:String, vam3:String,timestampM1:String, onSuccess: (List<Solicitud>?, String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val clientData= ClientData(context)
            try {



                val ccab = lib.generateRandomValue()



                val mensajeM2Texto = lib.aesDecrypt(mensajeM2, ccm2)

                val partes = mensajeM2Texto.split("|")
                if (partes.size < 3) {
                    return@launch onError("Formato de M2 inválido")
                }

                val timestampM2 = partes[0]
                val vam2 = partes[1]
                val listaSolicitudesString = partes[2]
                val vrm2 = partes[3]
                if(timestampM1>timestampM2)
                    throw Exception()

                val idA = clientData.idA
                val backend = BackendIdentityManager(context)
                val idB = backend.getIdB() ?: "null"

                val textoParaHash = "$idB$idA$timestampM2$vam2$listaSolicitudesString"
                val digest = MessageDigest.getInstance("SHA-256").digest(textoParaHash.toByteArray(Charsets.UTF_8))
                val hashCalculado = Base64.encodeToString(digest, Base64.NO_WRAP)

                if (vrm2.trim() != hashCalculado.trim()) {
                    return@launch onError("Integridad de M2 comprometida: VRM2 no válido")
                }

                val ts3 = System.currentTimeMillis().toString()
                val datos3 = ccab
                val VRM3 = lib.generateVRM3(idA, idB, ts3, vam3, datos3)
                val datos = "$ts3|$vam3|$datos3|$VRM3"
                val M3 = lib.aesEncrypt(datos, ccm3)
                val api = ApiClient()
                val respuesta = api.getSolicitudesM3(idA, idB, M3)
                if (listaSolicitudesString != "") {
                    val gson = Gson()
                    val tipoLista = object : TypeToken<List<Solicitud>>() {}.type
                    val listaRecuperada: List<Solicitud> =
                        gson.fromJson(listaSolicitudesString, tipoLista)

                    withContext(Dispatchers.Main) {
                        onSuccess(listaRecuperada,ccab)
                    }
                }else {
                    withContext(Dispatchers.Main) {
                        onSuccess(null, ccab)
                    }
                }


            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Error procesando M2 o enviando M3: ${e.message}")
                }
            }
        }
    }

    fun generarYEnviarM1Aceptar(userId: String, solicitudId: String, context: Context, onSuccess: (String, String, String, String, String, String, String,String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val clientData= ClientData(context)
                val backend = BackendIdentityManager(context)
                val timestamp = System.currentTimeMillis().toString()
                val idA = clientData.idA
                var idB = backend.getIdB()
                if(idB.isNullOrBlank())
                    idB = "null"
                val CCM2 = lib.generateRandomValue()
                val VAM2 = lib.generateRandomValue()
                val CCM3 = lib.generateRandomValue()
                val VAM3 = lib.generateRandomValue()
                val VRMI: String
                if(idB!= "null")
                    VRMI = lib.generateVRMI3M(idA,idB,timestamp,CCM2,VAM2,CCM3,VAM3,userId+solicitudId)
                else
                    VRMI = "null"
                val datos = "$timestamp|$CCM2|$VAM2|$CCM3|$VAM3|$userId|$solicitudId|$VRMI"
                val ccab = clientData.ccab
                val m1Cifrado = lib.aesEncrypt(datos, ccab)

                val api = ApiClient()
                val response = api.enviarAceptar(idA, idB, m1Cifrado)


                onSuccess(response.idA, response.idB ,response.M2,CCM2,VAM2,CCM3,VAM3,timestamp)

            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error generando o enviando M1: ${e.message}")
            }
        }
    }

    fun generarYEnviarM1Denegar(userId: String, solicitudId: String, context: Context, onSuccess: (String, String, String, String, String, String, String,String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val clientData= ClientData(context)
                val backend = BackendIdentityManager(context)
                val timestamp = System.currentTimeMillis().toString()
                val idA = clientData.idA
                var idB = backend.getIdB()
                if(idB.isNullOrBlank())
                    idB = "null"
                val CCM2 = lib.generateRandomValue()
                val VAM2 = lib.generateRandomValue()
                val CCM3 = lib.generateRandomValue()
                val VAM3 = lib.generateRandomValue()
                val VRMI: String
                if(idB!= "null")
                    VRMI = lib.generateVRMI3M(idA,idB,timestamp,CCM2,VAM2,CCM3,VAM3,userId+solicitudId)
                else
                    VRMI = "null"
                val datos = "$timestamp|$CCM2|$VAM2|$CCM3|$VAM3|$userId|$solicitudId|$VRMI"
                val ccab = clientData.ccab
                val m1Cifrado = lib.aesEncrypt(datos, ccab)

                val api = ApiClient()
                val response = api.enviarDenegar(idA, idB, m1Cifrado)


                onSuccess(response.idA, response.idB ,response.M2,CCM2,VAM2,CCM3,VAM3,timestamp)

            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error generando o enviando M1: ${e.message}")
            }
        }
    }

    fun procesarM2AceptarODenegar(mensajeM2: String, context: Context, ccm2:String, vam2:String, ccm3:String, vam3:String, timestampM1:String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val clientData= ClientData(context)
            try {







                val mensajeM2Texto = lib.aesDecrypt(mensajeM2, ccm2)

                val partes = mensajeM2Texto.split("|")
                if (partes.size < 4) {
                    return@launch onError("Formato de M2 inválido")
                }

                val timestampM2 = partes[0]
                val vam2 = partes[1]
                val newCCBA = partes[2]
                val vrm2 = partes[3]
                if(timestampM1>timestampM2)
                    throw Exception()

                val idA = clientData.idA
                val backend = BackendIdentityManager(context)
                val idB = backend.getIdB() ?: "null"

                val textoParaHash = "$idB$idA$timestampM2$vam2$newCCBA"
                val digest = MessageDigest.getInstance("SHA-256").digest(textoParaHash.toByteArray(Charsets.UTF_8))
                val hashCalculado = Base64.encodeToString(digest, Base64.NO_WRAP)

                if (vrm2.trim() != hashCalculado.trim()) {
                    return@launch onError("Integridad de M2 comprometida: VRM2 no válido")
                }






                withContext(Dispatchers.Main) {
                    onSuccess(newCCBA)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Error procesando M2 o enviando M3: ${e.message}")
                }
            }
        }
    }

    fun generarYEnviarM1ObtenerSolicitud(userId: String,requestId:String, context: Context, onSuccess: (String, String, String, String, String, String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val clientData= ClientData(context)
                val backend = BackendIdentityManager(context)
                val timestamp = System.currentTimeMillis().toString()
                val idA = clientData.idA
                var idB = backend.getIdB()
                if(idB.isNullOrBlank())
                    idB = "null"
                val CCM2 = lib.generateRandomValue()
                val VAM2 = lib.generateRandomValue()
                val CCM3 = lib.generateRandomValue()
                val VAM3 = lib.generateRandomValue()
                val VRMI: String
                if(idB!= "null")
                    VRMI = lib.generateVRMI3M(idA,idB,timestamp,CCM2,VAM2,CCM3,VAM3,userId+requestId)
                else
                    VRMI = "null"
                val datos = "$timestamp|$CCM2|$VAM2|$CCM3|$VAM3|$userId|$requestId|$VRMI"
                val ccab = clientData.ccab
                val m1Cifrado = lib.aesEncrypt(datos, ccab)

                val api = ApiClient()
                val response = api.getSolicitud(idA, idB, m1Cifrado)

                onSuccess(response.M2,CCM2,VAM2,CCM3,VAM3,timestamp)

            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error generando o enviando M1: ${e.message}")
            }
        }
    }



    fun procesarM2ObtenerSolicitud(mensajeM2: String, context: Context, ccm2:String, vam2:String, ccm3:String, vam3:String, timestampM1:String, onSuccess: (Solicitud,String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val clientData= ClientData(context)
            try {







                val mensajeM2Texto = lib.aesDecrypt(mensajeM2, ccm2)

                val partes = mensajeM2Texto.split("|")
                if (partes.size < 4) {
                    return@launch onError("Formato de M2 inválido")
                }

                val timestampM2 = partes[0]
                val vam2 = partes[1]
                val solicitud_string = partes[2]
                val newCCAB = partes[3]
                val vrm2 = partes[4]
                if(timestampM1>timestampM2)
                    throw Exception()

                val idA = clientData.idA
                val backend = BackendIdentityManager(context)
                val idB = backend.getIdB() ?: "null"

                val textoParaHash = "$idB$idA$timestampM2$vam2$solicitud_string$newCCAB"
                val digest = MessageDigest.getInstance("SHA-256").digest(textoParaHash.toByteArray(Charsets.UTF_8))
                val hashCalculado = Base64.encodeToString(digest, Base64.NO_WRAP)

                if (vrm2.trim() != hashCalculado.trim()) {
                    return@launch onError("Integridad de M2 comprometida: VRM2 no válido")
                }

                val gson = Gson()
                val solicitud: Solicitud = gson.fromJson(solicitud_string, Solicitud::class.java)





                withContext(Dispatchers.Main) {
                    onSuccess(solicitud,newCCAB)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Error procesando M2 o enviando M3: ${e.message}")
                }
            }
        }
    }

    fun generarYEnviarM1SolicitudVinculacion(username: String, pin: String,deviceId:String, context: Context, onSuccess: (String, String, String, String, String, String, String, String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val clientData= ClientData(context)
                val backend = BackendIdentityManager(context)
                val timestamp = System.currentTimeMillis().toString()
                val idA = clientData.idA
                val pinh= lib.sha256(pin)
                val mensaje:String ="Petición de cambio de dispositivo vinculado"
                val app:String ="App 2FA"
                val fechaHora: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                var idB = backend.getIdB()
                if(idB.isNullOrBlank())
                    idB = "null"
                val CCM2 = lib.generateRandomValue()
                val VAM2 = lib.generateRandomValue()
                val CCM3 = lib.generateRandomValue()
                val VAM3 = lib.generateRandomValue()
                val VRMI: String
                if(idB!= "null")
                    VRMI = lib.generateVRMI3M(idA,idB,timestamp,CCM2,VAM2,CCM3,VAM3,username + pinh+deviceId+mensaje+app+fechaHora)
                else
                    VRMI = "null"
                val datos = "$timestamp|$CCM2|$VAM2|$CCM3|$VAM3|$username|$pinh|$deviceId|$mensaje|$app|$fechaHora|$VRMI"
                val ccab = clientData.ccab
                val m1Cifrado = lib.aesEncrypt(datos, ccab)

                val api = ApiClient()
                val response = api.vincularDispositivo(idA, idB, m1Cifrado)


                onSuccess(response.idA, response.idB ,response.M2,CCM2,VAM2,CCM3,VAM3,timestamp)

            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error generando o enviando M1: ${e.message}")
            }
        }
    }

    fun procesarM2YEnviarM3SolicitudVinculacion(mensajeM2: String, context: Context, ccm2:String, vam2:String, ccm3:String, vam3:String,timestampM1:String, onSuccess: (String, String, String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {


                val clientData = ClientData(context)
                val ccab = lib.generateRandomValue()


                val mensajeM2Texto = lib.aesDecrypt(mensajeM2, ccm2)

                val partes = mensajeM2Texto.split("|")


                if (partes.size < 5) {
                    return@launch onError("Formato de M2 inválido")
                }

                val timestampM2 = partes[0]
                val vam2 = partes[1]
                val userId = partes[2]
                val requestId = partes[3]
                val vrm2 = partes[4]
                if (timestampM1 > timestampM2)
                    throw Exception()

                val idA = clientData.idA
                val backend = BackendIdentityManager(context)
                val idB = backend.getIdB() ?: "null"

                val textoParaHash = "$idB$idA$timestampM2$vam2$userId$requestId"
                val digest = MessageDigest.getInstance("SHA-256")
                    .digest(textoParaHash.toByteArray(Charsets.UTF_8))
                val hashCalculado = Base64.encodeToString(digest, Base64.NO_WRAP)

                if (vrm2.trim() != hashCalculado.trim()) {
                    return@launch onError("Integridad de M2 comprometida: VRM2 no válido")
                }

                val ts3 = System.currentTimeMillis().toString()
                val datos3 = ccab
                val VRM3 = lib.generateVRM3(idA, idB, ts3, vam3, datos3)
                val datos = "$ts3|$vam3|$datos3|$VRM3"
                val M3 = lib.aesEncrypt(datos, ccm3)
                val api = ApiClient()
                val respuesta = api.enviarM3VincularDispositivo(idA, idB, M3)





                withContext(Dispatchers.Main) {
                    onSuccess(userId, requestId, ccab)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Error procesando M2 o enviando M3: ${e.message}")
                }
            }
        }
    }



    fun generarYEnviarM1ComprobarVinculacion(userId:String, requestId: String, context: Context, onSuccess: (String, String, String, String, String, String, String,String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val clientData= ClientData(context)
                val backend = BackendIdentityManager(context)
                val timestamp = System.currentTimeMillis().toString()
                val idA = clientData.idA


                var idB = backend.getIdB()
                if(idB.isNullOrBlank())
                    idB = "null"
                val CCM2 = lib.generateRandomValue()
                val VAM2 = lib.generateRandomValue()
                val CCM3 = lib.generateRandomValue()
                val VAM3 = lib.generateRandomValue()
                val VRMI: String
                if(idB!= "null")
                    VRMI = lib.generateVRMI3M(idA,idB,timestamp,CCM2,VAM2,CCM3,VAM3,userId + requestId)
                else
                    VRMI = "null"

                val datos = "$timestamp|$CCM2|$VAM2|$CCM3|$VAM3|$userId|$requestId|$VRMI"
                val ccab = clientData.ccab
                val m1Cifrado = lib.aesEncrypt(datos, ccab)


                val api = ApiClient()
                val response = api.comprobarVinculacion(idA, idB, m1Cifrado)
                onSuccess(response.idA, response.idB ,response.M2,CCM2,VAM2,CCM3,VAM3,timestamp)

            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error generando o enviando M1: ${e.message}")
            }
        }
    }

    fun procesarM2YEnviarM3ComprobarVinculacion(mensajeM2: String, context: Context, ccm2:String, vam2:String, ccm3:String, vam3:String,timestampM1:String, onSuccess: (Boolean,Boolean, String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {


                val clientData = ClientData(context)
                val ccab = lib.generateRandomValue()


                val mensajeM2Texto = lib.aesDecrypt(mensajeM2, ccm2)

                val partes = mensajeM2Texto.split("|")

                if (partes.size < 4) {
                    return@launch onError("Formato de M2 inválido")
                }

                val timestampM2 = partes[0]
                val vam2 = partes[1]
                val estado = partes[2]
                val vrm2 = partes[3]
                if(timestampM1>timestampM2)
                    throw Exception()

                val idA = clientData.idA
                val backend = BackendIdentityManager(context)
                val idB = backend.getIdB() ?: "null"

                val textoParaHash = "$idB$idA$timestampM2$vam2$estado"
                val digest = MessageDigest.getInstance("SHA-256").digest(textoParaHash.toByteArray(Charsets.UTF_8))
                val hashCalculado = Base64.encodeToString(digest, Base64.NO_WRAP)

                if (vrm2.trim() != hashCalculado.trim()) {
                    return@launch onError("Integridad de M2 comprometida: VRM2 no válido")
                }

                val ts3 = System.currentTimeMillis().toString()
                val datos3 = ccab
                val VRM3 = lib.generateVRM3(idA,idB,ts3,vam3,datos3)
                val datos = "$ts3|$vam3|$datos3|$VRM3"
                val M3= lib.aesEncrypt(datos,ccm3)
                val api = ApiClient()
                val respuesta = api.enviarM3comprobarVinculacion(idA,idB,M3)
                val change: Boolean
                val state:Boolean
                if(estado=="aceptada"||estado == "denegada") {
                    change = true
                    if(estado=="aceptada"){
                        state=true
                    }else {
                        state = false
                    }
                }else {
                    change=false
                    state=false
                }






                withContext(Dispatchers.Main) {
                    onSuccess(change,state,ccab)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Error procesando M2 o enviando M3: ${e.message}")
                }
            }
        }
    }

    fun generarYEnviarM1cambioDevice(userId: String, solicitudId: String, context: Context, onSuccess: (String, String, String, String, String, String, String,String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val clientData= ClientData(context)
                val backend = BackendIdentityManager(context)
                val timestamp = System.currentTimeMillis().toString()
                val idA = clientData.idA
                var idB = backend.getIdB()
                if(idB.isNullOrBlank())
                    idB = "null"
                val CCM2 = lib.generateRandomValue()
                val VAM2 = lib.generateRandomValue()
                val CCM3 = lib.generateRandomValue()
                val VAM3 = lib.generateRandomValue()
                val VRMI: String
                if(idB!= "null")
                    VRMI = lib.generateVRMI3M(idA,idB,timestamp,CCM2,VAM2,CCM3,VAM3,userId+solicitudId)
                else
                    VRMI = "null"
                val datos = "$timestamp|$CCM2|$VAM2|$CCM3|$VAM3|$userId|$solicitudId|$VRMI"
                val ccab = clientData.ccab
                val m1Cifrado = lib.aesEncrypt(datos, ccab)

                val api = ApiClient()
                val response = api.cambioDevice(idA, idB, m1Cifrado)

                onSuccess(response.idA, response.idB ,response.M2,CCM2,VAM2,CCM3,VAM3,timestamp)

            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error generando o enviando M1: ${e.message}")
            }
        }
    }


    fun procesarM2YEnviarM3CambioDevice(mensajeM2: String, context: Context, ccm2:String, vam2:String, ccm3:String, vam3:String,timestampM1:String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val clientData = ClientData(context)
                val ccab = lib.generateRandomValue()


                val mensajeM2Texto = lib.aesDecrypt(mensajeM2, ccm2)

                val partes = mensajeM2Texto.split("|")

                if (partes.size < 4) {
                    return@launch onError("Formato de M2 inválido")
                }

                val timestampM2 = partes[0]
                val vam2 = partes[1]
                val deviceId = partes[2]
                val vrm2 = partes[3]
                if(timestampM1>timestampM2)
                    throw Exception()

                val idA = clientData.idA
                val backend = BackendIdentityManager(context)
                val idB = backend.getIdB() ?: "null"

                val textoParaHash = "$idB$idA$timestampM2$vam2$deviceId"
                val digest = MessageDigest.getInstance("SHA-256").digest(textoParaHash.toByteArray(Charsets.UTF_8))
                val hashCalculado = Base64.encodeToString(digest, Base64.NO_WRAP)

                if (vrm2.trim() != hashCalculado.trim()) {
                    return@launch onError("Integridad de M2 comprometida: VRM2 no válido")
                }

                val ts3 = System.currentTimeMillis().toString()
                val datos3 = ccab
                val VRM3 = lib.generateVRM3(idA,idB,ts3,vam3,datos3)
                val datos = "$ts3|$vam3|$datos3|$VRM3"
                val M3= lib.aesEncrypt(datos,ccm3)
                val api = ApiClient()
                val respuesta = api.enviarM3cambioDevice(idA,idB,M3)




                withContext(Dispatchers.Main) {
                    onSuccess(ccab)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("Error procesando M2 o enviando M3: ${e.message}")
                }
            }
        }
    }



}

