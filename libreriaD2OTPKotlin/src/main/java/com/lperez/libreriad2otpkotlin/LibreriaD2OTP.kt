package com.lperez.libreriad2otpkotlin

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class libreriaD2OTP {
    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))

        return Base64.getEncoder().encodeToString(bytes)
    }
    fun generateRandomValue(length: Int = 16): String {
        val random = SecureRandom()
        val value = ByteArray(length)
        random.nextBytes(value)
        return Base64.getEncoder().encodeToString(value)
    }

    fun aesEncrypt(plainText: String, base64Key: String): String {
        val keyBytes = Base64.getDecoder().decode(base64Key)
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encrypted)
    }


    fun aesDecrypt(encryptedBase64: String, base64Key: String): String {
        val keyBytes = Base64.getDecoder().decode(base64Key)
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec)
        val encryptedBytes = Base64.getDecoder().decode(encryptedBase64)
        val decrypted = cipher.doFinal(encryptedBytes)
        return String(decrypted, Charsets.UTF_8)
    }



    fun generateVRMI3M(
        idA: String,
        idB: String,
        timestamp: String,
        CCM2: String,
        VAM2: String,
        CCM3: String,
        VAM3: String,
        datosI: String
    ): String {
        val combined = idB + idA + timestamp + CCM2 + VAM2 + CCM3 + VAM3 + datosI
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    fun generateVRMI2M(
        idA: String,
        idB: String,
        timestamp: String,
        CCM2: String,
        VAM2: String,
        datosI: String
    ): String {
        val combined = idB + idA + timestamp + CCM2 + VAM2 + datosI
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    fun generateVRMI1M(
        idA: String,
        idB: String,
        timestamp:String,
        vam2:String,
        datos: String
    ): String {
        val combined = idB + idA +  timestamp+vam2+datos
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    fun generateVRM2(
        idA: String,
        idB: String,
        timestamp:String,
        vam2:String,
        datos: String
    ): String {
        val combined = idB + idA +  timestamp+vam2+datos
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    fun generateVRM3(
        idA: String,
        idB: String,
        timestamp:String,
        vam3:String,
        datos: String
    ): String {
        val combined = idB + idA +  timestamp+vam3+datos
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashBytes)
    }






}