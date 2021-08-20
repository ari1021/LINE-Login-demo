package com.example.login.line.api.v2

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import io.github.cdimascio.dotenv.dotenv
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.io.UnsupportedEncodingException
import java.util.*

@Component
class LineAPIService(private val restTemplate: RestTemplate) {
    private val dotenv = dotenv()
    private val clientId = dotenv["CLIENT_ID"]
    private val clientSecret = dotenv["CLIENT_SECRET"]

    companion object {
        const val redirectUri = "http://localhost:8080/auth"
    }

    fun getLineLoginUrl(state: String, nonce: String, scopes: List<String>): String {
        val scope = scopes.joinToString("%20")
        return "https://access.line.me/oauth2/v2.1/authorize?response_type=code" +
            "&client_id=$clientId" +
            "&redirect_uri=$redirectUri" +
            "&state=$state" +
            "&scope=$scope" +
            "&nonce=$nonce"
    }

    fun getToken(code: String): TokenResponse? {
        val uri = "https://api.line.me/oauth2/v2.1/token"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val map = LinkedMultiValueMap<String, String>()
        map.add("grant_type", "authorization_code")
        map.add("code", code)
        map.add("redirect_uri", redirectUri)
        map.add("client_id", clientId)
        map.add("client_secret", clientSecret)
        val entity = HttpEntity(map, headers)
        val response = restTemplate.exchange(uri, HttpMethod.POST, entity, TokenResponse::class.java)
        return response.body
    }

    fun verifyIdToken(idToken: String, nonce: String): Boolean {
        return try {
            JWT.require(Algorithm.HMAC256(clientSecret))
                .withIssuer("https://access.line.me")
                .withAudience(clientId)
                .withClaim("nonce", nonce)
                .acceptLeeway(60)
                .build()
                .verify(idToken)
            true
        } catch (e: UnsupportedEncodingException) {
            false
        } catch (e: JWTVerificationException) {
            false
        }
    }

    fun decodeIdToken(idToken: String): IdToken {
        try {
            val jwt = JWT.decode(idToken)
            return IdToken(
                iss = jwt.issuer,
                sub = jwt.subject,
                aud = jwt.audience,
                exp = jwt.expiresAt,
                iat = jwt.issuedAt,
                nonce = jwt.getClaim("nonce").asString(),
                name = jwt.getClaim("name").asString(),
                picture = jwt.getClaim("picture").asString()
            )
        } catch (e: JWTDecodeException) {
            throw RuntimeException(e)
        }
    }
}


data class TokenResponse(
    val accessToken: String,
    val expiresIn: Int,
    val idToken: String,
    val refreshToken: String,
    val scope: String,
    val tokenType: String
)

data class IdToken(
    val iss: String,
    val sub: String,
    val aud: List<String>,
    val exp: Date,
    val iat: Date,
    val nonce: String,
    val name: String,
    val picture: String
)

