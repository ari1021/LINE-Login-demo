package com.example.login.utils

import java.util.*
import kotlin.random.Random

class Utils {
    fun getRandom(): String {
        val bytes = Random.nextBytes(32)
        val e = Base64.getUrlEncoder().withoutPadding()
        return e.encodeToString(bytes)
    }
}