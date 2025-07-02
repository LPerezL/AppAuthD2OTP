package com.lperez.appauthd2otp.android.network

import android.content.Context
import com.lperez.appauthd2otp.android.utils.BackendIdentityManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier



@Serializable
data class RegisterClientRequest(
    val idClient: String,
    val claveCAB: String
)

@Serializable
data class RegisterClientResponse(
    val idServer: String
)











@Serializable
data class M1Message(
    val idA: String,
    val idB: String,
    val M1: String
)


@Serializable
data class M2Message(
    val idA: String,
    val idB: String,
    val M2: String
)




@Serializable
data class M3Message(
    val idA: String,
    val idB: String,
    val M3: String
)





class ApiClient {


    private val baseUrl = "https://b2eb-79-117-109-34.ngrok-free.app"


    private val client = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(10, TimeUnit.SECONDS)
                readTimeout(10, TimeUnit.SECONDS)
                hostnameVerifier(HostnameVerifier { _, _ -> true })
            }
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }





    suspend fun registrarAppBackend(
        context: Context,
        idClient: String,
        clavePublica: String
    ): Result<String> {
        return try {
            val response: RegisterClientResponse = client.post("$baseUrl/registroCliente") {
                contentType(ContentType.Application.Json)
                header("ngrok-skip-browser-warning", "true")
                setBody(RegisterClientRequest(idClient, clavePublica))
            }.body()

            val backendIdentityManager = BackendIdentityManager(context)
            backendIdentityManager.saveIdB(response.idServer)

            Result.success(response.idServer)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun registrarUsuarioConClave(
        idA: String,
        idB: String,
        M1: String
    ): M2Message {
        return client.post("$baseUrl/altaUsuario") {
            contentType(ContentType.Application.Json)
            setBody(
                M1Message(
                    idA = idA,
                    idB = idB,
                    M1=M1

                )
            )
        }.body()
    }

    suspend fun enviarM3Registry(
        IdA: String,
        IdB: String,
        M3:String
    ) {
        client.post("$baseUrl/m3_altaUsuario_confirmacion") {
            contentType(ContentType.Application.Json)
            setBody(
                M3Message(
                    idA = IdA,
                    idB = IdB,
                    M3 = M3
                )
            )
        }
    }




    suspend fun enviarLoginD2OTP(
        idA: String,
        idB: String,
        M1:String
    ): M2Message {

        return client.post("$baseUrl/login_d2otp") {
            contentType(ContentType.Application.Json)
            setBody(
                M1Message(
                    idA = idA,
                    idB = idB,
                    M1=M1

                )
            )
        }.body()
    }

    suspend fun enviarM3Login(
        IdA: String,
        IdB: String,
        M3:String
    ) {
        client.post("$baseUrl/m3_login_confirmacion") {
            contentType(ContentType.Application.Json)
            setBody(
                M3Message(
                    idA = IdA,
                    idB = IdB,
                    M3 = M3
                )
            )
        }
    }

    suspend fun getSolicitudes(
        idA: String,
        idB: String,
        M1:String
    ): M2Message {

        return client.post("$baseUrl/solicitudesPendientes") {
            contentType(ContentType.Application.Json)
            setBody(
                M1Message(
                    idA = idA,
                    idB = idB,
                    M1=M1

                )
            )
        }.body()
    }

    suspend fun getSolicitudesM3(
        IdA: String,
        IdB: String,
        M3:String
    ) {
        client.post("$baseUrl/m3_solicitudesPendientes_confirmacion") {
            contentType(ContentType.Application.Json)
            setBody(
                M3Message(
                    idA = IdA,
                    idB = IdB,
                    M3 = M3
                )
            )
        }
    }

    suspend fun enviarAceptar(
        idA: String,
        idB: String,
        M1: String
    ): M2Message {
        return client.post("$baseUrl/aceptarSolicitud") {
            contentType(ContentType.Application.Json)
            setBody(
                M1Message(
                    idA = idA,
                    idB = idB,
                    M1=M1

                )
            )
        }.body()
    }

    suspend fun enviarDenegar(
        idA: String,
        idB: String,
        M1: String
    ): M2Message {
        return client.post("$baseUrl/denegarSolicitud") {
            contentType(ContentType.Application.Json)
            setBody(
                M1Message(
                    idA = idA,
                    idB = idB,
                    M1=M1

                )
            )
        }.body()
    }

    suspend fun getSolicitud(
        idA: String,
        idB: String,
        M1:String
    ): M2Message {

        return client.post("$baseUrl/obtenerSolicitud") {
            contentType(ContentType.Application.Json)
            setBody(
                M1Message(
                    idA = idA,
                    idB = idB,
                    M1=M1

                )
            )
        }.body()
    }
    suspend fun vincularDispositivo(
        idA: String,
        idB: String,
        M1:String
    ): M2Message {
        return client.post("$baseUrl/vincularDispositivo") {
            contentType(ContentType.Application.Json)
            setBody(
                M1Message(
                    idA = idA,
                    idB = idB,
                    M1=M1

                )
            )
        }.body()
    }

    suspend fun enviarM3VincularDispositivo(
        IdA: String,
        IdB: String,
        M3:String
    ) {
        client.post("$baseUrl/m3_vincularDispositivo_confirmacion") {
            contentType(ContentType.Application.Json)
            setBody(
                M3Message(
                    idA = IdA,
                    idB = IdB,
                    M3 = M3
                )
            )
        }
    }

    suspend fun comprobarVinculacion(
        idA: String,
        idB: String,
        M1: String
    ): M2Message {
        return client.post("$baseUrl/comprobarSolicitud") {
            contentType(ContentType.Application.Json)
            setBody(
                M1Message(
                    idA = idA,
                    idB = idB,
                    M1=M1

                )
            )
        }.body()
    }

    suspend fun enviarM3comprobarVinculacion(
        idA: String,
        idB: String,
        M3: String
    ) {
        return client.post("$baseUrl/m3_comprobarSolicitud_confirmacion") {
            contentType(ContentType.Application.Json)
            setBody(
                M3Message(
                    idA = idA,
                    idB = idB,
                    M3=M3

                )
            )
        }.body()
    }

    suspend fun cambioDevice(
        idA: String,
        idB: String,
        M1: String
    ): M2Message {
        return client.post("$baseUrl/cambiarDevice") {
            contentType(ContentType.Application.Json)
            setBody(
                M1Message(
                    idA = idA,
                    idB = idB,
                    M1=M1

                )
            )
        }.body()
    }


    suspend fun enviarM3cambioDevice(
        idA: String,
        idB: String,
        M3: String
    ) {
        return client.post("$baseUrl/m3_cambiarDevice_confirmacion") {
            contentType(ContentType.Application.Json)
            setBody(
                M3Message(
                    idA = idA,
                    idB = idB,
                    M3=M3

                )
            )
        }.body()
    }
}

