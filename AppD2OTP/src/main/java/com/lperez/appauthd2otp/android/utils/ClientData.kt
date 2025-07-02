package com.lperez.appauthd2otp.android.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import android.util.Base64
import java.util.*

class ClientData(context: Context) {

    companion object {
        private const val PREF_NAME = "d2otp_prefs"
        private const val KEY_IDA = "idA"
        private const val KEY_CCAB = "ccab"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyGenParameterSpec(
            KeyGenParameterSpec.Builder(
                MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        masterKey,
        PrefKeyEncryptionScheme.AES256_SIV,
        PrefValueEncryptionScheme.AES256_GCM
    )

    val idA: String by lazy {
        sharedPreferences.getString(KEY_IDA, null) ?: generateIdA()
    }

    var ccab: String
        get() = sharedPreferences.getString(KEY_CCAB, null) ?: generateAndStoreCCAB()
        set(value) {
            sharedPreferences.edit().putString(KEY_CCAB, value).apply()
        }



    private fun generateIdA(): String {
        val newId = UUID.randomUUID().toString()
        sharedPreferences.edit().putString(KEY_IDA, newId).apply()
        return newId
    }

    private fun generateAndStoreCCAB(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        val base64Key = Base64.encodeToString(bytes, Base64.NO_WRAP)
        sharedPreferences.edit().putString(KEY_CCAB, base64Key).apply()
        return base64Key
    }
    fun storeCCAB(ccab: String) {
        sharedPreferences.edit().putString(KEY_CCAB, ccab).apply()
    }
}