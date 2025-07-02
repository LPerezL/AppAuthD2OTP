package com.lperez.appauthd2otp.android.utils

import android.content.Context
import java.util.UUID

object DeviceIdProvider {
    private const val PREFS_NAME = "device_prefs"
    private const val DEVICE_ID_KEY = "device_id"

    fun getOrCreateDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var deviceId = prefs.getString(DEVICE_ID_KEY, null)

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString(DEVICE_ID_KEY, deviceId).apply()
        }

        return deviceId
    }
}
