package com.lperez.appauthd2otp.android.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class BackendIdentityManager(context: Context) {

    companion object {
        private const val PREF_NAME = "backend_identity_prefs"
        private const val KEY_IDB = "idB"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveIdB(idB: String) {
        sharedPreferences.edit().putString(KEY_IDB, idB).apply()
    }

    fun getIdB(): String? {
        return sharedPreferences.getString(KEY_IDB, null)
    }
}


