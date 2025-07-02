package com.lperez.appauthd2otp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform