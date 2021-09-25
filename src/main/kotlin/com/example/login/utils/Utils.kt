package com.example.login.utils

import java.security.MessageDigest
import java.util.*
import kotlin.random.Random

object Utils {
    private val urlEncoderWithoutPadding = Base64.getUrlEncoder().withoutPadding()
//    how to Base64 encode
//    1. split data into 6 bits each(if less than 6 bits, add 0 to make it 6 bits)
//    2. convert each 6 bits into 4 characters with Base64 table
//    (there are two characters left at the end, so add two "=".
//    But, without padding, do nothing.)
//
//    e.g.) Random.nextBytes(4) = [5, 72, -104, 29]
//    -(binary)-> [00000101, 01001000, 10011000, 00011101]
//    -(split 6 bits each)-> [000001, 010100, 100010, 011000, 000111, 01]
//    -(add 0)-> [000001, 010100, 100010, 011000, 000111, 010000]
//    -(Base64 table)-> [BUiY, HQ]
//    -(if padding)-> [BUiY, HQ==]
//
//    If without padding, 32 bytes random is converted to String of 43 characters
//    because 32 * 8 / 6 = 42.6666

    fun getRandom(): String {
        val bytes = Random.nextBytes(32)
        return urlEncodeWithoutPadding(bytes)
    }

    fun sha256WithBase64UrlEncoded(message: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(message.toByteArray())
        return urlEncodeWithoutPadding(digest)
    }

    private fun urlEncodeWithoutPadding(content: ByteArray): String = urlEncoderWithoutPadding.encodeToString(content)
}
